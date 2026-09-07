package com.buckmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppSpacing
import com.buckmanager.app.ui.AppStroke
import com.buckmanager.app.ui.GoldAccent
import com.buckmanager.app.ui.components.UniversalHeader
import com.buckmanager.app.ui.components.parseHexColor
import com.buckmanager.app.viewmodel.BuckViewModel
import com.buckmanager.app.model.formatRp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionScreen(
    viewModel: BuckViewModel,
    onOpenSettings: () -> Unit,
    onEditFundGoal: () -> Unit
) {
    val globalBg by viewModel.globalBackground.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isEditLocked by viewModel.isEditLocked.collectAsState()
    val envelopes by viewModel.envelopes.collectAsState()

    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val cardBg = if (isDarkMode) Color(0xFF181C26) else Color(0xFFFFFFFF)
    val innerBoxBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFF4F5F9)
    val textPrimary = if (isDarkMode) Color.White else Color(0xFF121926)
    val globalTextColor = parseHexColor(globalBg.textColorHex, textPrimary)
    val textSecondary = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A)
    val inputBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFE2E8F0)
    val incomeColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF0E8345)
    val expenseColor = if (isDarkMode) Color(0xFFFB7185) else Color(0xFFD9254C)
    var transactionToDelete by remember { mutableStateOf<Long?>(null) }
    var showTransactionBottomSheet by remember { mutableStateOf(false) }

    // Filter state: "all", "income", "expense"
    var filterType by remember { mutableStateOf("all") }

    // Monthly summary calculation
    val now = remember { Calendar.getInstance() }
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    val monthlyTransactions = remember(transactions, currentMonth, currentYear) {
        transactions.filter { tx ->
            try {
                val txDate = dateFormat.parse(tx.date)
                val cal = Calendar.getInstance().apply { time = txDate!! }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) { false }
        }
    }
    val monthlyIncome = monthlyTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val monthlyExpense = monthlyTransactions.filter {
        it.type == "expense" && it.category != "goal" && !(it.category == "main" && it.description.contains("Goal"))
    }.sumOf { it.amount }
    val monthlyNet = monthlyIncome - monthlyExpense

    // Filtered transactions
    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            "income" -> transactions.filter { it.type == "income" }
            "expense" -> transactions.filter { it.type == "expense" }
            else -> transactions
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(parseHexColor(globalBg.backgroundColorHex, if (isDarkMode) Color(0xFF0F1117) else Color(0xFFF5F6FA)))
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
                    title = "Transactions",
                    hasPremium = viewModel.hasPremium(),
                    isEditLocked = isEditLocked,
                    isDarkMode = isDarkMode,
                    showUnlockCustomization = false,
                    showHideBalances = false,
                    onToggleLock = { viewModel.toggleEditLock() },
                    textColorHex = globalBg.textColorHex
                )
            }

            // Monthly Summary Card
            item {
                val monthName = remember(currentMonth) {
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now.time)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppShape.card))
                        .border(AppStroke.thin, inputBorder, RoundedCornerShape(AppShape.card))
                        .background(cardBg)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            monthName,
                            color = textPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Income", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "+${formatRp(monthlyIncome)}",
                                    color = incomeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Expense", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "-${formatRp(monthlyExpense)}",
                                    color = expenseColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${if (monthlyNet >= 0) "+" else ""}${formatRp(monthlyNet)}",
                                    color = if (monthlyNet >= 0) incomeColor else expenseColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("all" to "All", "income" to "Income", "expense" to "Expense").forEach { (key, label) ->
                        val isSelected = filterType == key
                        val chipColor = when (key) {
                            "income" -> incomeColor
                            "expense" -> expenseColor
                            else -> if (isDarkMode) Color.White else Color(0xFF121926)
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterType = key },
                            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.15f),
                                selectedLabelColor = chipColor,
                                labelColor = textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = inputBorder,
                                selectedBorderColor = chipColor,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transaction History",
                        color = globalTextColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        "${filteredTransactions.size} items",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // History List
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppShape.card))
                            .background(cardBg)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (filterType == "all") "No transactions recorded yet." else "No ${filterType} transactions found.",
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(filteredTransactions) { tx ->
                    val env = envelopes.find { it.id == tx.category }
                    val catColor = if (tx.type == "income") incomeColor else parseHexColor(env?.colorHex ?: "#FB7185")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.card),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.type == "income") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    tx.description,
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (tx.type == "income") "Income" else (env?.name ?: "Expense"),
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (tx.type == "income") "+" else "-"}${formatRp(tx.amount)}",
                                    color = if (tx.type == "income") incomeColor else textPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                IconButton(
                                    onClick = { transactionToDelete = tx.id },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB for adding transactions
        FloatingActionButton(
            onClick = { showTransactionBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp),
            containerColor = Color(0xFFFCBF36),
            contentColor = Color.White,
            shape = RoundedCornerShape(AppShape.button)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this transaction?", color = textSecondary) },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transactionToDelete!!)
                    transactionToDelete = null
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    // Transaction Bottom Sheet (reusing the one from DashboardScreen)
    if (showTransactionBottomSheet) {
        TransactionBottomSheet(
            viewModel = viewModel,
            envelopes = envelopes,
            isDarkMode = isDarkMode,
            onDismiss = { showTransactionBottomSheet = false }
        )
    }
}
