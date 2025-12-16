package com.example.mysterybox.data

import com.example.mysterybox.data.model.BoxStatus
import com.example.mysterybox.data.model.MysteryBox
import com.example.mysterybox.data.model.Reservation
import com.example.mysterybox.data.model.ReservationStatus

object SampleData {
    val mysteryBoxes = listOf(
        MysteryBox(
            id = "1",
            name = "烘焙點心驚喜包",
            description = "含 3-4 款今日烘焙麵包，隨機搭配",
            originalPrice = 300,
            discountedPrice = 89,
            imageUrl = "bakery",
            storeName = "Downtown Happy Bakery",
            storeAddress = "台北大安旗艦店",
            pickupTimeStart = "19:00",
            pickupTimeEnd = "21:00",
            status = BoxStatus.AVAILABLE,
            remainingCount = 3,
            discountPercent = 70
        ),
        MysteryBox(
            id = "2",
            name = "進口零食福袋",
            description = "每袋含隨機進口零食 5 包",
            originalPrice = 350,
            discountedPrice = 150,
            imageUrl = "snacks",
            storeName = "零食天堂",
            storeAddress = "信義松高店",
            pickupTimeStart = "19:00",
            pickupTimeEnd = "21:00",
            status = BoxStatus.ALMOST_SOLD_OUT,
            remainingCount = 1,
            discountPercent = 57
        ),
        MysteryBox(
            id = "3",
            name = "生鮮蔬菜箱",
            description = "當季新鮮蔬菜組合",
            originalPrice = 250,
            discountedPrice = 100,
            imageUrl = "vegetables",
            storeName = "有機農場直送",
            storeAddress = "中山北路店",
            pickupTimeStart = "17:00",
            pickupTimeEnd = "19:00",
            status = BoxStatus.SOLD_OUT,
            remainingCount = 0,
            discountPercent = 60
        ),
        MysteryBox(
            id = "4",
            name = "即期飲品驚喜箱",
            description = "果汁、茶飲隨機 3 瓶",
            originalPrice = 120,
            discountedPrice = 55,
            imageUrl = "drinks",
            storeName = "飲料工坊",
            storeAddress = "東區門市",
            pickupTimeStart = "20:00",
            pickupTimeEnd = "22:00",
            status = BoxStatus.AVAILABLE,
            remainingCount = 5,
            discountPercent = 54
        ),
        MysteryBox(
            id = "5",
            name = "Late Night Bakery Surprise",
            description = "A selection of today's unsold breads and cakes. Contents are a surprise but typically include 3-4 items such as croissants, bagels, or muffins. Perfect for tomorrow's breakfast!",
            originalPrice = 350,
            discountedPrice = 100,
            imageUrl = "bakery_night",
            storeName = "Downtown Happy Bakery",
            storeAddress = "Dunhua South Road",
            pickupTimeStart = "20:00",
            pickupTimeEnd = "22:00",
            status = BoxStatus.AVAILABLE,
            remainingCount = 2,
            discountPercent = 76
        )
    )

    val reservations = listOf(
        Reservation(
            id = "r1",
            orderId = "#8821",
            box = mysteryBoxes[0].copy(name = "驚喜麵包福袋"),
            status = ReservationStatus.READY_FOR_PICKUP,
            pickupDate = "今日",
            pickupTimeStart = "18:00",
            pickupTimeEnd = "20:00",
            price = 150
        ),
        Reservation(
            id = "r2",
            orderId = "#8815",
            box = mysteryBoxes[2].copy(name = "即期蔬果箱 (大)"),
            status = ReservationStatus.RESERVED,
            pickupDate = "明日",
            pickupTimeStart = "12:00",
            pickupTimeEnd = "14:00",
            price = 200
        ),
        Reservation(
            id = "r3",
            orderId = "#8809",
            box = mysteryBoxes[0].copy(name = "甜點驚喜盒"),
            status = ReservationStatus.PICKUP_MISSED,
            pickupDate = "昨日",
            pickupTimeStart = "21:00",
            pickupTimeEnd = "21:00",
            price = 180
        )
    )
}
