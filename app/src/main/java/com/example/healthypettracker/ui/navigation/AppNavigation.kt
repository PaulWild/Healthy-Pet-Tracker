package com.example.healthypettracker.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthypettracker.di.AppContainer
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

sealed class Screen(val route: String) {
    data object CatList : Screen("cats")
    data object AddCat : Screen("cats/add")
    data object EditCat : Screen("cats/{catId}/edit") {
        fun createRoute(catId: Long) = "cats/$catId/edit"
    }

    data object EditCatPhoto : Screen("cats/{catId}/edit/photo?uri={uri}") {
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
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Cats,
        BottomNavItem.Diary,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, bottomNavItems)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CatList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CatList.route) {
                CatListScreen(
                    container = container,
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
                    container = container,
                    catId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditCat.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                AddEditCatScreen(
                    container = container,
                    catId = catId,
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
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                val encodedUri = backStackEntry.arguments?.getString("uri") ?: return@composable
                val originalUri = Uri.decode(encodedUri).toUri()

                EditPhotoScreen(
                    originalUri = originalUri,
                    catId = catId,
                    container = container,
                    onCancel = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CatDetail.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                CatDetailScreen(
                    container = container,
                    catId = catId,
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
                    }
                )
            }

            composable(
                route = Screen.AddMedicine.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                AddEditMedicineScreen(
                    container = container,
                    catId = catId,
                    medicineId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditMedicine.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicineId =
                    backStackEntry.arguments?.getLong("medicineId") ?: return@composable
                AddEditMedicineScreen(
                    container = container,
                    catId = null,
                    medicineId = medicineId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MedicineSchedule.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicineId =
                    backStackEntry.arguments?.getLong("medicineId") ?: return@composable
                MedicineScheduleScreen(
                    container = container,
                    medicineId = medicineId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AddWeight.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                AddWeightScreen(
                    container = container,
                    catId = catId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.WeightHistory.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                WeightHistoryScreen(
                    container = container,
                    catId = catId,
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
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                AddFoodScreen(
                    container = container,
                    catId = catId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.FoodLog.route,
                arguments = listOf(navArgument("catId") { type = NavType.LongType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                FoodLogScreen(
                    container = container,
                    catId = catId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddFood = { navController.navigate(Screen.AddFood.createRoute(catId)) }
                )
            }

            composable(Screen.Diary.route) {
                DiaryScreen(
                    container = container,
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
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getLong("catId") ?: return@composable
                AddDiaryNoteScreen(
                    container = container,
                    catId = catId,
                    noteId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditDiaryNote.route,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
                AddDiaryNoteScreen(
                    container = container,
                    catId = null,
                    noteId = noteId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(container = container)
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
