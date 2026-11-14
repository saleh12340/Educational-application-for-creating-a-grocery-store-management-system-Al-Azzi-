package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.model.LessonItem
import com.example.ui.components.AlAzziTopBar
import com.example.ui.theme.AmberDebt
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardPrimary
import com.example.ui.theme.DarkPlayerSurface
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyInfo
import com.example.ui.theme.TextMutedOnDark
import com.example.ui.theme.TextOnDark
import kotlinx.coroutines.delay
import java.util.Locale

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun LessonPlayerScreen(
    lesson: LessonItem,
    totalLessons: Int,
    onBack: () -> Unit,
    onSelectLesson: (Int) -> Unit,
    onToggleComplete: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showCaptions by remember { mutableStateOf(true) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(30_000L) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubValueMs by remember { mutableFloatStateOf(0f) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0L) durationMs = d
                    playerError = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playerError = "تعذر تشغيل ملف الفيديو المحلي."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(lesson.id) {
        playerError = null
        currentPositionMs = 0L
        durationMs = 30_000L
        val rawUri = RawResourceDataSource.buildRawResourceUri(lesson.rawResId)
        exoPlayer.setMediaItem(MediaItem.fromUri(rawUri))
        exoPlayer.volume = if (isMuted) 0f else 1f
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    LaunchedEffect(isPlaying, isUserScrubbing) {
        while (true) {
            if (!isUserScrubbing) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val d = exoPlayer.duration
                if (d > 0L) durationMs = d
            }
            delay(200L)
        }
    }

    val activeStepIndex = remember(currentPositionMs, lesson.stepBoundariesMs) {
        val boundaries = lesson.stepBoundariesMs
        if (boundaries.isEmpty()) {
            0
        } else {
            boundaries.indexOfLast { currentPositionMs >= it }.coerceAtLeast(0)
        }
    }

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
                title = "الدرس ${lesson.id} من $totalLessons",
                subtitle = "${lesson.durationLabel} • صوت عربي بدون نت",
                onBackClick = onBack,
                actions = {
                    Button(
                        onClick = { onToggleComplete(lesson.id, !lesson.isCompleted) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lesson.isCompleted) EmeraldPrimary else DarkCardElevated,
                            contentColor = TextOnDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_lesson_complete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (lesson.isCompleted) PureWhite else EmeraldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (lesson.isCompleted) "مكتمل" else "إتمام",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dark Opaque Voiced Video Player Container
                Surface(
                    color = DarkPlayerSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(DarkPlayerSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = false
                                        keepScreenOn = true
                                    }
                                },
                                update = { view ->
                                    if (view.player != exoPlayer) {
                                        view.player = exoPlayer
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Synchronized on-video coaching caption: follows the current narration step.
                            Surface(
                                color = DarkCardPrimary.copy(alpha = 0.96f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, EmeraldBright),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = "موضع الإجراء",
                                        tint = EmeraldBright,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "الخطوة ${activeStepIndex + 1} من ${lesson.clickSteps.size} • اتبع الضغطات",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = lesson.clickSteps.getOrNull(activeStepIndex)
                                                ?: "تابع الشرح المرئي خطوة بخطوة.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnDark,
                                            softWrap = true
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = DarkCardPrimary,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, EmeraldPrimary),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OfflinePin,
                                        contentDescription = null,
                                        tint = EmeraldBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "فيديو صوتي بدون نت",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOnDark
                                    )
                                }
                            }

                            if (showCaptions && playerError == null) {
                                val captionText = lesson.clickSteps.getOrElse(activeStepIndex) { "" }
                                if (captionText.isNotEmpty()) {
                                    Surface(
                                        color = DarkCardPrimary,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, DarkCardBorder),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .testTag("video_arabic_subtitle")
                                    ) {
                                        Text(
                                            text = captionText,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 18.sp
                                            ),
                                            color = PureWhite,
                                            textAlign = TextAlign.Center,
                                            softWrap = true,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            playerError?.let { errMsg ->
                                Surface(
                                    color = DarkCardPrimary,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, AmberDebt),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = errMsg,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnDark
                                        )
                                        Button(
                                            onClick = {
                                                playerError = null
                                                val rawUri = RawResourceDataSource.buildRawResourceUri(lesson.rawResId)
                                                exoPlayer.setMediaItem(MediaItem.fromUri(rawUri))
                                                exoPlayer.prepare()
                                                exoPlayer.play()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("إعادة التشغيل")
                                        }
                                    }
                                }
                            }
                        }

                        // Interactive Controls: Timeline Slider, Mute, Speed, Rewind, Play/Pause, Fast-Forward
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCardPrimary)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val totalFloat = durationMs.coerceAtLeast(1000L).toFloat()
                            val sliderPos = if (isUserScrubbing) {
                                scrubValueMs
                            } else {
                                currentPositionMs.toFloat().coerceIn(0f, totalFloat)
                            }

                            Slider(
                                value = sliderPos,
                                onValueChange = { newVal ->
                                    isUserScrubbing = true
                                    scrubValueMs = newVal
                                },
                                onValueChangeFinished = {
                                    exoPlayer.seekTo(scrubValueMs.toLong())
                                    currentPositionMs = scrubValueMs.toLong()
                                    isUserScrubbing = false
                                },
                                valueRange = 0f..totalFloat,
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(EmeraldBright, CircleShape)
                                    )
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldBright,
                                    activeTrackColor = EmeraldBright,
                                    inactiveTrackColor = DarkCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .testTag("video_timeline_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${formatDurationMs(currentPositionMs)} / ${formatDurationMs(durationMs)}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 12.sp,
                                    color = TextMutedOnDark
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { showCaptions = !showCaptions },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .background(DarkCardElevated, RoundedCornerShape(8.dp))
                                            .testTag("video_captions_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ClosedCaption,
                                            contentDescription = stringResource(R.string.cd_captions),
                                            tint = if (showCaptions) EmeraldBright else TextMutedOnDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            isMuted = !isMuted
                                            exoPlayer.volume = if (isMuted) 0f else 1f
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .background(DarkCardElevated, RoundedCornerShape(8.dp))
                                            .testTag("video_mute_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = stringResource(R.string.cd_mute),
                                            tint = if (isMuted) AmberDebt else EmeraldBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val nextSpeed = when (playbackSpeed) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 2.0f
                                                else -> 1.0f
                                            }
                                            playbackSpeed = nextSpeed
                                            exoPlayer.setPlaybackSpeed(nextSpeed)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DarkCardElevated,
                                            contentColor = SkyInfo
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(
                                            text = "${playbackSpeed}x",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val target = (exoPlayer.currentPosition - 5_000L).coerceAtLeast(0L)
                                        exoPlayer.seekTo(target)
                                        currentPositionMs = target
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .background(DarkCardElevated, CircleShape)
                                        .testTag("video_rewind_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastRewind,
                                        contentDescription = stringResource(R.string.cd_rewind),
                                        tint = TextOnDark
                                    )
                                }

                                Spacer(Modifier.width(20.dp))

                                IconButton(
                                    onClick = {
                                        if (exoPlayer.playbackState == Player.STATE_ENDED) {
                                            exoPlayer.seekTo(0L)
                                            exoPlayer.play()
                                        } else if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.play()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(EmeraldPrimary, CircleShape)
                                        .testTag("video_play_pause_button")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.cd_play_pause),
                                        tint = PureWhite,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(Modifier.width(20.dp))

                                IconButton(
                                    onClick = {
                                        val maxPos = exoPlayer.duration.coerceAtLeast(durationMs)
                                        val target = (exoPlayer.currentPosition + 5_000L).coerceAtMost(maxPos)
                                        exoPlayer.seekTo(target)
                                        currentPositionMs = target
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .background(DarkCardElevated, CircleShape)
                                        .testTag("video_forward_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastForward,
                                        contentDescription = stringResource(R.string.cd_forward),
                                        tint = TextOnDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Prev / Next Lesson Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (lesson.id > 1) onSelectLesson(lesson.id - 1) },
                        enabled = lesson.id > 1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardPrimary,
                            contentColor = TextOnDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("prev_lesson_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                        Text("الدرس السابق")
                    }

                    Button(
                        onClick = { if (lesson.id < totalLessons) onSelectLesson(lesson.id + 1) },
                        enabled = lesson.id < totalLessons,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardPrimary,
                            contentColor = TextOnDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("next_lesson_button")
                    ) {
                        Text("الدرس التالي")
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                    }
                }

                // Website Click-by-Click Arabic Guide (Synced with Spoken Audio!)
                Surface(
                    color = DarkCardPrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = EmeraldBright
                            )
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDark
                                ),
                                softWrap = true
                            )
                        }
                        Text(
                            text = lesson.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedOnDark,
                            softWrap = true
                        )

                        lesson.clickSteps.forEachIndexed { idx, step ->
                            val isCurrentStep = idx == activeStepIndex
                            val stepStartMs = lesson.stepBoundariesMs.getOrElse(idx) { 0L }
                            Surface(
                                onClick = {
                                    exoPlayer.seekTo(stepStartMs)
                                    currentPositionMs = stepStartMs
                                },
                                color = if (isCurrentStep) DarkCardElevated else DarkCardPrimary,
                                shape = RoundedCornerShape(10.dp),
                                border = if (isCurrentStep) BorderStroke(1.dp, EmeraldBright) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lesson_step_card_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isCurrentStep) EmeraldBright else EmeraldPrimary,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isCurrentStep) DarkCardPrimary else PureWhite
                                        )
                                    }
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextOnDark,
                                        softWrap = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Ready-to-Copy Website Click Path / Arabic Prompt Card (NO CODE!)
                Surface(
                    color = DarkCardPrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lesson.guideBoxTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SkyInfo
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    copyPlainText(context, lesson.guideBoxTitle, lesson.readyGuideText)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.msg_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = PureWhite
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("copy_guide_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.cd_copy_text),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("نسخ", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkPlayerSurface, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = lesson.readyGuideText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldBright,
                                softWrap = true
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun copyPlainText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
