package com.example.data.model

data class LessonItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val durationLabel: String,
    val rawResId: Int,
    val stepBoundariesMs: List<Long>,
    val topics: List<String>,
    val clickSteps: List<String>,
    val guideBoxTitle: String,
    val readyGuideText: String,
    val isCompleted: Boolean = false
)
