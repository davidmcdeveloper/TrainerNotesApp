package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import com.davidmcdeveloper.trainernotes10.R


@Composable
fun LoginScreen(
    navController: NavController,
    auth: FirebaseAuth,
    context: Context
) {
    val sharedPreferences = context.getSharedPreferences("TrainerNotesPrefs", Context.MODE_PRIVATE)

    var rememberMe by remember { mutableStateOf(sharedPreferences.getBoolean("remember_email", false)) }  // Checkbox para recordar el email
    var email by remember { mutableStateOf(TextFieldValue(sharedPreferences.getString("saved_email", "") ?: "")) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }  // Para mostrar/ocultar la contraseña

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Campo de email
        TextField(
            value = email,
            onValueChange = {
                email = it
                if (rememberMe) {
                    sharedPreferences.edit { putString("saved_email", it.text) } // Email guardado
                }
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña con visualización
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.visibilityoff else R.drawable.visibilityon),
                        contentDescription = "Mostrar/ocultar contraseña"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para iniciar sesión
        Button(
            onClick = { signInUser(auth, email.text, password.text, context, navController) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para registrarse
        Button(
            onClick = { registerUser(auth, email.text, password.text, context, navController) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Registrarse")
        }

        //Checkbox para recordar el email
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { isChecked ->
                    rememberMe = isChecked
                    sharedPreferences.edit {
                        putBoolean("remember_email", isChecked)
                        if (!isChecked) remove("saved_email") // Si se desmarca, eliminar email guardado
                    }
                }
            )
            Text("Recordar email")
        }

    }

}

fun signInUser(auth: FirebaseAuth, email: String, password: String, context: Context, navController: NavController) {
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
        return
    }

    if (password.length < 6) {
        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                // Redirige al usuario a Home y login desaparece
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
}

fun registerUser(auth: FirebaseAuth, email: String, password: String, context: Context, navController: NavController) {
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
        return
    }

    if (password.length < 6) {
        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()

                // Redirige al usuario a Home y login desaparece
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
}
//TODO: añadir más formas de registro
//TODO: personalizar interfaz de usuario
