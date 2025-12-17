package com.example.mysterybox.data.network

object ApiConfig {
    // Backend API URL - Use 10.0.2.2 for Android emulator to reach host localhost
    const val BASE_URL = "http://10.0.2.2:8080"

    // Auth endpoints
    const val AUTH_LINE = "/api/auth/line"
    const val AUTH_REFRESH = "/api/auth/refresh"
    const val AUTH_LOGOUT = "/api/auth/logout"

    // Box endpoints
    const val BOXES = "/api/boxes"
    fun boxDetail(id: String) = "/api/boxes/$id"

    // Reservation endpoints
    const val RESERVATIONS = "/api/reservations"
    fun cancelReservation(id: String) = "/api/reservations/$id"

    // Merchant endpoints
    const val MERCHANT_LOGIN = "/api/merchant/login"
    const val MERCHANT_BOXES = "/api/merchant/boxes"

    // LINE OAuth configuration
    const val LINE_AUTH_URL = "https://access.line.me/oauth2/v2.1/authorize"

    // LINE Channel ID - Replace with your actual Channel ID from LINE Developers Console
    const val LINE_CHANNEL_ID = "YOUR_LINE_CHANNEL_ID"

    // OAuth redirect URI - must match LINE Developer Console configuration
    const val REDIRECT_URI = "mysterybox://auth/callback"

    // OAuth scopes - profile and openid for basic user info
    const val LINE_SCOPE = "profile openid"
}
