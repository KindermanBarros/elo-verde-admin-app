package com.eloverde.admin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eloverde.admin.data.ReservationRepository
import com.eloverde.admin.domain.Reservation
import com.eloverde.admin.domain.ReservationStatus
import com.eloverde.admin.presentation.theme.*
import java.time.YearMonth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BlockedDay = Mint
private val VisitDay = BlueSoft
private val PendingDay = AmberSoft
private val FreeDay = Color.Transparent

@Composable
fun CalendarScreen(padding: PaddingValues) {
    val repository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(repository) {
        val listener = repository.observe(
            onResult = {
                reservations = it
                loading = false
                error = null
            },
            onError = {
                loading = false
                error = it.message ?: "Não foi possível carregar o calendário."
            }
        )
        onDispose { listener.remove() }
    }

    val month = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val locale = remember { Locale.forLanguageTag("pt-BR") }
    val title = remember(month, locale) {
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
            .replaceFirstChar { it.titlecase(locale) }
    }
    val reservationsByDate = remember(reservations) {
        reservations.filter { it.date.isNotBlank() }.groupBy(Reservation::date)
    }

    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                ScreenHeader("Calendário", "Disponibilidade e visitas em um só lugar")
            }
            IconButton(
                enabled = !refreshing,
                onClick = {
                    refreshing = true
                    refreshError = null
                    FirebaseFirestore.getInstance().collection("reservationIntents")
                        .get(Source.SERVER)
                        .addOnSuccessListener {
                            // The active Firestore listener updates the displayed reservations.
                            refreshing = false
                        }
                        .addOnFailureListener {
                            refreshError = "Não foi possível atualizar. Verifique a conexão e tente novamente."
                            refreshing = false
                        }
                }
            ) {
                if (refreshing) CircularProgressIndicator(Modifier.width(22.dp).height(22.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Refresh, contentDescription = "Atualizar calendário")
            }
        }
        refreshError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
       Column(Modifier.padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { monthOffset-- }) { Text("‹ Anterior") }
            Text(title, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { monthOffset++ }) { Text("Próximo ›") }
        }

        if (loading) {
            CircularProgressIndicator(Modifier.padding(24.dp))
            return@Column
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            return@Column
        }

        TextButton(onClick = { monthOffset = 0; selectedDate = LocalDate.now().toString() }) { Text("Ir para hoje") }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }
        }

        val firstDayOffset = month.atDay(1).dayOfWeek.value % 7
        val cells = List(firstDayOffset) { null } + (1..month.lengthOfMonth()).map(month::atDay)

        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                (week + List(7 - week.size) { null }).forEach { day ->
                    val dayReservations = day?.toString()?.let { reservationsByDate[it] }.orEmpty()
                    val blocked = dayReservations.any {
                        it.status == ReservationStatus.RESERVED || it.status == ReservationStatus.PAID
                    }
                    val visits = dayReservations.count { it.status == ReservationStatus.VISIT }
                    val pending = dayReservations.count { it.status == ReservationStatus.PENDING }
                    val color = when {
                        blocked -> BlockedDay
                        visits > 0 -> VisitDay
                        pending > 0 -> PendingDay
                        else -> FreeDay
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .aspectRatio(0.9f)
                            .background(color, RoundedCornerShape(10.dp))
                            .then(if (day != null) Modifier.clickable { selectedDate = day.toString() } else Modifier)
                            .padding(5.dp)
                    ) {
                        if (day != null) {
                            Column {
                                Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge)
                                if (dayReservations.isNotEmpty()) {
                                    Text("${dayReservations.size} registro(s)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        CalendarLegendItem(BlockedDay, "Reservado ou quitado", "Data indisponível")
        CalendarLegendItem(VisitDay, "Visita", "Visita agendada")
        CalendarLegendItem(PendingDay, "Pendente", "Aguardando contato")
        CalendarLegendItem(MaterialTheme.colorScheme.surface, "Livre", "Sem reservas registradas")
        selectedDate?.let { date ->
            val selected = reservationsByDate[date].orEmpty()
            Text("Reservas em $date", style = MaterialTheme.typography.titleMedium)
            if (selected.isEmpty()) Text("Nenhum registro neste dia.")
            selected.forEach { reservation ->
                Text("• ${reservation.name.ifBlank { "Cliente sem nome" }} — ${reservation.status.label}")
            }
        }
       }
      }
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String, description: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(16.dp).height(16.dp).background(color, RoundedCornerShape(5.dp)))
        Column(Modifier.padding(start = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
