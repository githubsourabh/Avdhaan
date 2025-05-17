package com.avdhaan

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnboardingNav()
        }
    }
}

@Composable
fun OnboardingNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("permissions") { PermissionExplanationScreen(navController) }
        composable("done") { ConfirmationScreen() }
    }
}

@Composable
fun WelcomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Avdhaan", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your personal focus companion.")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.navigate("permissions") }) {
            Text("Get Started")
        }
    }
}

@Composable
fun PermissionExplanationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val hasPermission = remember { mutableStateOf(checkUsageStatsPermission(context)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Why we need Usage Access", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("To track your app usage and help block distractions, we need access to your usage stats.", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))

        if (!hasPermission.value) {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }) {
                Text("Grant Access")
            }
        } else {
            Button(onClick = {
                navController.navigate("done")
            }) {
                Text("Continue")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ClickableText(
            text = AnnotatedString("Already granted? Tap here to refresh."),
            onClick = { hasPermission.value = checkUsageStatsPermission(context) }
        )
    }
}

@Composable
fun ConfirmationScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("You're all set!", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Avdhaan is now ready to track your app usage in the background.", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            context.startActivity(Intent(context, MainActivity::class.java))
        }) {
            Text("Enter App")
        }
    }
}

@Suppress("DEPRECATION")
fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
