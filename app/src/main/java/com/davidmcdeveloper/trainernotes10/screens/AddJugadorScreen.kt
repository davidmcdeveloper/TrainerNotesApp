package com.davidmcdeveloper.trainernotes10.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.utils.addJugadorToFirestore
import com.davidmcdeveloper.trainernotes10.utils.convertMillisToDate
import com.davidmcdeveloper.trainernotes10.utils.uploadImageToFirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJugadorScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    //Fecha de Nacimiento
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var fechaNacimiento by remember { mutableStateOf("") }
    //Numero de licencia
    var nLicencia by remember { mutableStateOf("") }

    //ExposedDropdownMenuBox
    //Posicion Primaria
    val posicionPrimariaOptions =
        listOf("1ª Línea", "2ª Línea", "Talona", "Flanker", "Ocho", "Medio Melé", "Apertura", "Ala Interno", "Ala Externo", "Primer Centro", "Segundo Centro", "Zaguero")
    var posicionPrimariaExpanded by remember { mutableStateOf(false) }
    var posicionPrimariaSelectedText by remember { mutableStateOf("") }
    //Posicion Secundaria
    val posicionSecundariaOptions =
        listOf("1ª Línea", "2ª Línea", "Talona", "Flanker", "Ocho", "Medio Melé", "Apertura", "Ala Interno", "Ala Externo", "Primer Centro", "Segundo Centro", "Zaguero")
    var posicionSecundariaExpanded by remember { mutableStateOf(false) }
    var posicionSecundariaSelectedText by remember { mutableStateOf("") }
    //Peso
    val pesoOptions =
        (50..200).map { "$it kg" } // Rango de 50 a 200 kg
    var pesoExpanded by remember { mutableStateOf(false) }
    var pesoSelectedText by remember { mutableStateOf("") }
    //Altura
    val alturaOptions =
        (110..220).map { String.format("%.2f m", it.toFloat() / 100) } // Rango de 1.10 a 2.20 m
    var alturaExpanded by remember { mutableStateOf(false) }
    var alturaSelectedText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { //Cogemos el millis
                        fechaNacimiento = convertMillisToDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    //Image
    var jugadorImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        jugadorImageUri = uri
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Añadir Jugador") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    //Añadir Foto
                    Spacer(modifier = Modifier.height(8.dp)) //Reducimos el spacer.
                    //Añadimos box para superponer elementos.
                    Box(
                        modifier = Modifier
                            .size(100.dp) //Reducimos el tamaño.
                    ) {
                        //Imagen del jugador
                        if (jugadorImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(jugadorImageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto Jugador",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else { //Muestra la foto por defecto
                            Image(
                                painter = painterResource(id = R.drawable.jugadordefault),
                                contentDescription = "Añadir Foto",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .alpha(0.8f),
                                contentScale = ContentScale.Crop
                            )
                        }
                        //Boton Add
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .align(Alignment.BottomEnd)
                                .alpha(0.0f),


                            ) {
                            Icon(
                                Icons.Filled.AddCircle,
                                contentDescription = "Añadir imagen",
                                tint = Color.Black,
                            )
                        }
                    }
                    //Formulario
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        )
                        OutlinedTextField(
                            value = primerApellido,
                            onValueChange = { primerApellido = it },
                            label = { Text("Primer Apellido") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        )
                    }
                    //POSICION PRIMARIA Y SECUNDARIA EN FILA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        //Posicion Primaria
                        ExposedDropdownMenuBox(
                            expanded = posicionPrimariaExpanded,
                            onExpandedChange = { posicionPrimariaExpanded = !posicionPrimariaExpanded },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp) //Reducimos el padding.
                        ) {
                            OutlinedTextField(
                                value = posicionPrimariaSelectedText,
                                onValueChange = { posicionPrimariaSelectedText = it },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionPrimariaExpanded) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("1ª Posición") }
                            )
                            //Este es el código del desplegable
                            DropdownMenu(
                                expanded = posicionPrimariaExpanded,
                                onDismissRequest = { posicionPrimariaExpanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                posicionPrimariaOptions.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            posicionPrimariaSelectedText = item
                                            posicionPrimariaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        //Posicion Secundaria
                        ExposedDropdownMenuBox(
                            expanded = posicionSecundariaExpanded,
                            onExpandedChange = { posicionSecundariaExpanded = !posicionSecundariaExpanded },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp) //Reducimos el padding.
                        ) {
                            OutlinedTextField(
                                value = posicionSecundariaSelectedText,
                                onValueChange = { posicionSecundariaSelectedText = it },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionSecundariaExpanded) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("2ª Posición") }
                            )
                            //Este es el código del desplegable
                            DropdownMenu(
                                expanded = posicionSecundariaExpanded,
                                onDismissRequest = { posicionSecundariaExpanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                posicionSecundariaOptions.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            posicionSecundariaSelectedText = item
                                            posicionSecundariaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        //Peso
                        ExposedDropdownMenuBox(
                            expanded = pesoExpanded,
                            onExpandedChange = { pesoExpanded = !pesoExpanded },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            OutlinedTextField(
                                value = pesoSelectedText,
                                onValueChange = { pesoSelectedText = it },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pesoExpanded) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("Peso") }
                            )
                            //Este es el código del desplegable
                            DropdownMenu(
                                expanded = pesoExpanded,
                                onDismissRequest = { pesoExpanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                pesoOptions.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            pesoSelectedText = item
                                            pesoExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        //Altura
                        ExposedDropdownMenuBox(
                            expanded = alturaExpanded,
                            onExpandedChange = { alturaExpanded = !alturaExpanded },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            OutlinedTextField(
                                value = alturaSelectedText,
                                onValueChange = { alturaSelectedText = it },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alturaExpanded) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("Altura") }
                            )
                            //Este es el código del desplegable
                            DropdownMenu(
                                expanded = alturaExpanded,
                                onDismissRequest = { alturaExpanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                alturaOptions.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            alturaSelectedText = item
                                            alturaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    //Fecha de nacimiento
                    OutlinedTextField(
                        value = if (fechaNacimiento.isEmpty()) "Seleccionar Fecha de Nacimiento" else fechaNacimiento,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(text = "Fecha de Nacimiento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { showDatePicker = true },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Seleccionar Fecha",
                                modifier = Modifier.clickable { showDatePicker = true }
                            )
                        }
                    )
                    //Nuevo Textfield de Numero de Licencia
                    OutlinedTextField(
                        value = nLicencia,
                        onValueChange = { nLicencia = it },
                        label = { Text("Nº Licencia (Opcional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                //Comprobamos los datos
                                if (nombre.isEmpty() || primerApellido.isEmpty() || posicionPrimariaSelectedText.isEmpty() || posicionSecundariaSelectedText.isEmpty() || pesoSelectedText.isEmpty() || alturaSelectedText.isEmpty()) {
                                    snackbarHostState.showSnackbar("Debes rellenar todos los campos")
                                } else {
                                    val idJugador = generateJugadorId(nombre, primerApellido)
                                    //Declaramos la variable para usarla en ambos casos.
                                    var jugadorImageUrl = ""
                                    //Si la jugadorImageUri no es nula, subimos la foto a Firebase
                                    if (jugadorImageUri != null) {
                                        try {
                                            jugadorImageUrl = uploadImageToFirebaseStorage(
                                                jugadorImageUri!!,
                                                idJugador,
                                                context
                                            )
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error al subir la imagen")
                                        }
                                    }
                                    try {
                                        addJugadorToFirestore(
                                            db,
                                            idJugador,
                                            nombre,
                                            primerApellido,
                                            posicionPrimariaSelectedText,
                                            posicionSecundariaSelectedText,
                                            pesoSelectedText,
                                            alturaSelectedText,
                                            fechaNacimiento,
                                            jugadorImageUrl,
                                            categoryName,
                                            context,
                                            nLicencia //Enviamos el numero de licencia
                                        )
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error al añadir el jugador")
                                    }
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(text = "Añadir jugador")
                        }
                    }
                }
            }
        }
    }
}

fun generateJugadorId(nombre: String, primerApellido: String): String {
    val uniqueId = UUID.randomUUID().toString().substring(0, 8) // Tomamos los primeros 8 caracteres del UUID
    val sanitizedNombre = nombre.filter { it.isLetterOrDigit() }.take(9).lowercase() // Tomamos los primeros 9 caracteres del nombre
    val sanitizedApellido = primerApellido.filter { it.isLetterOrDigit() }.take(9).lowercase() // Tomamos los primeros 9 caracteres del apellido
    return "${sanitizedNombre}-${sanitizedApellido}-$uniqueId" // Ejemplo: luis-perez-f4a2b9d1
}