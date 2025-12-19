package com.example.mysterybox.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LineAuthRequest(
    val code: String,
    val state: String,
    @SerialName("redirect_uri")
    val redirectUri: String
)

@Serializable
data class LineAuthResponse(
    val success: Boolean,
    val session: AuthSession? = null,
    val error: String? = null,
    @SerialName("error_code")
    val errorCode: String? = null
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

