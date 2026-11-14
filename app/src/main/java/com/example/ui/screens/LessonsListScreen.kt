package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LessonItem
import com.example.ui.components.AlAzziTopBar
import com.example.ui.components.CrispWhiteTextField
import com.example.ui.components.NumericCounterBox
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardPrimary
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyInfo
import com.example.ui.theme.TextMutedOnDark
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextOnDark
import com.example.ui.viewmodel.LessonFilter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun LessonsListScreen(
    lessons: List<LessonItem>,
    completedCount: Int,
    totalCount: Int,
    searchQuery: String,
    activeFilter: LessonFilter,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (LessonFilter) -> Unit,
    onToggleComplete: (Int, Boolean) -> Unit,
    onOpenLesson: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current

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
                title = stringResource(R.string.app_name),
                subtitle = "10 دروس فيديو بالصوت والضغطات • بدون نت",
                actions = {
                    NumericCounterBox(text = "$completedCount/$totalCount")
                }
            )

            // Top-pinned Search & Filter Card (Always visible above keyboard)
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
                    CrispWhiteTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = stringResource(R.string.hint_search_lesson),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMutedOnWhite
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lesson_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPillButton(
                            label = "الكل ($totalCount)",
                            selected = activeFilter == LessonFilter.ALL,
                            onClick = { onFilterChange(LessonFilter.ALL) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPillButton(
                            label = "قيد المشاهدة",
                            selected = activeFilter == LessonFilter.INCOMPLETE,
                            onClick = { onFilterChange(LessonFilter.INCOMPLETE) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPillButton(
                            label = "مكتمل ($completedCount)",
                            selected = activeFilter == LessonFilter.COMPLETED,
                            onClick = { onFilterChange(LessonFilter.COMPLETED) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lessons, key = { it.id }) { lesson ->
                    Surface(
                        color = DarkCardPrimary,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (lesson.isCompleted) EmeraldPrimary else DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onOpenLesson(lesson.id) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleComplete(lesson.id, !lesson.isCompleted)
                                }
                            )
                            .testTag("lesson_card_${lesson.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldPrimary, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%02d", lesson.id),
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PureWhite
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(DarkCardElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = lesson.durationLabel,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 12.sp,
                                            color = SkyInfo
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(DarkCardElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                tint = EmeraldBright,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "صوت عربي",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMutedOnDark
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onToggleComplete(lesson.id, !lesson.isCompleted) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (lesson.isCompleted) {
                                                Icons.Default.CheckCircle
                                            } else {
                                                Icons.Default.RadioButtonUnchecked
                                            },
                                            contentDescription = "تحديد كمكتمل",
                                            tint = if (lesson.isCompleted) EmeraldBright else TextMutedOnDark
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(DarkCardElevated, RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "تشغيل الدرس",
                                            tint = EmeraldBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDark
                                ),
                                softWrap = true
                            )
                            Text(
                                text = lesson.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedOnDark,
                                softWrap = true
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                lesson.topics.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .background(DarkCardElevated, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkyInfo
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPillButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) EmeraldPrimary else DarkCardElevated,
            contentColor = if (selected) PureWhite else TextMutedOnDark
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
