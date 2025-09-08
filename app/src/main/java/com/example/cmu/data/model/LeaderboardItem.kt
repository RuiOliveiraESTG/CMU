package com.example.cmu.data.model

data class LeaderboardItem(
    val placeId: String,
    val media: Double,
    val total: Int,
    val name: String? = null,
    val address: String? = null
)
