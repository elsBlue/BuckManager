package com.buckmanager.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.buckmanager.app.model.Envelope
import com.buckmanager.app.ui.components.*

import com.buckmanager.app.ui.screens.DashboardScreen
import com.buckmanager.app.ui.screens.LoginScreen
import com.buckmanager.app.ui.screens.OnboardingScreen
import com.buckmanager.app.ui.screens.TransactionBottomSheet
import com.buckmanager.app.ui.screens.TransactionScreen
import com.buckmanager.app.viewmodel.BuckViewModel

@Composable
fun BuckApp(viewModel: BuckViewModel = viewModel()) {
    BuckManagerTheme {
        val context = androidx.compose.ui.platform.LocalContext.current
        val navController = rememberNavController()
        val userEmail by viewModel.userEmail.collectAsState()
        val userProfilePicUrl by viewModel.userProfilePicUrl.collectAsState()
        val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsState()

        val startDestination = if (!hasSeenOnboarding) "onboarding" else if (userEmail == null) "login" else "dashboard"

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: startDestination

        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    viewModel.loadAllData()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // Modal States
        var showSettings by remember { mutableStateOf(false) }
        var showWidgetCustomizer by remember { mutableStateOf(false) }

        var editingEnvelope by remember { mutableStateOf<Envelope?>(null) }
        var showAddEnvelope by remember { mutableStateOf(false) }
        var showTransactionSheet by remember { mutableStateOf(false) }
        var editingHeaderCard by remember { mutableStateOf<String?>(null) }
        var showFundGoalEditor by remember { mutableStateOf(false) }
        var showBackgroundEditor by remember { mutableStateOf(false) }



        val globalBg by viewModel.globalBackground.collectAsState()
        val headerCards by viewModel.headerCardsConfig.collectAsState()
        val fundGoal by viewModel.fundGoal.collectAsState()
        val envelopes by viewModel.envelopes.collectAsState()
        val monetization by viewModel.monetization.collectAsState()
        val notificationEnabled by viewModel.notificationEnabled.collectAsState()
        val lastBackupDate by viewModel.lastBackupDate.collectAsState()
        val isDarkMode by viewModel.isDarkMode.collectAsState()
        val isThemeCustomized by viewModel.isThemeCustomized.collectAsState()
        val isCustomizationLocked by viewModel.isCustomizationLocked.collectAsState()
        val userNotice by viewModel.userNotice.collectAsState()


        val view = androidx.compose.ui.platform.LocalView.current
        val canvasColor = parseHexColor(
            globalBg.backgroundColorHex,
            if (isDarkMode) DarkBackground else AppChrome.pageLight
        )
        val userTextColor = parseHexColor(
            globalBg.textColorHex,
            if (isDarkMode) Color.White else Color(0xFF0F172A)
        )
        val hasPhoto = !globalBg.backgroundImageUri.isNullOrBlank()
        val navIsDark = navSurfaceIsDark(canvasColor, userTextColor, hasPhoto, globalBg.dimOpacity)
        androidx.compose.runtime.SideEffect {
            val window = (view.context as android.app.Activity).window
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !navIsDark
            insetsController.isAppearanceLightNavigationBars = !navIsDark
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                window.isNavigationBarContrastEnforced = false
            }
        }
        var displayedNotice by remember { mutableStateOf<String?>(null) }
        var isNoticeVisible by remember { mutableStateOf(false) }

        LaunchedEffect(userNotice) {
            if (userNotice != null) {
                displayedNotice = userNotice
                kotlinx.coroutines.delay(180) // Premium entrance delay
                isNoticeVisible = true
                kotlinx.coroutines.delay(3800)
                isNoticeVisible = false
                kotlinx.coroutines.delay(400) // Wait for slide out
                viewModel.clearUserNotice()
            } else {
                isNoticeVisible = false
            }
        }



        val rootBg = canvasColor

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(rootBg)
        ) {
            if (currentRoute in listOf("dashboard", "transactions") && hasPhoto) {
                AsyncImage(
                    model = globalBg.backgroundImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (globalBg.dimOpacity / 100f).coerceIn(0f, 0.98f)))
                )
            }
            if (currentRoute in listOf("dashboard", "transactions")) {
                ParticleEffectCanvas(effectType = globalBg.particleEffect)
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                    composable("onboarding") {
                        OnboardingScreen(
                            isDarkMode = isDarkMode,
                            onFinish = {
                                viewModel.completeOnboarding()
                                navController.navigate(if (userEmail == null) "login" else "dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            isDarkMode = isDarkMode,
                            onLoginSuccess = { email, profilePicUrl ->
                                viewModel.setUserEmail(email)
                                viewModel.setUserProfilePicUrl(profilePicUrl)
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onContinueLocally = {
                                viewModel.continueLocally()
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { navController.navigate("transactions") },
                            onOpenSettings = { showSettings = true },
                            onEditEnvelope = { env -> editingEnvelope = env },
                            onAddEnvelopeClick = { showAddEnvelope = true },
                            onEditHeaderCard = { cardKey -> editingHeaderCard = cardKey },
                            onEditFundGoal = { showFundGoalEditor = true },
                            onEditBackground = { showBackgroundEditor = true }
                        )
                    }

                    composable("transactions") {
                        TransactionScreen(
                            viewModel = viewModel,
                            onOpenSettings = { showSettings = true },
                            onEditFundGoal = { showFundGoalEditor = true }
                        )
                    }
                }

            if (currentRoute in listOf("dashboard", "transactions")) {
                GlassBottomNav(
                    currentRoute = currentRoute,
                    canvasColor = canvasColor,
                    textColor = userTextColor,
                    hasPhoto = hasPhoto,
                    dimOpacity = globalBg.dimOpacity,
                    onDashboard = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTransactions = {
                        navController.navigate("transactions") {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAdd = { showTransactionSheet = true },
                    onSettings = { showSettings = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                )
            }

            if (showTransactionSheet) {
                TransactionBottomSheet(
                    viewModel = viewModel,
                    envelopes = envelopes,
                    isDarkMode = isDarkMode,
                    onDismiss = { showTransactionSheet = false }
                )
            }

            // Modals
            SettingsModal(
                visible = showSettings,
                notificationEnabled = notificationEnabled,
                userEmail = userEmail,
                userProfilePicUrl = userProfilePicUrl,
                lastBackupDate = lastBackupDate,
                monetization = monetization,
                isDarkMode = isDarkMode,
                isThemeCustomized = isThemeCustomized,
                isCustomizationLocked = isCustomizationLocked,
                onDismiss = { showSettings = false },
                onToggleNotification = { viewModel.toggleNotification(it) },
                onToggleTheme = { viewModel.toggleThemeMode(it) },
                onResetCustomization = { viewModel.resetCustomizationToDefaultTheme() },
                onUnlockCustomization = { viewModel.unlockTemporaryCustomization() },
                onLoginClick = {
                    showSettings = false
                    navController.navigate("login")
                },
                onLogoutClick = {
                    viewModel.setUserEmail(null)
                    viewModel.setUserProfilePicUrl(null)
                    showSettings = false
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenCustomizeWidget = {
                    showSettings = false
                    showWidgetCustomizer = true
                },
                onExportJson = { uri ->
                    viewModel.exportToJson(context, uri)
                    android.widget.Toast.makeText(context, "Exporting JSON backup...", android.widget.Toast.LENGTH_SHORT).show()
                },
                onRestorePurchases = { viewModel.restorePurchases() },
                onToggleTestPremium = { viewModel.setTestPremiumEnabled(it) }
            )


            EnvelopeEditorModal(
                envelope = editingEnvelope,
                visible = editingEnvelope != null,
                hasPremium = viewModel.hasPremium(),
                isDarkMode = isDarkMode,
                totalAllocatedPercentage = envelopes.sumOf { it.percentage },
                onDismiss = { editingEnvelope = null },
                onSave = { updated -> viewModel.updateEnvelope(updated) },
                onDelete = { id -> viewModel.deleteEnvelope(id) }
            )

            AddEnvelopeModal(
                visible = showAddEnvelope,
                isDarkMode = isDarkMode,
                hasPremium = viewModel.hasPremium(),
                totalAllocatedPercentage = envelopes.sumOf { it.percentage },
                onDismiss = { showAddEnvelope = false },
                onAdd = { env ->
                    viewModel.addEnvelope(env)
                }
            )

            HeaderCardEditorModal(
                visible = editingHeaderCard != null,
                cardKey = editingHeaderCard,
                currentCards = headerCards,
                isDarkMode = isDarkMode,
                onDismiss = { editingHeaderCard = null },
                onSave = { cardKey, newConfig -> viewModel.updateHeaderCard(cardKey, newConfig) }
            )

            FundGoalEditorModal(
                visible = showFundGoalEditor,
                currentConfig = fundGoal,
                isDarkMode = isDarkMode,
                onDismiss = { showFundGoalEditor = false },
                onSave = { updated -> viewModel.updateFundGoal(updated) }
            )

            BackgroundEditorModal(
                visible = showBackgroundEditor,
                currentConfig = globalBg,
                isDarkMode = isDarkMode,
                onDismiss = { showBackgroundEditor = false },
                onSave = { updated -> viewModel.updateBackground(updated) }
            )

            WidgetCustomizerModal(
                visible = showWidgetCustomizer,
                fundGoalConfig = fundGoal,
                isDarkMode = isDarkMode,
                onDismiss = { showWidgetCustomizer = false },
                onSaveFundGoal = { updated -> viewModel.updateFundGoal(updated) }
            )




            // User Notice Floating Banner (Premium Glassmorphic Toast)
            AnimatedVisibility(
                visible = isNoticeVisible && !displayedNotice.isNullOrBlank(),
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(350)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                val bannerBg = if (isDarkMode) Color(0xF0181C26) else Color(0xF00F172A)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.panel))
                        .border(
                            AppStroke.thin,
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF10B981), GoldAccent, Color(0xFF10B981))
                            ),
                            RoundedCornerShape(AppShape.panel)
                        )
                        .clickable { isNoticeVisible = false },
                    color = bannerBg,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(AppShape.chip))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SUCCESS",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayedNotice ?: "",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { isNoticeVisible = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
