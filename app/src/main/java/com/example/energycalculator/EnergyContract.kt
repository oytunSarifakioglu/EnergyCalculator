package com.example.energycalculator

import android.provider.BaseColumns

object EnergyContract {
    object EnergyEntry : BaseColumns {
        const val TABLE_NAME = "energy"
        const val COLUMN_ID = "_id"
        const val COLUMN_HOUSE_ID = "house_id"
        const val COLUMN_READING = "reading"
        const val COLUMN_COST = "cost"
        const val COLUMN_COST_PER_UNIT = "cost_per_unit"
    }
}