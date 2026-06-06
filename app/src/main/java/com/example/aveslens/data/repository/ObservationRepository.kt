package com.example.aveslens.data.repository

import com.example.aveslens.data.model.Observation

interface ObservationRepository {
    suspend fun getObservations(userId: String): List<Observation>
}
