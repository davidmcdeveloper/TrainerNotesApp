package com.davidmcdeveloper.trainernotes10.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.dataclass.Categoria
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlin.text.set
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(navController: NavController, teamName: String, db: FirebaseFirestore) {
    var categoryName by remember { mutableStateOf(TextFieldValue()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agregar Categoría") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.TeamDetails.createRoute(teamName)) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver a los detalles del equipo"
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Introduzca el nombre de la categoría")

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Ej. Senior Masculino") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (categoryName.text.isNotEmpty()) {
                        saveCategoryToFirestore(db, teamName, categoryName.text, context, navController)
                    } else {
                        Toast.makeText(
                            context,
                            "Debes introducir un nombre para la categoría",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar categoría")
            }
        }
    }
}
fun saveCategoryToFirestore(
    db: FirebaseFirestore,
    teamName: String,
    categoryName: String,
    context: android.content.Context,
    navController: NavController
) {
    val categoryId = UUID.randomUUID().toString() // Generar un ID único para la categoría
    val categoryData = hashMapOf(
        "nombre" to categoryName,
    )

    //Guardamos la categoria con el id creado.
    db.collection("equipos").document(teamName).collection("categorias").document(categoryId)
        .set(categoryData)
        .addOnSuccessListener {
            //Una vez guardada, actualizamos el documento del equipo con el nombre de la categoria
            db.collection("equipos").document(teamName)
                .update("categorias", FieldValue.arrayUnion(categoryName)) //Aquí actualiza el campo categorias, añadiento el nombre.
                .addOnSuccessListener {
                    Toast.makeText(context, "Categoría añadida correctamente", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.TeamDetails.createRoute(teamName))
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al guardar categoría", Toast.LENGTH_SHORT).show()
        }
}