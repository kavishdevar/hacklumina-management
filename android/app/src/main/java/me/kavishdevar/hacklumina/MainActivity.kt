package me.kavishdevar.hacklumina

import android.annotation.SuppressLint
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.kavishdevar.hacklumina.ui.theme.HackluminaCheckinTheme

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    @SuppressLint("UnsafeIntentLaunch")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        AirtableAPI.init()
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "registrations") {
                composable("registrations") {
                    RegistrationsScreen(this@MainActivity)
                }
                composable("checkin") {
                    NFCCheckInScreen(this@MainActivity)
                }
            }
            val items = listOf("Registrations", "Check In")
            var selectedItem by remember { mutableIntStateOf(0) }
            val selectedIcons = listOf(Icons.Filled.Person, Icons.Filled.CheckCircle)
            val unselectedIcons = listOf(Icons.Outlined.Person, Icons.Outlined.CheckCircle)
            if (selectedItem == 0) {
                navController.navigate("registrations")
            }
            else {
                navController.navigate("checkin")
            }
            HackluminaCheckinTheme {
                Scaffold (
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "Hacklumina Check-in",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                // restart activity
                                finish()
                                AirtableAPI.reInit()
                                startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Restart")
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                                            contentDescription = item
                                        )
                                    },
                                    label = { Text(item) },
                                    selected = selectedItem == index,
                                    onClick = { selectedItem = index }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedItem == 0) {
                            RegistrationsScreen(this@MainActivity)
                        } else {
                            NFCCheckInScreen(this@MainActivity)
                        }
                    }
                }
            }
        }
    }
}