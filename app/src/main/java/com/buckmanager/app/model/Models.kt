package com.buckmanager.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Envelope(
    val id: String,
    val name: String,
    val percentage: Int,
    val colorHex: String,
    val iconName: String = "folder",
    val backgroundColorHex: String = "#FFFFFF",
    val backgroundImageUri: String? = null,
    val dimOpacity: Int = 30,
    val orderIndex: Int = 0,
    val labelColorHex: String = "#3B82F6",
    val valueColorHex: String = "#1E293B",
    val descriptionColorHex: String = "#64748B",
    val radiusTopLeft: Int = 24,
    val radiusTopRight: Int = 24,
    val radiusBottomRight: Int = 24,
    val radiusBottomLeft: Int = 24,
    val borderTop: Int = 1,
    val borderRight: Int = 1,
    val borderBottom: Int = 1,
    val borderLeft: Int = 1,
    val borderColorHex: String = "#E2E8F0",
    val paddingTop: Int = 21,
    val paddingRight: Int = 21,
    val paddingBottom: Int = 21,
    val paddingLeft: Int = 21,
    val useGradient: Boolean = false,
    val gradientColors: List<String> = emptyList(),
    val gradientAngle: Float = 0f,
    val elevation: Int = 0
)

@Serializable
data class GlobalBackgroundConfig(
    val backgroundColorHex: String = "#F6FAFD",
    val backgroundImageUri: String? = null,
    val dimOpacity: Int = 30,
    val particleEffect: String = "none", // "none", "lines", "starfall"
    val textColorHex: String = "#000000",
    val appNameColorHex: String = "#000000",
    val titleColorHex: String = "#000000",
    val budgetEnvelopesColorHex: String = "#000000"
)

@Serializable
data class HeaderCardConfig(
    val backgroundColorHex: String,
    val backgroundImageUri: String? = null,
    val dimOpacity: Int = 30,
    val radiusTopLeft: Int = 24,
    val radiusTopRight: Int = 24,
    val radiusBottomRight: Int = 24,
    val radiusBottomLeft: Int = 24,
    val labelColorHex: String = "#FFFFFF",
    val valueColorHex: String = "#1E293B",
    val paddingTop: Int = 21,
    val paddingRight: Int = 21,
    val paddingBottom: Int = 21,
    val paddingLeft: Int = 21,
    val borderTop: Int = 1,
    val borderRight: Int = 1,
    val borderBottom: Int = 1,
    val borderLeft: Int = 1,
    val borderColorHex: String = "#CBD5E1",
    val useGradient: Boolean = false,
    val gradientColors: List<String> = emptyList(),
    val gradientAngle: Float = 0f,
    val elevation: Int = 2
)

@Serializable
data class HeaderCardsConfig(
    val netWorth: HeaderCardConfig = HeaderCardConfig(backgroundColorHex = "#3673FC", radiusTopLeft = 28, radiusTopRight = 28, radiusBottomRight = 28, radiusBottomLeft = 28, labelColorHex = "#FFFFFF", valueColorHex = "#FFFFFF", borderTop = 0, borderRight = 0, borderBottom = 0, borderLeft = 0),
    val income: HeaderCardConfig = HeaderCardConfig(backgroundColorHex = "#F4F7FE", radiusTopLeft = 20, radiusTopRight = 20, radiusBottomRight = 20, radiusBottomLeft = 20, labelColorHex = "#3673FC", valueColorHex = "#3673FC", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, borderColorHex = "#3673FC", elevation = 0),
    val expense: HeaderCardConfig = HeaderCardConfig(backgroundColorHex = "#FDF9ED", radiusTopLeft = 20, radiusTopRight = 20, radiusBottomRight = 20, radiusBottomLeft = 20, labelColorHex = "#FCBF36", valueColorHex = "#FCBF36", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, borderColorHex = "#FCBF36", elevation = 0)
)

@Serializable
data class FundGoalConfig(
    val name: String = "My Goal",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val backgroundColorHex: String = "#FFFFFF",
    val backgroundImageUri: String? = null,
    val dimOpacity: Int = 30,
    val radiusTopLeft: Int = 21,
    val radiusTopRight: Int = 21,
    val radiusBottomRight: Int = 21,
    val radiusBottomLeft: Int = 21,
    val labelColorHex: String = "#3673FC",
    val valueColorHex: String = "#121926",
    val borderTop: Int = 1,
    val borderRight: Int = 1,
    val borderBottom: Int = 1,
    val borderLeft: Int = 1,
    val borderColorHex: String = "#E2E8F0",
    val paddingTop: Int = 13,
    val paddingRight: Int = 13,
    val paddingBottom: Int = 13,
    val paddingLeft: Int = 13,
    val useGradient: Boolean = false,
    val gradientColors: List<String> = emptyList(),
    val gradientAngle: Float = 0f,
    val btnBgColorHex: String = "#3673FC",
    val btnTextColorHex: String = "#FFFFFF",
    val elevation: Int = 0
)

@Serializable
data class MonetizationState(
    val isPremium: Boolean = false,
    val premiumExpiryDate: Long = 0L,
    val adTickets: Int = 0
)

object AuthSession {
    const val LOCAL_EMAIL = "local@buckmanager.app"

    fun isLocal(email: String?): Boolean = email == LOCAL_EMAIL

    fun isGoogle(email: String?): Boolean = !email.isNullOrBlank() && email != LOCAL_EMAIL
}



data class EnvelopeStats(
    val allocated: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0
)

@Serializable
data class BackupTransactionDto(
    val type: String,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String
)

@Serializable
data class BackupPayload(
    val appName: String = "Buck Manager",
    val exportDate: String = "",
    val userEmail: String? = null,
    val transactions: List<BackupTransactionDto> = emptyList(),
    val envelopes: List<Envelope> = emptyList(),
    val globalBackground: GlobalBackgroundConfig = GlobalBackgroundConfig(),
    val headerCardsConfig: HeaderCardsConfig = HeaderCardsConfig(),
    val fundGoal: FundGoalConfig = FundGoalConfig()
)

@Serializable
data class SharedCustomizationPayload(
    val format: String = "bucktheme",
    val version: Int = 1,
    val themeName: String = "Custom Buck Theme",
    val globalBackground: GlobalBackgroundConfig = GlobalBackgroundConfig(),
    val headerCardsConfig: HeaderCardsConfig = HeaderCardsConfig(),
    val envelopes: List<Envelope> = emptyList(),
    val fundGoal: FundGoalConfig = FundGoalConfig()
)

