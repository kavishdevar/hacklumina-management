@file:Suppress("unused")

package me.kavishdevar.hacklumina

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class AirtableTable(
    val id: String,
    var name: String,
    var fields: List<AirtableField>,
    val base: AirtableBase,
    var records: List<AirtableRecord>
) {
    fun getRecordByField(fieldName: String, fieldValue: String): AirtableRecord? {
        println(records.find { it.fields[getFieldByName(fieldName)!!.id] == fieldValue }?.fields["Participant Name (from Participant)"] ?: "Record not found")
        return records.find { it.fields[getFieldByName(fieldName)!!.id] == fieldValue }
    }
    fun getFieldById(id: String): AirtableField? {
        return fields.find { it.id == id }
    }
    fun getFieldByName(name: String): AirtableField? {
        return fields.find { it.name == name }
    }
    fun getFieldIdByName(name: String): String? {
        return getFieldByName(name)?.id
    }
    fun getPrimaryField(): AirtableField? {
        return fields.find { it.isPrimary == true }
    }
    fun getPrimaryFieldName(): String? {
        return getPrimaryField()?.name
    }
    fun updateRecordFieldByField(lookupFieldName: String, lookupFieldValue: String, fieldName: String, value: Any) {
        val record = records.find { it.fields[getFieldByName(lookupFieldName)!!.id] == lookupFieldValue }
        if (record != null) {
            updateRecordField(fieldName, value.toString(), record.id)
        } else {
            Log.e("Airtable", "Record not found")
        }
    }

    fun updateRecord(record: AirtableRecord) {
        val client = OkHttpClient()
        val baseId = base.id
        val apiKey = base.apiKey
        val tableName = name
        val url = "https://api.airtable.com/v0/$baseId/$tableName/${record.id}"
        val computedTypes = listOf<AirtableFieldType>(
            AirtableFieldType.AUTONUMBER,
            AirtableFieldType.CREATED_BY,
            AirtableFieldType.CREATED_TIME,
            AirtableFieldType.LAST_MODIFIED_BY,
            AirtableFieldType.LAST_MODIFIED_TIME
        )
        val bodyStr = """
            {
                "fields": {
                    ${record.fields.filter { !computedTypes.contains(getFieldById(it.key)?.type) && !it.value.toString().startsWith("[") && !it.value.toString().endsWith("]") }.map { (key, value) -> "\"${getFieldById(key)?.name}\": \"${value}\"" }.joinToString(",\n")}
                }
            }
        """
        Log.d("Airtable", "Updating record with body: $bodyStr")
        val body = bodyStr.trimIndent().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .patch(body)
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d("Airtable", "Record updated successfully")
        } else {
            Log.e("Airtable", "Error updating record: ${response.body.string()}")
        }
    }

    fun updateRecordField(changedFieldName: String, changedFieldValue : String, id: String) {
        val client = OkHttpClient()
        val baseId = base.id
        val apiKey = base.apiKey
        val tableName = name
        val url = "https://api.airtable.com/v0/$baseId/$tableName/$id"
        val bodyStr = """
            {
                "fields": {
                    "$changedFieldName": "$changedFieldValue"
                }
            }
        """.trimIndent()
        val body = bodyStr.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .patch(body)
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Record updated successfully")
        } else {
            println("Error updating record: ${response.body.string()}")
        }
    }
    fun createRecord(fields: Map<String, Any>) {
        val client = OkHttpClient()
        val baseId = base.id
        val apiKey = base.apiKey
        val tableName = name
        val url = "https://api.airtable.com/v0/$baseId/$tableName"
        val bodyStr = """
            {
                "fields": {
                    ${fields.map { (key, value) -> "\"$key\": \"$value\"" }.joinToString(",\n")}
                }
            }
        """.trimIndent()
        Log.d("Airtable", "Creating record with body: $bodyStr")
        val body = bodyStr.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Record created successfully")
        } else {
            println("Error creating record: ${response.body.string()}")
        }
    }
}

enum class AirtableFieldType(val value: String) {
    LINK_TO_RECORD("multipleRecordLinks"),
    SINGLE_LINE_TEXT("singleLineText"),
    MULTI_LINE_TEXT("multilineText"), // WHY IS THIS NOT multi*L*ineText
    MULTIPLE_ATTACHMENTS("multipleAttachments"),
    ATTACHMENT("attachment"),
    CHECKBOX("checkbox"),
    MULTIPLE_SELECT("multipleSelects"),
    SINGLE_SELECT("singleSelect"),
    USER("user"),
    DATE("date"),
    PHONE_NUMBER("phoneNumber"),
    EMAIL("email"),
    URL("url"),
    NUMBER("number"),
    CURRENCY("currency"),
    PERCENT("percent"),
    DURATION("duration"),
    RATING("rating"),
    FORMULA("formula"),
    ROLLUP("rollup"),
    COUNT("count"),
    LOOKUP("lookup"),
    CREATED_TIME("createdTime"),
    LAST_MODIFIED_TIME("lastModifiedTime"),
    CREATED_BY("createdBy"),
    LAST_MODIFIED_BY("lastModifiedBy"),
    AUTONUMBER("autoNumber"),
    MULTIPLE_LOOKUP_VALUES("multipleLookupValues"),
    BARCODE("barcode"),
    BUTTON("button");
    companion object {
        private val map = AirtableFieldType.entries.associateBy(AirtableFieldType::value)
        fun fromValue(value: String) = map[value]
    }
}

data class AirtableSelectFieldOption(
    val id: String,
    val name: String,
    val color: String
)

data class AirtableField(
    val isPrimary: Boolean,
    val id: String,
    val name: String,
    val type: AirtableFieldType,
    val options: List<AirtableSelectFieldOption>? = null,
    val multipleRecordLinksField: AirtableMultipleRecordLinksField? = null,
    val table: AirtableTable
) {
    fun json(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("type", type.value)
        if (type == AirtableFieldType.SINGLE_SELECT || type == AirtableFieldType.MULTIPLE_SELECT) {
            val optionsArray = options?.map { option ->
                JSONObject().apply {
                    put("id", option.id)
                    put("name", option.name)
                    put("color", option.color)
                }
            }
            json.put("options", optionsArray)
        }
        if (type == AirtableFieldType.LINK_TO_RECORD) {
            json.put("linkedTableId", multipleRecordLinksField?.linkedTableId)
            json.put("isReversed", multipleRecordLinksField?.isReversed)
            json.put("prefersSingleRecordLink", multipleRecordLinksField?.prefersSingleRecordLink)
            json.put("inverseLinkFieldId", multipleRecordLinksField?.inverseLinkFieldId)
        }
        return json
    }
}
data class AirtableMultipleRecordLinksField(
    val id: String,
    val name: String,
    val linkedTableId: String,
    val isReversed: Boolean,
    val prefersSingleRecordLink: Boolean,
    val inverseLinkFieldId: String,
    val table: AirtableTable
)

data class AirtableRecord(
    val id: String,
    val fields: MutableMap<String, Any>,
    val table: AirtableTable
)

data class AirtableBase (
    val id: String,
    var name: String,
    var tables: List<AirtableTable>,
    val apiKey: String
) {
    fun addTable(table: AirtableTable) {
        tables += table
        val client = OkHttpClient()
        
    }
}
