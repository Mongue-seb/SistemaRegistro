package com.example.registroestudiantes

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SistemaRegistroApp()
        }
    }
}

// 1. Modelo de Datos para transferir la información entre pantallas
data class Estudiante(
    val matricula: String,
    val nombre: String,
    val carrera: String,
    val turno: String,
    val estatus: Boolean
)

// 2. Controlador de Navegación Fluida (Reemplaza la necesidad de usar Intents)
@Composable
fun SistemaRegistroApp() {
    var pantallaActual by remember { mutableStateOf("Formulario") }
    var datosEstudiante by remember { mutableStateOf<Estudiante?>(null) }

    // Intercambio de vistas (Composables) manteniendo la misma Activity
    if (pantallaActual == "Formulario") {
        FormularioScreen(
            onRegistrar = { estudiante ->
                datosEstudiante = estudiante
                pantallaActual = "Detalle" // Navegamos hacia adelante
            }
        )
    } else {
        datosEstudiante?.let {
            DetalleScreen(
                estudiante = it,
                onVolver = { pantallaActual = "Formulario" } // Navegamos hacia atrás
            )
        }
    }
}

// 3. Pantalla 1: Formulario de Registro
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(onRegistrar: (Estudiante) -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("RegistroAppPrefs", Context.MODE_PRIVATE)
    }

    // --- Estados de los Controles ---
    var matricula by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    // Spinner
    var carrera by remember { mutableStateOf("Ingeniería de Software") }
    var dropdownExpandido by remember { mutableStateOf(false) }
    val opcionesCarrera = listOf("Ingeniería de Software", "Ingeniería en Sistemas", "Ingeniería Mecatrónica")

    // RadioGroup
    var turno by remember { mutableStateOf("Matutino") }
    val opcionesTurno = listOf("Matutino", "Vespertino")

    // Switch
    var estatusActivo by remember { mutableStateOf(true) }

    // --- Carga de Persistencia (Práctica 5) ---
    LaunchedEffect(Unit) {
        matricula = sharedPreferences.getString("ULTIMA_MATRICULA", "") ?: ""
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Registro de Estudiantes", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matrícula") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown Menu para la Carrera
        ExposedDropdownMenuBox(
            expanded = dropdownExpandido,
            onExpandedChange = { dropdownExpandido = it }
        ) {
            OutlinedTextField(
                value = carrera,
                onValueChange = {},
                readOnly = true,
                label = { Text("Programa Académico") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandido) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpandido,
                onDismissRequest = { dropdownExpandido = false }
            ) {
                opcionesCarrera.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            carrera = opcion
                            dropdownExpandido = false
                        }
                    )
                }
            }
        }

        // RadioGroup para el Turno
        Text("Turno asignado:", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            opcionesTurno.forEach { opcion ->
                Row(
                    Modifier.selectable(
                        selected = (turno == opcion),
                        onClick = { turno = opcion },
                        role = Role.RadioButton
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (turno == opcion), onClick = null)
                    Text(text = opcion, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Switch para el Estatus
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Estatus: ${if (estatusActivo) "Activo" else "Inactivo"}", style = MaterialTheme.typography.labelLarge)
            Switch(
                checked = estatusActivo,
                onCheckedChange = { estatusActivo = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de Procesamiento y Guardado
        Button(
            onClick = {
                if (matricula.isNotBlank() && nombre.isNotBlank()) {
                    // Guardar Matrícula localmente antes de cambiar de pantalla
                    sharedPreferences.edit().putString("ULTIMA_MATRICULA", matricula).apply()
                    Toast.makeText(context, "Configuración persistida", Toast.LENGTH_SHORT).show()

                    // Empaquetar datos y disparar la navegación
                    onRegistrar(Estudiante(matricula, nombre, carrera, turno, estatusActivo))
                } else {
                    Toast.makeText(context, "Llena los campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Procesar Registro")
        }
    }
}

// 4. Pantalla 2: Detalle y Confirmación
@Composable
fun DetalleScreen(estudiante: Estudiante, onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Confirmación Exitosa", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // Contenedor visual para los parámetros recibidos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Matrícula: ${estudiante.matricula}", style = MaterialTheme.typography.bodyLarge)
                Text("Nombre: ${estudiante.nombre}", style = MaterialTheme.typography.bodyLarge)
                Text("Programa: ${estudiante.carrera}", style = MaterialTheme.typography.bodyLarge)
                Text("Turno: ${estudiante.turno}", style = MaterialTheme.typography.bodyLarge)
                Text("Estatus en sistema: ${if (estudiante.estatus) "Activo" else "Inactivo"}", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al Formulario")
        }
    }
}