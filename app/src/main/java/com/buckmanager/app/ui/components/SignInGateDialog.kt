package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.buckmanager.app.auth.GoogleAuth
import com.buckmanager.app.auth.GoogleSignInOutcome
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.GoldAccent
import kotlinx.coroutines.launch

enum class SignInGateReason { Purchase, Restore }

@Composable
fun SignInGateDialog(
    visible: Boolean,
    reason: SignInGateReason,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSignedIn: (email: String, profilePicUrl: String?) -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sheetBg = if (isDarkMode) Color(0xFF181C26) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val muted = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)
    val hairline = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFE2E8F0)

    val title = if (reason == SignInGateReason.Restore) "Sign in to restore" else "Sign in to buy Premium"
    val body = if (reason == SignInGateReason.Restore) {
        "Restore checks Lifetime Premium on your Google Play account. A local session cannot see that purchase."
    } else {
        "Lifetime is tied to Google Play so you can restore it on a new phone. Local data stays on this device."
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, hairline, RoundedCornerShape(AppShape.panel)),
            shape = RoundedCornerShape(AppShape.panel),
            color = sheetBg
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3673FC).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF3673FC), modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF12151C) else Color(0xFFF1F5F9))
                            .clickable(enabled = !isLoading, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = muted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(title, color = textColor, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(body, color = muted, fontSize = 14.sp, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isLoading) return@Button
                        val activity = GoogleAuth.findActivity(context)
                        if (activity == null) {
                            errorMessage = "Unable to open Google sign-in."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            when (val outcome = GoogleAuth.signIn(activity)) {
                                is GoogleSignInOutcome.Success -> onSignedIn(outcome.email, outcome.profilePicUrl)
                                is GoogleSignInOutcome.Canceled ->
                                    errorMessage = "Sign-in didn't complete. Try again, or stay local."
                                is GoogleSignInOutcome.Failed -> errorMessage = outcome.message
                            }
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3673FC)),
                    shape = RoundedCornerShape(AppShape.button)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Connecting to Google…", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue with Google", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp, lineHeight = 18.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Not now",
                    color = muted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoading, onClick = onDismiss)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
