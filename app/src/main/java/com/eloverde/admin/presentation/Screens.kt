package com.eloverde.admin.presentation

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable fun LoginScreen(onAuthenticated: () -> Unit) { var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) { Text("Elo Verde · Administração", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(20.dp)); OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(password, { password = it }, label = { Text("Senha") }, modifier = Modifier.fillMaxWidth()); if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error); Button(onClick = { FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener { onAuthenticated() }.addOnFailureListener { error = "E-mail ou senha inválidos." } }, modifier = Modifier.fillMaxWidth()) { Text("Entrar") } }
}
@Composable fun ReservationsScreen(padding: PaddingValues) { Column(Modifier.padding(padding).padding(20.dp)) { Text("Reservas", style = MaterialTheme.typography.headlineMedium); Text("As reservas do Firestore aparecerão aqui.") } }
@Composable fun CalendarScreen(padding: PaddingValues) { Column(Modifier.padding(padding).padding(20.dp)) { Text("Calendário", style = MaterialTheme.typography.headlineMedium); Text("Calendário visual com navegação mensal.") } }
@Composable fun ChartsScreen(padding: PaddingValues) { Column(Modifier.padding(padding).padding(20.dp)) { Text("Gráficos", style = MaterialTheme.typography.headlineMedium); Text("Métricas de reservas serão exibidas aqui.") } }
@Composable fun MoreScreen(padding: PaddingValues) { val context = LocalContext.current; Column(Modifier.padding(padding).padding(20.dp)) { Text("Mais", style = MaterialTheme.typography.headlineMedium); Button(onClick = { FirebaseAuth.getInstance().signOut(); context.startActivity(Intent(context, com.eloverde.admin.MainActivity::class.java)) }) { Text("Sair") } } }
