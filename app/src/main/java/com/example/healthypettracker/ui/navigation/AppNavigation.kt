package com.example.healthypettracker.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthypettracker.ui.screens.cats.AddEditCatScreen
import com.example.healthypettracker.ui.screens.cats.CatDetailScreen
import com.example.healthypettracker.ui.screens.cats.CatListScreen
import com.example.healthypettracker.ui.screens.cats.EditPhotoScreen
import com.example.healthypettracker.ui.screens.diary.AddDiaryNoteScreen
import com.example.healthypettracker.ui.screens.diary.DiaryScreen
import com.example.healthypettracker.ui.screens.food.AddFoodScreen
import com.example.healthypettracker.ui.screens.food.FoodLogScreen
import com.example.healthypettracker.ui.screens.medicine.AddEditMedicineScreen
import com.example.healthypettracker.ui.screens.medicine.MedicineScheduleScreen
import com.example.healthypettracker.ui.screens.settings.SettingsScreen
import com.example.healthypettracker.ui.screens.weight.AddWeightScreen
import com.example.healthypettracker.ui.screens.weight.WeightHistoryScreen


sealed interface BottomBarConfig {
    data object None : BottomBarConfig
    data object Default : BottomBarConfig

    data class SaveCancel(
        val onSave: () -> Unit = {},
        val onCancel: () -> Unit = {},
        val saveEnabled: Boolean = true,
        val saveText: String = "Save",
        val cancelText: String = "Cancel"
    ) : BottomBarConfig
}

class BottomBarState {
    var config: BottomBarConfig by mutableStateOf(BottomBarConfig.Default)
        private set

    fun updateConfig(newConfig: BottomBarConfig) {
        config = newConfig
    }

    fun clear() {
        config = BottomBarConfig.Default
    }
}


sealed class Screen(val route: String) {


    data object CatList : Screen("cats")
    data object AddCat : Screen("cats/add")
    data object EditCat : Screen("cats/{catId}/edit") {
        fun createRoute(catId: Long) = "cats/$catId/edit"
    }

    data object EditCatPhoto :
        Screen("cats/{catId}/edit/photo?uri={uri}") {
        fun createRoute(catId: Long, uri: String) =
            "cats/$catId/edit/photo?uri=${Uri.encode(uri)}"
    }

    data object CatDetail : Screen("cats/{catId}") {
        fun createRoute(catId: Long) = "cats/$catId"
    }

    data object AddMedicine : Screen("cats/{catId}/medicine/add") {
        fun createRoute(catId: Long) = "cats/$catId/medicine/add"
    }

    data object EditMedicine : Screen("medicine/{medicineId}/edit") {
        fun createRoute(medicineId: Long) = "medicine/$medicineId/edit"
    }

    data object MedicineSchedule : Screen("medicine/{medicineId}/schedule") {
        fun createRoute(medicineId: Long) = "medicine/$medicineId/schedule"
    }

    data object AddWeight : Screen("cats/{catId}/weight/add") {
        fun createRoute(catId: Long) = "cats/$catId/weight/add"
    }

    data object WeightHistory : Screen("cats/{catId}/weight") {
        fun createRoute(catId: Long) = "cats/$catId/weight"
    }

    data object AddFood : Screen("cats/{catId}/food/add") {
        fun createRoute(catId: Long) = "cats/$catId/food/add"
    }

    data object FoodLog : Screen("cats/{catId}/food") {
        fun createRoute(catId: Long) = "cats/$catId/food"
    }

    data object Diary : Screen("diary")
    data object AddDiaryNote : Screen("cats/{catId}/diary/add") {
        fun createRoute(catId: Long) = "cats/$catId/diary/add"
    }

    data object EditDiaryNote : Screen("diary/{noteId}/edit") {
        fun createRoute(noteId: Long) = "diary/$noteId/edit"
    }

