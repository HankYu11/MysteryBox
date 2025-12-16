package com.example.mysterybox.data.repository

import com.example.mysterybox.data.model.CreateBoxRequest
import com.example.mysterybox.data.model.Merchant
import com.example.mysterybox.data.model.MerchantLoginRequest
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Result

interface MerchantRepository {
    suspend fun login(request: MerchantLoginRequest): Result<Merchant>
    fun logout()
    fun getCurrentMerchant(): Merchant?
    fun isLoggedIn(): Boolean
    suspend fun createBox(request: CreateBoxRequest): Result<MysteryBox>
    fun getMerchantBoxes(): List<MysteryBox>
}
