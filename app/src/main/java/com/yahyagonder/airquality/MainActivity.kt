@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.yahyagonder.airquality

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yahyagonder.airquality.viewmodel.DashboardViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.yahyagonder.airquality.ui.theme.AirQualityTheme
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import android.content.res.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AirQualityTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, @StringRes val titleRes: Int? = null, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Map : Screen("map", R.string.nav_map, Icons.Default.Map)
    object Measurements : Screen("measurements", R.string.nav_measurements, Icons.Default.History)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    object Share : Screen("share")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var currentLanguage by rememberSaveable { 
        mutableStateOf(if (context.resources.configuration.locales[0].language == "tr") "tr" else "en") 
    }

    @Suppress("DEPRECATION") val locale = java.util.Locale(currentLanguage)
    java.util.Locale.setDefault(locale)
    val configuration = Configuration(LocalConfiguration.current)
    configuration.setLocale(locale)
    
    val localizedContext = context.createConfigurationContext(configuration)

    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides localizedContext,
        LocalLifecycleOwner provides (context as ComponentActivity),
        LocalViewModelStoreOwner provides (context as ComponentActivity),
        LocalSavedStateRegistryOwner provides (context as ComponentActivity),
        LocalOnBackPressedDispatcherOwner provides (context as ComponentActivity),
        LocalActivityResultRegistryOwner provides (context as ComponentActivity)
    ) {
        val dashboardViewModel: DashboardViewModel = viewModel()
        val isWifiConnected by dashboardViewModel.isWifiConnected.collectAsState()
        val notificationsEnabled by dashboardViewModel.notificationsEnabled.collectAsState()
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentRoute = navBackStackEntry?.destination?.route

        val bottomBarItems = listOf(Screen.Home, Screen.Map, Screen.Measurements, Screen.Profile)
        
        // Simulated connection status state
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current

        // Handle POST_NOTIFICATIONS permission for Android 13+
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                dashboardViewModel.setNotificationsEnabled(true, context)
            } else {
                Toast.makeText(context, context.getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

        val auth = remember { FirebaseAuth.getInstance() }
        val startDestination = remember { 
            if (auth.currentUser != null) Screen.Home.route else Screen.Login.route 
        }

        // Show bars only if not on login screen and not on share screen
        val showBars = currentRoute != Screen.Login.route && currentRoute != Screen.Share.route

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showBars) {
                    TopAppBar(
                        title = { Text("") },
                        actions = {
                            Row(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Wi-Fi Toggle/Indicator
                                Icon(
                                    imageVector = if (isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = "Wi-Fi Connection Status",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            val newState = !isWifiConnected
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            dashboardViewModel.setWifiConnected(newState, context)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (newState) localizedContext.getString(R.string.device_connected) else localizedContext.getString(R.string.connection_lost)
                                                )
                                            }
                                        }
                                )

                                // Vertical Divider
                                Spacer(
                                    modifier = Modifier
                                        .height(16.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                )

                                // Live Notification Toggle/Indicator (active bell when service is running, otherwise standard notifications status icon)
                                val isServiceRunning = isWifiConnected && notificationsEnabled
                                Icon(
                                    imageVector = if (isServiceRunning) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = "Live Notification Status",
                                    tint = if (isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            if (isWifiConnected) {
                                                val newState = !notificationsEnabled
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (newState) {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    } else {
                                                        dashboardViewModel.setNotificationsEnabled(true, context)
                                                    }
                                                } else {
                                                    dashboardViewModel.setNotificationsEnabled(false, context)
                                                }
                                            } else {
                                                Toast.makeText(context, localizedContext.getString(R.string.no_connection), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            bottomBar = {
                if (showBars) {
                    Surface(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                        shadowElevation = 6.dp,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            bottomBarItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon!!, contentDescription = stringResource(screen.titleRes!!)) },
                                    label = { Text(stringResource(screen.titleRes!!)) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(
                    top = if (showBars) innerPadding.calculateTopPadding() else 0.dp,
                    bottom = 0.dp,
                    start = if (showBars) innerPadding.calculateStartPadding(LocalLayoutDirection.current) else 0.dp,
                    end = if (showBars) innerPadding.calculateEndPadding(LocalLayoutDirection.current) else 0.dp
                ),
                enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                composable(Screen.Login.route) { 
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = { currentLanguage = it }
                    ) 
                }
                composable(Screen.Home.route) { 
                    DashboardScreen(
                        isConnected = isWifiConnected,
                        bottomPadding = innerPadding.calculateBottomPadding()
                    ) 
                }
                composable(Screen.Map.route) { MapScreen(navController) }
                composable(Screen.Measurements.route) { 
                    MyMeasurementsScreen(bottomPadding = innerPadding.calculateBottomPadding()) 
                }
                composable(Screen.Profile.route) { 
                    UserScreen(
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        onLogout = {
                            dashboardViewModel.resetState()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) 
                }
                composable(Screen.Share.route) { ShareScreen(navController, isWifiConnected) }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val haptic = LocalHapticFeedback.current

    fun attemptLoginOrSignup() {
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(context, context.getString(R.string.enter_credentials), Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        auth?.signInWithEmailAndPassword(username, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLoading = false
                    onLoginSuccess()
                } else {
                    // Try to sign up if login fails
                    auth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener { signupTask ->
                            isLoading = false
                            if (signupTask.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, context.getString(R.string.auth_failed, signupTask.exception?.message ?: ""), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp)
        ) {
            LanguageToggle(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            val textFieldShape = RoundedCornerShape(16.dp)
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Lock, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    attemptLoginOrSignup() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), 
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_register), 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.designed_by),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun LanguageToggle(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageOption(
                text = "EN",
                isSelected = currentLanguage == "en",
                onClick = {
                    if (currentLanguage != "en") {
                        onLanguageChange("en")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            LanguageOption(
                text = "TR",
                isSelected = currentLanguage == "tr",
                onClick = {
                    if (currentLanguage != "tr") {
                        onLanguageChange("tr")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
        }
    }
}

@Composable
fun LanguageOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "bg_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun DashboardScreen(
    isConnected: Boolean,
    bottomPadding: Dp,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val sensorData by viewModel.sensorData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val haptic = LocalHapticFeedback.current

    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email
    val displayName = remember(email) {
        if (!email.isNullOrBlank() && email.contains("@")) {
            email.substringBefore("@")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        } else {
            context.getString(R.string.unknown)
        }
    }
    var selectedMetricForDialog by remember { mutableStateOf<String?>(null) }

    if (!isConnected) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_connection),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.error_prefix, error ?: ""),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) {
            val pm25Value = sensorData?.pm25 ?: 0
            
            val overallStatus = remember(pm25Value) {
                when {
                    pm25Value <= 12 -> context.getString(R.string.status_fresh)
                    pm25Value <= 35 -> context.getString(R.string.status_moderate)
                    else -> context.getString(R.string.status_poor)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.welcome_user, displayName),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = overallStatus,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val pagerState = rememberPagerState(pageCount = { 3 })
            
            LaunchedEffect(pagerState.currentPage) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp
            ) { page ->
                when (page) {
                    0 -> {
                        val (pmStatus, pmBgColor, pmContentColor) = remember(pm25Value) {
                            when {
                                pm25Value <= 12 -> Triple(context.getString(R.string.status_good), Color(0xD9E8F5E9), Color(0xFF1B5E20))
                                pm25Value <= 35 -> Triple(context.getString(R.string.status_moderate_label), Color(0xD9FFF3E0), Color(0xFFE65100))
                                else -> Triple(context.getString(R.string.status_unhealthy), Color(0xD9FFEBEE), Color(0xFFC62828))
                            }
                        }
                        SensorCard(
                            title = stringResource(R.string.pm25_title),
                            value = "$pm25Value ${stringResource(R.string.unit_pm25)}",
                            status = pmStatus,
                            icon = Icons.Default.Air,
                            backgroundColor = pmBgColor,
                            contentColor = pmContentColor,
                            onLearnMoreClick = { selectedMetricForDialog = "PM2.5" }
                        )
                    }
                    1 -> {
                        val vocValue = sensorData?.voc ?: 0
                        val (vocStatus, vocBgColor, vocContentColor) = remember(vocValue) {
                            when {
                                vocValue <= 200 -> Triple(context.getString(R.string.status_good), Color(0xD9E8F5E9), Color(0xFF1B5E20))
                                vocValue <= 500 -> Triple(context.getString(R.string.status_warning), Color(0xD9FFF3E0), Color(0xFFE65100))
                                else -> Triple(context.getString(R.string.status_danger), Color(0xD9FFEBEE), Color(0xFFC62828))
                            }
                        }
                        SensorCard(
                            title = stringResource(R.string.voc_title),
                            value = "$vocValue ${stringResource(R.string.unit_voc)}",
                            status = vocStatus,
                            icon = Icons.Default.Science,
                            backgroundColor = vocBgColor,
                            contentColor = vocContentColor,
                            onLearnMoreClick = { selectedMetricForDialog = "VOC" }
                        )
                    }
                    2 -> {
                        val uvValue = sensorData?.uv ?: 0
                        val (uvStatus, uvBgColor, uvContentColor) = remember(uvValue) {
                            when {
                                uvValue <= 2 -> Triple(context.getString(R.string.status_low), Color(0xD9F3E5F5), Color(0xFF4A148C))
                                uvValue <= 5 -> Triple(context.getString(R.string.status_moderate_label), Color(0xD9E1BEE7), Color(0xFF6A1B9A))
                                uvValue <= 7 -> Triple(context.getString(R.string.status_high), Color(0xD9CE93D8), Color(0xFF7B1FA2))
                                else -> Triple(context.getString(R.string.status_very_high), Color(0xD9BA68C8), Color(0xFF4A148C))
                            }
                        }
                        SensorCard(
                            title = stringResource(R.string.uv_title),
                            value = "$uvValue",
                            status = uvStatus,
                            icon = Icons.Default.WbSunny,
                            backgroundColor = uvBgColor,
                            contentColor = uvContentColor,
                            onLearnMoreClick = { selectedMetricForDialog = "UV" }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        selectedMetricForDialog?.let { metric ->
            MetricInfoDialog(
                metricType = metric,
                onDismiss = { selectedMetricForDialog = null }
            )
        }
    }
}

@Composable
fun SensorCard(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onLearnMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "value_animation"
                ) { targetValue ->
                    Text(
                        text = targetValue,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedContent(
                    targetState = status,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "status_animation"
                ) { targetStatus ->
                    Text(
                        text = targetStatus,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
            
            TextButton(
                onClick = onLearnMoreClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.learn_more), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricInfoDialog(metricType: String, onDismiss: () -> Unit) {
    val title: String
    val description: String
    when (metricType) {
        "PM2.5" -> {
            title = stringResource(R.string.about_pm25)
            description = stringResource(R.string.desc_pm25)
        }
        "VOC" -> {
            title = stringResource(R.string.about_voc)
            description = stringResource(R.string.desc_voc)
        }
        "UV" -> {
            title = stringResource(R.string.about_uv)
            description = stringResource(R.string.desc_uv)
        }
        else -> {
            title = ""
            description = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.got_it))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val sharedLocations by viewModel.sharedLocations.collectAsState()
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.0082, 28.9784), 10f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        )
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            // Keep the fixed station marker as well
            Marker(
                state = remember { MarkerState(position = LatLng(41.0082, 28.9784)) },
                title = stringResource(R.string.map_station_title),
                snippet = stringResource(R.string.map_station_snippet)
            )
            
            // Plot shared locations
            sharedLocations.forEach { loc ->
                Marker(
                    state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                    title = stringResource(R.string.map_shared_title, loc.pm25, loc.voc, loc.uv),
                    snippet = stringResource(R.string.map_shared_by, loc.sharedBy)
                )
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        FloatingActionButton(
            onClick = { navController.navigate(Screen.Share.route) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .scale(scale),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_title))
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ShareScreen(
    navController: NavController,
    isWifiConnected: Boolean,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val sensorData by viewModel.sensorData.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val haptic = LocalHapticFeedback.current

    var shareAnonymously by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    val pm25Display = if (isWifiConnected) "${sensorData?.pm25 ?: 0}" else "-"
    val vocDisplay = if (isWifiConnected) "${sensorData?.voc ?: 0}" else "-"
    val uvDisplay = if (isWifiConnected) "${sensorData?.uv ?: 0}" else "-"

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.share_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sensor Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.current_readings),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(stringResource(R.string.label_pm25), pm25Display, stringResource(R.string.unit_pm25))
                        MetricItem(stringResource(R.string.label_voc), vocDisplay, stringResource(R.string.unit_voc))
                        MetricItem(stringResource(R.string.label_uv), uvDisplay, stringResource(R.string.unit_uv))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = isWifiConnected,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "share_interface_transition"
            ) { connected ->
                if (connected) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.share_anonymously), style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = shareAnonymously,
                                onCheckedChange = { 
                                    shareAnonymously = it
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (isSharing) return@Button
                                isSharing = true
                                val hasLocationPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasLocationPermission) {
                                    Toast.makeText(context, context.getString(R.string.location_permission_required), Toast.LENGTH_SHORT).show()
                                    isSharing = false
                                    return@Button
                                }

                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val sharedBy = if (shareAnonymously) "Anonymous" else (currentUser?.email ?: "Unknown")
                                        viewModel.shareLocation(
                                            latitude = location.latitude,
                                            longitude = location.longitude,
                                            pm25 = sensorData?.pm25 ?: 0,
                                            voc = sensorData?.voc ?: 0,
                                            uv = sensorData?.uv ?: 0,
                                            sharedBy = sharedBy,
                                            userId = currentUser?.uid ?: ""
                                        )
                                        Toast.makeText(context, context.getString(R.string.location_shared), Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.location_error), Toast.LENGTH_SHORT).show()
                                        isSharing = false
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(context, context.getString(R.string.location_get_error), Toast.LENGTH_SHORT).show()
                                    isSharing = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSharing
                        ) {
                            if (isSharing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text(stringResource(R.string.share_on_map), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.connect_device_warning),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AirQualityTheme {
        LoginScreen(
            onLoginSuccess = {},
            currentLanguage = "en",
            onLanguageChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // Note: ProfileScreen and DashboardScreen might fail in preview due to Firebase/ViewModel
    Text("Please use LoginScreenPreview for UI verification")
}
