package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.CustomerEntity
import com.example.data.local.TransactionType
import com.example.data.model.LedgerRow
import com.example.data.model.formatNumericOnly
import com.example.ui.components.AlAzziTopBar
import com.example.ui.components.CrispWhiteTextField
import com.example.ui.components.NumericBalanceBox
import com.example.ui.theme.AmberDebt
import com.example.ui.theme.CrispDivider
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardPrimary
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TableAltRowWhite
import com.example.ui.theme.TextMutedOnDark
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.TextOnWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerLedgerScreen(
    customer: CustomerEntity?,
    ledgerRows: List<LedgerRow>,
    onBack: () -> Unit,
    onAddTransaction: (TransactionType, Double, String) -> Unit,
    onUpdateTransaction: (Long, TransactionType, Double, String) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onOpenStatementReceipt: () -> Unit,
    onOpenTxReceipt: (Long) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val currentBalance = ledgerRows.firstOrNull()?.cumulativeBalance ?: 0.0

    var selectedType by rememberSaveable { mutableStateOf(TransactionType.DEBT) }
    var amountInput by rememberSaveable { mutableStateOf("") }
    var descInput by rememberSaveable { mutableStateOf("") }

    var longPressedRow by remember { mutableStateOf<LedgerRow?>(null) }
    var editingRow by remember { mutableStateOf<LedgerRow?>(null) }

    Surface(
        color = PureWhite,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Bar with Top Back Arrow + Full Name + Numeric Balance Box
            AlAzziTopBar(
                title = customer?.fullName ?: "كشف الحساب",
                subtitle = customer?.phone?.takeIf { it.isNotBlank() },
                onBackClick = onBack,
                actions = {
                    NumericBalanceBox(balance = currentBalance)
                    IconButton(
                        onClick = onOpenStatementReceipt,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("print_statement_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = stringResource(R.string.action_print_statement),
                            tint = TextOnDark
                        )
                    }
                }
            )

            // TOP-PINNED TRANSACTION INPUT CARD (Stays above soft keyboard!)
            Surface(
                color = DarkCardPrimary,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedType = TransactionType.DEBT },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == TransactionType.DEBT) AmberDebt else DarkCardElevated,
                                contentColor = if (selectedType == TransactionType.DEBT) DarkCardPrimary else TextOnDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("type_debt_button")
                        ) {
                            Text(
                                text = stringResource(R.string.type_debt),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Button(
                            onClick = { selectedType = TransactionType.PAYMENT },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == TransactionType.PAYMENT) EmeraldBright else DarkCardElevated,
                                contentColor = if (selectedType == TransactionType.PAYMENT) DarkCardPrimary else TextOnDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("type_payment_button")
                        ) {
                            Text(
                                text = stringResource(R.string.type_payment),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CrispWhiteTextField(
                            value = amountInput,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '.' }
                                amountInput = filtered
                            },
                            placeholder = stringResource(R.string.hint_amount),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tx_amount_input")
                        )
                        CrispWhiteTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            placeholder = stringResource(R.string.hint_tx_description),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("tx_desc_input")
                        )
                    }

                    Button(
                        onClick = {
                            val parsed = amountInput.toDoubleOrNull() ?: 0.0
                            if (parsed > 0.0) {
                                onAddTransaction(selectedType, parsed, descInput)
                                amountInput = ""
                                descInput = ""
                            }
                        },
                        enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0.0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("save_transaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_save_tx),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Long-press Hint Banner
            Text(
                text = stringResource(R.string.hint_long_press_tx),
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedOnWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // FULL-WIDTH LEDGER TABLE HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardPrimary)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.col_operation),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnDark,
                    modifier = Modifier.weight(2.1f)
                )
                Text(
                    text = stringResource(R.string.col_date),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = stringResource(R.string.col_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = stringResource(R.string.col_amount),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = stringResource(R.string.col_cumulative),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
            }

            if (ledgerRows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = DarkCardPrimary,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = "لا توجد عمليات مسجلة لهذا العميل حتى الآن. سجّل أول عملية من النموذج العلوي.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(ledgerRows, key = { _, row -> row.id }) { index, row ->
                        val bg = if (index % 2 == 0) PureWhite else TableAltRowWhite
                        val amountColor = if (row.type == TransactionType.DEBT) {
                            CrimsonDanger
                        } else {
                            EmeraldPrimary
                        }
                        val typeBadgeText = if (row.type == TransactionType.DEBT) "آجل" else "سداد"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        longPressedRow = row
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .testTag("ledger_row_${row.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2.1f).padding(end = 4.dp)) {
                                Text(
                                    text = row.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextOnWhite
                                    ),
                                    softWrap = true
                                )
                                Text(
                                    text = typeBadgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = amountColor
                                )
                            }

                            Text(
                                text = row.dateLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedOnWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.3f)
                            )

                            Text(
                                text = row.timeLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedOnWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.9f)
                            )

                            Text(
                                text = formatNumericOnly(row.amount),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = amountColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.1f)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .background(DarkCardPrimary, RoundedCornerShape(6.dp))
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formatNumericOnly(row.cumulativeBalance),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (row.cumulativeBalance > 0.0) AmberDebt else EmeraldBright,
                                    maxLines = 1
                                )
                            }
                        }
                        HorizontalDivider(color = CrispDivider, thickness = 1.dp)
                    }
                }
            }
        }
    }

    // Long-press Transaction Action Dialog
    longPressedRow?.let { row ->
        AlertDialog(
            onDismissRequest = { longPressedRow = null },
            containerColor = DarkCardPrimary,
            titleContentColor = TextOnDark,
            title = {
                Text(
                    text = "خيارات العملية (${formatNumericOnly(row.amount)})",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${row.description} • ${row.dateLabel} ${row.timeLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedOnDark
                    )
                    Button(
                        onClick = {
                            longPressedRow = null
                            editingRow = row
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_action_edit")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("تعديل العملية", color = TextOnDark)
                    }
                    Button(
                        onClick = {
                            longPressedRow = null
                            onOpenTxReceipt(row.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_action_print")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("طباعة فاتورة حرارية", color = PureWhite)
                    }
                    Button(
                        onClick = {
                            onDeleteTransaction(row.id)
                            longPressedRow = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_action_delete")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("حذف العملية", color = PureWhite)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { longPressedRow = null }) {
                    Text(stringResource(R.string.btn_cancel), color = TextMutedOnDark)
                }
            }
        )
    }

    // Edit Transaction Dialog
    editingRow?.let { target ->
        var editType by remember(target.id) { mutableStateOf(target.type) }
        var editAmount by remember(target.id) { mutableStateOf(formatNumericOnly(target.amount)) }
        var editDesc by remember(target.id) { mutableStateOf(target.description) }

        AlertDialog(
            onDismissRequest = { editingRow = null },
            containerColor = DarkCardPrimary,
            titleContentColor = TextOnDark,
            title = { Text("تعديل العملية", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { editType = TransactionType.DEBT },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editType == TransactionType.DEBT) AmberDebt else DarkCardElevated,
                                contentColor = if (editType == TransactionType.DEBT) DarkCardPrimary else TextOnDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("آجل") }
                        Button(
                            onClick = { editType = TransactionType.PAYMENT },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editType == TransactionType.PAYMENT) EmeraldBright else DarkCardElevated,
                                contentColor = if (editType == TransactionType.PAYMENT) DarkCardPrimary else TextOnDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("سداد") }
                    }
                    CrispWhiteTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        placeholder = stringResource(R.string.hint_amount),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    CrispWhiteTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        placeholder = stringResource(R.string.hint_tx_description),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = editAmount.toDoubleOrNull() ?: 0.0
                        if (parsed > 0.0) {
                            onUpdateTransaction(target.id, editType, parsed, editDesc)
                            editingRow = null
                        }
                    },
                    enabled = (editAmount.toDoubleOrNull() ?: 0.0) > 0.0,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("حفظ", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRow = null }) {
                    Text(stringResource(R.string.btn_cancel), color = TextMutedOnDark)
                }
            }
        )
    }
}
