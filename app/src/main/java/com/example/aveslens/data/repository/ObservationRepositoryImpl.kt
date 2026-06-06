package com.example.aveslens.data.repository

import com.example.aveslens.data.model.Observation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class ObservationRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : ObservationRepository {

    override suspend fun getObservations(userId: String): List<Observation> {
        return supabase.from("bird_observation")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }
}
