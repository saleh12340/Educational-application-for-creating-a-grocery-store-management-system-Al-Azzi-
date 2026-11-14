package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlAzziDatabase
import com.example.data.model.LessonItem
import com.example.data.repository.AlAzziRepository
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

    private val _lessonSearchQuery = MutableStateFlow("")
    val lessonSearchQuery: StateFlow<String> = _lessonSearchQuery

    private val _lessonFilter = MutableStateFlow(LessonFilter.ALL)
    val lessonFilter: StateFlow<LessonFilter> = _lessonFilter

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

    fun updateLessonSearch(query: String) {
        _lessonSearchQuery.value = query
    }

    fun updateLessonFilter(filter: LessonFilter) {
        _lessonFilter.value = filter
    }

    fun toggleLessonCompleted(lessonId: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.setLessonCompleted(lessonId, completed)
        }
    }
}
