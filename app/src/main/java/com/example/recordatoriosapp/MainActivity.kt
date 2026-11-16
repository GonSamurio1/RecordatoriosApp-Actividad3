package com.example.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recordatoriosapp.ui.theme.RecordatoriosAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val appContainer = remember { AppContainer(context) }
            val preferencias by appContainer.preferenciasRepository.preferenciasFlow
                .collectAsStateWithLifecycle(initialValue = PreferenciasApp())

            RecordatoriosAppTheme(darkTheme = preferencias.temaOscuro) {
                AppNavigation(appContainer = appContainer)
            }
        }
    }
}

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    val factory = remember(appContainer) { AppViewModelFactory(appContainer) }

    NavHost(
        navController = navController,
        startDestination = "listado"
    ) {
        composable(route = "listado") {
            val vm: ListadoViewModel = viewModel(factory = factory)
            ListadoScreen(
                viewModel = vm,
                onRecordatorioClick = { id -> navController.navigate(route = "detalle/$id") },
                onCrearRecordatorio = { navController.navigate(route = "creacion") }
            )
        }

        composable(route = "creacion") {
            val vm: CreacionViewModel = viewModel(factory = factory)
            CreacionScreen(
                viewModel = vm,
                onGuardarExito = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() }
            )
        }

        composable(
            route = "detalle/{id}",
            arguments = listOf(navArgument(name = "id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val vm: DetalleViewModel = viewModel(factory = factory)
            DetalleScreen(
                id = id,
                viewModel = vm,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}