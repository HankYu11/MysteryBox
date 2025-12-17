package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantDashboard
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MerchantOrder
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result

interface MerchantRepository {
    suspend fun login(request: MerchantLoginRequest): Result<Merchant>
    fun logout()
    fun getCurrentMerchant(): Merchant?
    fun isLoggedIn(): Boolean
    suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox>
    suspend fun getMerchantBoxes(): Result<List<MysteryBox>>
    suspend fun getDashboard(): Result<MerchantDashboard>
    suspend fun getOrders(status: String? = null, search: String? = null): Result<List<MerchantOrder>>
    suspend fun verifyOrder(orderId: String): Result<Unit>
    suspend fun cancelOrder(orderId: String): Result<Unit>
}
