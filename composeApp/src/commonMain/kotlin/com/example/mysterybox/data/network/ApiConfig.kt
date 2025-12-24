package com.example.mysterybox.data.network

object ApiConfig {
    // Backend API URL - Use 10.0.2.2 for Android emulator to reach host localhost
    const val BASE_URL = "http://10.70.169.80:8080"

    // LINE SDK Configuration
    const val LINE_CHANNEL_ID = "2008724728" // Replace with your actual LINE Channel ID

    // Auth endpoints
    const val AUTH_LINE_VERIFY = "/api/auth/line/verify-token"  // Verify LINE access token (LINE SDK flow)
    const val AUTH_REFRESH = "/api/auth/refresh"
    const val AUTH_LOGOUT = "/api/auth/logout"
    const val AUTH_ME = "/api/auth/me"  // Get current user info and verify token

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
}
