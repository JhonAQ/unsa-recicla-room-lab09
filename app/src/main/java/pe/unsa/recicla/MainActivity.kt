package pe.unsa.recicla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.unsa.recicla.data.AppDatabase
import pe.unsa.recicla.data.EcoTransactionDao
import pe.unsa.recicla.data.EcoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.get(this)
        val dao = database.ecoTransactionDao()
        setContent { ReciclaScreen(dao, EcoRepository(database)) }
    }
}

@Composable
private fun ReciclaScreen(dao: EcoTransactionDao, repository: EcoRepository) {
    val rows by dao.observeAll().collectAsState(initial = emptyList())
    val balance by dao.observeBalance().collectAsState(initial = 0)
    var quantity by remember { mutableStateOf("1") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val count = quantity.toIntOrNull()?.takeIf { it in 1..99 }
    MaterialTheme(colorScheme = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF176B55), secondary = androidx.compose.ui.graphics.Color(0xFFE3A72F))) {
        Scaffold(containerColor = androidx.compose.ui.graphics.Color(0xFFF4F7F3)) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    Text("UNSA RECICLA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Tus EcoPuntos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Registro local de retornos PET y canjes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Column(Modifier.fillMaxWidth().padding(22.dp)) {
                            Text("SALDO DISPONIBLE", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium)
                            Text("$balance", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text("EcoPuntos", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Registrar retorno", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Cada botella PET devuelta acredita 1 EcoPunto.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter(Char::isDigit).take(2) }, label = { Text("Cantidad de botellas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Button(onClick = {
                                if (count == null) { message = "Ingresa una cantidad entre 1 y 99." } else {
                                    scope.launch { withContext(Dispatchers.IO) { repository.recordReturn(count) }; message = "Retorno guardado en Room." }
                                }
                            }, modifier = Modifier.fillMaxWidth(), enabled = count != null) { Text("Registrar retorno") }
                        }
                    }
                }
                item {
                    Text("Canjear beneficio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Los costos siguen la propuesta UNSA RECICLA.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item { RedeemCard("Impresiones gratuitas", "10 impresiones en bibliotecas", 5, balance, onRedeem = { pts, name ->
                    scope.launch { val ok = withContext(Dispatchers.IO) { repository.redeem(pts, name) }; message = if (ok) "Canje registrado." else "Saldo insuficiente." }
                }) }
                item { RedeemCard("Descuento en comedor", "S/ 1.00 de descuento", 10, balance, onRedeem = { pts, name ->
                    scope.launch { val ok = withContext(Dispatchers.IO) { repository.redeem(pts, name) }; message = if (ok) "Canje registrado." else "Saldo insuficiente." }
                }) }
                if (message != null) item { Text(message!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
                item {
                    Text("Actividad reciente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Se actualiza desde el flujo de Room.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (rows.isEmpty()) item { Text("Aún no hay movimientos. Registra un retorno para comenzar.", modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(rows, key = { it.id }) { row ->
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(row.description, fontWeight = FontWeight.SemiBold)
                                Text("${row.type} · ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-PE")).format(Date(row.createdAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(if (row.points > 0) "+${row.points}" else row.points.toString(), color = if (row.points > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { scope.launch { withContext(Dispatchers.IO) { dao.delete(row) }; message = "Movimiento eliminado." } }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RedeemCard(title: String, detail: String, cost: Int, balance: Int, onRedeem: (Int, String) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text("$detail · $cost puntos", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Button(onClick = { onRedeem(cost, title) }, enabled = balance >= cost) { Text("Canjear") }
        }
    }
}

