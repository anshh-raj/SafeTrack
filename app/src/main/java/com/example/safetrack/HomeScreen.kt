package com.example.safetrack

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.safetrack.ui.theme.locationTracker.ForegroundService

@Composable
fun HomeScreen(context: Context, innerPadding: PaddingValues){

    var hasPermissions by remember { mutableStateOf(false) }

    var isServiceRunning by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.all { it.value }
    }

    LaunchedEffect(Unit) {
        hasPermissions = hasPermission(context)
        if (!hasPermissions) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Button(
            onClick = {
                if (!isServiceRunning) {

                    ForegroundService.startService(context)
                    isServiceRunning = true

                } else {

                    ForegroundService.stopService(context)
                    isServiceRunning = false
                }
            },
            enabled = hasPermissions,
            modifier = Modifier.size(150.dp)
        ) {
            Text(
                text = if (isServiceRunning) "Stop SOS" else "Start SOS",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun hasPermission(context: Context): Boolean {
    return ( ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}