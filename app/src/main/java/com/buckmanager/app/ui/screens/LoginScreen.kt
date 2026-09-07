package com.buckmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.auth.GoogleAuth
import com.buckmanager.app.auth.GoogleSignInOutcome
import com.buckmanager.app.ui.AppChrome
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppStroke
import com.buckmanager.app.ui.GoldAccent
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    isDarkMode: Boolean,
    onLoginSuccess: (email: String, profilePicUrl: String?) -> Unit,
    onContinueLocally: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val latestOnLoginSuccess by rememberUpdatedState(onLoginSuccess)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDarkMode) AppChrome.pageDark else AppChrome.pageLight
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDarkMode) Color(0xFF8B92A5) else Color(0xFF64748B)
    val btnBgColor = if (isDarkMode) Color(0xFF1E1B2E) else Color.White
    val btnTextColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val footerTextColor = if (isDarkMode) Color(0xFF5A5E70) else Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.size(144.dp),
                shape = RoundedCornerShape(AppShape.hero),
                color = GoldAccent,
                shadowElevation = 16.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "B",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 68.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Buck Manager",
                color = titleColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SECURE. PRIVATE. ESSENTIAL.",
                color = subtitleColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                onClick = {
                    if (isLoading) return@Surface
                    val activity = GoogleAuth.findActivity(context)
                    if (activity == null) {
                        errorMessage = "Unable to open Google sign-in."
                        return@Surface
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        when (val outcome = GoogleAuth.signIn(activity)) {
                            is GoogleSignInOutcome.Success -> latestOnLoginSuccess(
                                outcome.email,
                                outcome.profilePicUrl
                            )
                            is GoogleSignInOutcome.Canceled ->
                                errorMessage = "Google sign-in didn't complete. You can continue locally and use the app on this phone."
                            is GoogleSignInOutcome.Failed -> errorMessage = outcome.message
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(AppShape.button),
                color = btnBgColor,
                border = androidx.compose.foundation.BorderStroke(AppStroke.thin, if (isDarkMode) AppChrome.mutedDark else AppChrome.mutedLight),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Connecting to Google...",
                            color = btnTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Google Sign In",
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Continue with Google",
                            color = btnTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    if (isLoading) return@TextButton
                    onContinueLocally()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Continue locally",
                    color = GoldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Works offline on this phone. Sign in later when you buy Premium.",
                color = footerTextColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Envelopes, transactions, and themes stay on this device. Premium and restore need Google so Play can keep the purchase.",
                color = footerTextColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
