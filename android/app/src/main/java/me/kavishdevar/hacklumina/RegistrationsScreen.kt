package me.kavishdevar.hacklumina

import android.app.Activity
import android.content.Intent
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.MessageDigest

fun writeNfcTag(tag: Tag, tagId: String, recordId: String, toRecompose: MutableState<Boolean>): Boolean {
    val ndefMessage = NdefMessage(arrayOf(
        NdefRecord.createTextRecord("en", tagId)
    ))

    try {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            if (!ndef.isWritable) {
                Log.e("NFC", "Tag is not writable")
                return false
            }
            if (ndef.maxSize < ndefMessage.toByteArray().size) {
                Log.e("NFC", "Tag size is too small")
                return false
            }
            ndef.writeNdefMessage(ndefMessage)
            CoroutineScope(Dispatchers.IO).launch {
                val client = OkHttpClient()
                val passwordHash = getHash("REDACTED")
                val password = passwordHash.joinToString("") { "%02x".format(it) }
                val json = """
                                {
                                    "status": "Confirmed",
                                    "tag-id": "$tagId"
                                }
                            """.trimIndent()
                Log.d("RegistrationsScreen", "JSON: $json")
                val request = Request.Builder()
                    .url("http://192.168.1.90:8080/update-registration-status/${recordId}")
                    .addHeader("password", password)
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d("RegistrationsScreen", "Record updated successfully")
                    AirtableAPI.fetchRecords(AirtableAPI.bases.find { base -> base.name == "Hacklumina" }?.tables?.find { table -> table.name == "Check-ins" }!!)
                    AirtableAPI.fetchRecords(AirtableAPI.bases.find { base -> base.name == "Hacklumina" }?.tables?.find { table -> table.name == "Participants" }!!)
                    toRecompose.value = true
                    ndef.close()
                } else {
                    Log.e("RegistrationsScreen", "Error updating record: ${response.body.string()}")
                }
            }
            ndef.close()
            return true
        } else {
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                ndefFormatable.connect()
                ndefFormatable.format(ndefMessage)
                ndefFormatable.close()
                return true
            } else {
                Log.e("NFC", "Tag does not support NDEF")
                return false
            }
        }
    } catch (e: IOException) {
        Log.e("NFC", "IOException while writing NFC tag", e)
    } catch (e: FormatException) {
        Log.e("NFC", "FormatException while writing NFC tag", e)
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationsScreen(activity: Activity) {
    var tagId by remember { mutableStateOf("") }
    var recordId by remember { mutableStateOf("") }
    var dialogShown by remember { mutableStateOf(false) }
    val toRecompose = remember { mutableStateOf(true) }

    if (dialogShown) {
        BasicAlertDialog(
            onDismissRequest = { dialogShown = false },
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.3f)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("writing $tagId to the NFC card, waiting to scan it...")
                val tag = remember { mutableStateOf<Tag?>(null) }
                val intent = Intent(LocalContext.current, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_FROM_BACKGROUND)
                val nfcAdapter = NfcAdapter.getDefaultAdapter(LocalContext.current)
                nfcAdapter?.enableReaderMode(activity, { tag ->
                    tag?.let {
                        writeNfcTag(it, tagId, recordId, toRecompose)
                    }
                }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B, null)

                if (tag.value != null) {
                    if (writeNfcTag(tag.value!!, tagId, recordId, toRecompose)) {
                        Text("Tag written successfully")
                    } else {
                        Text("Failed to write tag")
                    }
                }
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
        ) {
            if (toRecompose.value) {
                toRecompose.value = false
                val table = AirtableAPI.bases.find { base -> base.name == "Hacklumina" }?.tables?.find { table -> table.name == "Participants" }
                table?.records?.let { records ->
                    items(records.size) { recordIndex ->
                        val record = records[recordIndex]
                        ElevatedCard (
                           modifier = Modifier
                               .fillMaxWidth()
                               .padding(4.dp),
                        ) {
                           Column(
                               modifier = Modifier
                                   .fillMaxWidth()
                                   .padding(16.dp)
                           ) {
                               Text(text = record.fields[table.getFieldIdByName("Participant Name")].toString())
                               Text(text = record.fields[table.getFieldIdByName("Email")].toString())
                               Text(text = record.fields[table.getFieldIdByName("Contact Number")].toString())
                               Text(text = record.fields[table.getFieldIdByName("Registration Status")].toString())
                               OutlinedButton (
                                   onClick = {
                                       record.fields[table.getFieldIdByName("Registration Status")!!] = "Confirmed"
                                       recordId = record.id
                                       tagId = (0..9).map { ('a'..'z').random() }.joinToString("")
                                       dialogShown = true
                                   },
                                   modifier = Modifier
                                       .fillMaxWidth()
                               ) {
                                   Text("Approve (and add to check-ins)")
                               }
                           }
                       }
                    }
                }
            }
    }
}

fun getHash(string: String): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(string.toByteArray())
}