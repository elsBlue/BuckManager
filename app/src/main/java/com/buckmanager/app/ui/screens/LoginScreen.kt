package com.buckmanager.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import android.util.Log
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.buckmanager.app.ui.GoldAccent
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Prefer email from JWT claim; fall back to credential id when it looks like an email. */
private fun resolveAccountEmail(credential: GoogleIdTokenCredential): String? {
    val fromId = credential.id.trim()
    if (fromId.contains("@")) return fromId

    return try {
        val parts = credential.idToken.split(".")
        if (parts.size < 2) return fromId.ifBlank { null }
        var payload = parts[1]
        val pad = (4 - payload.length % 4) % 4
        if (pad > 0) payload += "=".repeat(pad)
        val json = String(
            Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP),
            Charsets.UTF_8
        )
        val email = JSONObject(json).optString("email").trim()
        when {
            email.contains("@") -> email
            fromId.isNotBlank() -> fromId
            else -> null
        }
    } catch (e: Exception) {
        Log.w("LoginScreen", "Failed to parse email from idToken", e)
        fromId.ifBlank { null }
    }
}

@Composable
fun LoginScreen(
    isDarkMode: Boolean,
    onLoginSuccess: (email: String, profilePicUrl: String?) -> Unit,
    onContinueLocally: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val latestOnLoginSuccess by rememberUpdatedState(onLoginSuccess)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDarkMode) Color(0xFF0D0C14) else Color(0xFFF8FAFC)
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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.size(144.dp),
                shape = RoundedCornerShape(32.dp),
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
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val activity = context.findActivity()
                                ?: error("Activity required for Google Sign-In")

                            val webClientId =
                                "11378030984-giam2jtt3ckg0n16670a1v35nm9prceb.apps.googleusercontent.com"

                            val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(signInOption)
                                .build()

                            val result = credentialManager.getCredential(
                                context = activity,
                                request = request
                            )
                            val credential = result.credential
                            Log.i(
                                "LoginScreen",
                                "credential class=${credential.javaClass.name} type=${credential.type}"
                            )

                            val googleIdTokenCredential = when {
                                credential is CustomCredential &&
                                    (
                                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                                        ) -> {
                                    GoogleIdTokenCredential.createFrom(credential.data)
                                }
                                credential is CustomCredential -> {
                                    // Some Play services builds still wrap SiWG as CustomCredential
                                    // with a vendor-specific type string — try parse anyway.
                                    try {
                                        GoogleIdTokenCredential.createFrom(credential.data)
                                    } catch (parse: GoogleIdTokenParsingException) {
                                        errorMessage =
                                            "Unexpected credential type: ${credential.type}"
                                        Log.e("LoginScreen", "parse failed for ${credential.type}", parse)
                                        null
                                    }
                                }
                                else -> {
                                    errorMessage =
                                        "Unexpected credential: ${credential.javaClass.simpleName}"
                                    null
                                }
                            } ?: return@launch

                            val email = resolveAccountEmail(googleIdTokenCredential)
                            if (email.isNullOrBlank()) {
                                errorMessage =
                                    "Google did not return an email. Check OAuth client / SHA-1."
                                return@launch
                            }

                            Log.i("LoginScreen", "sign-in ok email=$email")
                            withContext(Dispatchers.Main.immediate) {
                                latestOnLoginSuccess(
                                    email,
                                    googleIdTokenCredential.profilePictureUri?.toString()
                                )
                            }
                        } catch (_: GetCredentialCancellationException) {
                            // Often fires after account pick when OAuth/SHA-1/package is wrong,
                            // not only when the user taps back.
                            errorMessage =
                                "Google sign-in didn't complete. You can continue locally and use the app on this phone."
                            Log.w("LoginScreen", "GetCredentialCancellationException after picker")
                        } catch (e: GetCredentialException) {
                            Log.e("LoginScreen", "Google sign in failed: ${e.javaClass.simpleName}", e)
                            errorMessage = listOfNotNull(
                                e.javaClass.simpleName,
                                e.message?.takeIf { it.isNotBlank() }
                            ).joinToString(": ").ifBlank {
                                "Google sign-in failed. Check Play services and OAuth client."
                            }
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e("LoginScreen", "Invalid Google ID token", e)
                            errorMessage = "Invalid Google ID token"
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Google sign in failed", e)
                            errorMessage = e.message?.takeIf { it.isNotBlank() }
                                ?: "Google sign-in failed"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = btnBgColor,
                shadowElevation = 4.dp
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
                text = "Works offline on this phone. Google is optional.",
                color = footerTextColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Envelopes, transactions, and themes stay on this device. Export a JSON backup from Settings. Google Drive sync is coming later.",
                color = footerTextColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
