package com.example.aveslens.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuditLog(
    val id: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("changed_by") val changedBy: String = "",
    @SerialName("table_name") val tableName: String = "",
    val action: String = "",
    @SerialName("old_data") val oldData: String? = null,
    @SerialName("new_data") val newData: String? = null,
    @SerialName("bird_table_id") val birdTableId: String? = null,
)
