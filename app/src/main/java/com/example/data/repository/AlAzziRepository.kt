package com.example.data.repository

import com.example.data.lessons.LessonsCatalog
import com.example.data.local.LessonProgressEntity
import com.example.data.local.PosDao
import com.example.data.model.LessonItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlAzziRepository(private val dao: PosDao) {

    fun observeLessonsWithProgress(): Flow<List<LessonItem>> {
        return dao.observeAllLessonProgress().map { progressList ->
            val map = progressList.associateBy { it.lessonId }
            LessonsCatalog.lessons.map { lesson ->
                lesson.copy(isCompleted = map[lesson.id]?.isCompleted == true)
            }
        }
    }

    suspend fun setLessonCompleted(lessonId: Int, completed: Boolean) {
        dao.upsertLessonProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                isCompleted = completed
            )
        )
    }
}
