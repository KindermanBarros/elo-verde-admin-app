package com.eloverde.admin.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eloverde.admin.data.ReservationRepository
import com.eloverde.admin.domain.Reservation
import com.eloverde.admin.domain.ReservationStatus
import com.google.firebase.auth.FirebaseAuth
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LoginScreen(onAuthenticated: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Elo Verde · Administração", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            onClick = {
                loading = true
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener {
                        loading = false
                        onAuthenticated()
                    }
                    .addOnFailureListener {
                        loading = false
                        error = "E-mail ou senha inválidos."
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Entrando…" else "Entrar")
        }
    }
}

@Composable
fun ReservationsScreen(padding: PaddingValues) {
    val repository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(repository) {
        val listener = repository.observe(
            onResult = {
                reservations = it
                loading = false
                error = null
            },
            onError = {
                loading = false
                error = "Não foi possível carregar as reservas."
            }
        )
        onDispose { listener.remove() }
    }

    Column(
        Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
    ) {
        Text("Reservas", style = MaterialTheme.typography.headlineMedium)
        Text("${reservations.size} intenção(ões) cadastrada(s)")

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
    var updateError by remember { mutableStateOf<String?>(null) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                reservation.name.ifBlank { "Cliente sem nome" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                listOf(reservation.date, reservation.time)
                    .filter(String::isNotBlank)
                    .joinToString(" · ")
                    .ifBlank { "Data não informada" }
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    val phone = normalizeBrazilianPhone(reservation.phone)
                    val message = Uri.encode(
                        "Olá, ${reservation.name}! Aqui é da Chácara Elo Verde. " +
                            "Recebemos sua intenção de reserva para ${reservation.date} " +
                            "às ${reservation.time} e gostaríamos de confirmar alguns detalhes."
                    )
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$message"))
                    )
                },
                enabled = reservation.phone.isNotBlank()
            ) {
                Text(reservation.phone.ifBlank { "Telefone não informado" })
            }

            Box {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    enabled = !updating
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
        }
    }
}

private fun normalizeBrazilianPhone(rawPhone: String): String {
    val digits = rawPhone.filter(Char::isDigit)
    return if (digits.length in 10..11) "55$digits" else digits
}

@Composable
fun CalendarScreen(padding: PaddingValues) {
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val month = remember(monthOffset) {
        YearMonth.now().plusMonths(monthOffset.toLong())
    }
    val locale = remember { Locale.forLanguageTag("pt-BR") }
    val title = remember(month, locale) {
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
            .replaceFirstChar { it.titlecase(locale) }
    }

    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Calendário", style = MaterialTheme.typography.headlineMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { monthOffset-- }) { Text("‹ Anterior") }
            Text(
                title,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { monthOffset++ }) { Text("Próximo ›") }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                Text(it, style = MaterialTheme.typography.labelMedium)
            }
        }

        val firstDayOffset = month.atDay(1).dayOfWeek.value % 7
        val cells = List(firstDayOffset) { null } +
            (1..month.lengthOfMonth()).map(month::atDay)

        cells.chunked(7).forEach { week ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    Text(
                        day?.dayOfMonth?.toString().orEmpty(),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                repeat(7 - week.size) { Spacer(Modifier.width(38.dp)) }
            }
        }
        Text(
            "Use as setas para paginar os meses. Os dias são apenas informativos.",
            style = MaterialTheme.typography.bodySmall
        )
    }
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

    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Gráficos", style = MaterialTheme.typography.headlineMedium)
        Text("Resumo das intenções de reserva")
        Spacer(Modifier.height(16.dp))
        ReservationStatus.entries.forEach { status ->
            ListItem(
                headlineContent = { Text(status.label) },
                trailingContent = {
                    Text(
                        counts.getValue(status).toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
            HorizontalDivider()
        }
        Spacer(Modifier.height(12.dp))
        Text("Total: ${reservations.size}", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun MoreScreen(padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Mais", style = MaterialTheme.typography.headlineMedium)
        Text(
            FirebaseAuth.getInstance().currentUser?.email.orEmpty(),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { FirebaseAuth.getInstance().signOut() }) {
            Text("Sair")
        }
    }
}
