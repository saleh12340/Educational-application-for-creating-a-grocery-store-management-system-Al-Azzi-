package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: Int,
    val isCompleted: Boolean = false,
    val lastPositionMs: Long = 0L
)
