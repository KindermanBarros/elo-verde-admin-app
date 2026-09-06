package com.eloverde.admin.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.auth.FirebaseAuth

private enum class Destination(val label: String, val icon: ImageVector) {
    RESERVAS("Reservas", Icons.Default.EventNote),
    CALENDARIO("Calendário", Icons.Default.CalendarMonth),
    GRAFICOS("Métricas", Icons.Default.Insights),
    MAIS("Perfil", Icons.Default.Person)
}

@Composable
fun AdminApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user == null) {
        LoginScreen { user = auth.currentUser }
        return
    }

    var destination by remember { mutableStateOf(Destination.RESERVAS) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (destination) {
            Destination.RESERVAS -> ReservationsScreen(padding)
            Destination.CALENDARIO -> CalendarScreen(padding)
            Destination.GRAFICOS -> ChartsScreen(padding)
            Destination.MAIS -> MoreScreen(padding)
        }
    }
}
