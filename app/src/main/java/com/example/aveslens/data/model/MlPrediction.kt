package com.example.aveslens.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MlResponse(
    val predictions: List<MlPrediction>,
)

@Serializable
data class MlPrediction(
    val label: String,
    val confidence: Float,
)
