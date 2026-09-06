package com.eloverde.admin.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eloverde.admin.data.GoogleAuthRepository
import com.eloverde.admin.data.ReservationRepository
import com.eloverde.admin.domain.Reservation
import com.eloverde.admin.domain.ReservationStatus
import com.eloverde.admin.presentation.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onAuthenticated: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { GoogleAuthRepository() }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(ForestDark, Forest, Color(0xFF23815C)))
        ).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = WarmSurface)) {
       Column(Modifier.padding(horizontal = 28.dp, vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(88.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Park, null, Modifier.size(48.dp), tint = Forest)
        }
        Spacer(Modifier.height(24.dp))
        Text("Elo Verde", style = MaterialTheme.typography.headlineLarge, color = ForestDark)
        Text("Administração da chácara", color = StoneMuted)
        Spacer(Modifier.height(32.dp))
        Button(
            enabled = !loading,
            onClick = {
                loading = true
                error = null
                scope.launch {
                    repository.signIn(context)
                        .onSuccess { onAuthenticated() }
                        .onFailure { failure ->
                            val detail = failure.message
                                ?.replace("\n", " ")
                                ?.take(180)
                                ?.takeIf(String::isNotBlank)
                            error = buildString {
                                append("Não foi possível entrar com Google")
                                append(" (")
                                append(failure.javaClass.simpleName)
                                append(").")
                                if (detail != null) {
                                    append(" ")
                                    append(detail)
                                }
                            }
                        }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            else Icon(Icons.Default.AccountCircle, null)
            Spacer(Modifier.width(10.dp))
            Text(if (loading) "Entrando…" else "Continuar com Google")
        }
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text("Acesso exclusivo da equipe", color = StoneMuted, style = MaterialTheme.typography.bodyMedium)
       }
      }
    }
}

@Composable
internal fun ScreenHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp)) {
        Text("CHÁCARA ELO VERDE", style = MaterialTheme.typography.labelLarge, color = Forest)
        Spacer(Modifier.height(5.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, color = StoneMuted)
    }
}

