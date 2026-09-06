package com.eloverde.admin.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eloverde.admin.MainActivity
import com.eloverde.admin.data.ReservationRepository
import com.eloverde.admin.domain.Reservation
import com.eloverde.admin.domain.ReservationStatus
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LoginScreen(onAuthenticated: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Elo Verde · Administração", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Senha") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            onClick = {
                loading = true
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { loading = false; onAuthenticated() }
                    .addOnFailureListener { loading = false; error = "E-mail ou senha inválidos." }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (loading) "Entrando…" else "Entrar") }
    }
}

@Composable
fun ReservationsScreen(padding: PaddingValues) {
    val repository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(repository) {
        val listener = repository.observe({ reservations = it; error = null }, { error = "Não foi possível carregar as reservas." })
        onDispose { listener.remove() }
    }

    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
        Text("Reservas", style = MaterialTheme.typography.headlineMedium)
        Text("${reservations.size} intenção(ões) cadastrada(s)", style = MaterialTheme.typography.bodyMedium)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
        if (reservations.isEmpty() && error == null) {
            Text("Nenhuma reserva encontrada.", modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
                items(reservations, key = { it.id }) { reservation ->
                    ReservationCard(reservation, repository)
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(reservation: Reservation, repository: ReservationRepository) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    val status = ReservationStatus.entries.firstOrNull { it.name.equals(reservation.status, true) }
        ?: ReservationStatus.entries.firstOrNull { it.label.equals(reservation.status, true) }
        ?: ReservationStatus.PENDING

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(reservation.name.ifBlank { "Cliente sem nome" }, style = MaterialTheme.typography.titleMedium)
            Text(reservation.date.ifBlank { "Data não informada" }, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                val phone = reservation.phone.filter(Char::isDigit)
                if (phone.isNotBlank()) {
                    val message = Uri.encode("Olá, ${reservation.name}! Aqui é da Chácara Elo Verde. Recebemos sua intenção de reserva para ${reservation.date} e gostaríamos de confirmar alguns detalhes.")
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$message")))
                }
            }, enabled = reservation.phone.isNotBlank()) { Text(reservation.phone.ifBlank { "Telefone não informado" }) }
            Box {
                OutlinedButton(onClick = { menuOpen = true }) { Text(status.label) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    ReservationStatus.entries.forEach { option ->
                        DropdownMenuItem(text = { Text(option.label) }, onClick = {
                            menuOpen = false
                            repository.updateStatus(reservation.id, option.name)
                        })
                    }
                }
            }
            if (reservation.email.isNotBlank()) Text(reservation.email, style = MaterialTheme.typography.bodySmall)
            if (reservation.notes.isNotBlank()) Text(reservation.notes, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CalendarScreen(padding: PaddingValues) {
    var monthOffset by remember { mutableIntStateOf(0) }
    val month = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val title = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")))
        .replaceFirstChar { it.titlecase(Locale("pt", "BR")) }

    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Calendário", style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { monthOffset-- }) { Text("‹ Anterior") }
            Text(title, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { monthOffset++ }) { Text("Próximo ›") }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { Text(it, style = MaterialTheme.typography.labelMedium) }
        }
        val firstDay = month.atDay(1).dayOfWeek.value % 7
        val cells = List(firstDay) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                week.forEach { day -> Text(day?.dayOfMonth?.toString().orEmpty(), modifier = Modifier.padding(12.dp)) }
                repeat(7 - week.size) { Spacer(Modifier.width(38.dp)) }
            }
        }
        Text("Use as setas para paginar os meses. Os dias são apenas informativos.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ChartsScreen(padding: PaddingValues) {
    val repository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    DisposableEffect(repository) {
        val listener = repository.observe({ reservations = it }, {})
        onDispose { listener.remove() }
    }
    val counts = ReservationStatus.entries.associateWith { status ->
        reservations.count { it.status.equals(status.name, true) || it.status.equals(status.label, true) }
    }
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Gráficos", style = MaterialTheme.typography.headlineMedium)
        Text("Resumo das intenções de reserva", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        ReservationStatus.entries.forEach { status ->
            ListItem(headlineContent = { Text(status.label) }, trailingContent = { Text(counts[status].toString(), style = MaterialTheme.typography.titleLarge) })
            HorizontalDivider()
        }
        Spacer(Modifier.height(12.dp))
        Text("Total: ${reservations.size}", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun MoreScreen(padding: PaddingValues) {
    val context = LocalContext.current
    Column(Modifier.padding(padding).padding(20.dp)) {
        Text("Mais", style = MaterialTheme.typography.headlineMedium)
        Text(FirebaseAuth.getInstance().currentUser?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            context.startActivity(Intent(context, MainActivity::class.java))
        }) { Text("Sair") }
    }
}
