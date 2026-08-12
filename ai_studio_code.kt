package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val partyId: Long = 0,
    val businessName: String,
    val contactPerson: String,
    val phone: String,
    val addressTerritory: String,
    val currentBalance: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val itemCode: String,
    val productName: String,
    val category: String,
    val stockQuantity: Int,
    val unitPrice: Double,
    val unitType: String = "Box",
    val lowStockThreshold: Int = 10,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["partyId"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("partyId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    val partyId: Long,
    val partyName: String,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val deliveryStatus: String = "DELIVERED", // "DELIVERED" or "PENDING"
    val notes: String = ""
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("orderId"), Index("itemId")]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val orderItemId: Long = 0,
    val orderId: Long,
    val itemId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

@Entity(
    tableName = "collections",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["partyId"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("partyId")]
)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val collectionId: Long = 0,
    val partyId: Long,
    val partyName: String,
    val amountCollected: Double,
    val paymentMode: String, // "CASH", "CHECK", "BANK_TRANSFER", "MFS"
    val referenceNote: String = "",
    val date: Long = System.currentTimeMillis(),
    val collectedBy: String = "Field Rep"
)

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["partyId"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("partyId")]
)
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0,
    val partyId: Long,
    val partyName: String,
    val date: Long = System.currentTimeMillis(),
    val transactionType: String, // "DELIVERY_DEBIT", "PAYMENT_CREDIT"
    val amount: Double,
    val runningBalance: Double,
    val invoiceReference: String = ""
)