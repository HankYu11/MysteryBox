package com.example.mysterybox.data.network

object ApiConfig {
    // Backend API URL - Replace with your actual backend URL
    const val BASE_URL = "https://api.mysterybox.example.com"

    // LINE OAuth configuration
    const val LINE_AUTH_URL = "https://access.line.me/oauth2/v2.1/authorize"

    // LINE Channel ID - Replace with your actual Channel ID from LINE Developers Console
    const val LINE_CHANNEL_ID = "YOUR_LINE_CHANNEL_ID"

    // OAuth redirect URI - must match LINE Developer Console configuration
    const val REDIRECT_URI = "mysterybox://auth/callback"

    // OAuth scopes - profile and openid for basic user info
    const val LINE_SCOPE = "profile openid"
}
