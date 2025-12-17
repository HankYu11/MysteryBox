package com.example.mysterybox.data.dto

import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.testutil.TestFixtures
import kotlin.test.Test
import kotlin.test.assertEquals

class BoxDtoMappingTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = TestFixtures.createMysteryBoxDto(
            id = "box-123",
            name = "Lunch Box",
            description = "Delicious lunch items",
            originalPrice = 800,
            discountedPrice = 500,
            imageUrl = "https://example.com/lunch.jpg",
            storeName = "Tasty Restaurant",
            storeAddress = "456 Food Street",
            pickupTimeStart = "12:00",
            pickupTimeEnd = "14:00",
            status = "AVAILABLE",
            remainingCount = 3,
            discountPercent = 38
        )

        val domain = dto.toDomain()

        assertEquals("box-123", domain.id)
        assertEquals("Lunch Box", domain.name)
        assertEquals("Delicious lunch items", domain.description)
        assertEquals(800, domain.originalPrice)
        assertEquals(500, domain.discountedPrice)
        assertEquals("https://example.com/lunch.jpg", domain.imageUrl)
        assertEquals("Tasty Restaurant", domain.storeName)
        assertEquals("456 Food Street", domain.storeAddress)
        assertEquals("12:00", domain.pickupTimeStart)
        assertEquals("14:00", domain.pickupTimeEnd)
        assertEquals(BoxStatus.AVAILABLE, domain.status)
        assertEquals(3, domain.remainingCount)
        assertEquals(38, domain.discountPercent)
    }

    @Test
    fun `toDomain handles null description with empty string`() {
        val dto = TestFixtures.createMysteryBoxDto(description = null)
        val domain = dto.toDomain()
        assertEquals("", domain.description)
    }

    @Test
    fun `toDomain handles null imageUrl with empty string`() {
        val dto = TestFixtures.createMysteryBoxDto(imageUrl = null)
        val domain = dto.toDomain()
        assertEquals("", domain.imageUrl)
    }

    @Test
    fun `toDomain parses AVAILABLE status correctly`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "AVAILABLE")
        assertEquals(BoxStatus.AVAILABLE, dto.toDomain().status)
    }

    @Test
    fun `toDomain parses ALMOST_SOLD_OUT status correctly`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "ALMOST_SOLD_OUT")
        assertEquals(BoxStatus.ALMOST_SOLD_OUT, dto.toDomain().status)
    }

    @Test
    fun `toDomain parses SOLD_OUT status correctly`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "SOLD_OUT")
        assertEquals(BoxStatus.SOLD_OUT, dto.toDomain().status)
    }

    @Test
    fun `toDomain parses RESERVED status correctly`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "RESERVED")
        assertEquals(BoxStatus.RESERVED, dto.toDomain().status)
    }

    @Test
    fun `toDomain defaults to AVAILABLE for unknown status`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "UNKNOWN_STATUS")
        assertEquals(BoxStatus.AVAILABLE, dto.toDomain().status)
    }

    @Test
    fun `toDomain defaults to AVAILABLE for empty status`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "")
        assertEquals(BoxStatus.AVAILABLE, dto.toDomain().status)
    }

    @Test
    fun `toDomain defaults to AVAILABLE for lowercase status`() {
        val dto = TestFixtures.createMysteryBoxDto(status = "available")
        assertEquals(BoxStatus.AVAILABLE, dto.toDomain().status)
    }

    @Test
    fun `toDomain handles both null description and imageUrl`() {
        val dto = TestFixtures.createMysteryBoxDto(description = null, imageUrl = null)
        val domain = dto.toDomain()
        assertEquals("", domain.description)
        assertEquals("", domain.imageUrl)
    }
}