    data object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Cats : BottomNavItem(
        route = Screen.CatList.route,
        title = "Cats",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Diary : BottomNavItem(
        route = Screen.Diary.route,
        title = "Diary",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )

    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

@Composable
fun AppBottomBar(
    config: BottomBarConfig,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    when (config) {
        is BottomBarConfig.None -> { /* No bottom bar */
        }

        is BottomBarConfig.SaveCancel -> SaveCancelBottomBar(config, navController, modifier)
        is BottomBarConfig.Default -> {
            val bottomNavItems = listOf(
                BottomNavItem.Cats,
                BottomNavItem.Diary,
                BottomNavItem.Settings
            )
            BottomNavigationBar(navController, bottomNavItems)
        }

    }
}


@Composable
fun SaveCancelBottomBar(
    config: BottomBarConfig.SaveCancel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    config.onCancel()
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(config.cancelText)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    config.onSave()
                    navController.popBackStack()
                },
                enabled = config.saveEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(config.saveText)
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomBarState = remember { BottomBarState() }

    Scaffold(
        bottomBar = {
            AppBottomBar(config = bottomBarState.config, navController)
        }

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Diary.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CatList.route) {
                CatListScreen(
                    onNavigateToAddCat = { navController.navigate(Screen.AddCat.route) },
                    onNavigateToCatDetail = { catId ->
                        navController.navigate(Screen.CatDetail.createRoute(catId))
                    },
                    onNavigateToEditPhoto = { catId, uri ->
                        navController.navigate(
                            Screen.EditCatPhoto.createRoute(
                                catId,
                                uri.toString()
                            )
                        )
                    }
                )
            }

            composable(Screen.AddCat.route) {
                AddEditCatScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditCat.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) {
                AddEditCatScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditCatPhoto.route,
                arguments = listOf(
                    navArgument("catId") { type = NavType.LongType },
                    navArgument("uri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                backStackEntry.arguments?.getLong("catId") ?: return@composable
                val encodedUri = backStackEntry.arguments?.getString("uri") ?: return@composable
                val originalUri = Uri.decode(encodedUri).toUri()

                EditPhotoScreen(
                    originalUri = originalUri,
                    bottomBarState = bottomBarState
                )
            }

            composable(
                route = Screen.CatDetail.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                CatDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditCat = { navController.navigate(Screen.EditCat.createRoute(catId)) },
                    onNavigateToAddMedicine = {
                        navController.navigate(
                            Screen.AddMedicine.createRoute(
                                catId
                            )
                        )
                    },
                    onNavigateToEditMedicine = { medicineId ->
                        navController.navigate(Screen.EditMedicine.createRoute(medicineId))
                    },
                    onNavigateToMedicineSchedule = { medicineId ->
                        navController.navigate(Screen.MedicineSchedule.createRoute(medicineId))
                    },
                    onNavigateToAddWeight = {
                        navController.navigate(
                            Screen.AddWeight.createRoute(
                                catId
                            )
                        )
                    },
                    onNavigateToWeightHistory = {
                        navController.navigate(
                            Screen.WeightHistory.createRoute(
                                catId
                            )
                        )
                    },
                    onNavigateToAddFood = { navController.navigate(Screen.AddFood.createRoute(catId)) },
                    onNavigateToFoodLog = { navController.navigate(Screen.FoodLog.createRoute(catId)) },
                    onNavigateToAddDiaryNote = {
                        navController.navigate(
                            Screen.AddDiaryNote.createRoute(
                                catId
                            )
                        )
                    },
                    onNavigateToEditPhoto = { catId, uri ->
                        navController.navigate(
                            Screen.EditCatPhoto.createRoute(
                                catId,
                                uri.toString()
                            )
                        )
                    }
                )
            }

            composable(
                route = Screen.AddMedicine.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) {
                AddEditMedicineScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditMedicine.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) {
                AddEditMedicineScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MedicineSchedule.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) {
                MedicineScheduleScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AddWeight.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) {
                AddWeightScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.WeightHistory.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                WeightHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddWeight = {
                        navController.navigate(
                            Screen.AddWeight.createRoute(
                                catId
                            )
                        )
                    }
                )
            }

            composable(
                route = Screen.AddFood.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) {
                AddFoodScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.FoodLog.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                FoodLogScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddFood = { navController.navigate(Screen.AddFood.createRoute(catId)) }
                )
            }

            composable(Screen.Diary.route) {
                DiaryScreen(
                    onNavigateToAddDiaryNote = { catId ->
                        navController.navigate(Screen.AddDiaryNote.createRoute(catId))
                    },
                    onNavigateToEditDiaryNote = { noteId ->
                        navController.navigate(Screen.EditDiaryNote.createRoute(noteId))
                    }
                )
            }

            composable(
                route = Screen.AddDiaryNote.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) {
                AddDiaryNoteScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditDiaryNote.route,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) {
                AddDiaryNoteScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
