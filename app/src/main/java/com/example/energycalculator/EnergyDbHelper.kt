package com.example.energycalculator

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EnergyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = "CREATE TABLE ${EnergyContract.EnergyEntry.TABLE_NAME} (" +
                "${EnergyContract.EnergyEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${EnergyContract.EnergyEntry.COLUMN_HOUSE_ID} TEXT," +
                "${EnergyContract.EnergyEntry.COLUMN_READING} INTEGER," +
                "${EnergyContract.EnergyEntry.COLUMN_COST} INTEGER)"

        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropTableStatement = "DROP TABLE IF EXISTS ${EnergyContract.EnergyEntry.TABLE_NAME}"
        db.execSQL(dropTableStatement)
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "energy.db"
        private const val DATABASE_VERSION = 1
    }
}