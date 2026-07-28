package com.example.travelplanner.data.repo

import com.example.travelplanner.util.Supabase
import io.github.jan.supabase.storage.storage
import java.io.File

sealed class StorageResult {
    data class Success(val publicUrl: String) : StorageResult()
    data class Error(val message: String) : StorageResult()
}

class StorageRepository {

    /**
     * Uploads a ByteArray file (e.g. image from camera/gallery) to a Supabase Storage bucket.
     * @param bucketName Name of the storage bucket (e.g., "trip-photos", "avatars")
     * @param path Target path in bucket (e.g., "user_123/avatar.jpg" or "trips/trip_456/cover.jpg")
     * @param bytes ByteArray image content
     */
    suspend fun uploadFile(bucketName: String, path: String, bytes: ByteArray): StorageResult {
        return try {
            val bucket = Supabase.client.storage.from(bucketName)
            bucket.upload(path, bytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(path)
            StorageResult.Success(publicUrl)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Failed to upload image")
        }
    }

    /**
     * Uploads a local File to a Supabase Storage bucket.
     */
    suspend fun uploadFile(bucketName: String, path: String, file: File): StorageResult {
        return try {
            val bytes = file.readBytes()
            uploadFile(bucketName, path, bytes)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Failed to read or upload file")
        }
    }

    /**
     * Deletes a file from a Supabase Storage bucket.
     */
    suspend fun deleteFile(bucketName: String, path: String): Boolean {
        return try {
            val bucket = Supabase.client.storage.from(bucketName)
            bucket.delete(listOf(path))
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Gets the public URL of an existing file in a Supabase Storage bucket.
     */
    fun getPublicUrl(bucketName: String, path: String): String {
        return try {
            Supabase.client.storage.from(bucketName).publicUrl(path)
        } catch (_: Exception) {
            ""
        }
    }
}