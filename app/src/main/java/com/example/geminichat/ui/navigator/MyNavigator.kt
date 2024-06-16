package com.example.geminichat.ui.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geminichat.ui.data.MainScreenViewModel
import com.example.geminichat.ui.screen.MainScreen
import com.example.geminichat.ui.screen.SelectTextScreen

@Composable
fun MyNavigator(
    navController: NavHostController = rememberNavController(),
    startDestination: String = RouteName.MAIN_SCREEN
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val textToSelect by mainScreenViewModel.textToSelect.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(RouteName.MAIN_SCREEN) {
            MainScreen(
                mainScreenViewModel = mainScreenViewModel,
                onNavigateToSelectText = {
                    navController.navigate(RouteName.SELECT_TEXT_SCREEN)
                }
            )
        }

        composable(RouteName.SELECT_TEXT_SCREEN) {
            SelectTextScreen(textToSelect)
        }
    }
}