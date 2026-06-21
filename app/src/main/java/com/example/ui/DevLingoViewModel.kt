package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface QuizState {
    object Idle : QuizState
    object Generating : QuizState
    data class Lesson(val challenge: GeminiChallenge) : QuizState
    data class Active(val challenge: GeminiChallenge) : QuizState
    data class Result(
        val challenge: GeminiChallenge,
        val userAnswer: String,
        val isCorrect: Boolean,
        val xpEarned: Int
    ) : QuizState
    data class Error(val message: String) : QuizState
}

class DevLingoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = DevLingoRepository(db)

    // UI Tab State: 0 -> Learn / Pathways, 1 -> Daily, 2 -> Leaderboards, 3 -> Profile
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Active Domain State
    private val _selectedDomain = MutableStateFlow("DevOps")
    val selectedDomain: StateFlow<String> = _selectedDomain.asStateFlow()

    // active Quiz State
    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    // Reactive progress streams
    val userProgress: StateFlow<UserProgressEntity?> = repo.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardOpponentEntity>> = repo.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedChallenges: StateFlow<List<CompletedChallengeEntity>> = repo.completedChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Check if real API Key exists to toggle AI Mode vs Local Mode
    val isApiKeyAvailable: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    // Error / status feedback message
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.initializeDatabase()
            repo.checkDailyStreakWatchdog()
        }
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun selectDomain(domain: String) {
        _selectedDomain.value = domain
        _quizState.value = QuizState.Idle
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // Generate dynamic lesson challenge (tries Gemini, fallback to high quality offline)
    fun startTopicChallenge(domain: String) {
        viewModelScope.launch {
            _quizState.value = QuizState.Generating
            val challenge = if (isApiKeyAvailable) {
                GeminiClient.generateChallenge(domain) ?: LocalChallengeGenerator.getRandomChallenge(domain)
            } else {
                LocalChallengeGenerator.getRandomChallenge(domain)
            }
            _quizState.value = QuizState.Lesson(challenge)
        }
    }

    // Daily special challenge (XP multipliers!)
    fun startDailyChallenge() {
        viewModelScope.launch {
            _quizState.value = QuizState.Generating
            // Get user domain context to make daily challenge matching
            val domain = LocalChallengeGenerator.getAllDomains().random()
            
            val challenge = if (isApiKeyAvailable) {
                val generated = GeminiClient.generateChallenge(domain, "Extreme Profound Daily")
                if (generated != null) {
                    generated.copy(id = "daily_chall_${System.currentTimeMillis()}")
                } else {
                    LocalChallengeGenerator.getRandomChallenge(domain).copy(id = "daily_chall_${System.currentTimeMillis()}")
                }
            } else {
                LocalChallengeGenerator.getRandomChallenge(domain).copy(id = "daily_chall_${System.currentTimeMillis()}")
            }
            _quizState.value = QuizState.Lesson(challenge)
        }
    }

    fun startActiveQuizFromLesson(challenge: GeminiChallenge) {
        _quizState.value = QuizState.Active(challenge)
    }

    // Match candidate answer
    fun submitAnswer(challenge: GeminiChallenge, selected: String) {
        val activeState = _quizState.value
        if (activeState !is QuizState.Active) return

        val isCorrect = selected.uppercase() == challenge.correctAnswer.uppercase()
        val isDaily = challenge.id.startsWith("daily_")
        val xpEarned = if (isCorrect) {
            if (isDaily) 50 else 25 // 50 XP for daily, 25 XP for topic
        } else {
            5 // 5 pity XP for attempting the challenge
        }

        viewModelScope.launch {
            if (isCorrect) {
                repo.awardXp(challenge.domain, xpEarned, challenge.id, challenge.topic)
                _feedbackMessage.value = "Great Job! +$xpEarned XP rewarded."
            } else {
                repo.awardXp(challenge.domain, xpEarned, challenge.id, "${challenge.topic} (Attempted)")
                _feedbackMessage.value = "Incorrect. No worries, gained +$xpEarned consolation XP! Keep learning."
            }
            _quizState.value = QuizState.Result(challenge, selected, isCorrect, xpEarned)
        }
    }

    fun exitQuiz() {
        _quizState.value = QuizState.Idle
    }

    // Manual simulation tick to boost competitive tension!
    fun forceCompetitorTick() {
        viewModelScope.launch {
            repo.simulateCompetitorProgress()
            _feedbackMessage.value = "Syncing leaderboard matches... Opponents are moving!"
        }
    }
}
