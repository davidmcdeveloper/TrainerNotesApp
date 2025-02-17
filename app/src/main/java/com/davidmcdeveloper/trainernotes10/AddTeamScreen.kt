package com.davidmcdeveloper.trainernotes10

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddTeamScreen(navController: NavController, db: FirebaseFirestore) {
    var teamName by remember { mutableStateOf(TextFieldValue()) }
    var category by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = teamName,
            onValueChange = { teamName = it },
            label = { Text("Nombre del equipo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría (ej. Senior Masculino)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                addTeam(db, teamName.text, category.text)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir equipo")
        }
    }
}

fun addTeam(db: FirebaseFirestore, teamName: String, category: String) {
    if (teamName.isNotEmpty() && category.isNotEmpty()) {
        val teamRef = db.collection("equipos").document(teamName)
        val categoryRef = teamRef.collection("categorías").document(category)

        categoryRef.set(hashMapOf("nombre" to category))
            .addOnSuccessListener {
                Log.d("Firestore", "Equipo y categoría añadidos correctamente")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al añadir equipo", e)
            }
    } else {
        Log.w("Firestore", "Nombre del equipo o categoría vacíos")
    }
}
