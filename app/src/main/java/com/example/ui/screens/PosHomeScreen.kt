package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CustomerEntity
import com.example.data.model.CustomerSummary
import com.example.data.model.PosDashboardTotals
import com.example.data.model.formatNumericOnly
import com.example.ui.components.AlAzziTopBar
import com.example.ui.components.CrispWhiteTextField
import com.example.ui.components.NumericBalanceBox
import com.example.ui.components.SummaryMetricCard
import com.example.ui.theme.AmberDebt
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardPrimary
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyInfo
import com.example.ui.theme.TextMutedOnDark
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.TextOnWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PosHomeScreen(
    customers: List<CustomerSummary>,
    totals: PosDashboardTotals,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddCustomer: (String, String) -> Unit,
    onEditCustomer: (CustomerEntity, String, String) -> Unit,
    onDeleteCustomer: (Long) -> Unit,
    onOpenCustomerLedger: (Long) -> Unit,
    onOpenCustomerReceipt: (Long) -> Unit,
    onOpenLessons: () -> Unit,
    onExportBackup: () -> Unit,
    onSeedSampleData: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showAddForm by rememberSaveable { mutableStateOf(false) }
    var newCustomerName by rememberSaveable { mutableStateOf("") }
    var newCustomerPhone by rememberSaveable { mutableStateOf("") }
    var longPressedCustomer by remember { mutableStateOf<CustomerSummary?>(null) }
    var editingCustomer by remember { mutableStateOf<CustomerSummary?>(null) }

    Surface(
        color = PureWhite,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Bar — No Bottom Home Bar!
            AlAzziTopBar(
                title = stringResource(R.string.store_full_name),
                subtitle = stringResource(R.string.app_subtitle),
                actions = {
                    IconButton(
                        onClick = onExportBackup,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("export_backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = stringResource(R.string.action_export_backup),
                            tint = TextOnDark
                        )
                    }
                    Button(
                        onClick = onOpenLessons,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("nav_lessons_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.nav_lessons_count),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )

            // TOP-PINNED INPUTS SECTION (Never covered by soft keyboard!)
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CrispWhiteTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = stringResource(R.string.hint_search_customer),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMutedOnWhite
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("customer_search_input")
                        )

                        Button(
                            onClick = { showAddForm = !showAddForm },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showAddForm) CrimsonDanger else EmeraldPrimary,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_add_customer_button")
                        ) {
                            Icon(
                                imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = stringResource(R.string.btn_add_customer),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAddForm) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            CrispWhiteTextField(
                                value = newCustomerName,
                                onValueChange = { newCustomerName = it },
                                placeholder = stringResource(R.string.hint_customer_full_name),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_customer_name_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CrispWhiteTextField(
                                    value = newCustomerPhone,
                                    onValueChange = { newCustomerPhone = it },
                                    placeholder = stringResource(R.string.hint_customer_phone),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_customer_phone_input")
                                )
                                Button(
                                    onClick = {
                                        if (newCustomerName.isNotBlank()) {
                                            onAddCustomer(newCustomerName, newCustomerPhone)
                                            newCustomerName = ""
                                            newCustomerPhone = ""
                                            showAddForm = false
                                        }
                                    },
                                    enabled = newCustomerName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldBright,
                                        contentColor = DarkCardPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("save_customer_button")
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_save_customer),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Numeric Strip (Dark Opaque Cards, Numeric Values Only)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricCard(
                    label = stringResource(R.string.label_total_debt),
                    numericValue = formatNumericOnly(totals.totalDebt),
                    accentColor = AmberDebt,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    label = stringResource(R.string.label_total_paid),
                    numericValue = formatNumericOnly(totals.totalPaid),
                    accentColor = EmeraldBright,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    label = stringResource(R.string.label_customers_count),
                    numericValue = totals.customerCount.toString(),
                    accentColor = SkyInfo,
                    modifier = Modifier.weight(0.8f)
                )
            }

            // Customer Cards List or Clean Empty State
            if (customers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = DarkCardPrimary,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = EmeraldBright,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = stringResource(R.string.empty_customers_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextOnDark
                            )
                            Text(
                                text = stringResource(R.string.empty_customers_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedOnDark
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Button(
                                    onClick = { showAddForm = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text(text = stringResource(R.string.btn_add_customer))
                                }
                                OutlinedButton(
                                    onClick = onSeedSampleData,
                                    border = BorderStroke(1.dp, DarkCardBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("seed_sample_data_button")
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_load_sample_data),
                                        color = TextOnDark
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        Surface(
                            color = DarkCardPrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onOpenCustomerLedger(customer.id) },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        longPressedCustomer = customer
                                    }
                                )
                                .testTag("customer_card_${customer.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Full Customer Name (NEVER truncated!)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = customer.fullName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextOnDark
                                        ),
                                        softWrap = true
                                    )
                                    val meta = buildString {
                                        if (customer.phone.isNotBlank()) {
                                            append(customer.phone)
                                            append("  •  ")
                                        }
                                        append("${customer.transactionCount} عملية")
                                    }
                                    Text(
                                        text = meta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMutedOnDark
                                    )
                                }

                                // Numeric-Only Balance Box
                                NumericBalanceBox(balance = customer.currentBalance)
                            }
                        }
                    }
                }
            }
        }
    }

    // Long-press Customer Action Dialog
    longPressedCustomer?.let { target ->
        AlertDialog(
            onDismissRequest = { longPressedCustomer = null },
            containerColor = DarkCardPrimary,
            titleContentColor = TextOnDark,
            textContentColor = TextMutedOnDark,
            title = {
                Text(
                    text = target.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    softWrap = true
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            longPressedCustomer = null
                            editingCustomer = target
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("تعديل بيانات العميل", color = TextOnDark)
                    }
                    Button(
                        onClick = {
                            longPressedCustomer = null
                            onOpenCustomerReceipt(target.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("معاينة وطباعة كشف الحساب", color = PureWhite)
                    }
                    Button(
                        onClick = {
                            onDeleteCustomer(target.id)
                            longPressedCustomer = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("حذف العميل وسجلاته", color = PureWhite)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { longPressedCustomer = null }) {
                    Text(stringResource(R.string.btn_cancel), color = TextMutedOnDark)
                }
            }
        )
    }

    // Edit Customer Dialog
    editingCustomer?.let { target ->
        var editName by remember(target.id) { mutableStateOf(target.fullName) }
        var editPhone by remember(target.id) { mutableStateOf(target.phone) }
        AlertDialog(
            onDismissRequest = { editingCustomer = null },
            containerColor = DarkCardPrimary,
            titleContentColor = TextOnDark,
            title = { Text("تعديل العميل", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CrispWhiteTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        placeholder = stringResource(R.string.hint_customer_full_name),
                        modifier = Modifier.fillMaxWidth()
                    )
                    CrispWhiteTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        placeholder = stringResource(R.string.hint_customer_phone),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditCustomer(
                            CustomerEntity(
                                id = target.id,
                                fullName = target.fullName,
                                phone = target.phone,
                                notes = target.notes
                            ),
                            editName,
                            editPhone
                        )
                        editingCustomer = null
                    },
                    enabled = editName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("حفظ التعديل", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCustomer = null }) {
                    Text(stringResource(R.string.btn_cancel), color = TextMutedOnDark)
                }
            }
        )
    }
}
