package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.lessons.LessonsCatalog
import com.example.ui.screens.LessonPlayerScreen
import com.example.ui.screens.LessonsListScreen
import com.example.ui.theme.AlAzziPosTheme
import com.example.ui.viewmodel.AlAzziViewModel

object AppRoutes {
    const val ROUTE_LESSONS_LIST = "lessons_list"
    const val ROUTE_LESSON_PLAYER = "lesson_player/{lessonId}"

    fun lessonPlayer(lessonId: Int) = "lesson_player/$lessonId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlAzziPosTheme {
                AlAzziAppRoot()
            }
        }
    }
}

@Composable
fun AlAzziAppRoot(
    viewModel: AlAzziViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.ROUTE_LESSONS_LIST
    ) {
        composable(AppRoutes.ROUTE_LESSONS_LIST) {
            val allLessons by viewModel.allLessons.collectAsStateWithLifecycle()
            val filteredLessons by viewModel.filteredLessons.collectAsStateWithLifecycle()
            val searchQuery by viewModel.lessonSearchQuery.collectAsStateWithLifecycle()
            val activeFilter by viewModel.lessonFilter.collectAsStateWithLifecycle()
            val completedCount = allLessons.count { it.isCompleted }

            LessonsListScreen(
                lessons = filteredLessons,
                completedCount = completedCount,
                totalCount = LessonsCatalog.lessons.size,
                searchQuery = searchQuery,
                activeFilter = activeFilter,
                onSearchQueryChange = viewModel::updateLessonSearch,
                onFilterChange = viewModel::updateLessonFilter,
                onToggleComplete = viewModel::toggleLessonCompleted,
                onOpenLesson = { lessonId ->
                    navController.navigate(AppRoutes.lessonPlayer(lessonId))
                }
            )
        }

        composable(
            route = AppRoutes.ROUTE_LESSON_PLAYER,
            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 1
            val allLessons by viewModel.allLessons.collectAsStateWithLifecycle()
            val lesson = allLessons.find { it.id == lessonId }
                ?: LessonsCatalog.lessons.find { it.id == lessonId }
                ?: LessonsCatalog.lessons.first()

            LessonPlayerScreen(
                lesson = lesson,
                totalLessons = LessonsCatalog.lessons.size,
                onBack = { navController.popBackStack() },
                onSelectLesson = { nextId ->
                    navController.navigate(AppRoutes.lessonPlayer(nextId)) {
                        popUpTo(AppRoutes.ROUTE_LESSON_PLAYER) { inclusive = true }
                    }
                },
                onToggleComplete = viewModel::toggleLessonCompleted
            )
        }
    }
}
