package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    auth: FirebaseAuth,
    context: Context
) {
    val sharedPreferences = context.getSharedPreferences("TrainerNotesPrefs", Context.MODE_PRIVATE)

    var rememberMe by remember { mutableStateOf(sharedPreferences.getBoolean("remember_email", false)) }
    var email by remember { mutableStateOf(TextFieldValue(sharedPreferences.getString("saved_email", "") ?: "")) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }
    val localContext = LocalContext.current
    //Variable para guardar el error del email
    var emailError by remember { mutableStateOf(false) }
    //Variable para guardar el error de la contraseña
    var passwordError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.backgroundlogin), // Reemplaza con el nombre de tu imagen
            contentDescription = "Background Login",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(top = 100.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.trainernoteslogin),
                contentDescription = "Trainer Notes Logo",
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Inside,

                )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (rememberMe) {
                            sharedPreferences.edit { putString("saved_email", it.text) }
                        }
                        emailError = !Patterns.EMAIL_ADDRESS.matcher(it.text).matches()
                    },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    isError = emailError
                )
                if (emailError) {
                    Text(text = "Introduce un email válido.", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(25.dp))

                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = it.text.length < 6
                    },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f),
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
                    },
                    isError = passwordError
                )
                if (passwordError) {
                    Text(text = "La contraseña debe tener al menos 6 caracteres", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { signInUser(auth, email.text, password.text, localContext, navController) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !emailError && !passwordError && email.text.isNotEmpty() && password.text.isNotEmpty()
                ) {
                    Text(text = "Iniciar sesión")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate(Screen.Register.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Registrarse")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { isChecked ->
                            rememberMe = isChecked
                            sharedPreferences.edit {
                                putBoolean("remember_email", isChecked)
                                if (!isChecked) remove("saved_email")
                            }
                        }
                    )
                    Text(
                        text = "Recordar email",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

fun signInUser(auth: FirebaseAuth, email: String, password: String, context: Context, navController: NavController) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    if (email == "admin@trainernotes.com") {
                        // Es el administrador, se permite el acceso sin verificar
                        Toast.makeText(context, "Bienvenido administrador", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else if (user.isEmailVerified) {
                        // El correo está verificado, se permite el inicio de sesión
                        Toast.makeText(context, "Usuario logeado correctamente", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        // El correo no está verificado y no se permite el inicio de sesión
                        Toast.makeText(context, "Por favor, verifica tu correo electrónico", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
}