package me.kavishdevar.hacklumina

import android.app.Activity
import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.nio.charset.Charset

enum class ScanStatus {
    WAITING, CHECKING_IN, CHECKED_IN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCCheckInScreen(activity: Activity) {
    var scanStatus by remember { mutableStateOf(ScanStatus.WAITING) }

    var selectedField by remember { mutableStateOf("") }
    val markAsNotCheckedIn = remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val fieldNamesToIgnore = listOf(
            "Attendee ID",
            "Participant",
            "RegistrationID (from Participant)",
            "Participant (from Participant)",
            "Participant Name (from Participant)",
            "Tag ID",
            "Registration Status (from Participant)",
            "Check-in Time"
        )
        AirtableAPI.waitForInitialization()
        var menuItems =
            AirtableAPI.bases.find { it.name == "Hacklumina" }?.tables?.find { it.name == "Check-ins" }?.fields?.filter {
                !fieldNamesToIgnore.contains(it.name)
            }?.map { it.name } ?: emptyList()
        val menuExpanded = remember { mutableStateOf(false) }
        Text(text = "Select the field to check in...")
        Box(modifier = Modifier.wrapContentSize(Alignment.Center)) {
            Button(
                onClick = { menuExpanded.value = true },
            ) {
                Text(text = if (selectedField.isEmpty()) "Select Field" else selectedField)
            }
            DropdownMenu(
                expanded = menuExpanded.value,
                onDismissRequest = { menuExpanded.value = false }
            ) {
                menuItems.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            selectedField = item
                            menuExpanded.value = false
                        },
                        text = { Text(text = item) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = markAsNotCheckedIn.value,
                onCheckedChange = { markAsNotCheckedIn.value = it },
            )
            Text(
                text = "Mark as not checked in",
                modifier = Modifier.clickable(onClick = {
                    markAsNotCheckedIn.value = !markAsNotCheckedIn.value
                })
            )
        }
        when (scanStatus) {
            ScanStatus.WAITING -> {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Waiting for NFC tag",
                    tint = Color.Gray
                )
                Text(text = "Waiting for NFC tag...")
            }
            ScanStatus.CHECKING_IN -> {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Checking in",
                    modifier = Modifier
                        .size(48.dp)
                )
                Text(text = "Checking in...")
            }
            ScanStatus.CHECKED_IN -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Checked in",
                    tint = Color.Green,
                    modifier = Modifier
                        .size(48.dp)
                )
                Text(text = "Checked in $name for $selectedField!")
            }
        }
        Button(
            onClick = {
                activity.startLockTask()
            }
        ) {
            Text("Enter Kiosk Mode")
        }
        val intent = Intent(LocalContext.current, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND)
        val nfcAdapter = NfcAdapter.getDefaultAdapter(LocalContext.current)
        nfcAdapter?.enableReaderMode(activity, { tag ->
            tag?.let {
                val ndef = Ndef.get(it)
                if (ndef != null) {
                    ndef.connect()
                    val ndefMessage = ndef.ndefMessage
                    val records = ndefMessage.records
                    for (record in records) {
                        if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(
                                NdefRecord.RTD_TEXT
                            )
                        ) {
                            val payload = record.payload
                            val textEncoding =
                                if ((payload[0].toInt() and 0x80) == 0) Charset.forName("UTF-8") else Charset.forName(
                                    "UTF-16"
                                )
                            val languageCodeLength = payload[0].toInt() and 0x3F
                            val text = String(
                                payload,
                                languageCodeLength + 1,
                                payload.size - languageCodeLength - 1,
                                textEncoding
                            )
                            scanStatus = ScanStatus.CHECKING_IN
                            AirtableAPI.bases.find { it.name == "Hacklumina" }?.tables?.find { it.name == "Check-ins" }?.updateRecordFieldByField("Tag ID", text, selectedField, if (markAsNotCheckedIn.value) "Not Checked In" else "Checked In")
                            name = AirtableAPI.bases.find { it.name == "Hacklumina" }?.tables?.find { it.name == "Check-ins" }?.getRecordByField("Tag ID", text)?.fields?.get("Participant Name (from Participant)") as String
                            scanStatus = ScanStatus.CHECKED_IN
                            Log.d("NFC", "NFC Tag: $text")
                        }
                    }
                    ndef.close()
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B, null)
    }
}