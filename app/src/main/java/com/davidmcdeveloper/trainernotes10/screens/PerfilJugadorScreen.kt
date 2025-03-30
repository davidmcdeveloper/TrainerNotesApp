package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.dataclass.Jugador
import com.davidmcdeveloper.trainernotes10.utils.Valoracion
import com.davidmcdeveloper.trainernotes10.utils.calculateCurrentWeek
import com.davidmcdeveloper.trainernotes10.utils.getJugadorById
import com.davidmcdeveloper.trainernotes10.utils.getRatingsFromFirestore
import com.davidmcdeveloper.trainernotes10.utils.saveRating
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilJugadorScreen(navController: NavController, db: FirebaseFirestore, jugadorId: String) {
    val context = LocalContext.current
    var jugador by remember { mutableStateOf<Jugador?>(null) }
    //Estado para guardar la lista de habilidades.
    val skills = listOf("Placaje","Contacto","Pase","Aceleración","Fuerza","Resistencia")
    //Estados para el boton de guardar
    var isButtonEnabled by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("Guardar") }
    var ratings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var initialRatings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var lastRatingDate by remember { mutableStateOf("") }
    var currentWeek by remember { mutableStateOf("") }
    //Estado para saber cuando esta cargando la informacion
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = jugadorId) {
        jugador = getJugadorById(db, jugadorId, context) // Utilizamos la función de utils.
    }
    // Llama a la funcion de obtener datos de firestore.
    LaunchedEffect(key1 = jugadorId) {
        getRatingsFromFirestore(db, jugadorId, onSuccess = { rating ->
            // Guardar los datos originales antes de modificarlos.
            initialRatings = rating.ratings.associate { it.skill to it.rating }
            ratings = rating.ratings.associate { it.skill to it.rating }
            lastRatingDate = rating.lastRatingDate
            isLoading = false
            currentWeek = calculateCurrentWeek()
            //Comprobamos que la semana coincida.
            buttonText = if (lastRatingDate.isNotEmpty() && lastRatingDate == currentWeek) {
                "Actualizar"
            } else {
                "Guardar"
            }
            //Comprobamos que esten iguales.
            isButtonEnabled = ratings != initialRatings
        }, onFailure = { exception ->
            Log.e("PerfilJugadorScreen", "Error al obtener las valoraciones", exception)
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil del Jugador") },
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
        //TODO:Añadir registro de valoraciones con gráfica de evolución.
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mostrar la información del jugador si está disponible
                jugador?.let {
                    //IMAGEN DEL JUGADOR
                    Box(modifier = Modifier.size(100.dp)) {
                        if (jugador!!.fotoUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(jugador!!.fotoUrl)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto del jugador",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.jugadordefault),
                                contentDescription = "Foto Jugador",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "${jugador!!.nombre} ${jugador!!.primerApellido}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    //Nueva disposicion con Row y Column.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        //Columna Posiciones
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = "Pos.Prim. : ${jugador!!.posicionPrimaria}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Pos.Sec. : ${jugador!!.posicionSecundaria}")
                        }
                        //Columna Peso y Altura
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = "Peso: ${jugador!!.peso}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Altura: ${jugador!!.altura}")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Fecha de Nacimiento: ${jugador!!.fechaNacimiento}")
                    Text(text = "Nº de Licencia: ${jugador!!.nLicencia}")
                    //Nueva seccion de valoracion.
                    Spacer(modifier = Modifier.height(16.dp))
                    //Columna para las habilidades.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        skills.forEach { skill ->
                            SkillRating(
                                skillName = skill,
                                rating = ratings[skill],
                                onRatingChanged = { newRating ->
                                    //Cambiamos el valor nuevo.
                                    ratings = ratings.toMutableMap().apply { put(skill, newRating) }
                                    //Comprobamos que sea distinto.
                                    isButtonEnabled = ratings != initialRatings
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    //Boton de guardar
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Accion de guardar.
                            val valoraciones = skills.map { skill ->
                                Valoracion(skill, ratings[skill] ?: 0)
                            }
                            saveRating(db, jugadorId, valoraciones, onSuccess = {
                                Toast.makeText(context, "Valoración guardada correctamente.", Toast.LENGTH_SHORT).show()
                                // Comprobar si se ha guardado en la misma semana
                                if (currentWeek == calculateCurrentWeek()) {
                                    buttonText = "Actualizar"
                                    //Actualizamos los ratings.
                                    initialRatings = ratings
                                    isButtonEnabled = false
                                } else {
                                    buttonText = "Guardar"
                                }
                            }, onFailure = { exception ->
                                Log.e("PerfilJugadorScreen", "Error al guardar la valoracion.", exception)
                            })
                        },
                        enabled = isButtonEnabled
                    ) {
                        Text(buttonText)
                    }
                }
            }
        }
    }
}

//Funcion para devolver el color en funcion del valor.
fun getColorForRating(rating: Int): Color {
    return when (rating) {
        1 -> Color(0xFFFFF9C4) // Amarillo Claro Pastel
        2 -> Color(0xFFFFF59D) // Amarillo Claro
        3 -> Color(0xFFFFF176) // Amarillo Medio
        4 -> Color(0xFFFFEE58) // Amarillo Intenso
        5 -> Color(0xFFFDD835) // Amarillo Oscuro
        else -> Color(0xFFBDBDBD) // Gris Claro
    }
}

@Composable
fun RatingStars(rating: Int, onRatingChanged: (Int) -> Unit) {
    Log.d("RatingStars", "Llamando a RatingStars con rating: $rating")
    Row {
        for (i in 1..5) {
            Log.d("RatingStars", "Procesando estrella $i")
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Estrella $i",
                tint = if (i <= rating) getColorForRating(rating) else Color.Gray,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        Log.d("RatingStars", "Estrella $i clickeada")
                        onRatingChanged(i)
                    }
            )
        }
    }
}

@Composable
fun SkillRating(skillName: String, rating: Int?, onRatingChanged: (Int) -> Unit) {
    Log.d("SkillRating", "Llamando a SkillRating con skillName: $skillName y rating: $rating")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = skillName, fontWeight = FontWeight.Bold)
        RatingStars(rating = rating ?: 0, onRatingChanged = onRatingChanged)
    }
}