@Composable
fun ReservationsScreen(padding: PaddingValues) {
    val repository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(repository) {
        val listener = repository.observe(
            onResult = {
                reservations = it
                loading = false
                error = null
            },
            onError = { failure ->
                loading = false
                val detail = failure.message
                    ?.replace("\n", " ")
                    ?.take(180)
                    ?.takeIf(String::isNotBlank)
                error = buildString {
                    append("Não foi possível carregar as reservas")
                    append(" (")
                    append(failure.javaClass.simpleName)
                    append(").")
                    if (detail != null) {
                        append(" ")
                        append(detail)
                    }
                }
            }
        )
        onDispose { listener.remove() }
    }

    if (showCreateDialog) {
        NewReservationDialog(
            saving = creating,
            error = createError,
            onDismiss = {
                showCreateDialog = false
                createError = null
            },
            onConfirm = { reservation ->
                creating = true
                createError = null
                val createdBy = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
                repository.create(reservation, createdBy)
                    .addOnSuccessListener {
                        creating = false
                        showCreateDialog = false
                    }
                    .addOnFailureListener { failure ->
                        creating = false
                        createError = failure.message
                            ?.take(180)
                            ?: "Não foi possível adicionar a reserva."
                    }
            }
        )
    }

    Column(
        Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
    ) {
        ScreenHeader("Reservas", "${reservations.size} registros para acompanhar")
        Button(
            onClick = {
                createError = null
                showCreateDialog = true
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Adicionar reserva")
        }

        when {
            loading -> CircularProgressIndicator(Modifier.padding(top = 32.dp))
            error != null -> Text(
                error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 24.dp)
            )
            reservations.isEmpty() -> Text(
                "Nenhuma reserva encontrada.",
                modifier = Modifier.padding(top = 32.dp)
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(reservations, key = Reservation::id) { reservation ->
                    ReservationCard(reservation, repository)
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    repository: ReservationRepository
) {
    val context = LocalContext.current
    val updatedBy = FirebaseAuth.getInstance().currentUser?.email ?: "mobile-admin"
    var menuOpen by remember { mutableStateOf(false) }
    var updating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                reservation.name.ifBlank { "Cliente sem nome" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(reservation.date.ifBlank { "Data não informada" }, color = StoneMuted)
            if (reservation.notes.isNotBlank()) {
                Text(
                    reservation.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val phone = normalizeBrazilianPhone(reservation.phone)
                    val message = Uri.encode(
                        "Olá, ${reservation.name}! Aqui é da Chácara Elo Verde. " +
                            "Recebemos sua intenção de reserva para ${reservation.date} " +
                            "e gostaríamos de confirmar alguns detalhes."
                    )
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$message"))
                    )
                },
                enabled = reservation.phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Chat, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("WhatsApp · ${reservation.phone.ifBlank { "sem telefone" }}")
            }

            Box {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    enabled = !updating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (updating) "Atualizando…" else reservation.status.label)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    ReservationStatus.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            enabled = option != reservation.status,
                            onClick = {
                                menuOpen = false
                                updating = true
                                updateError = null
                                repository.updateStatus(reservation.id, option, updatedBy)
                                    .addOnSuccessListener { updating = false }
                                    .addOnFailureListener {
                                        updating = false
                                        updateError = "Não foi possível alterar o status."
                                    }
                            }
                        )
                    }
                }
            }

            updateError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (reservation.email.isNotBlank()) {
                Text(reservation.email, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(
                enabled = !updating && !deleting,
                onClick = { confirmDelete = true }
            ) {
                Text("Remover", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { if (!deleting) confirmDelete = false },
            title = { Text("Remover reserva?") },
            text = {
                Text("A reserva de ${reservation.name.ifBlank { "este cliente" }} será removida permanentemente.")
            },
            confirmButton = {
                Button(
                    enabled = !deleting,
                    onClick = {
                        deleting = true
                        updateError = null
                        repository.remove(reservation.id)
                            .addOnSuccessListener {
                                deleting = false
                                confirmDelete = false
                            }
                            .addOnFailureListener { failure ->
                                deleting = false
                                updateError = failure.message ?: "Não foi possível remover a reserva."
                            }
                    }
                ) {
                    Text(if (deleting) "Removendo…" else "Remover")
                }
            },
            dismissButton = {
                TextButton(enabled = !deleting, onClick = { confirmDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun normalizeBrazilianPhone(rawPhone: String): String {
    val digits = rawPhone.filter(Char::isDigit)
    return if (digits.length in 10..11) "55$digits" else digits
}

@Composable
fun ChartsScreen(padding: PaddingValues) {
    val repository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }

    DisposableEffect(repository) {
        val listener = repository.observe(
            onResult = { reservations = it },
            onError = {}
        )
        onDispose { listener.remove() }
    }

    val counts = remember(reservations) {
        ReservationStatus.entries.associateWith { status ->
            reservations.count { it.status == status }
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
        ScreenHeader("Métricas", "Uma visão rápida da operação")
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Forest)
        ) {
            Column(Modifier.padding(24.dp)) {
                Icon(Icons.Default.AutoGraph, null, tint = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(reservations.size.toString(), style = MaterialTheme.typography.displaySmall, color = Color.White)
                Text("reservas cadastradas", color = Color.White.copy(alpha = .82f))
            }
        }
        Spacer(Modifier.height(18.dp))
        ReservationStatus.entries.forEach { status ->
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(status.label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(counts.getValue(status).toString(), style = MaterialTheme.typography.titleLarge, color = Forest)
                }
            }
        }
    }
}

@Composable
fun MoreScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val repository = remember { GoogleAuthRepository() }
    val scope = rememberCoroutineScope()

    val user = FirebaseAuth.getInstance().currentUser
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
        ScreenHeader("Perfil", "Conta e acesso administrativo")
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Forest, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(user?.displayName ?: "Administrador", style = MaterialTheme.typography.titleMedium)
                    Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = StoneMuted)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                scope.launch { repository.signOut(context) }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Sair")
        }
    }
}
