package com.example.mysterybox.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    @SerialName("line_user_id")
    val lineUserId: String? = null,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("picture_url")
    val pictureUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AuthSession(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Long,
    val user: User
)
