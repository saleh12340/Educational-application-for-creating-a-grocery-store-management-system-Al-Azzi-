package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlAzziDatabase
import com.example.data.local.CustomerEntity
import com.example.data.local.TransactionType
import com.example.data.model.CustomerSummary
import com.example.data.model.LedgerRow
import com.example.data.model.LessonItem
import com.example.data.model.PosDashboardTotals
import com.example.data.repository.AlAzziRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LessonFilter {
    ALL,
    INCOMPLETE,
    COMPLETED
}

class AlAzziViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlAzziRepository(
        AlAzziDatabase.getInstance(application).posDao()
    )

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery

    private val _lessonSearchQuery = MutableStateFlow("")
    val lessonSearchQuery: StateFlow<String> = _lessonSearchQuery

    private val _lessonFilter = MutableStateFlow(LessonFilter.ALL)
    val lessonFilter: StateFlow<LessonFilter> = _lessonFilter

    val dashboardTotals: StateFlow<PosDashboardTotals> = repository.observeDashboardTotals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PosDashboardTotals()
        )

    val filteredCustomers: StateFlow<List<CustomerSummary>> = combine(
        repository.observeCustomerSummaries(),
        _customerSearchQuery
    ) { customers, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            customers
        } else {
            customers.filter {
                it.fullName.contains(trimmed, ignoreCase = true) ||
                    it.phone.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLessons: StateFlow<List<LessonItem>> = repository.observeLessonsWithProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredLessons: StateFlow<List<LessonItem>> = combine(
        allLessons,
        _lessonSearchQuery,
        _lessonFilter
    ) { lessons, query, filter ->
        val trimmed = query.trim()
        lessons.filter { lesson ->
            val matchesQuery = trimmed.isEmpty() ||
                lesson.title.contains(trimmed, ignoreCase = true) ||
                lesson.subtitle.contains(trimmed, ignoreCase = true) ||
                lesson.topics.any { it.contains(trimmed, ignoreCase = true) }
            val matchesFilter = when (filter) {
                LessonFilter.ALL -> true
                LessonFilter.INCOMPLETE -> !lesson.isCompleted
                LessonFilter.COMPLETED -> lesson.isCompleted
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateCustomerSearch(query: String) {
        _customerSearchQuery.value = query
    }

    fun updateLessonSearch(query: String) {
        _lessonSearchQuery.value = query
    }

    fun updateLessonFilter(filter: LessonFilter) {
        _lessonFilter.value = filter
    }

    fun observeCustomer(customerId: Long): Flow<CustomerEntity?> =
        repository.observeCustomerById(customerId)

    fun observeLedger(customerId: Long): Flow<List<LedgerRow>> =
        repository.observeCustomerLedgerRows(customerId)

    fun addCustomer(fullName: String, phone: String, onCreated: (Long) -> Unit = {}) {
        if (fullName.isBlank()) return
        viewModelScope.launch {
            val id = repository.addCustomer(fullName, phone)
            onCreated(id)
        }
    }

    fun updateCustomer(customer: CustomerEntity, newName: String, newPhone: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateCustomer(
                customer.copy(
                    fullName = newName.trim(),
                    phone = newPhone.trim()
                )
            )
        }
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
        }
    }

    fun addTransaction(
        customerId: Long,
        type: TransactionType,
        amount: Double,
        description: String
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addTransaction(customerId, type, amount, description)
        }
    }

    fun updateTransaction(
        txId: Long,
        type: TransactionType,
        amount: Double,
        description: String
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.updateTransaction(txId, type, amount, description)
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
        }
    }

    fun toggleLessonCompleted(lessonId: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.setLessonCompleted(lessonId, completed)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleStoreData()
        }
    }

    suspend fun generateBackupJson(): String = repository.buildJsonBackup()
}
