package com.catpokedex.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cats")
data class Cat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val photoPath: String,
    val capturedAt: Long = System.currentTimeMillis()
)
