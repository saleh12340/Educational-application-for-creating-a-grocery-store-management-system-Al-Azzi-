package com.example.data.repository

import com.example.data.lessons.LessonsCatalog
import com.example.data.local.CustomerEntity
import com.example.data.local.LessonProgressEntity
import com.example.data.local.PosDao
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.model.CustomerSummary
import com.example.data.model.LedgerRow
import com.example.data.model.LessonItem
import com.example.data.model.PosDashboardTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlAzziRepository(private val dao: PosDao) {

    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    fun observeCustomerSummaries(): Flow<List<CustomerSummary>> {
        return combine(
            dao.observeAllCustomers(),
            dao.observeAllTransactions()
        ) { customers, transactions ->
            val txByCustomer = transactions.groupBy { it.customerId }
            customers.map { customer ->
                val list = txByCustomer[customer.id].orEmpty()
                val debt = list.filter { it.type == TransactionType.DEBT }.sumOf { it.amount }
                val paid = list.filter { it.type == TransactionType.PAYMENT }.sumOf { it.amount }
                val lastActivity = list.maxOfOrNull { it.timestamp } ?: customer.createdAt
                CustomerSummary(
                    id = customer.id,
                    fullName = customer.fullName,
                    phone = customer.phone,
                    notes = customer.notes,
                    totalDebt = debt,
                    totalPaid = paid,
                    currentBalance = debt - paid,
                    transactionCount = list.size,
                    lastActivityAt = lastActivity
                )
            }
        }
    }

    fun observeDashboardTotals(): Flow<PosDashboardTotals> {
        return observeCustomerSummaries().map { summaries ->
            val totalDebt = summaries.sumOf { it.totalDebt }
            val totalPaid = summaries.sumOf { it.totalPaid }
            PosDashboardTotals(
                totalDebt = totalDebt,
                totalPaid = totalPaid,
                netOutstanding = totalDebt - totalPaid,
                customerCount = summaries.size
            )
        }
    }

    fun observeCustomerById(customerId: Long): Flow<CustomerEntity?> =
        dao.observeCustomerById(customerId)

    fun observeCustomerLedgerRows(customerId: Long): Flow<List<LedgerRow>> {
        return dao.observeTransactionsForCustomer(customerId).map { ascendingList ->
            var running = 0.0
            val computed = ascendingList.map { tx ->
                running = when (tx.type) {
                    TransactionType.DEBT -> running + tx.amount
                    TransactionType.PAYMENT -> running - tx.amount
                }
                val dateObj = Date(tx.timestamp)
                LedgerRow(
                    id = tx.id,
                    customerId = tx.customerId,
                    type = tx.type,
                    amount = tx.amount,
                    description = tx.description,
                    timestamp = tx.timestamp,
                    dateLabel = dateFormatter.format(dateObj),
                    timeLabel = timeFormatter.format(dateObj),
                    cumulativeBalance = running
                )
            }
            computed.reversed()
        }
    }

    fun observeLessonsWithProgress(): Flow<List<LessonItem>> {
        return dao.observeAllLessonProgress().map { progressList ->
            val map = progressList.associateBy { it.lessonId }
            LessonsCatalog.lessons.map { lesson ->
                lesson.copy(isCompleted = map[lesson.id]?.isCompleted == true)
            }
        }
    }

    suspend fun addCustomer(fullName: String, phone: String, notes: String = ""): Long {
        return dao.insertCustomer(
            CustomerEntity(
                fullName = fullName.trim(),
                phone = phone.trim(),
                notes = notes.trim()
            )
        )
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        dao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customerId: Long) {
        dao.getCustomerById(customerId)?.let { dao.deleteCustomer(it) }
    }

    suspend fun addTransaction(
        customerId: Long,
        type: TransactionType,
        amount: Double,
        description: String
    ): Long {
        val cleanDesc = description.trim().ifEmpty {
            if (type == TransactionType.DEBT) "مواد غذائية (آجل)" else "دفعة سداد نقدي"
        }
        return dao.insertTransaction(
            TransactionEntity(
                customerId = customerId,
                type = type,
                amount = amount,
                description = cleanDesc
            )
        )
    }

    suspend fun updateTransaction(
        txId: Long,
        type: TransactionType,
        amount: Double,
        description: String
    ) {
        val existing = dao.getTransactionById(txId) ?: return
        dao.updateTransaction(
            existing.copy(
                type = type,
                amount = amount,
                description = description.trim().ifEmpty { existing.description }
            )
        )
    }

    suspend fun deleteTransaction(txId: Long) {
        dao.getTransactionById(txId)?.let { dao.deleteTransaction(it) }
    }

    suspend fun setLessonCompleted(lessonId: Int, completed: Boolean) {
        dao.upsertLessonProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                isCompleted = completed
            )
        )
    }

    suspend fun seedSampleStoreData() {
        val existing = dao.getAllCustomersOnce()
        if (existing.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 24 * hour

        val c1 = dao.insertCustomer(
            CustomerEntity(
                fullName = "عبدالله محمد صالح العزي اليافعي",
                phone = "777123456",
                createdAt = now - 5 * day
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                customerId = c1,
                type = TransactionType.DEBT,
                amount = 18500.0,
                description = "كيس أرز بسمتي + زيت طبخ + سكر",
                timestamp = now - 4 * day
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                customerId = c1,
                type = TransactionType.DEBT,
                amount = 4200.0,
                description = "ألبان وأجبان ومعلبات إفطار",
                timestamp = now - 2 * day
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                customerId = c1,
                type = TransactionType.PAYMENT,
                amount = 10000.0,
                description = "دفعة نقدية على الحساب",
                timestamp = now - 1 * day
            )
        )

        val c2 = dao.insertCustomer(
            CustomerEntity(
                fullName = "سعيد أحمد ناصر الحميري",
                phone = "733987654",
                createdAt = now - 3 * day
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                customerId = c2,
                type = TransactionType.DEBT,
                amount = 7500.0,
                description = "مستلزمات بقالة أسبوعية",
                timestamp = now - 2 * day
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                customerId = c2,
                type = TransactionType.PAYMENT,
                amount = 7500.0,
                description = "سداد كامل الفاتورة نقداً",
                timestamp = now - 6 * hour
            )
        )

        val c3 = dao.insertCustomer(
            CustomerEntity(
                fullName = "أمين علي قاسم السالمي",
                phone = "711456789",
                createdAt = now - 2 * day
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                customerId = c3,
                type = TransactionType.DEBT,
                amount = 9600.0,
                description = "دقيق + شاي عدني + حليب مجفف",
                timestamp = now - 10 * hour
            )
        )
    }

    suspend fun buildJsonBackup(): String {
        val customers = dao.getAllCustomersOnce()
        val transactions = dao.getAllTransactionsOnce()
        val root = JSONObject()
        root.put("store", "بقالة العزي للمواد الغذائية")
        root.put("exportedAt", dateFormatter.format(Date()))

        val customersArray = JSONArray()
        customers.forEach { c ->
            customersArray.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("fullName", c.fullName)
                    put("phone", c.phone)
                    put("notes", c.notes)
                    put("createdAt", c.createdAt)
                }
            )
        }
        root.put("customers", customersArray)

        val txArray = JSONArray()
        transactions.forEach { t ->
            txArray.put(
                JSONObject().apply {
                    put("id", t.id)
                    put("customerId", t.customerId)
                    put("type", t.type.name)
                    put("amount", t.amount)
                    put("description", t.description)
                    put("timestamp", t.timestamp)
                }
            )
        }
        root.put("transactions", txArray)
        return root.toString(2)
    }
}
