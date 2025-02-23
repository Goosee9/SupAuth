package com.example.supauthppp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.supauthppp.data.model.UserState
import com.example.supauthppp.ui.theme.BlueButton
import com.example.supauthppp.ui.theme.SupAuthpppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupAuthpppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: SupabaseAuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController, viewModel)
        }
        composable("second") {
            SecondScreen(navController, viewModel)
        }
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: SupabaseAuthViewModel
) {
    val context = LocalContext.current
    val userState by viewModel.userState

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }

    // Automatically check if the user is logged in when the screen is launched
    LaunchedEffect(Unit) {
        viewModel.isUserLoggedIn(context)
    }

    // Handle user state changes
    val currentUserState = when (userState) {
        UserState.Original -> "Welcome! Please log in or sign up." // Default message for Original state
        is UserState.Loading -> "Loading..."
        is UserState.Success -> {
            // Navigate to SecondScreen on successful login
            LaunchedEffect(userState) {
                navController.navigate("second")
            }
            (userState as UserState.Success).message
        }
        is UserState.Error -> (userState as UserState.Error).message
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email Input
        OutlinedTextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text("Enter email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            label = { Text("Enter password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { viewModel.login(context, userEmail, userPassword) },
            colors = ButtonDefaults.buttonColors(containerColor = BlueButton),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Up Prompt
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Вы впервые? ")
            TextButton(onClick = { viewModel.signUp(context, userEmail, userPassword) }) {
                Text(text = "Создать пользователя")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display User State
        if (currentUserState.isNotEmpty()) {
            Text(
                text = currentUserState,
                color = if (userState is UserState.Error) Color.Red else Color.Unspecified
            )
        }
    }
}