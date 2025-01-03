@file:Suppress("unused")

package me.kavishdevar.hacklumina

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object AirtableAPI {

    fun reInit() {
        initialized = false
        bases = emptyList()
        tables = emptyList()
        init(apiKey)
    }

    private lateinit var apiKey: String
    private var initialized = false
    fun init(apiKey: String = "REDACTED")  {
        CoroutineScope(Dispatchers.IO).launch {
            this@AirtableAPI.apiKey = apiKey
            fetchBases()
            fetchTables()
            bases.forEach { base ->
                Log.d("AirtableAPI", "Fetching records for base ${base.name}")
                CoroutineScope(Dispatchers.IO).launch {
                    base.tables.forEach { table ->
                        Log.d("AirtableAPI", "Fetching records for table ${table.name}")
                        CoroutineScope(Dispatchers.IO).launch {
                            fetchRecords(table)
                        }
                    }
                }
            }
            initialized = true
        }
    }

    fun waitForInitialization() {
        while (!initialized) {
            Thread.sleep(100)
        }
    }

    var bases = emptyList<AirtableBase>()
    private lateinit var tables: List<AirtableTable>

    fun fetchBases() {
        val client = OkHttpClient()
        val url = "https://api.airtable.com/v0/meta/bases"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val body = response.body.string()
            val json = JSONObject(body)
            val basesJSON = json.optJSONArray("bases")
            if (basesJSON != null) {
                for (i in 0 until basesJSON.length()) {
                    val base = basesJSON.getJSONObject(i)
                    val name = base.optString("name", "N/A")
                    val id = base.getString("id")
                    val existingBase = bases.find { it.id == id }
                    if (existingBase != null) {
                        existingBase.name = name
                    } else {
                        bases += AirtableBase(
                            id = id,
                            name = name,
                            tables = emptyList<AirtableTable>(),
                            apiKey = apiKey
                        )
                    }
                }
            }
        }
    }
    fun fetchTables() {
        val client = OkHttpClient()
        bases.forEach { base ->
            val url = "https://api.airtable.com/v0/meta/bases/${base.id}/tables"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body.string()
                val tables = JSONObject(body).optJSONArray("tables")
                if (tables != null) {
                    for (i in 0 until tables.length()) {
                        val tableJSON = tables.getJSONObject(i)
                        val tableId = tableJSON.getString("id")
                        val tableName = tableJSON.getString("name")
                        val existingTable = base.tables.find { it.id == tableId }
                        val table = existingTable ?: AirtableTable(tableId, tableName, listOf(), base, listOf())
                        table.name = tableName
                        val fields = mutableListOf<AirtableField>()
                        val fieldsArray = tableJSON.optJSONArray("fields")
                        if (fieldsArray != null) {
                            for (j in 0 until fieldsArray.length()) {
                                val field = fieldsArray.getJSONObject(j)
                                val fieldId = field.getString("id")
                                val fieldName = field.getString("name")
                                val fieldTypeString = field.getString("type")
                                val fieldType = AirtableFieldType.fromValue(fieldTypeString)
                                val fieldOptions = field.optJSONObject("options")
                                val options = mutableListOf<AirtableSelectFieldOption>()

                                if (fieldType == AirtableFieldType.SINGLE_SELECT || fieldType == AirtableFieldType.MULTIPLE_SELECT) {
                                    fieldOptions?.keys()?.forEach { key ->
                                        try {
                                            val value = fieldOptions.get(key)
                                            if (value is JSONObject) {
                                                val choiceId = value.getString("id")
                                                val choiceName = value.getString("name")
                                                val choiceColor = value.getString("color")
                                                options.add(
                                                    AirtableSelectFieldOption(
                                                        choiceId,
                                                        choiceName,
                                                        choiceColor
                                                    )
                                                )
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                fields.add(
                                    AirtableField(
                                        isPrimary = fieldId == tableJSON.getString("primaryFieldId"),
                                        id = fieldId,
                                        name = fieldName,
                                        type = fieldType!!,
                                        options = options,
                                        table = table
                                    )
                                )
                            }
                        }
                        table.fields = fields
                        if (existingTable == null) {
                            base.tables += table
                        }
                    }
                }
            } else {
                Log.e("AirtableAPI", "Failed to fetch tables: ${response.body.string()}")
            }
        }
    }

    fun fetchRecords(table: AirtableTable) {
        val client = OkHttpClient()
        val url = "https://api.airtable.com/v0/${table.base.id}/${table.id}"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val body = response.body.string()
            val records = JSONObject(body).optJSONArray("records")
            if (records != null) {
                for (i in 0 until records.length()) {
                    val recordJSON = records.getJSONObject(i)
                    val recordId = recordJSON.getString("id")
                    val fields = mutableMapOf<String, Any>()
                    recordJSON.optJSONObject("fields")?.keys()?.forEach { key ->
                        val value = recordJSON.getJSONObject("fields").get(key)
                        fields[table.getFieldByName(key)!!.id] = value
                    }
                    val existingRecord = table.records.find { it.id == recordId }
                    if (existingRecord != null) {
                        existingRecord.fields.putAll(fields)
                    } else {
                        table.records += AirtableRecord(recordId, fields, table)
                    }
                }
            }
            Log.d("AirtableAPI", "Fetched ${table.records.size} records for table ${table.name}")
        } else {
            Log.e("AirtableAPI", "Failed to fetch records: ${response.body.string()}")
        }
    }
}