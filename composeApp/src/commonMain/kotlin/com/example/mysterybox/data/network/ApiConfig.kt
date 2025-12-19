package com.example.mysterybox.data.network

object ApiConfig {
    // Backend API URL - Use 10.0.2.2 for Android emulator to reach host localhost
    const val BASE_URL = "http://192.168.0.59:8080"

    // LINE OAuth Configuration
    const val LINE_CHANNEL_ID = "2008724728" // Replace with your actual LINE Channel ID
    const val LINE_AUTH_URL = "https://access.line.me/oauth2/v2.1/authorize"
    const val LINE_OAUTH_STATE = "mystery_box_state"

    // Auth endpoints
    const val AUTH_LINE = "/api/auth/line"
    const val AUTH_REDIRECT = "/api/auth/line/callback"  // Backend handles LINE callback
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
    const val MERCHANT_DASHBOARD = "/api/merchant/dashboard"
    const val MERCHANT_ORDERS = "/api/merchant/orders"
    fun merchantOrderDetail(id: String) = "/api/merchant/orders/$id"
    fun merchantOrderVerify(id: String) = "/api/merchant/orders/$id/verify"
    fun merchantOrderCancel(id: String) = "/api/merchant/orders/$id/cancel"

    // OAuth redirect URI - Must be HTTP/HTTPS for LINE OAuth
    // Backend will handle the callback and redirect to app with session token
    const val REDIRECT_URI = "$BASE_URL$AUTH_REDIRECT"
}
