package com.example.mysterybox.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Welcome

@Serializable
object Login

@Serializable
object Home

@Serializable
data class BoxDetail(val boxId: String)

@Serializable
object MyReservations

@Serializable
object Profile

@Serializable
object MerchantLogin

@Serializable
object MerchantDashboard

@Serializable
object UploadBox
