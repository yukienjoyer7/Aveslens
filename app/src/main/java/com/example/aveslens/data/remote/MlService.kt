package com.example.aveslens.data.remote

import com.example.aveslens.BuildConfig
import com.example.aveslens.data.model.MlPrediction
import com.example.aveslens.data.model.MlResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlService @Inject constructor(
    private val client: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = BuildConfig.AVESLENS_MODEL_URL

    suspend fun predict(imageBytes: ByteArray): MlPrediction {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaType()),
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/predict")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from ML API")

        if (!response.isSuccessful) {
            throw Exception("ML API error ${response.code}: $responseBody")
        }

        val parsed = json.decodeFromString<MlResponse>(responseBody)
        return parsed.predictions.firstOrNull()
            ?: throw Exception("No predictions returned")
    }
}
