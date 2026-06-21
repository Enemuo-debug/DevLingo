package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CompletedChallengeEntity
import com.example.data.GeminiChallenge
import com.example.data.LeaderboardOpponentEntity
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppScreen(viewModel: DevLingoViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val selectedDomain by viewModel.selectedDomain.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val currentStreak = userProgress?.currentStreak ?: 0
    val totalXp = userProgress?.totalXp ?: 0

    Scaffold(
        bottomBar = {
            if (quizState is QuizState.Idle) {
                DevLingoBottomNav(currentTab = currentTab, onTabSelected = { viewModel.selectTab(it) })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content based on selected tab
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    slideInHorizontally { if (it > targetState) -it else it } togetherWith
                            slideOutHorizontally { if (it > targetState) it else -it }
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> PathLearnScreen(
                        viewModel = viewModel,
                        selectedDomain = selectedDomain,
                        userProgress = userProgress
                    )
                    1 -> DailyChallengeScreen(viewModel = viewModel, userProgress = userProgress)
                    2 -> LeaderboardScreen(viewModel = viewModel)
                    3 -> ProfileStatsScreen(viewModel = viewModel, userProgress = userProgress)
                }
            }

            // SnackBar/Feedback Banner at Top
            feedbackMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter)
                        .clickable { viewModel.clearFeedback() },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (msg.contains("Great")) "🚀" else "💡",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearFeedback() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Auto clear feedback after 4 seconds
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(4000)
                    viewModel.clearFeedback()
                }
            }

            // Active Quiz Screen Cover Overlay
            if (quizState !is QuizState.Idle) {
                QuizOverlay(
                    state = quizState,
                    onSubmitAnswer = { challenge, answer -> viewModel.submitAnswer(challenge, answer) },
                    onProceedToQuiz = { challenge -> viewModel.startActiveQuizFromLesson(challenge) },
                    onExitQuiz = { viewModel.exitQuiz() }
                )
            }
        }
    }
}

