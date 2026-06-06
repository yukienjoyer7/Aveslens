package com.example.aveslens.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Observation(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("species_name") val speciesName: String? = null,
    @SerialName("confidence_score") val confidenceScore: Float? = null,
    val location: String? = null,
    val note: String? = null,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)
