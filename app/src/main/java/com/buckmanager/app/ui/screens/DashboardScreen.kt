package com.buckmanager.app.ui.screens

import android.app.Activity
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.buckmanager.app.model.Envelope
import com.buckmanager.app.model.formatRp
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppSpacing
import com.buckmanager.app.ui.GoldAccent
import com.buckmanager.app.ui.appTextFieldColors
import com.buckmanager.app.utils.customCardStyle
import com.buckmanager.app.ui.components.GoalDepositModal
import com.buckmanager.app.ui.components.PremiumModal
import com.buckmanager.app.model.MonetizationState
import com.buckmanager.app.ui.components.UniversalHeader
import com.buckmanager.app.ui.components.getIconVector
import com.buckmanager.app.ui.components.parseHexColor
import com.buckmanager.app.viewmodel.BuckViewModel
import com.buckmanager.app.widget.GoalAppWidgetProvider
import java.text.NumberFormat
import java.util.Locale



@Composable
fun DashboardScreen(
    viewModel: BuckViewModel,
    onNavigateToTransactions: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditEnvelope: (Envelope) -> Unit,
    onAddEnvelopeClick: () -> Unit,
    onEditHeaderCard: (String) -> Unit,
    onEditFundGoal: () -> Unit,
    onEditBackground: () -> Unit
) {
    val context = LocalContext.current
    val globalBg by viewModel.globalBackground.collectAsState()
    val headerCards by viewModel.headerCardsConfig.collectAsState()
    val fundGoal by viewModel.fundGoal.collectAsState()
    val envelopes by viewModel.envelopes.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isEditLocked by viewModel.isEditLocked.collectAsState()

    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showGoalDepositModal by remember { mutableStateOf(false) }
    var showPremiumModal by remember { mutableStateOf(false) }
    var showTransactionBottomSheet by remember { mutableStateOf(false) }
    val monetization by viewModel.monetization.collectAsState(initial = MonetizationState())

    val hideBalances by viewModel.hideBalances.collectAsState()

    val transactions by viewModel.transactions.collectAsState()
    val netWorth = remember(transactions) { viewModel.getNetWorth() }
    val totalIncome = remember(transactions) { viewModel.getTotalIncome() }
    val totalExpense = remember(transactions) { viewModel.getTotalExpense() }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                UniversalHeader(
                    title = "Dashboard",
                    hasPremium = viewModel.hasPremium(),
                    isEditLocked = isEditLocked,
                    isDarkMode = isDarkMode,
                    hideBalances = hideBalances,
                    onToggleLock = { viewModel.toggleEditLock() },
                    onCustomizeClick = onEditBackground,
                    onPremiumClick = { showPremiumModal = true },
                    onToggleHideBalances = { viewModel.toggleHideBalances() },
                    textColorHex = globalBg.textColorHex,
                    appNameColorHex = globalBg.appNameColorHex,
                    titleColorHex = globalBg.titleColorHex
                )
            }

            // Total Net Worth Card
            item {
                val netCard = headerCards.netWorth
                val netContainerColor = if (!netCard.backgroundImageUri.isNullOrBlank()) Color.Transparent else parseHexColor(netCard.backgroundColorHex, Color(0xFF3673FC))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!isEditLocked && viewModel.hasPremium()) onEditHeaderCard("netWorth") }
                        .customCardStyle(
                            shape = RoundedCornerShape(
                                topStart = netCard.radiusTopLeft.dp,
                                topEnd = netCard.radiusTopRight.dp,
                                bottomEnd = netCard.radiusBottomRight.dp,
                                bottomStart = netCard.radiusBottomLeft.dp
                            ),
                            backgroundColor = netContainerColor,
                            useGradient = netCard.useGradient,
                            gradientColors = netCard.gradientColors.map { parseHexColor(it) },
                            gradientAngle = netCard.gradientAngle,
                            borderTop = netCard.borderTop.dp,
                            borderRight = netCard.borderRight.dp,
                            borderBottom = netCard.borderBottom.dp,
                            borderLeft = netCard.borderLeft.dp,
                            borderColor = parseHexColor(netCard.borderColorHex, Color.Gray)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (!netCard.backgroundImageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = netCard.backgroundImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = (netCard.dimOpacity / 100f).coerceIn(0f, 0.98f)))
                            )
                        }

                        Column(modifier = Modifier.padding(start = netCard.paddingLeft.dp, top = netCard.paddingTop.dp, end = netCard.paddingRight.dp, bottom = netCard.paddingBottom.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "NET WORTH",
                                        color = parseHexColor(netCard.labelColorHex, Color.White.copy(alpha = 0.8f)),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (hideBalances) currencySymbol + "••••••••" else formatRp(netWorth),
                                        color = parseHexColor(netCard.valueColorHex, Color.White),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 32.sp
                                    )

                                }

                                // Pencil icon
                                    if (!isEditLocked && viewModel.hasPremium()) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .clickable { onEditHeaderCard("netWorth") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Card",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            // Income / Expense Split Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val incCard = headerCards.income
                    val incContainerColor = if (!incCard.backgroundImageUri.isNullOrBlank()) Color.Transparent else parseHexColor(incCard.backgroundColorHex, Color(0xFFF4F7FE))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isEditLocked && viewModel.hasPremium()) onEditHeaderCard("income") }
                            .customCardStyle(
                                shape = RoundedCornerShape(
                                    topStart = incCard.radiusTopLeft.dp,
                                    topEnd = incCard.radiusTopRight.dp,
                                    bottomEnd = incCard.radiusBottomRight.dp,
                                    bottomStart = incCard.radiusBottomLeft.dp
                                ),
                                backgroundColor = incContainerColor,
                                useGradient = incCard.useGradient,
                                gradientColors = incCard.gradientColors.map { parseHexColor(it) },
                                gradientAngle = incCard.gradientAngle,
                                borderTop = incCard.borderTop.dp,
                                borderRight = incCard.borderRight.dp,
                                borderBottom = incCard.borderBottom.dp,
                                borderLeft = incCard.borderLeft.dp,
                                borderColor = parseHexColor(incCard.borderColorHex, Color.Gray)
                            )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (!incCard.backgroundImageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = incCard.backgroundImageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = (incCard.dimOpacity / 100f).coerceIn(0f, 0.98f)))
                                )
                            }

                            Column(modifier = Modifier.padding(start = incCard.paddingLeft.dp, top = incCard.paddingTop.dp, end = incCard.paddingRight.dp, bottom = incCard.paddingBottom.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Icon in circle
                                        Box(
                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF3673FC)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "INCOME",
                                            color = parseHexColor(incCard.labelColorHex, Color(0xFF9CA3AF)),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Pencil icon
                                    if (!isEditLocked && viewModel.hasPremium()) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .clickable { onEditHeaderCard("income") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Card",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (hideBalances) "+" + currencySymbol + "•••••" else "+${formatRp(totalIncome)}",
                                    color = parseHexColor(incCard.valueColorHex, Color(0xFF10B981)),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )

                            }
                        }
                    }

                    val expCard = headerCards.expense
                    val expContainerColor = if (!expCard.backgroundImageUri.isNullOrBlank()) Color.Transparent else parseHexColor(expCard.backgroundColorHex, Color(0xFFFDF9ED))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isEditLocked && viewModel.hasPremium()) onEditHeaderCard("expense") }
                            .customCardStyle(
                                shape = RoundedCornerShape(
                                    topStart = expCard.radiusTopLeft.dp,
                                    topEnd = expCard.radiusTopRight.dp,
                                    bottomEnd = expCard.radiusBottomRight.dp,
                                    bottomStart = expCard.radiusBottomLeft.dp
                                ),
                                backgroundColor = expContainerColor,
                                useGradient = expCard.useGradient,
                                gradientColors = expCard.gradientColors.map { parseHexColor(it) },
                                gradientAngle = expCard.gradientAngle,
                                borderTop = expCard.borderTop.dp,
                                borderRight = expCard.borderRight.dp,
                                borderBottom = expCard.borderBottom.dp,
                                borderLeft = expCard.borderLeft.dp,
                                borderColor = parseHexColor(expCard.borderColorHex, Color.Gray)
                            )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (!expCard.backgroundImageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = expCard.backgroundImageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = (expCard.dimOpacity / 100f).coerceIn(0f, 0.98f)))
                                )
                            }
                            Column(modifier = Modifier.padding(start = expCard.paddingLeft.dp, top = expCard.paddingTop.dp, end = expCard.paddingRight.dp, bottom = expCard.paddingBottom.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Icon in circle
                                        Box(
                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFFCBF36)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "EXPENSE",
                                            color = parseHexColor(expCard.labelColorHex, Color(0xFF9CA3AF)),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Pencil icon
                                    if (!isEditLocked && viewModel.hasPremium()) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .clickable { onEditHeaderCard("expense") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Card",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (hideBalances) "-" + currencySymbol + "•••••" else "-${formatRp(totalExpense)}",
                                    color = parseHexColor(expCard.valueColorHex, Color(0xFFFB7185)),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )

                            }
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Surface(
                        onClick = { showTransactionBottomSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.card),
                        color = if (isDarkMode) Color(0xFF1C1929) else Color.White,
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Add your first income",
                                    color = if (isDarkMode) Color.White else Color(0xFF121926),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Paycheck, transfer, or cash in — envelopes fill after that.",
                                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Goal Feature Widget
            item {
                val progressRatio = if (fundGoal.targetAmount > 0) {
                    (fundGoal.currentAmount / fundGoal.targetAmount).toFloat().coerceIn(0f, 1f)
                } else 0f
                val percentageInt = (progressRatio * 100).toInt()
                val remainingAmount = (fundGoal.targetAmount - fundGoal.currentAmount).coerceAtLeast(0.0)

                val labelColor = parseHexColor(fundGoal.labelColorHex, GoldAccent)
                val valueColor = parseHexColor(fundGoal.valueColorHex, if (isDarkMode) Color.White else Color(0xFF121926))

                val fundContainerColor = if (!fundGoal.backgroundImageUri.isNullOrBlank()) Color.Transparent else parseHexColor(fundGoal.backgroundColorHex, Color(0xFF181C26))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .customCardStyle(
                            shape = RoundedCornerShape(
                                topStart = fundGoal.radiusTopLeft.dp,
                                topEnd = fundGoal.radiusTopRight.dp,
                                bottomEnd = fundGoal.radiusBottomRight.dp,
                                bottomStart = fundGoal.radiusBottomLeft.dp
                            ),
                            backgroundColor = fundContainerColor,
                            useGradient = fundGoal.useGradient,
                            gradientColors = fundGoal.gradientColors.map { parseHexColor(it) },
                            gradientAngle = fundGoal.gradientAngle,
                            borderTop = fundGoal.borderTop.dp,
                            borderRight = fundGoal.borderRight.dp,
                            borderBottom = fundGoal.borderBottom.dp,
                            borderLeft = fundGoal.borderLeft.dp,
                            borderColor = parseHexColor(fundGoal.borderColorHex, labelColor)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (!fundGoal.backgroundImageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = fundGoal.backgroundImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = (fundGoal.dimOpacity / 100f).coerceIn(0f, 0.98f)))
                            )
                        }

                        Column(
                            modifier = Modifier.padding(start = fundGoal.paddingLeft.dp, top = fundGoal.paddingTop.dp, end = fundGoal.paddingRight.dp, bottom = fundGoal.paddingBottom.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header Row: Title & Percentage Badge & Edit Pencil
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = null, tint = labelColor, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = fundGoal.name.ifBlank { "My Goal" },
                                            color = valueColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "TARGET SAVINGS",
                                            color = labelColor.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Percentage
                                    Text(
                                        text = "$percentageInt%",
                                        color = valueColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )

                                    // Pin Widget to Android Home Screen
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(labelColor.copy(alpha = 0.25f))
                                            .clickable { GoalAppWidgetProvider.pinWidgetToHomeScreen(context) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Widgets,
                                            contentDescription = "Pin Widget to Home Screen",
                                            tint = labelColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Edit Pencil
                                    if (!isEditLocked && viewModel.hasPremium()) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .clickable { onEditFundGoal() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Goal settings",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (fundGoal.targetAmount <= 0.0) {
                                Text(
                                    text = "Set a target when you are ready. For now, record income first.",
                                    color = labelColor.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Button(
                                    onClick = { onEditFundGoal() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(fundGoal.btnBgColorHex, labelColor)),
                                    shape = RoundedCornerShape(AppShape.button)
                                ) {
                                    Text("Set a goal", color = parseHexColor(fundGoal.btnTextColorHex, Color.White), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Value Row
                                Column {
                                    Text(
                                        text = if (hideBalances) currencySymbol + "••••••" else formatRp(fundGoal.currentAmount),
                                        color = valueColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Target: ${formatRp(fundGoal.targetAmount)}",
                                            color = labelColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (remainingAmount <= 0) "Goal reached!" else "Remaining: ${if (hideBalances) currencySymbol + "•••••" else formatRp(remainingAmount)}",
                                            color = if (remainingAmount <= 0) Color(0xFF34D399) else labelColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Progress Bar
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = labelColor,
                                    trackColor = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        onClick = { showGoalDepositModal = true },
                                        shape = RoundedCornerShape(AppShape.button),
                                        color = parseHexColor(fundGoal.btnBgColorHex, labelColor)
                                    ) {
                                        Text(
                                            text = "+ Deposit",
                                            color = parseHexColor(fundGoal.btnTextColorHex, Color.White),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Envelopes Header
            item {
                val totalPercentage = envelopes.sumOf { it.percentage }
                val defaultTextColor = if (isDarkMode) Color.White else Color(0xFF121926)
                val headerColor = parseHexColor(globalBg.budgetEnvelopesColorHex, parseHexColor(globalBg.textColorHex, defaultTextColor))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Budget Envelopes",
                            color = headerColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        TextButton(
                            onClick = onAddEnvelopeClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ Envelope",
                                color = Color(0xFFFCBF36),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    // Total allocation indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val mainEnv = envelopes.find { it.id == "main" }
                        val bufferPct = mainEnv?.percentage ?: 0
                        val indicatorColor = if (bufferPct > 0) Color(0xFF34D399) else Color(0xFFFCBF36)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(indicatorColor)
                        )
                        Text(
                            text = if (bufferPct > 0) "Unassigned $bufferPct%" else "All income is assigned",
                            color = indicatorColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Envelopes List
            items(envelopes) { env ->
                val stats = viewModel.getEnvelopeStats(env.id)
                val envContainerColor = if (!env.backgroundImageUri.isNullOrBlank()) Color.Transparent else parseHexColor(env.backgroundColorHex, Color(0xFF1A1E30))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .customCardStyle(
                            shape = RoundedCornerShape(
                                topStart = env.radiusTopLeft.dp,
                                topEnd = env.radiusTopRight.dp,
                                bottomEnd = env.radiusBottomRight.dp,
                                bottomStart = env.radiusBottomLeft.dp
                            ),
                            backgroundColor = envContainerColor,
                            useGradient = env.useGradient,
                            gradientColors = env.gradientColors.map { parseHexColor(it) },
                            gradientAngle = env.gradientAngle,
                            borderTop = env.borderTop.dp,
                            borderRight = env.borderRight.dp,
                            borderBottom = env.borderBottom.dp,
                            borderLeft = env.borderLeft.dp,
                            borderColor = parseHexColor(env.borderColorHex, Color.Gray)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (!env.backgroundImageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = env.backgroundImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = (env.dimOpacity / 100f).coerceIn(0f, 0.98f)))
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = env.paddingLeft.dp, top = env.paddingTop.dp, end = env.paddingRight.dp, bottom = if (env.paddingBottom > 8) (env.paddingBottom - 8).dp else env.paddingBottom.dp)
                                    .clickable { onEditEnvelope(env) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(env.colorHex).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconVector(env.iconName),
                                    contentDescription = env.name,
                                    tint = parseHexColor(env.colorHex),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${env.name} · ${env.percentage}%",
                                    color = parseHexColor(env.labelColorHex, Color(0xFF64748B)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (hideBalances) "$currencySymbol••••••" else formatRp(stats.remaining),
                                    color = parseHexColor(env.valueColorHex, if (isDarkMode) Color.White else Color(0xFF0F172A)),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when {
                                        stats.allocated <= 0.0 -> "No money assigned yet"
                                        env.id == "savings" -> "Secured funds. Do not touch!"
                                        else -> "Left of ${if (hideBalances) "$currencySymbol•••••" else formatRp(stats.allocated)}"
                                    },
                                    color = parseHexColor(env.descriptionColorHex, Color(0xFF9CA3AF)),
                                    fontSize = 11.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (env.id != "main") {
                            // Allocation Slider
                            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                            androidx.compose.material3.Slider(
                                value = env.percentage.toFloat(),
                                onValueChange = { newValue ->
                                    viewModel.updateEnvelope(env.copy(percentage = newValue.toInt()))
                                },
                                valueRange = 0f..100f,
                                steps = 99,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = env.paddingLeft.dp, vertical = 0.dp)
                                    .padding(bottom = 8.dp),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(parseHexColor(env.colorHex), androidx.compose.foundation.shape.CircleShape)
                                    )
                                },
                                track = {
                                    val fraction = env.percentage.toFloat() / 100f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                    colors = listOf(
                                                        parseHexColor(env.colorHex).copy(alpha = 0.3f),
                                                        parseHexColor(env.colorHex).copy(alpha = 0.05f)
                                                    )
                                                ),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .background(parseHexColor(env.colorHex), androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            } // closes Box(647)
        } // closes items(envelopes)
    } // closes LazyColumn(104)

        GoalDepositModal(
            visible = showGoalDepositModal,
            goalName = fundGoal.name.ifBlank { "My Goal" },
            currentAmount = fundGoal.currentAmount,
            targetAmount = fundGoal.targetAmount,
            isDarkMode = isDarkMode,
            onDismiss = { showGoalDepositModal = false },
            onConfirmDeposit = { amount ->
                viewModel.depositToGoal(amount)
            }
        )

        val activity = LocalContext.current as? Activity
        val premiumPrice = viewModel.premiumFormattedPrice()
        PremiumModal(
            visible = showPremiumModal,
            isDarkMode = isDarkMode,
            monetizationState = monetization,
            purchasePriceLabel = premiumPrice,
            onDismiss = { showPremiumModal = false },
            onRestorePurchases = { viewModel.restorePurchases() },
            onPurchase = {
                if (activity != null) {
                    viewModel.purchaseLifetimePremium(activity)
                } else {
                    viewModel.showNotice("Unable to open Play purchase UI.")
                }
            },
            onEnableTestPremium = {
                viewModel.setTestPremiumEnabled(true)
                showPremiumModal = false
            }
        )

        if (showTransactionBottomSheet) {
            TransactionBottomSheet(
                viewModel = viewModel,
                envelopes = envelopes,
                isDarkMode = isDarkMode,
                onDismiss = { showTransactionBottomSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    viewModel: BuckViewModel,
    envelopes: List<Envelope>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val cardBg = if (isDarkMode) Color(0xFF181C26) else Color(0xFFFFFFFF)
    val innerBoxBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFF4F5F9)
    val textPrimary = if (isDarkMode) Color.White else Color(0xFF121926)
    val textSecondary = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A)
    val inputBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFE2E8F0)
    val incomeColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF0E8345)
    val expenseColor = if (isDarkMode) Color(0xFFFB7185) else Color(0xFFD9254C)
    val expenseActiveBg = if (isDarkMode) Color(0xFF3A1A2A) else Color(0xFFFDF0F2)
    val incomeActiveBg = if (isDarkMode) Color(0xFF1A3A2A) else Color(0xFFE8F8EE)

    var type by remember { mutableStateOf("expense") } // "expense" or "income"
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(envelopes.firstOrNull { it.id != "main" }?.id ?: envelopes.firstOrNull()?.id ?: "needs")
    }
    var descriptionText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = textSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Record Transaction", color = textPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
            
            // Type toggle using TabRow style
            TabRow(
                selectedTabIndex = if (type == "expense") 0 else 1,
                containerColor = innerBoxBg,
                contentColor = textPrimary,
                indicator = { },
                divider = { },
                modifier = Modifier.clip(RoundedCornerShape(AppShape.button))
            ) {
                Tab(
                    selected = type == "expense",
                    onClick = { type = "expense" },
                    modifier = Modifier
                        .padding(AppSpacing.micro)
                        .clip(RoundedCornerShape(AppShape.button))
                        .background(if (type == "expense") expenseActiveBg else Color.Transparent)
                ) {
                    Text(
                        "Expense",
                        color = if (type == "expense") expenseColor else textSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = AppSpacing.compact)
                    )
                }
                Tab(
                    selected = type == "income",
                    onClick = { type = "income" },
                    modifier = Modifier
                        .padding(AppSpacing.micro)
                        .clip(RoundedCornerShape(AppShape.button))
                        .background(if (type == "income") incomeActiveBg else Color.Transparent)
                ) {
                    Text(
                        "Income",
                        color = if (type == "income") incomeColor else textSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = AppSpacing.compact)
                    )
                }
            }

            // Amount field
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.base)) {
                Text("AMOUNT (${com.buckmanager.app.model.CurrencyConfig.currencyCode})", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("0", color = textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = appTextFieldColors(isDarkMode)
                )
            }

            // Category selection if expense
            if (type == "expense") {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.base)) {
                    Text("CATEGORY ENVELOPE", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.base)) {
                        items(envelopes) { env ->
                            val isSelected = selectedCategory == env.id
                            val envColor = parseHexColor(env.colorHex)
                            val envStats = viewModel.getEnvelopeStats(env.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = env.id },
                                label = {
                                    Column {
                                        Text(env.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (isSelected) {
                                            Text(
                                                text = "Avail: ${formatRp(envStats.remaining)}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = envColor.copy(alpha = 0.2f),
                                    selectedLabelColor = envColor,
                                    labelColor = textSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = inputBorder,
                                    selectedBorderColor = envColor,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }
            }

            // Description
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.base)) {
                Text("DESCRIPTION", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = { Text("Lunch, Coffee, Salary...", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = appTextFieldColors(isDarkMode)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && amt <= 999_999_999_999.0) {
                        val cat = if (type == "expense") selectedCategory else "income"
                        viewModel.addTransaction(type, amt, cat, descriptionText)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(AppSpacing.touchTarget),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "expense") expenseColor else incomeColor
                ),
                shape = RoundedCornerShape(AppShape.button)
            ) {
                Text(
                    "Record Transaction",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}
