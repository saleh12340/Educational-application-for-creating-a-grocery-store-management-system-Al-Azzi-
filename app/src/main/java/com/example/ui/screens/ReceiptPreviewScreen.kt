package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.CustomerEntity
import com.example.data.local.TransactionType
import com.example.data.model.LedgerRow
import com.example.data.model.formatNumericOnly
import com.example.ui.components.AlAzziTopBar
import com.example.ui.theme.CrispDivider
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardPrimary
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.TextOnWhite
import com.example.util.ThermalPrinterHelper

@Composable
fun ReceiptPreviewScreen(
    customer: CustomerEntity?,
    ledgerRows: List<LedgerRow>,
    focusedTxId: Long?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusedTx = focusedTxId?.let { id -> ledgerRows.find { it.id == id } }
    val currentBalance = ledgerRows.firstOrNull()?.cumulativeBalance ?: 0.0
    val customerName = customer?.fullName ?: "عميل بقالة العزي"
    val customerPhone = customer?.phone.orEmpty()

    Surface(
        color = PureWhite,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            AlAzziTopBar(
                title = stringResource(R.string.receipt_screen_title),
                subtitle = stringResource(R.string.store_full_name),
                onBackClick = onBack
            )

            // Top-pinned Print / Share Actions
            Surface(
                color = DarkCardPrimary,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            ThermalPrinterHelper.printThermalReceipt(
                                context = context,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                rows = ledgerRows,
                                currentBalance = currentBalance,
                                focusedTx = focusedTx
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("print_thermal_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("طباعة / PDF", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            val text = ThermalPrinterHelper.buildPlainTextReceipt(
                                customerName = customerName,
                                customerPhone = customerPhone,
                                rows = ledgerRows,
                                currentBalance = currentBalance,
                                focusedTx = focusedTx
                            )
                            ThermalPrinterHelper.shareReceiptText(context, text)
                        },
                        border = BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("share_thermal_button")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = TextOnDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("مشاركة نصية", color = TextOnDark, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Thermal Slip Preview
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, DarkCardPrimary),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.store_full_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextOnWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "إيصال حراري محلي (58mm / 80mm)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedOnWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(color = DarkCardPrimary, thickness = 1.dp)

                        Text(
                            text = "العميل: $customerName",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextOnWhite,
                            softWrap = true
                        )
                        if (customerPhone.isNotBlank()) {
                            Text(
                                text = "الهاتف: $customerPhone",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                color = TextMutedOnWhite
                            )
                        }

                        HorizontalDivider(color = CrispDivider, thickness = 1.dp)

                        val rowsToRender = if (focusedTx != null) listOf(focusedTx) else ledgerRows
                        if (rowsToRender.isEmpty()) {
                            Text(
                                text = "لا توجد عمليات مسجلة.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedOnWhite
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkCardPrimary)
                                    .padding(6.dp)
                            ) {
                                Text("البيان", color = TextOnDark, fontSize = 11.sp, modifier = Modifier.weight(2f))
                                Text("المبلغ", color = TextOnDark, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                Text("التراكمي", color = TextOnDark, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            }
                            rowsToRender.forEach { row ->
                                val sign = if (row.type == TransactionType.DEBT) "+" else "-"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(2f)) {
                                        Text(
                                            text = row.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextOnWhite
                                        )
                                        Text(
                                            text = "${row.dateLabel} ${row.timeLabel}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = TextMutedOnWhite
                                        )
                                    }
                                    Text(
                                        text = "$sign${formatNumericOnly(row.amount)}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextOnWhite,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatNumericOnly(row.cumulativeBalance),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextOnWhite,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                HorizontalDivider(color = CrispDivider, thickness = 1.dp)
                            }
                        }

                        Surface(
                            color = DarkCardPrimary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الرصيد النهائي",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextOnDark
                                )
                                Text(
                                    text = formatNumericOnly(currentBalance),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = EmeraldBright
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