@Composable
fun DevLingoBottomNav(currentTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = SpaceDarkSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(72.dp)
        ) {
            NavigationBarItem(
                selected = currentTab == 0,
                onClick = { onTabSelected(0) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Pathways") },
                label = { Text("Learn", fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryTechGreen,
                    unselectedIconColor = TextSecondarySlate,
                    selectedTextColor = PrimaryTechGreen,
                    unselectedTextColor = TextSecondarySlate,
                    indicatorColor = SpaceDarkBackground
                ),
                modifier = Modifier.testTag("nav_badge_learn")
            )
            NavigationBarItem(
                selected = currentTab == 1,
                onClick = { onTabSelected(1) },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Daily Special") },
                label = { Text("Daily", fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TertiaryWarmOrange,
                    unselectedIconColor = TextSecondarySlate,
                    selectedTextColor = TertiaryWarmOrange,
                    unselectedTextColor = TextSecondarySlate,
                    indicatorColor = SpaceDarkBackground
                ),
                modifier = Modifier.testTag("nav_badge_daily")
            )
            NavigationBarItem(
                selected = currentTab == 2,
                onClick = { onTabSelected(2) },
                icon = { Icon(Icons.Default.Star, contentDescription = "Leaderboard") },
                label = { Text("League", fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SecondaryCloudBlue,
                    unselectedIconColor = TextSecondarySlate,
                    selectedTextColor = SecondaryCloudBlue,
                    unselectedTextColor = TextSecondarySlate,
                    indicatorColor = SpaceDarkBackground
                ),
                modifier = Modifier.testTag("nav_badge_league")
            )
            NavigationBarItem(
                selected = currentTab == 3,
                onClick = { onTabSelected(3) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile Stats") },
                label = { Text("Stats", fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryTechLight,
                    unselectedIconColor = TextSecondarySlate,
                    selectedTextColor = PrimaryTechLight,
                    unselectedTextColor = TextSecondarySlate,
                    indicatorColor = SpaceDarkBackground
                ),
                modifier = Modifier.testTag("nav_badge_stats")
            )
        }
    }
}

// --- SCREEN 1: LEARN PATHS ---
@Composable
fun PathLearnScreen(
    viewModel: DevLingoViewModel,
    selectedDomain: String,
    userProgress: com.example.data.UserProgressEntity?
) {
    val domains = listOf("DevOps", "Unity Game Dev", "Node.js (JS & TS)", "ASP.NET")
    val currentStreak = userProgress?.currentStreak ?: 0
    val totalXp = userProgress?.totalXp ?: 0
    val isDark = isSystemInDarkTheme()

    val domainXp = when (selectedDomain) {
        "DevOps" -> userProgress?.devOpsXp ?: 0
        "Unity Game Dev" -> userProgress?.unityXp ?: 0
        "Node.js (JS & TS)" -> userProgress?.nodeJsXp ?: 0
        "ASP.NET" -> userProgress?.aspNetXp ?: 0
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Stats bar matching Bento Grid Header exactly
        StatsHeader(currentStreak = currentStreak, totalXp = totalXp, level = (totalXp / 100) + 1)

        Spacer(modifier = Modifier.height(16.dp))

        // API Engine Mode Warning Indicator styled cleanly as a Bento subsystem block
        ApiEngineModeCard(viewModel.isApiKeyAvailable)

        Spacer(modifier = Modifier.height(20.dp))

        // Domain Selector Scroll Row
        Text(
            text = "CHOOSE LEARNING TRACK",
            style = MaterialTheme.typography.labelMedium,
            color = if (isDark) Color(0xFF90A4AE) else BentoTextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            domains.forEach { dom ->
                val isSelected = dom == selectedDomain
                val borderBrush = if (isSelected) {
                    Brush.horizontalGradient(listOf(PrimaryTechGreen, SecondaryCloudBlue))
                } else {
                    Brush.horizontalGradient(listOf(
                        if (isDark) Color(0xFF1E213A) else BentoBorder.copy(alpha = 0.5f),
                        if (isDark) Color(0xFF1E213A) else BentoBorder.copy(alpha = 0.5f)
                    ))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color(0xFF1F2544) else Color(0xFFD1E4FF)
                            } else {
                                if (isDark) Color(0xFF131629) else Color(0xFFF1F3F9)
                            }
                        )
                        .border(1.5.dp, borderBrush, RoundedCornerShape(20.dp))
                        .clickable { viewModel.selectDomain(dom) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (dom) {
                                "DevOps" -> "🐳"
                                "Unity Game Dev" -> "🎮"
                                "Node.js (JS & TS)" -> "⚡"
                                "ASP.NET" -> "🕸️"
                                else -> "💻"
                             },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = dom,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) {
                                if (isDark) Color.White else BentoBluePrimary
                            } else {
                                if (isDark) Color(0xFFB0BEC5) else BentoTextPrimary
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Component 1: Elegant Bento Active Module Card
        val blueCardBg = if (isDark) Color(0xFF112240) else Color(0xFFD1E4FF)
        val blueCardText = if (isDark) Color(0xFFD1E4FF) else Color(0xFF001D36)
        val blueCardPrimary = if (isDark) Color(0xFF5AB2FF) else Color(0xFF0061A4)

        val currentTopicTitle = when (selectedDomain) {
            "DevOps" -> when {
                domainXp < 150 -> "Docker Layers"
                domainXp < 300 -> "Service Discovery"
                domainXp < 450 -> "ReplicaSets"
                domainXp < 600 -> "DaemonSets"
                else -> "Terraform Plan"
            }
            "Unity Game Dev" -> when {
                domainXp < 150 -> "MonoBehaviours"
                domainXp < 300 -> "Physics Loop"
                domainXp < 450 -> "Matrices & Vectors"
                domainXp < 600 -> "Asset Bundles"
                else -> "Scriptables"
            }
            "Node.js (JS & TS)" -> when {
                domainXp < 150 -> "Async Queues"
                domainXp < 300 -> "Microtask Queues"
                domainXp < 450 -> "TypeScript Narrowing"
                domainXp < 600 -> "WriteStreams"
                else -> "V8 Optimizations"
            }
            "ASP.NET" -> when {
                domainXp < 150 -> "Life Scoping"
                domainXp < 300 -> "Program Pipelines"
                domainXp < 450 -> "EF Tracking"
                domainXp < 600 -> "Action Filters"
                else -> "Route Patterns"
            }
            else -> "Advanced Engineering Core"
        }

        val trackEmojiName = when (selectedDomain) {
            "DevOps" -> "devops"
            "Unity Game Dev" -> "unity"
            "Node.js (JS & TS)" -> "node"
            "ASP.NET" -> "dotnet"
            else -> "core"
        }

        val trackEmoji = when (selectedDomain) {
            "DevOps" -> "🐳"
            "Unity Game Dev" -> "🎮"
            "Node.js (JS & TS)" -> "⚡"
            "ASP.NET" -> "🕸️"
            else -> "💻"
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = blueCardBg),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Absolute watermark decoration at the bottom-right representing mastery
                Text(
                    text = trackEmoji,
                    fontSize = 110.sp,
                    color = blueCardText.copy(alpha = 0.08f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 20.dp)
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    // Micro Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(blueCardPrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE MODULE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when (selectedDomain) {
                            "DevOps" -> "🐳 DevOps Eng:\n$currentTopicTitle"
                            "Unity Game Dev" -> "🎮 Unity 3D:\n$currentTopicTitle"
                            "Node.js (JS & TS)" -> "⚡ Node.js V8:\n$currentTopicTitle"
                            "ASP.NET" -> "🕸️ ASP.NET Core:\n$currentTopicTitle"
                            else -> "💻 Developer Study:\n$currentTopicTitle"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 21.sp,
                        lineHeight = 28.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        color = blueCardText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Level ${domainXp / 150 + 1} / 10 • Path Progress",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = blueCardText.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress indicators
                    val progressValue = (domainXp % 150) / 150f
                    val percentage = (progressValue * 100).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = blueCardPrimary,
                            trackColor = blueCardPrimary.copy(alpha = 0.25f)
                        )
                        Text(
                            text = "$percentage%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = blueCardText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Left and Right side-by-side Bento Row Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Component 2: Bento League Card (Violet theme)
            val violetCardBg = if (isDark) Color(0xFF1E1430) else Color(0xFFEADDFF)
            val violetCardBorder = if (isDark) Color(0xFF3B2F57) else Color(0xFFD0BCFF)
            val violetCardText = if (isDark) Color(0xFFE2D1FF) else Color(0xFF21005D)

            val leagueLevelString = when {
                totalXp < 150 -> "Quartz IV"
                totalXp < 400 -> "Sapphire II"
                totalXp < 900 -> "Gold III"
                totalXp < 2000 -> "Ruby I"
                else -> "Dimnd League"
            }

            val divisionRanking = when {
                totalXp < 150 -> "#8 in Division"
                totalXp < 400 -> "#6 in Division"
                totalXp < 900 -> "#4 in Division"
                totalXp < 2000 -> "#2 in Division"
                else -> "#1 in Division"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = violetCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(125.dp),
                border = BorderStroke(1.dp, violetCardBorder),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                        ) {
                            Text("🏆", fontSize = 15.sp)
                        }
                        Text(
                            text = "LEAGUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = violetCardText.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }

                    Column {
                        Text(
                            text = leagueLevelString,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = violetCardText
                        )
                        Text(
                            text = divisionRanking,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = violetCardText.copy(alpha = 0.77f)
                        )
                    }
                }
            }

            // Component 3: Bento Streak Card (Red/Pink theme)
            val redCardBg = if (isDark) Color(0xFF331B1B) else Color(0xFFFFDAD6)
            val redCardBorder = if (isDark) Color(0xFF5C2D2D) else Color(0xFFFFB4AB)
            val redCardText = if (isDark) Color(0xFFFFB4AB) else Color(0xFF410002)

            Card(
                colors = CardDefaults.cardColors(containerColor = redCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(125.dp),
                border = BorderStroke(1.dp, redCardBorder),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                        ) {
                            Text("⚡", fontSize = 15.sp)
                        }
                        Text(
                            text = "STREAK",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = redCardText.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }

                    Column {
                        Text(
                            text = if (currentStreak == 1) "1 Day" else "$currentStreak Days",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = redCardText
                        )
                        Text(
                            text = if (currentStreak > 4) "Personal Best!" else "Keep it going!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = redCardText.copy(alpha = 0.77f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Component 4: Bento Daily Challenge Preview Capsule
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141A2E) else Color.White),
            border = BorderStroke(
                1.dp,
                if (isDark) Color(0xFF1D2849) else BentoBorder.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY CHALLENGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFF90A4AE) else BentoTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF3E1B1B) else Color(0xFFF2B8B5))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "12h left",
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            color = if (isDark) Color(0xFFFF8A84) else Color(0xFF601410)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF1F294D) else Color(0xFFF7F9FF))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(blueCardPrimary)
                    ) {
                        Text(
                            text = when (selectedDomain) {
                                "DevOps" -> "🐳"
                                "Unity Game Dev" -> "🎮"
                                "Node.js (JS & TS)" -> "JS"
                                "ASP.NET" -> "ASP"
                                else -> "💻"
                            },
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Containerize & deploy a live microservice",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else BentoTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "+50 XP • Double Reward Active",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFFB0BEC5) else BentoTextSecondary
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(blueCardPrimary)
                            .clickable { viewModel.selectTab(1) } // Triggers redirection to Daily tab
                    ) {
                        Text("▶", color = Color.White, fontSize = 12.sp, modifier = Modifier.offset(x = 1.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Learn curriculum path node
        Text(
            text = "PROGRESSIVE LEARNING PATHWAY",
            style = MaterialTheme.typography.labelMedium,
            color = if (isDark) Color(0xFF90A4AE) else BentoTextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Beautiful gamified learning nodes aligning like Duolingo!
        PathwayGameNodes(domain = selectedDomain, onNodeClicked = {
            viewModel.startTopicChallenge(selectedDomain)
        })
    }
}

@Composable
fun StatsHeader(currentStreak: Int, totalXp: Int, level: Int) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Bio
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1E4FF))
                    .border(1.5.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = "PL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF001D36)
                )
            }
            Column {
                Text(
                    text = "STUDENT PATH",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color(0xFF90A4AE) else BentoTextSecondary,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "Lvl $level Master",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else BentoTextPrimary
                )
            }
        }

        // Streak Count & Diamonds Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDark) Color(0xFF1E223D) else BentoGrayLight)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🔥", fontSize = 14.sp)
                Text(
                    text = "$currentStreak",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = if (isDark) Color.White else BentoTextPrimary
                )
            }
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(
                        if (isDark) Color(0xFF3B4066) else BentoBorder.copy(alpha = 0.6f)
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("💎", fontSize = 14.sp)
                Text(
                    text = "$totalXp",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = if (isDark) Color.White else BentoTextPrimary
                )
            }
        }
    }
}

