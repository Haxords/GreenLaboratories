package com.greenlaboratories.fieldops


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.db.AppDatabase
import com.example.data.repository.FieldOpsRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Parties : Screen("parties", "Parties", Icons.Default.People)
    object Inventory : Screen("inventory", "Stock", Icons.Default.Inventory2)
    object OrderEntry : Screen("order_entry", "Order", Icons.Default.ShoppingCart)
    object Collection : Screen("collection", "Collect", Icons.Default.Payments)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = FieldOpsRepository(db)
        val viewModelFactory = MainViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
                MainAppContent(viewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        Screen.Dashboard,
        Screen.Parties,
        Screen.Inventory,
        Screen.OrderEntry,
        Screen.Collection,
        Screen.Reports
    )

    LaunchedEffect(Unit) {
        viewModel.orderSuccessEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in navItems.map { it.route }) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars
                ) {
                    navItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToNewOrder = { navController.navigate(Screen.OrderEntry.route) },
                    onNavigateToCollection = { navController.navigate(Screen.Collection.route) },
                    onNavigateToAddParty = { navController.navigate(Screen.Parties.route) },
                    onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                    onNavigateToOrders = { navController.navigate(Screen.Reports.route) }
                )
            }

            composable(Screen.Parties.route) {
                PartiesScreen(
                    viewModel = viewModel,
                    onSelectPartyDetail = { partyId ->
                        navController.navigate("party_detail/$partyId")
                    },
                    onNavigateToNewOrderWithParty = { party ->
                        viewModel.selectPartyForOrder(party)
                        navController.navigate(Screen.OrderEntry.route)
                    },
                    onNavigateToCollectionWithParty = { party ->
                        navController.navigate(Screen.Collection.route)
                    }
                )
            }

            composable(
                route = "party_detail/{partyId}",
                arguments = listOf(navArgument("partyId") { type = NavType.LongType })
            ) { backStackEntry ->
                val partyId = backStackEntry.arguments?.getLong("partyId") ?: 0L
                PartyDetailScreen(
                    partyId = partyId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNewOrder = { navController.navigate(Screen.OrderEntry.route) },
                    onRecordCollection = { navController.navigate(Screen.Collection.route) }
                )
            }

            composable(Screen.Inventory.route) {
                InventoryScreen(viewModel = viewModel)
            }

            composable(Screen.OrderEntry.route) {
                val selectedParty by viewModel.selectedPartyForOrder.collectAsState()
                OrderEntryScreen(
                    viewModel = viewModel,
                    preselectedParty = selectedParty,
                    onOrderCompleted = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Collection.route) {
                CollectionScreen(
                    viewModel = viewModel,
                    preselectedParty = null
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
        }
    }
}
