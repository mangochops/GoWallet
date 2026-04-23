package com.example.helatrack.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BusinessProfile(
    @SerialName("business_name")
    val businessName: String,

    @SerialName("provider_type")
    val providerType: String,

    @SerialName("identifier_hash")
    val identifierHash: String,

    @SerialName("user_id")
    val userId: String // Or use java.util.UUID if you have a custom serializer
)