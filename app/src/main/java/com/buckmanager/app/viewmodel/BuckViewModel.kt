package com.buckmanager.app.viewmodel

import android.app.Activity
import android.app.Application
import com.buckmanager.app.BuildConfig
import com.buckmanager.app.billing.BillingManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buckmanager.app.data.AppDatabase
import com.buckmanager.app.data.SettingEntity
import com.buckmanager.app.data.TransactionEntity
import com.buckmanager.app.model.*
import com.buckmanager.app.widget.GoalAppWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class BuckViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val json = Json { ignoreUnknownKeys = true }

    // State Flows
    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _currencySymbol = MutableStateFlow(com.buckmanager.app.model.CurrencyConfig.symbol)
    val currencySymbol: StateFlow<String> = _currencySymbol

    fun refreshCurrency() {
        _currencySymbol.value = com.buckmanager.app.model.CurrencyConfig.symbol
    }

    fun createLocalSnapshot(context: android.content.Context) {
        viewModelScope.launch {
            com.buckmanager.app.utils.BackupUtils.createLocalSnapshot(context)
        }
    }

    fun exportToJson(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            com.buckmanager.app.utils.BackupUtils.exportToJson(context, db, uri)
        }
    }

    private val _envelopes = MutableStateFlow<List<Envelope>>(defaultEnvelopes())
    val envelopes: StateFlow<List<Envelope>> = _envelopes.asStateFlow()

    private val _globalBackground = MutableStateFlow(GlobalBackgroundConfig())
    val globalBackground: StateFlow<GlobalBackgroundConfig> = _globalBackground.asStateFlow()

    private val _headerCardsConfig = MutableStateFlow(HeaderCardsConfig())
    val headerCardsConfig: StateFlow<HeaderCardsConfig> = _headerCardsConfig.asStateFlow()

    private val _hasSeenOnboarding = MutableStateFlow(false)
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()

    fun completeOnboarding() {
        _hasSeenOnboarding.value = true
        viewModelScope.launch(Dispatchers.IO) {
            saveSetting("has_seen_onboarding", "true")
        }
    }

    private val _fundGoal = MutableStateFlow(FundGoalConfig())
    val fundGoal: StateFlow<FundGoalConfig> = _fundGoal.asStateFlow()

    private val _monetization = MutableStateFlow(MonetizationState())
    val monetization: StateFlow<MonetizationState> = _monetization.asStateFlow()




    private val _isEditLocked = MutableStateFlow(true)
    val isEditLocked: StateFlow<Boolean> = _isEditLocked.asStateFlow()

    private val _userNotice = MutableStateFlow<String?>(null)
    val userNotice: StateFlow<String?> = _userNotice.asStateFlow()

    fun clearUserNotice() {
        _userNotice.value = null
    }

    fun showNotice(message: String) {
        _userNotice.value = message
    }

    fun toggleEditLock() {
        val newState = !_isEditLocked.value
        _isEditLocked.value = newState
        viewModelScope.launch(Dispatchers.IO) {
            saveSetting("edit_locked", newState.toString())
        }
    }
    private val _isCustomizationLocked = MutableStateFlow(true)
    val isCustomizationLocked: StateFlow<Boolean> = _isCustomizationLocked.asStateFlow()

    private val _hideBalances = MutableStateFlow(false)
    val hideBalances: StateFlow<Boolean> = _hideBalances.asStateFlow()

    private val _notificationEnabled = MutableStateFlow(true)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _lastBackupDate = MutableStateFlow<String?>(null)
    val lastBackupDate: StateFlow<String?> = _lastBackupDate.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userProfilePicUrl = MutableStateFlow<String?>(null)
    val userProfilePicUrl: StateFlow<String?> = _userProfilePicUrl.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isThemeCustomized = MutableStateFlow(false)
    val isThemeCustomized: StateFlow<Boolean> = _isThemeCustomized.asStateFlow()

    private val billingManager = BillingManager(
        context = application,
        scope = viewModelScope,
        onPremiumOwned = { owned ->
            if (owned) {
                applyLifetimePremiumFromPlay()
            }
        },
        onMessage = { msg ->
            _userNotice.value = msg
        }
    )

    val premiumProductDetails = billingManager.productDetails

    fun premiumFormattedPrice(): String? = billingManager.formattedPrice()

    init {
        loadAllData()
        billingManager.start()
    }

    override fun onCleared() {
        billingManager.end()
        super.onCleared()
    }

    private fun applyLifetimePremiumFromPlay() {
        updateMonetization(_monetization.value.copy(isPremium = true))
        _isEditLocked.value = false
        viewModelScope.launch(Dispatchers.IO) {
            saveSetting("edit_locked", "false")
        }
    }

    private fun defaultEnvelopes(): List<Envelope> = defaultEnvelopesLight()

    private fun defaultEnvelopesDark(): List<Envelope> = listOf(
        Envelope(id = "main", name = "Main Envelope", percentage = 0, colorHex = "#3673FC", iconName = "wallet", backgroundColorHex = "#152040", orderIndex = 0, labelColorHex = "#3673FC", valueColorHex = "#FFFFFF", descriptionColorHex = "#6B7280"),
        Envelope(id = "needs", name = "Needs", percentage = 50, colorHex = "#FCBF36", iconName = "home", backgroundColorHex = "#2A2010", orderIndex = 1, labelColorHex = "#FCBF36", valueColorHex = "#FFFFFF", descriptionColorHex = "#9CA3AF"),
        Envelope(id = "wants", name = "Wants", percentage = 30, colorHex = "#38BDF8", iconName = "game-controller", backgroundColorHex = "#102838", orderIndex = 2, labelColorHex = "#38BDF8", valueColorHex = "#FFFFFF", descriptionColorHex = "#9CA3AF"),
        Envelope(id = "savings", name = "Savings", percentage = 20, colorHex = "#F5B041", iconName = "chart", backgroundColorHex = "#282010", orderIndex = 3, labelColorHex = "#F5B041", valueColorHex = "#FFFFFF", descriptionColorHex = "#9CA3AF")
    )

    private fun defaultEnvelopesLight(): List<Envelope> = listOf(
        Envelope(id = "main", name = "Main Envelope", percentage = 0, colorHex = "#3673FC", iconName = "wallet", backgroundColorHex = "#EBF0FF", orderIndex = 0, labelColorHex = "#2856C8", valueColorHex = "#0F172A", descriptionColorHex = "#94A3B8"),
        Envelope(id = "needs", name = "Needs", percentage = 50, colorHex = "#D4950A", iconName = "home", backgroundColorHex = "#FFF8E7", orderIndex = 1, labelColorHex = "#B8860B", valueColorHex = "#0F172A", descriptionColorHex = "#64748B"),
        Envelope(id = "wants", name = "Wants", percentage = 30, colorHex = "#2563EB", iconName = "game-controller", backgroundColorHex = "#EFF6FF", orderIndex = 2, labelColorHex = "#1D6DB8", valueColorHex = "#0F172A", descriptionColorHex = "#64748B"),
        Envelope(id = "savings", name = "Savings", percentage = 20, colorHex = "#A67B00", iconName = "chart", backgroundColorHex = "#FFF6E0", orderIndex = 3, labelColorHex = "#A67B00", valueColorHex = "#0F172A", descriptionColorHex = "#64748B")
    )

    fun loadAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load transactions
            val txList = db.transactionDao().getAllTransactions()
            _transactions.value = txList


            // Load settings
            val settings = db.settingDao().getAllSettings().associate { it.key to it.value }
            _hasSeenOnboarding.value = settings["has_seen_onboarding"] == "true"

            // Envelopes config
            settings["envelopes_config"]?.let { envStr ->
                try {
                    val parsed = json.decodeFromString<List<Envelope>>(envStr)
                    _envelopes.value = parsed.sortedBy { it.orderIndex }
                } catch (e: Exception) {
                    _envelopes.value = defaultEnvelopes()
                }
            } ?: run {
                saveSetting("envelopes_config", json.encodeToString(defaultEnvelopes()))
            }

            // Global background
            settings["global_background_config"]?.let { bgStr ->
                try {
                    _globalBackground.value = json.decodeFromString(bgStr)
                } catch (e: Exception) {}
            }

            // Header cards
            settings["header_cards_config"]?.let { hcStr ->
                try {
                    _headerCardsConfig.value = json.decodeFromString(hcStr)
                } catch (e: Exception) {}
            }

            // Fund goal (synchronous load to ensure it's available for applyThemePreset)
            settings["fund_goal_config"]?.let { fgStr ->
                try {
                    _fundGoal.value = json.decodeFromString<FundGoalConfig>(fgStr)
                } catch (e: Exception) {}
            }

            // Fund goal (observed continuously to sync with widget)
            viewModelScope.launch(Dispatchers.IO) {
                db.settingDao().getSettingFlow("fund_goal_config").collect { entity ->
                    entity?.value?.let { fgStr ->
                        try {
                            val fg = json.decodeFromString<FundGoalConfig>(fgStr)
                            _fundGoal.value = fg
                            GoalAppWidgetProvider.saveGoalToPrefs(getApplication(), fg)
                        } catch (e: Exception) {}
                    }
                }
            }

            // Monetization
            settings["monetization"]?.let { monStr ->
                try {
                    val mon = json.decodeFromString<MonetizationState>(monStr)
                    _monetization.value = mon
                    val now = System.currentTimeMillis()
                    _isCustomizationLocked.value = !(mon.isPremium || mon.premiumExpiryDate > now || mon.adTickets > 0)
                } catch (e: Exception) {}
            } ?: run {
                _isCustomizationLocked.value = true
            }

            settings["edit_locked"]?.let { locked ->
                _isEditLocked.value = locked.toBoolean()
            }

            // Debug APK (GitHub Actions) starts with premium so customization can be tested.
            if (BuildConfig.DEBUG && settings["test_premium"] != "false") {
                applyLifetimePremiumFromPlay()
            }



            // User Email and Profile Pic
            _userEmail.value = settings["user_email"]
            _userProfilePicUrl.value = settings["user_profile_pic"]
            _notificationEnabled.value = settings["notification_enabled"]?.toBoolean() ?: true
            _isDarkMode.value = (settings["app_theme_mode"] ?: "light") == "dark"
            _isThemeCustomized.value = settings["is_theme_customized"]?.toBoolean() ?: false
            _lastBackupDate.value = settings["last_backup_date"]
            
            if (!_isThemeCustomized.value) {
                applyThemePreset(_isDarkMode.value)
            }
        }
    }

    // Calculated fields
    fun getTotalIncome(): Double = _transactions.value.filter { it.type == "income" }.sumOf { it.amount }
    
    fun getTotalExpense(): Double = _transactions.value.filter { 
        it.type == "expense" && it.category != "goal" && !(it.category == "main" && it.description.contains("Goal"))
    }.sumOf { it.amount }
    
    fun getNetWorth(): Double = getTotalIncome() - getTotalExpense()

    fun getEnvelopeStats(envelopeId: String): EnvelopeStats {
        val envelopes = _envelopes.value
        val env = envelopes.find { it.id == envelopeId } ?: return EnvelopeStats()
        
        val totalIncome = getTotalIncome()
        
        // Goal deposits shrink the shared pool, regular expenses do not.
        val goalTransfers = _transactions.value.filter { 
            it.type == "expense" && (it.category == "goal" || (it.category == "main" && it.description.contains("Goal")))
        }.sumOf { it.amount }
        
        val pool = (totalIncome - goalTransfers).coerceAtLeast(0.0)
        
        // Calculate raw remaining for ALL envelopes to find overspending
        var totalOverspent = 0.0
        val rawStats = envelopes.associate { e ->
            val allocated = pool * (e.percentage / 100.0)
            val spent = _transactions.value.filter { 
                it.type == "expense" && it.category == e.id && !(it.category == "main" && it.description.contains("Goal"))
            }.sumOf { it.amount }
            val rawRemaining = allocated - spent
            if (rawRemaining < 0) {
                totalOverspent += -rawRemaining
            }
            e.id to Triple(allocated, spent, rawRemaining)
        }
        
        val totalPositiveRemaining = rawStats.values.filter { it.third > 0 }.sumOf { it.third }
        val myRaw = rawStats[envelopeId] ?: Triple(0.0, 0.0, 0.0)
        
        val allocated = myRaw.first
        val spent = myRaw.second
        val rawRemaining = myRaw.third
        
        val adjustedRemaining = if (rawRemaining < 0) {
            0.0 // No minus number
        } else {
            // Deduct proportional overspend from other envelopes
            if (totalPositiveRemaining > 0) {
                val deduction = totalOverspent * (rawRemaining / totalPositiveRemaining)
                (rawRemaining - deduction).coerceAtLeast(0.0)
            } else {
                0.0 // Overspent entire net worth
            }
        }
        
        return EnvelopeStats(allocated = allocated, spent = spent, remaining = adjustedRemaining)
    }

    // Actions
    fun addTransaction(type: String, amount: Double, category: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val tx = TransactionEntity(
                type = type,
                amount = amount,
                category = category,
                date = dateIso,
                description = description.ifBlank { if (type == "income") "Income" else "Expense" }
            )
            db.transactionDao().insertTransaction(tx)
            _transactions.value = db.transactionDao().getAllTransactions()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.transactionDao().deleteTransaction(id)
            _transactions.value = db.transactionDao().getAllTransactions()
        }
    }

    private suspend fun markCustomized() {
        if (!_isThemeCustomized.value) {
            _isThemeCustomized.value = true
            saveSetting("is_theme_customized", "true")
        }
    }

    fun toggleThemeMode(isDark: Boolean) {
        if (_isThemeCustomized.value) return // Disabled when custom modifications exist
        viewModelScope.launch(Dispatchers.IO) {
            _isDarkMode.value = isDark
            saveSetting("app_theme_mode", if (isDark) "dark" else "light")
            applyThemePreset(isDark)
        }
    }

    fun resetCustomizationToDefaultTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            _isThemeCustomized.value = false
            saveSetting("is_theme_customized", "false")
            applyThemePreset(_isDarkMode.value)
        }
    }

    private suspend fun applyThemePreset(isDark: Boolean) {
        if (isDark) {
            val bg = GlobalBackgroundConfig(backgroundColorHex = "#0F1117", textColorHex = "#FFFFFF", appNameColorHex = "#FFFFFF", titleColorHex = "#FFFFFF", budgetEnvelopesColorHex = "#FFFFFF")
            val hc = HeaderCardsConfig(
                netWorth = HeaderCardConfig(backgroundColorHex = "#181C26", radiusTopLeft = 28, radiusTopRight = 28, radiusBottomRight = 28, radiusBottomLeft = 28, labelColorHex = "#FFFFFF", valueColorHex = "#FFFFFF", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, elevation = 0),
                income = HeaderCardConfig(backgroundColorHex = "#0F172A", radiusTopLeft = 20, radiusTopRight = 20, radiusBottomRight = 20, radiusBottomLeft = 20, labelColorHex = "#3673FC", valueColorHex = "#3673FC", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, borderColorHex = "#3673FC", elevation = 0),
                expense = HeaderCardConfig(backgroundColorHex = "#1C1910", radiusTopLeft = 20, radiusTopRight = 20, radiusBottomRight = 20, radiusBottomLeft = 20, labelColorHex = "#FCBF36", valueColorHex = "#FCBF36", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, borderColorHex = "#FCBF36", elevation = 0)
            )
            val fg = _fundGoal.value.copy(backgroundColorHex = "#181C26", labelColorHex = "#2563EB", valueColorHex = "#FFFFFF", borderColorHex = "#38BDF8", btnBgColorHex = "#2563EB", btnTextColorHex = "#FFFFFF")
            
            val envs = _envelopes.value.map { env ->
                when(env.id) {
                    "main" -> env.copy(colorHex = "#3673FC", backgroundColorHex = "#152040", labelColorHex = "#3673FC", valueColorHex = "#FFFFFF", descriptionColorHex = "#6B7280")
                    "needs" -> env.copy(colorHex = "#FCBF36", backgroundColorHex = "#2A2010", labelColorHex = "#FCBF36", valueColorHex = "#FFFFFF", descriptionColorHex = "#9CA3AF")
                    "wants" -> env.copy(colorHex = "#38BDF8", backgroundColorHex = "#102838", labelColorHex = "#38BDF8", valueColorHex = "#FFFFFF", descriptionColorHex = "#9CA3AF")
                    "savings" -> env.copy(colorHex = "#F5B041", backgroundColorHex = "#282010", labelColorHex = "#F5B041", valueColorHex = "#FFFFFF", descriptionColorHex = "#9CA3AF")
                    else -> env
                }
            }

            _globalBackground.value = bg
            _headerCardsConfig.value = hc
            _fundGoal.value = fg
            _envelopes.value = envs

            saveSetting("global_background_config", json.encodeToString(bg))
            saveSetting("header_cards_config", json.encodeToString(hc))
            saveSetting("fund_goal_config", json.encodeToString(fg))
            saveSetting("envelopes_config", json.encodeToString(envs))
        } else {
            val bg = GlobalBackgroundConfig(backgroundColorHex = "#F6FAFD", textColorHex = "#0F172A", appNameColorHex = "#000000", titleColorHex = "#000000", budgetEnvelopesColorHex = "#000000")
            val hc = HeaderCardsConfig(
                netWorth = HeaderCardConfig(backgroundColorHex = "#3673FC", radiusTopLeft = 28, radiusTopRight = 28, radiusBottomRight = 28, radiusBottomLeft = 28, labelColorHex = "#FFFFFF", valueColorHex = "#FFFFFF", borderTop = 0, borderRight = 0, borderBottom = 0, borderLeft = 0, elevation = 0),
                income = HeaderCardConfig(backgroundColorHex = "#F4F7FE", radiusTopLeft = 20, radiusTopRight = 20, radiusBottomRight = 20, radiusBottomLeft = 20, labelColorHex = "#3673FC", valueColorHex = "#3673FC", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, borderColorHex = "#3673FC", elevation = 0),
                expense = HeaderCardConfig(backgroundColorHex = "#FDF9ED", radiusTopLeft = 20, radiusTopRight = 20, radiusBottomRight = 20, radiusBottomLeft = 20, labelColorHex = "#FCBF36", valueColorHex = "#FCBF36", borderTop = 1, borderRight = 1, borderBottom = 1, borderLeft = 1, borderColorHex = "#FCBF36", elevation = 0)
            )
            val fg = _fundGoal.value.copy(backgroundColorHex = "#FFFFFF", labelColorHex = "#3673FC", valueColorHex = "#121926", borderColorHex = "#E2E8F0", btnBgColorHex = "#3673FC", btnTextColorHex = "#FFFFFF", elevation = 0)
            
            val envs = _envelopes.value.map { env ->
                when(env.id) {
                    "main" -> env.copy(colorHex = "#3673FC", backgroundColorHex = "#EBF0FF", labelColorHex = "#2856C8", valueColorHex = "#0F172A", descriptionColorHex = "#94A3B8")
                    "needs" -> env.copy(colorHex = "#D4950A", backgroundColorHex = "#FFF8E7", labelColorHex = "#B8860B", valueColorHex = "#0F172A", descriptionColorHex = "#64748B")
                    "wants" -> env.copy(colorHex = "#2563EB", backgroundColorHex = "#EFF6FF", labelColorHex = "#1D6DB8", valueColorHex = "#0F172A", descriptionColorHex = "#64748B")
                    "savings" -> env.copy(colorHex = "#A67B00", backgroundColorHex = "#FFF6E0", labelColorHex = "#A67B00", valueColorHex = "#0F172A", descriptionColorHex = "#64748B")
                    else -> env
                }
            }

            _globalBackground.value = bg
            _headerCardsConfig.value = hc
            _fundGoal.value = fg
            _envelopes.value = envs

            saveSetting("global_background_config", json.encodeToString(bg))
            saveSetting("header_cards_config", json.encodeToString(hc))
            saveSetting("fund_goal_config", json.encodeToString(fg))
            saveSetting("envelopes_config", json.encodeToString(envs))
        }
    }

    fun updateEnvelope(updated: Envelope) {
        if (updated.id == "main") return // Main is a calculated buffer

        viewModelScope.launch(Dispatchers.IO) {
            markCustomized()
            val oldList = _envelopes.value
            
            // Calculate sum of all other envelopes (excluding main and the one being updated)
            val otherSum = oldList.filter { it.id != "main" && it.id != updated.id }.sumOf { it.percentage }
            
            // Cap the new percentage so it doesn't exceed 100% total
            val cappedPercentage = minOf(updated.percentage, (100 - otherSum).coerceAtLeast(0))
            val finalUpdated = updated.copy(percentage = cappedPercentage)
            
            // Main envelope takes whatever is left
            val mainPercentage = (100 - otherSum - cappedPercentage).coerceAtLeast(0)
            
            val newList = oldList.map { env ->
                when (env.id) {
                    updated.id -> finalUpdated
                    "main" -> env.copy(percentage = mainPercentage)
                    else -> env
                }
            }
            
            _envelopes.value = newList
            saveSetting("envelopes_config", json.encodeToString(newList))
        }
    }

    fun addEnvelope(newEnv: Envelope) {
        viewModelScope.launch(Dispatchers.IO) {
            markCustomized()
            val currentList = _envelopes.value
            val otherSum = currentList.filter { it.id != "main" }.sumOf { it.percentage }
            
            val cappedPercentage = minOf(newEnv.percentage, (100 - otherSum).coerceAtLeast(0))
            val mainPercentage = (100 - otherSum - cappedPercentage).coerceAtLeast(0)
            
            val envWithId = newEnv.copy(
                id = "custom_${System.currentTimeMillis()}",
                orderIndex = currentList.size,
                percentage = cappedPercentage
            )
            
            val newList = currentList.map { env ->
                if (env.id == "main") env.copy(percentage = mainPercentage) else env
            } + envWithId
            
            _envelopes.value = newList
            saveSetting("envelopes_config", json.encodeToString(newList))
        }
    }

    fun deleteEnvelope(id: String) {
        if (id == "main") return // Protect main envelope
        viewModelScope.launch(Dispatchers.IO) {
            val listWithoutDeleted = _envelopes.value.filter { it.id != id }
            val otherSum = listWithoutDeleted.filter { it.id != "main" }.sumOf { it.percentage }
            val newList = listWithoutDeleted.map { env ->
                if (env.id == "main") env.copy(percentage = (100 - otherSum).coerceAtLeast(0)) else env
            }
            _envelopes.value = newList
            saveSetting("envelopes_config", json.encodeToString(newList))
        }
    }

    fun updateAllEnvelopes(newList: List<Envelope>) {
        viewModelScope.launch(Dispatchers.IO) {
            val otherSum = newList.filter { it.id != "main" }.sumOf { it.percentage }
            val finalList = newList.map { env ->
                if (env.id == "main") env.copy(percentage = (100 - otherSum).coerceAtLeast(0)) else env
            }
            _envelopes.value = finalList
            saveSetting("envelopes_config", json.encodeToString(finalList))
        }
    }

    fun applyAllocationStrategy(percentages: Map<String, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            val otherSum = percentages.filterKeys { it != "main" }.values.sum()
            val mainPercent = (100 - otherSum).coerceAtLeast(0)
            
            val newList = _envelopes.value.map { env ->
                if (env.id == "main") {
                    env.copy(percentage = mainPercent)
                } else {
                    val newPercentage = percentages[env.id]
                    if (newPercentage != null) env.copy(percentage = newPercentage) else env
                }
            }
            _envelopes.value = newList
            saveSetting("envelopes_config", json.encodeToString(newList))
        }
    }

    fun updateBackground(config: GlobalBackgroundConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            markCustomized()
            _globalBackground.value = config
            saveSetting("global_background_config", json.encodeToString(config))
        }
    }

    fun updateHeaderCard(cardKey: String, config: HeaderCardConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            markCustomized()
            val current = _headerCardsConfig.value
            val newCards = when (cardKey) {
                "netWorth" -> current.copy(netWorth = config)
                "income" -> current.copy(income = config)
                "expense" -> current.copy(expense = config)
                else -> current
            }
            _headerCardsConfig.value = newCards
            saveSetting("header_cards_config", json.encodeToString(newCards))
        }
    }

    fun updateFundGoal(config: FundGoalConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            markCustomized()
            _fundGoal.value = config
            saveSetting("fund_goal_config", json.encodeToString(config))
            GoalAppWidgetProvider.saveGoalToPrefs(getApplication(), config)
        }
    }

    fun depositToGoal(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentGoal = _fundGoal.value
            val applied = if (amount > 0) {
                val room = (getNetWorth() - currentGoal.currentAmount).coerceAtLeast(0.0)
                minOf(amount, room)
            } else {
                maxOf(amount, -currentGoal.currentAmount)
            }
            if (applied == 0.0) {
                _userNotice.value = if (amount > 0) {
                    "Not enough balance to deposit to your goal."
                } else {
                    "Goal is empty."
                }
                return@launch
            }
            val newAmount = (currentGoal.currentAmount + applied).coerceAtLeast(0.0)
            val updatedGoal = currentGoal.copy(currentAmount = newAmount)
            _fundGoal.value = updatedGoal
            saveSetting("fund_goal_config", json.encodeToString(updatedGoal))
            GoalAppWidgetProvider.saveGoalToPrefs(getApplication(), updatedGoal)

            addTransaction(
                type = "expense",
                amount = applied,
                category = "goal",
                description = if (applied > 0) "Deposit to My Goal" else "Withdraw from My Goal"
            )
        }
    }

    fun hasPremium(): Boolean {
        val mon = _monetization.value
        return mon.isPremium || mon.premiumExpiryDate > System.currentTimeMillis() || mon.adTickets > 0
    }

    fun updateMonetization(mon: MonetizationState) {
        viewModelScope.launch(Dispatchers.IO) {
            _monetization.value = mon
            val now = System.currentTimeMillis()
            _isCustomizationLocked.value = !(mon.isPremium || mon.premiumExpiryDate > now || mon.adTickets > 0)
            saveSetting("monetization", json.encodeToString(mon))
        }
    }

    fun unlockTemporaryCustomization() {
        val currentMon = _monetization.value
        if (currentMon.adTickets > 0) {
            val newMon = currentMon.copy(adTickets = currentMon.adTickets - 1)
            updateMonetization(newMon)
            _isCustomizationLocked.value = false
            _isEditLocked.value = false
            _userNotice.value = "Customization unlocked. Edit mode is on."
        }
    }

    fun watchAd() {
        // AdMob rewarded ads not wired yet - do not grant premium for free.
        _userNotice.value = "Rewarded ads coming soon. Buy Lifetime Premium via Play Billing for now."
    }

    fun purchaseLifetimePremium(activity: Activity) {
        billingManager.launchPremiumPurchase(activity)
    }

    fun restorePurchases() {
        if (!billingManager.isReady.value) {
            _userNotice.value = "Play Billing isn't available on this install yet. Restore will work after the app is listed on Play."
            billingManager.start()
            return
        }
        billingManager.refreshPurchases(notifyIfMissing = true)
        _userNotice.value = "Checking Play purchases..."
    }

    fun setTestPremiumEnabled(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch(Dispatchers.IO) {
            saveSetting("test_premium", enabled.toString())
            if (enabled) {
                applyLifetimePremiumFromPlay()
                _userNotice.value = "Test Premium on. Tap the gold lock, then a card to customize."
            } else {
                updateMonetization(_monetization.value.copy(isPremium = false, premiumExpiryDate = 0L, adTickets = 0))
                _isEditLocked.value = true
                saveSetting("edit_locked", "true")
                _userNotice.value = "Test Premium off."
            }
        }
    }

    fun continueLocally() {
        setUserEmail(AuthSession.LOCAL_EMAIL)
        setUserProfilePicUrl(null)
    }




    fun toggleHideBalances() { _hideBalances.value = !_hideBalances.value }

    fun toggleNotification(enabled: Boolean) {
        _notificationEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            saveSetting("notification_enabled", enabled.toString())
        }
    }

    fun setUserEmail(email: String?) {
        _userEmail.value = email
        viewModelScope.launch(Dispatchers.IO) {
            if (email != null) {
                saveSetting("user_email", email)
            } else {
                db.settingDao().deleteSetting("user_email")
            }
        }
    }

    fun setUserProfilePicUrl(url: String?) {
        _userProfilePicUrl.value = url
        viewModelScope.launch(Dispatchers.IO) {
            if (url != null) {
                saveSetting("user_profile_pic", url)
            } else {
                db.settingDao().deleteSetting("user_profile_pic")
            }
        }
    }

    // Backup & Restore via Google Drive
    fun backupDataToDrive(context: android.content.Context, onResult: (Boolean, String, android.content.Intent?) -> Unit) {
        val userEmail = _userEmail.value
        if (userEmail == null || AuthSession.isLocal(userEmail)) {
            onResult(false, "Google Drive backup is coming soon. Export a JSON file from Settings for now.", null)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = BackupPayload(
                    exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                    userEmail = userEmail,
                    transactions = _transactions.value.map {
                        BackupTransactionDto(it.type, it.amount, it.category, it.date, it.description)
                    },
                    envelopes = _envelopes.value,
                    globalBackground = _globalBackground.value,
                    headerCardsConfig = _headerCardsConfig.value,
                    fundGoal = _fundGoal.value
                )
                val jsonString = json.encodeToString(payload)

                val zipFile = com.buckmanager.app.utils.ZipUtils.createBackupZip(context, jsonString)
                val driveServiceHelper = com.buckmanager.app.utils.DriveServiceHelper.getDriveService(context, userEmail)
                
                val success = driveServiceHelper.uploadBackup(zipFile)
                if (success) {
                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())
                    saveSetting("last_backup_date", dateStr)
                    _lastBackupDate.value = dateStr
                    withContext(Dispatchers.Main) {
                        onResult(true, "Data successfully backed up to Google Drive!", null)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Failed to upload backup.", null)
                    }
                }
            } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Authorization required.", e.intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.message ?: "Failed to upload backup."}", null)
                }
            }
        }
    }

    fun restoreDataFromDrive(context: android.content.Context, onResult: (Boolean, String, android.content.Intent?) -> Unit) {
        val userEmail = _userEmail.value
        if (userEmail == null || AuthSession.isLocal(userEmail)) {
            onResult(false, "Google Drive restore is coming soon. Use Export JSON as your backup for now.", null)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destFile = java.io.File(context.cacheDir, "downloaded_backup.zip")
                val driveServiceHelper = com.buckmanager.app.utils.DriveServiceHelper.getDriveService(context, userEmail)
                
                val success = driveServiceHelper.downloadBackup(destFile)
                if (!success) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "No backup found in Google Drive.", null)
                    }
                    return@launch
                }

                val jsonString = com.buckmanager.app.utils.ZipUtils.extractBackupZip(context, destFile)
                if (jsonString == null) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Invalid or corrupt backup file.", null)
                    }
                    return@launch
                }

                val payload = json.decodeFromString<BackupPayload>(jsonString)
                
                // Clear existing transactions
                db.transactionDao().getAllTransactions().forEach { db.transactionDao().deleteTransaction(it.id) }

                // Insert transactions
                payload.transactions.forEach { tx ->
                    db.transactionDao().insertTransaction(
                        TransactionEntity(
                            type = tx.type,
                            amount = tx.amount,
                            category = tx.category,
                            date = tx.date,
                            description = tx.description
                        )
                    )
                }

                // Save configurations
                if (payload.envelopes.isNotEmpty()) {
                    saveSetting("envelopes_config", json.encodeToString(payload.envelopes))
                }
                saveSetting("global_background_config", json.encodeToString(payload.globalBackground))
                saveSetting("header_cards_config", json.encodeToString(payload.headerCardsConfig))
                saveSetting("fund_goal_config", json.encodeToString(payload.fundGoal))
                if (!payload.userEmail.isNullOrBlank()) {
                    saveSetting("user_email", payload.userEmail)
                }

                loadAllData()
                withContext(Dispatchers.Main) {
                    onResult(true, "Data successfully restored from Google Drive!", null)
                }
            } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Authorization required.", e.intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.message ?: "Invalid backup format or corrupt data."}", null)
                }
            }
        }
    }

    // Share Customization
    fun exportCustomizationJson(): String {
        val payload = SharedCustomizationPayload(
            globalBackground = _globalBackground.value,
            headerCardsConfig = _headerCardsConfig.value,
            envelopes = _envelopes.value
        )
        return json.encodeToString(payload)
    }

    fun importCustomizationJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                markCustomized()
                val payload = json.decodeFromString<SharedCustomizationPayload>(jsonString)
                saveSetting("global_background_config", json.encodeToString(payload.globalBackground))
                saveSetting("header_cards_config", json.encodeToString(payload.headerCardsConfig))
                if (payload.envelopes.isNotEmpty()) {
                    saveSetting("envelopes_config", json.encodeToString(payload.envelopes))
                }
                loadAllData()
                onResult(true, "Custom theme applied successfully!")
            } catch (e: Exception) {
                onResult(false, "Invalid theme customization code.")
            }
        }
    }

    private suspend fun saveSetting(key: String, value: String) {
        db.settingDao().insertSetting(SettingEntity(key, value))
    }
}

