package com.example.mysterybox.data.dto

import com.example.mysterybox.data.model.AuthSession
import com.example.mysterybox.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LineTokenVerifyRequest(
    @SerialName("line_access_token")
    val lineAccessToken: String
)

@Serializable
data class AuthResponseDto(
    val success: Boolean,
    val session: SessionInfoDto? = null,
    val error: String? = null
)

@Serializable
data class SessionInfoDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    val user: UserResponseDto
)

@Serializable
data class UserResponseDto(
    val id: String,
    val lineUserId: String?,
    val displayName: String,
    val pictureUrl: String?,
    val createdAt: String
)

@Serializable
data class RefreshTokenRequestDto(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponseDto(
    val success: Boolean,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Long? = null,
    val error: String? = null
)

@Serializable
data class ErrorResponseDto(
    val success: Boolean = false,
    val error: String
)

fun UserResponseDto.toDomain(): User = User(
    id = id,
    lineUserId = lineUserId,
    displayName = displayName,
    pictureUrl = pictureUrl,
    createdAt = createdAt
)

fun SessionInfoDto.toDomain(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    user = user.toDomain()
)