@Composable
fun ApiEngineModeCard(isApiLive: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isApiLive) PrimaryTechGreen.copy(alpha = 0.1f) else SpaceDarkSurface
        ),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            width = 1.dp,
            color = if (isApiLive) PrimaryTechGreen.copy(alpha = 0.4f) else TextSecondarySlate.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (isApiLive) PrimaryTechGreen.copy(alpha = 0.2f) else TextSecondarySlate.copy(alpha = 0.15f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isApiLive) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = "API Status",
                    tint = if (isApiLive) PrimaryTechGreen else TertiaryWarmOrange,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (isApiLive) "Gemini AI Engine Online" else "Standard Local Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryWhite
                )
                Text(
                    text = if (isApiLive) {
                        "Dynamic micro-challenges generated on-the-fly inside the cloud."
                    } else {
                        "Self-contained high-concept challenges. Set keys in AI Studio for AI limits."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun PathwayGameNodes(domain: String, onNodeClicked: () -> Unit) {
    val nodeTitles = when (domain) {
        "DevOps" -> listOf("Docker Layers", "Service Discovery", "ReplicaSets", "DaemonSets", "Terraform Plan")
        "Unity Game Dev" -> listOf("MonoBehaviours", "Physics loop", "Matrices & Vectors", "Asset Bundles", "Scriptables")
        "Node.js (JS & TS)" -> listOf("Async queues", "Microtask queues", "TypeScript narrowers", "WriteStreams", "V8 optimizations")
        "ASP.NET" -> listOf("Life scoping", "Program Pipelines", "EF Tracking", "Action Filters", "Route patterns")
        else -> listOf("Basics I", "Core II", "Intermediary III", "Advanced IV", "Mastery V")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        nodeTitles.forEachIndexed { idx, title ->
            // Sinusoidal offset to match Duolingo's snake pathway look!
            val angle = idx * (Math.PI / 2.5)
            val offsetDp = (Math.sin(angle) * 75).dp

            Column(
                modifier = Modifier
                    .offset(x = offsetDp)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onNodeClicked,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (idx == 0) PrimaryTechGreen else SpaceDarkSurface
                    ),
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("path_node_btn_$idx")
                        .border(
                            4.dp,
                            if (idx == 0) PrimaryTechLight else SecondaryCloudBlue.copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = when (domain) {
                            "DevOps" -> "🐳"
                            "Unity Game Dev" -> "🎮"
                            "Node.js (JS & TS)" -> "⚡"
                            "ASP.NET" -> "🕸️"
                            else -> "✏️"
                        },
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CodeEditorBlack)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryWhite
                    )
                }
            }

            // Draw clean linker line between nodes (except for the last node)
            if (idx < nodeTitles.lastIndex) {
                val nextAngle = (idx + 1) * (Math.PI / 2.5)
                val nextOffsetDp = (Math.sin(nextAngle) * 75).dp

                // Simplified vertical connector line using a custom canvas drawing behind or simple Spacer with Canvas
                Spacer(
                    modifier = Modifier
                        .height(30.dp)
                        .width(200.dp)
                        .drawBehind {
                            val startX = size.width / 2 + offsetDp.toPx()
                            val endX = size.width / 2 + nextOffsetDp.toPx()
                            drawLine(
                                color = SecondaryCloudBlue.copy(alpha = 0.4f),
                                start = Offset(startX, 0f),
                                end = Offset(endX, size.height),
                                strokeWidth = 4.dp.toPx()
                            )
                        }
                )
            }
        }
    }
}

