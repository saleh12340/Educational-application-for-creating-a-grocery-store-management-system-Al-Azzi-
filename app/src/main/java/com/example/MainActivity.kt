package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.lessons.LessonsCatalog
import com.example.ui.screens.CustomerLedgerScreen
import com.example.ui.screens.GitHubWorkflowScreen
import com.example.ui.screens.LessonPlayerScreen
import com.example.ui.screens.LessonsListScreen
import com.example.ui.screens.PosHomeScreen
import com.example.ui.screens.ReceiptPreviewScreen
import com.example.ui.theme.AlAzziPosTheme
import com.example.ui.viewmodel.AlAzziViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AppRoutes {
    const val ROUTE_POS_HOME = "pos_home"
    const val ROUTE_CUSTOMER_LEDGER = "customer_ledger/{customerId}"
    const val ROUTE_RECEIPT_PREVIEW = "receipt_preview/{customerId}?txId={txId}"
    const val ROUTE_LESSONS_LIST = "lessons_list"
    const val ROUTE_LESSON_PLAYER = "lesson_player/{lessonId}"
    const val ROUTE_GITHUB_WORKFLOW = "github_workflow"

    fun customerLedger(customerId: Long) = "customer_ledger/$customerId"
    fun receiptPreview(customerId: Long, txId: Long? = null): String =
        if (txId != null) "receipt_preview/$customerId?txId=$txId" else "receipt_preview/$customerId"

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Zero-permission Storage Access Framework backup exporter
    val backupExporterLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.generateBackupJson()
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        }
                        true
                    }.getOrDefault(false)
                }
                val msg = if (ok) "تم تصدير النسخة الاحتياطية بنجاح" else "تعذر حفظ النسخة الاحتياطية"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.ROUTE_POS_HOME
    ) {
        composable(AppRoutes.ROUTE_POS_HOME) {
            val customers by viewModel.filteredCustomers.collectAsStateWithLifecycle()
            val totals by viewModel.dashboardTotals.collectAsStateWithLifecycle()
            val searchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()

            PosHomeScreen(
                customers = customers,
                totals = totals,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateCustomerSearch,
                onAddCustomer = { name, phone -> viewModel.addCustomer(name, phone) },
                onEditCustomer = viewModel::updateCustomer,
                onDeleteCustomer = viewModel::deleteCustomer,
                onOpenCustomerLedger = { customerId ->
                    navController.navigate(AppRoutes.customerLedger(customerId))
                },
                onOpenCustomerReceipt = { customerId ->
                    navController.navigate(AppRoutes.receiptPreview(customerId))
                },
                onOpenLessons = {
                    navController.navigate(AppRoutes.ROUTE_LESSONS_LIST)
                },
                onExportBackup = {
                    backupExporterLauncher.launch("al_azzi_pos_backup.json")
                },
                onSeedSampleData = viewModel::seedSampleData
            )
        }

        composable(
            route = AppRoutes.ROUTE_CUSTOMER_LEDGER,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            val customer by viewModel.observeCustomer(customerId).collectAsStateWithLifecycle(initialValue = null)
            val ledgerRows by viewModel.observeLedger(customerId).collectAsStateWithLifecycle(initialValue = emptyList())

            CustomerLedgerScreen(
                customer = customer,
                ledgerRows = ledgerRows,
                onBack = { navController.popBackStack() },
                onAddTransaction = { type, amount, desc ->
                    viewModel.addTransaction(customerId, type, amount, desc)
                },
                onUpdateTransaction = viewModel::updateTransaction,
                onDeleteTransaction = viewModel::deleteTransaction,
                onOpenStatementReceipt = {
                    navController.navigate(AppRoutes.receiptPreview(customerId))
                },
                onOpenTxReceipt = { txId ->
                    navController.navigate(AppRoutes.receiptPreview(customerId, txId))
                }
            )
        }

        composable(
            route = AppRoutes.ROUTE_RECEIPT_PREVIEW,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("txId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            val txIdArg = backStackEntry.arguments?.getLong("txId") ?: -1L
            val customer by viewModel.observeCustomer(customerId).collectAsStateWithLifecycle(initialValue = null)
            val ledgerRows by viewModel.observeLedger(customerId).collectAsStateWithLifecycle(initialValue = emptyList())

            ReceiptPreviewScreen(
                customer = customer,
                ledgerRows = ledgerRows,
                focusedTxId = txIdArg.takeIf { it > 0L },
                onBack = { navController.popBackStack() }
            )
        }

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
                },
                onOpenGitHubWorkflow = {
                    navController.navigate(AppRoutes.ROUTE_GITHUB_WORKFLOW)
                },
                onBack = { navController.popBackStack() }
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

        composable(AppRoutes.ROUTE_GITHUB_WORKFLOW) {
            GitHubWorkflowScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
