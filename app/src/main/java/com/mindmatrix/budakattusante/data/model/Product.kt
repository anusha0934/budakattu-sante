package com.mindmatrix.budakattusante.data.model

import com.google.firebase.firestore.PropertyName

data class Product(
    val productId: String = "",
    val vendorId: String = "",
    val artisanId: String = "",
    val batchId: String = "",
    val name: String = "",
    val category: String = "",
    val availableKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val mspPrice: Double = 0.0, 
    val marketPrice: Double = 0.0,
    val fairTradeMargin: Double = 0.0,
    @get:PropertyName("governmentApproved")
    @set:PropertyName("governmentApproved")
    var governmentApproved: Boolean = false,
    @get:PropertyName("isFairTradeCertified")
    @set:PropertyName("isFairTradeCertified")
    var isFairTradeCertified: Boolean = true,
    val certificationId: String = "",
    val pricingAuthority: String = "TRIFED",
    val mspLastUpdated: String = "",
    val tribalShare: Double = 0.0,
    val familyName: String = "",
    val tribeName: String = "Soliga Tribe",
    val forestRegion: String = "B.R. Hills",
    val village: String = "",
    val collectionZone: String = "",
    val location: String = "B.R. Hills",    
    val locationLat: Double = 11.99, 
    val locationLng: Double = 77.13,
    val rating: Double = 4.8,               
    val sellerPhone: String = "",
    val description: String = "",
    val harvestDate: String = "", 
    val expiryDate: String = "",
    val imageUrl: String = "",
    val vendorProfileImageUrl: String = "",
    val artisanImageUrl: String = "",
    val audioDescUrl: String = "",
    @get:PropertyName("isHandmade")
    @set:PropertyName("isHandmade")
    var isHandmade: Boolean = true,
    @get:PropertyName("isLocalPendingSync")
    @set:PropertyName("isLocalPendingSync")
    var isLocalPendingSync: Boolean = false,
    
    // Pre-order fields
    @get:PropertyName("isPreOrder")
    @set:PropertyName("isPreOrder")
    var isPreOrder: Boolean = false,
    var preorderStock: Int = 0,
    var preorderCount: Int = 0,
    var expectedHarvestDate: String = "",
    
    // Supply Chain Timeline
    val supplyChainTimeline: List<TimelineEvent> = emptyList()
) {
    val isPreOrderAvailable: Boolean
        get() = isPreOrder && preorderCount < preorderStock

    val tribalEarnings: Double
        get() = pricePerKg * (if (tribalShare > 0) tribalShare / 100.0 else 0.8)
}

data class TimelineEvent(
    val stage: String = "", // e.g., "Harvested", "Quality Check", "In Transit"
    val date: String = "",
    val description: String = "",
    val location: String = ""
)