// --- SCREEN 2: DAILY CHALLENGE ---
@Composable
fun DailyChallengeScreen(viewModel: DevLingoViewModel, userProgress: com.example.data.UserProgressEntity?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High quality illustration placeholder drawn natively
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(TertiaryWarmOrange.copy(alpha = 0.1f), CircleShape)
                .border(2.dp, TertiaryWarmOrange, CircleShape)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("🔥", fontSize = 72.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DAILY SPECIAL PIPELINE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = TertiaryWarmOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete your daily challenge sequence to sustain your hot streak count! Daily quizzes challenge you across diverse fields with double points reward.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondarySlate,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SpaceDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(PrimaryTechGreen.copy(alpha = 0.2f), CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "XP multiplier status",
                        tint = PrimaryTechGreen
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Double Rewards Active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryWhite
                    )
                    Text(
                        text = "Earn +50 XP instead of standard 25 XP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { viewModel.startDailyChallenge() },
            colors = ButtonDefaults.buttonColors(containerColor = TertiaryWarmOrange),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("launch_daily_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START DAILY CLASH (-50 XP POTENTIAL)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
        }
    }
}

// --- SCREEN 3: LEADERBOARD ---
@Composable
fun LeaderboardScreen(viewModel: DevLingoViewModel) {
    val opponents by viewModel.leaderboard.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // League bracket summary
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DiamondCrown.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Diamond Code Chamber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DiamondCrown
                    )
                    Text(
                        text = "Weekly promotion bracket finishes in 23 hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GLOBAL CODE RATINGS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondarySlate,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            TextButton(onClick = { viewModel.forceCompetitorTick() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Simulate tick", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Compile Opponents", fontSize = 11.sp, color = SecondaryCloudBlue)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(opponents) { idx, opp ->
                val isSelf = opp.isUser
                val itemBackground = if (isSelf) {
                    Brush.horizontalGradient(listOf(PrimaryTechGreen.copy(alpha = 0.15f), SpaceDarkSurface))
                } else {
                    Brush.horizontalGradient(listOf(SpaceDarkSurface, SpaceDarkSurface))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelf) Color.Transparent else SpaceDarkSurface)
                        .drawBehind {
                            if (isSelf) {
                                drawRect(itemBackground)
                            }
                        }
                        .border(
                            width = if (isSelf) 1.5.dp else 0.dp,
                            color = if (isSelf) PrimaryTechGreen else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position Number
                    Text(
                        text = "${idx + 1}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = when (idx) {
                            0 -> GoldCrown
                            1 -> SilverCrown
                            2 -> BronzeCrown
                            else -> TextSecondarySlate
                        },
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Avatar badge icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = when (opp.badge) {
                                    "Diamond" -> DiamondCrown.copy(alpha = 0.15f)
                                    "Ruby" -> RubyCrown.copy(alpha = 0.15f)
                                    "Gold" -> GoldCrown.copy(alpha = 0.15f)
                                    "Silver" -> SilverCrown.copy(alpha = 0.12f)
                                    else -> BronzeCrown.copy(alpha = 0.1f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (opp.badge) {
                                "Diamond" -> "💎"
                                "Ruby" -> "🎈"
                                "Gold" -> "🥇"
                                "Silver" -> "🥈"
                                else -> "🥉"
                            },
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Competitor name
                    Text(
                        text = opp.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelf) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelf) PrimaryTechGreen else TextPrimaryWhite,
                        modifier = Modifier.weight(1f)
                    )

                    // Competitor XP
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${opp.xp} XP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = opp.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (opp.badge) {
                                "Diamond" -> DiamondCrown
                                "Ruby" -> RubyCrown
                                "Gold" -> GoldCrown
                                "Silver" -> SilverCrown
                                else -> BronzeCrown
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: PROFILE & STATISTICS ---
@Composable
fun ProfileStatsScreen(viewModel: DevLingoViewModel, userProgress: com.example.data.UserProgressEntity?) {
    val completedChallenges by viewModel.completedChallenges.collectAsStateWithLifecycle()

    val currentStreak = userProgress?.currentStreak ?: 0
    val maxStreak = userProgress?.highestStreak ?: 0
    val devOpsXp = userProgress?.devOpsXp ?: 0
    val unityXp = userProgress?.unityXp ?: 0
    val nodeXp = userProgress?.nodeJsXp ?: 0
    val aspNetXp = userProgress?.aspNetXp ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // User Profile Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .background(PrimaryTechGreen.copy(alpha = 0.15f), CircleShape)
                        .border(3.dp, PrimaryTechGreen, CircleShape)
                ) {
                    Text("🧠", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Professional SoftDev Coach",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryWhite
                )

                Text(
                    text = "Full-Stack Learning Champion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$currentStreak 🔥",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TertiaryWarmOrange
                        )
                        Text(
                            text = "Streak Count",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$maxStreak 🚀",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = PrimaryTechGreen
                        )
                        Text(
                            text = "Record Streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid-like Domain Breakdown Row Cards
        Text(
            text = "SUBJECT SCORE CARDS",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondarySlate,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val domainsData = listOf(
            Triple("🐳 DevOps", devOpsXp, PrimaryTechGreen),
            Triple("🎮 Unity Game Dev", unityXp, SecondaryCloudBlue),
            Triple("⚡ Node.js & TS", nodeXp, PrimaryTechLight),
            Triple("🕸️ ASP.NET", aspNetXp, TertiaryWarmOrange)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            domainsData.take(2).forEach { data ->
                DomainScoreGridCard(
                    title = data.first,
                    xp = data.second,
                    color = data.third,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            domainsData.drop(2).forEach { data ->
                DomainScoreGridCard(
                    title = data.first,
                    xp = data.second,
                    color = data.third,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History logs list
        Text(
            text = "COMPLETED CHALLENGE HISTORY (${completedChallenges.size})",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondarySlate,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (completedChallenges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceDarkSurface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No milestones met yet. Complete paths to log history here!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            completedChallenges.forEach { log ->
                HistoryItemRow(log)
            }
        }
    }
}

@Composable
fun DomainScoreGridCard(title: String, xp: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceDarkSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$xp",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryItemRow(log: CompletedChallengeEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SpaceDarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (log.theme) {
                "DevOps" -> "🐳"
                "Unity Game Dev" -> "🎮"
                "Node.js (JS & TS)" -> "⚡"
                "ASP.NET" -> "🕸️"
                else -> "📒"
            },
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = log.theme,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondarySlate
            )
        }
        Text(
            text = "+${log.xpPoints} XP",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryTechGreen
        )
    }
}

// --- QUIZ DIALOG OVERLAY ---
@Composable
fun QuizOverlay(
    state: QuizState,
    onSubmitAnswer: (GeminiChallenge, String) -> Unit,
    onProceedToQuiz: (GeminiChallenge) -> Unit,
    onExitQuiz: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SpaceDarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Close header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitQuiz) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Exit challenge", tint = TextPrimaryWhite)
                }

                // Header status
                Text(
                    text = when (state) {
                        is QuizState.Generating -> "COMPILING DIRECTIVE..."
                        is QuizState.Lesson -> "EDUCATION CLASSROOM"
                        is QuizState.Active -> "ACTIVE COMPLIANCE TEST"
                        is QuizState.Result -> "REPORT LOG GENERATED"
                        else -> "DIRECTIVE FEED"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryCloudBlue,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(48.dp)) // Equalizer placeholder
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (state) {
                is QuizState.Generating -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryTechGreen,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Analyzing syllabus parameters...",
                            color = TextPrimaryWhite,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generating dynamic challenge vector dynamically on cloud stack...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                is QuizState.Lesson -> {
                    val chal = state.challenge
                    Box(modifier = Modifier.weight(1f)) {
                        QuizLessonView(
                            chal = chal,
                            onProceed = { onProceedToQuiz(chal) }
                        )
                    }
                }

                is QuizState.Active -> {
                    val chal = state.challenge
                    QuizActiveView(chal = chal, onSubmitAnswer = { onSubmitAnswer(chal, it) })
                }

                is QuizState.Result -> {
                    QuizResultView(
                        result = state,
                        onContinue = onExitQuiz
                    )
                }

                is QuizState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("❌", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Failed to Compile Directive",
                            color = TextPrimaryWhite,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onExitQuiz, colors = ButtonDefaults.buttonColors(containerColor = SecondaryCloudBlue)) {
                            Text("Return to pathways")
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun ColumnScope.QuizActiveView(chal: GeminiChallenge, onSubmitAnswer: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
        // Linear track node progress and domain tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryTechGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = chal.domain,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryTechGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Topic: ${chal.topic}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondarySlate,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terminal Terminal style code window
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CodeEditorBlack),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TextSecondarySlate.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header window bar
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("devlingo_terminal.sh", color = TextSecondarySlate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = chal.question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryWhite,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SELECT COMPILER DIRECTIVE:",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondarySlate,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Multiple choice option buttons
        val options = listOf(
            "A" to chal.optionA,
            "B" to chal.optionB,
            "C" to chal.optionC,
            "D" to chal.optionD
        )

        options.forEach { opt ->
            Button(
                onClick = { onSubmitAnswer(opt.first) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpaceDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(1.dp, SecondaryCloudBlue.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .testTag("quiz_option_${opt.first}"),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(SecondaryCloudBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = opt.first,
                            fontWeight = FontWeight.Black,
                            color = SecondaryCloudBlue,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = opt.second,
                        color = TextPrimaryWhite,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnScope.QuizResultView(
    result: QuizState.Result,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    if (result.isCorrect) PrimaryTechGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (result.isCorrect) "✓" else "✗",
                fontSize = 48.sp,
                color = if (result.isCorrect) PrimaryTechGreen else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (result.isCorrect) "COMPILATION SUCCESS" else "COMPILATION ERROR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = if (result.isCorrect) PrimaryTechGreen else Color.Red,
            letterSpacing = 1.sp
        )

        Text(
            text = "+${result.xpEarned} XP Rep Points Added",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondarySlate
        )

        Spacer(modifier = Modifier.height(24.dp))

        // In-depth educational lesson parameters (The 'profound' mandate)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PROF_BREAKDOWN_LOG.MD",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextSecondarySlate,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Correct Answer was Directive [${result.challenge.correctAnswer}]",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (result.isCorrect) PrimaryTechGreen else TertiaryWarmOrange
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.challenge.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryWhite,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTechGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("quiz_result_continue")
        ) {
            Text(
                text = "CONTINUE COMPILER SCAN",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}

// --- LESSON VIEW MODULE ---
@Composable
fun QuizLessonView(
    chal: GeminiChallenge,
    onProceed: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Module Top Poster (Hero space)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141D30) else Color(0xFFEBF3FF)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1F3559) else Color(0xFFB3D4FF))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .background(if (isDark) Color(0xFF1F3559) else Color(0xFFCCE3FF), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = when (chal.domain) {
                                "DevOps" -> "🐳"
                                "Unity Game Dev" -> "🎮"
                                "Node.js (JS & TS)" -> "⚡"
                                "ASP.NET" -> "🕸️"
                                else -> "📖"
                            },
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = chal.domain.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFF90CAF9) else Color(0xFF0D47A1),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = chal.topic,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF01579B),
                            fontSize = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFECEFF1))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("💡", fontSize = 11.sp)
                    Text(
                        text = "STUDY MODULE: Read this brief lecture then proceed to the class test.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFB0BEC5) else Color(0xFF455A64)
                    )
                }
            }
        }

        // Lesson Text Renderer Card
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) SpaceDarkSurface else Color.White),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Get pre-formatted lesson string, falling back if empty
                val lessonMarkdown = remember(chal) {
                    if (chal.lesson.isNotEmpty()) chal.lesson else {
                        """
                        ### 1. Concept Overview
                        Welcome to this specialized focus on **${chal.topic}**. In modern systems engineering, mastering the core principles of ${chal.domain} is pivotal for maintaining fast, robust builds and clean application layers.

                        ### 2. Architectural Mechanics & Examples
                        The topic of **${chal.topic}** details several operational rules:
                        • **State Reliability**: Structuring attributes cleanly avoids memory leaks or processing blocks.
                        • **Isolated Scope**: Separating layers prevents side-effects.

                        A conceptual snippet demonstrating application details:
                        ```
                        // Design Principle of ${chal.topic}
                        Configure ${chal.topic.replace(" ", "")} using optimal configurations.
                        Prevent redundant layer execution or process loops.
                        ```

                        ### 3. Summary Cheat Sheet
                        • **Isolate Variables**: Always minimize coupling of dynamic blocks.
                        • **Study Thoroughly**: Let's verify your memory and understanding with a quick conceptual check immediately based on the principles discussed.
                        """.trimIndent()
                    }
                }

                LessonContentRenderer(text = lessonMarkdown)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Proceed Button
        Button(
            onClick = onProceed,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTechGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("lesson_proceed_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TAKE MODULE CHALLENGE",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Text("➡️", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LessonContentRenderer(text: String) {
    val isDark = isSystemInDarkTheme()
    val codeBg = if (isDark) Color(0xFF060812) else Color(0xFFF1F3F9)
    val codeColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF006064)
    val textColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF263238)
    val headerColor = if (isDark) Color(0xFF80DEEA) else Color(0xFF00838F)

    // Split text into lines or render blocks safely
    val blocks = remember(text) {
        val result = mutableListOf<LessonBlock>()
        val lines = text.lines()
        var inCode = false
        val currentCode = StringBuilder()
        val currentText = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("```")) {
                if (inCode) {
                    // end code block
                    result.add(LessonBlock.Code(currentCode.toString().trimEnd()))
                    currentCode.clear()
                    inCode = false
                } else {
                    // start code block
                    if (currentText.isNotEmpty()) {
                        result.add(LessonBlock.Paragraph(currentText.toString().trim()))
                        currentText.clear()
                    }
                    inCode = true
                }
            } else {
                if (inCode) {
                    currentCode.append(line).append("\n")
                } else {
                    if (trimmed.startsWith("###")) {
                        if (currentText.isNotEmpty()) {
                            result.add(LessonBlock.Paragraph(currentText.toString().trim()))
                            currentText.clear()
                        }
                        result.add(LessonBlock.Heading(trimmed.removePrefix("###").trim()))
                    } else if (trimmed.startsWith("##")) {
                        if (currentText.isNotEmpty()) {
                            result.add(LessonBlock.Paragraph(currentText.toString().trim()))
                            currentText.clear()
                        }
                        result.add(LessonBlock.Heading(trimmed.removePrefix("##").trim()))
                    } else if (trimmed.startsWith("•") || trimmed.startsWith("-")) {
                        if (currentText.isNotEmpty()) {
                            result.add(LessonBlock.Paragraph(currentText.toString().trim()))
                            currentText.clear()
                        }
                        result.add(LessonBlock.Bullet(trimmed.removePrefix("•").removePrefix("-").trim()))
                    } else {
                        currentText.append(line).append("\n")
                    }
                }
            }
        }
        if (currentText.isNotEmpty()) {
            result.add(LessonBlock.Paragraph(currentText.toString().trim()))
        }
        if (inCode && currentCode.isNotEmpty()) {
            result.add(LessonBlock.Code(currentCode.toString().trimEnd()))
        }
        result
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        blocks.forEach { block ->
            when (block) {
                is LessonBlock.Heading -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = block.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = headerColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                is LessonBlock.Paragraph -> {
                    if (block.text.isNotEmpty()) {
                        Text(
                            text = block.text,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is LessonBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 12.sp,
                            color = headerColor,
                            modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                        )
                        Text(
                            text = block.text,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is LessonBlock.Code -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = codeBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1F293D) else Color(0xFFCFD8DC))
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = block.code,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = codeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class LessonBlock {
    data class Heading(val text: String) : LessonBlock()
    data class Paragraph(val text: String) : LessonBlock()
    data class Bullet(val text: String) : LessonBlock()
    data class Code(val code: String) : LessonBlock()
}
