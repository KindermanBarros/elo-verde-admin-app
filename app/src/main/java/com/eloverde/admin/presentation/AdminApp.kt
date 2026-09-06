package com.eloverde.admin.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.auth.FirebaseAuth

private enum class Destination(val label: String, val icon: ImageVector) {
    RESERVAS("Reservas", Icons.Default.List),
    CALENDARIO("Calendário", Icons.Default.DateRange),
    GRAFICOS("Gráficos", Icons.Default.BarChart),
    MAIS("Mais", Icons.Default.MoreHoriz)
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
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) }
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
