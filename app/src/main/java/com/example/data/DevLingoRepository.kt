package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DevLingoRepository(private val db: AppDatabase) {

    val userProgress: Flow<UserProgressEntity?> = db.userProgressDao().getUserProgress()
    val leaderboard: Flow<List<LeaderboardOpponentEntity>> = db.leaderboardDao().getAllOpponents()
    val completedChallenges: Flow<List<CompletedChallengeEntity>> = db.completedChallengeDao().getCompletedChallenges()

    // Setup initial opponents if the database is brand new
    suspend fun initializeDatabase() {
        val currentProgress = db.userProgressDao().getUserProgressDirect()
        if (currentProgress == null) {
            db.userProgressDao().insertOrUpdate(UserProgressEntity())
        }

        val opponents = db.leaderboardDao().getAllOpponentsDirect()
        if (opponents.isEmpty()) {
            val list = listOf(
                LeaderboardOpponentEntity(name = "KubernetesKing 👑", xp = 1500, badge = "Diamond"),
                LeaderboardOpponentEntity(name = "DockerDave 🐳", xp = 1200, badge = "Ruby"),
                LeaderboardOpponentEntity(name = "NodeNinja 🥷", xp = 1100, badge = "Ruby"),
                LeaderboardOpponentEntity(name = "UnityUnicorn 🦄", xp = 950, badge = "Gold"),
                LeaderboardOpponentEntity(name = "LinusTheGoat 🐐", xp = 850, badge = "Gold"),
                LeaderboardOpponentEntity(name = "DotNetDan 🚀", xp = 700, badge = "Silver"),
                LeaderboardOpponentEntity(name = "AdaLove 💻", xp = 600, badge = "Silver"),
                LeaderboardOpponentEntity(name = "GraceHopper 🐜", xp = 450, badge = "Bronze"),
                LeaderboardOpponentEntity(name = "TypeScriptTitan ⚡", xp = 300, badge = "Bronze"),
                // The user is also placed in the list dynamically based on their live XP
                LeaderboardOpponentEntity(name = "You (Novice Coach)", xp = 0, badge = "Bronze", isUser = true)
            )
            db.leaderboardDao().insertOpponents(list)
        }
    }

    // Award XP to user, update domain, check/advance streak, and simulate competitor spikes!
    suspend fun awardXp(domain: String, amount: Int, challengeId: String, challengeTitle: String) {
        val progress = db.userProgressDao().getUserProgressDirect() ?: UserProgressEntity()
        val todayStr = getCurrentDateString()

        // 1. Streak calculations
        val lastActive = progress.lastActiveTimestamp
        val currentStreak = when {
            lastActive == 0L -> 1
            isToday(lastActive) -> progress.currentStreak // Already advanced today
            isYesterday(lastActive) -> progress.currentStreak + 1 // Consecutive day
            else -> 1 // Streak broken, start again
        }
        val targetHighest = if (currentStreak > progress.highestStreak) currentStreak else progress.highestStreak

        // 2. Compute updated progress
        val updatedProgress = progress.copy(
            currentStreak = currentStreak,
            highestStreak = targetHighest,
            lastActiveTimestamp = System.currentTimeMillis(),
            totalXp = progress.totalXp + amount,
            devOpsXp = if (domain == "DevOps") progress.devOpsXp + amount else progress.devOpsXp,
            unityXp = if (domain == "Unity Game Dev") progress.unityXp + amount else progress.unityXp,
            nodeJsXp = if (domain == "Node.js (JS & TS)") progress.nodeJsXp + amount else progress.nodeJsXp,
            aspNetXp = if (domain == "ASP.NET") progress.aspNetXp + amount else progress.aspNetXp,
            lastDailyChallengeDate = if (challengeId.startsWith("daily_")) todayStr else progress.lastDailyChallengeDate
        )

        db.userProgressDao().insertOrUpdate(updatedProgress)

        // 3. Mark challenge as completed
        db.completedChallengeDao().insertCompleted(
            CompletedChallengeEntity(
                id = challengeId,
                theme = domain,
                title = challengeTitle,
                xpPoints = amount
            )
        )

        // 4. Update the User's score in the leaderboard
        val allOpponents = db.leaderboardDao().getAllOpponentsDirect()
        val userRow = allOpponents.find { it.isUser }
        if (userRow != null) {
            val userNewXp = updatedProgress.totalXp
            val userNewBadge = determineBadge(userNewXp)
            db.leaderboardDao().updateOpponent(userRow.copy(xp = userNewXp, badge = userNewBadge))
        }

        // 5. Game Engine: Simulate competitor ticks so leaderboards climb dynamically!
        simulateCompetitorProgress()
    }

    // Daily streak watchdog check (runs of app open to check if they've missed a day/need streak reset)
    suspend fun checkDailyStreakWatchdog() {
        val progress = db.userProgressDao().getUserProgressDirect() ?: return
        val lastActive = progress.lastActiveTimestamp
        if (lastActive != 0L && !isToday(lastActive) && !isYesterday(lastActive)) {
            // Streak broken! Reset to 0
            db.userProgressDao().insertOrUpdate(progress.copy(currentStreak = 0))
        }
    }

    // Competitors advance randomly to simulate live competition
    suspend fun simulateCompetitorProgress() {
        val opponents = db.leaderboardDao().getAllOpponentsDirect()
        opponents.forEach { opponent ->
            if (!opponent.isUser) {
                // 30% chance for an opponent to gain some extra random developer hours
                if (Random.nextDouble() < 0.35) {
                    val gain = Random.nextInt(15, 65)
                    val newXp = opponent.xp + gain
                    val newBadge = determineBadge(newXp)
                    db.leaderboardDao().updateOpponent(opponent.copy(xp = newXp, badge = newBadge))
                }
            }
        }
    }

    // Helper functions for date operations
    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun isToday(timestamp: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(timestamp)) == fmt.format(Date())
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(timestamp)) == fmt.format(cal.time)
    }

    private fun determineBadge(xp: Int): String {
        return when {
            xp >= 1500 -> "Diamond"
            xp >= 1000 -> "Ruby"
            xp >= 600 -> "Gold"
            xp >= 300 -> "Silver"
            else -> "Bronze"
        }
    }
}
