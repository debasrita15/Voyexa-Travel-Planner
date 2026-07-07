package com.example.travelplanner.services

import android.content.Context
import android.net.Uri
import com.example.travelplanner.domain.TripExportPayload
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class JsonShareService(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun writeTripPayload(payload: TripExportPayload, outputStream: OutputStream) {
        val text = json.encodeToString(TripExportPayload.serializer(), payload)
        outputStream.write(text.toByteArray())
    }

    fun readTripPayload(inputStream: InputStream): TripExportPayload {
        val text = inputStream.readBytes().toString(Charsets.UTF_8)
        return json.decodeFromString(TripExportPayload.serializer(), text)
    }
}
