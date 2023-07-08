package com.example.energycalculator

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.room.Room
import com.example.energycalculator.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: EnergyDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        dbHelper = EnergyDbHelper(this)

        calculateConsumption()
    }

    private fun calculateConsumption() {
        binding.submit.setOnClickListener {
            if (binding.houseId.text.length == 10) {
                val unitInput: String? = binding.electricReader.text.toString()
                val unitValue: Int? = unitInput?.toIntOrNull()

                if (unitValue != null) {
                    val totalCost = calculateCost(unitValue)

                    binding.totalValue.text = "Total Cost ==> $totalCost"
                    binding.saveButton.visibility = View.VISIBLE

                    val houseId = binding.houseId.text.toString()
                    if (houseId.isNotBlank()) {
                        val previousReading = getPreviousReading(houseId)
                        if (previousReading != null && unitValue <= previousReading) {
                            Toast.makeText(this, "New reading must be higher than the previous reading.", Toast.LENGTH_SHORT).show()
                        } else {
                            saveReadingAndCost(houseId, unitValue, totalCost)
                            updateHistoricalValues(houseId)
                        }
                    } else {
                        Toast.makeText(this, "Please enter a valid house ID.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid house ID.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.saveButton.setOnClickListener {
            binding.saveButton.visibility = View.INVISIBLE
            binding.totalValue.text = ""
            binding.electricReader.text.clear()
        }
    }



    private fun calculateCost(unitValue: Int): Int {
        var totalCost = 0
        val firstSeperator = 100
        val secondSeperator = 400
        val thirdSeperator = 500
        val firstCost = 5
        val secondCost = 8
        val thirdCost = 10

        if (unitValue <= firstSeperator) {
            totalCost += unitValue * firstCost
        } else if (unitValue <= thirdSeperator && unitValue > firstSeperator) {
            val firstUnit = unitValue - firstSeperator
            totalCost = firstUnit * secondCost + firstSeperator * firstCost
        } else if (unitValue > thirdSeperator) {
            val firstUnit = unitValue - firstSeperator
            val secondUnit = firstUnit - secondSeperator
            totalCost = secondUnit * thirdCost + secondSeperator * secondCost + firstSeperator * firstCost
        }

        return totalCost
    }

    private fun saveReadingAndCost(houseId: String, reading: Int, cost: Int) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(EnergyContract.EnergyEntry.COLUMN_HOUSE_ID, houseId)
            put(EnergyContract.EnergyEntry.COLUMN_READING, reading)
            put(EnergyContract.EnergyEntry.COLUMN_COST, cost)
        }

        val newRowId = db.insert(EnergyContract.EnergyEntry.TABLE_NAME, null, values)

        if (newRowId == -1L) {
            Toast.makeText(this, "Error saving reading and cost.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Reading and cost saved.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getPreviousReading(houseId: String): Int? {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(EnergyContract.EnergyEntry.COLUMN_READING)
        val sortOrder = "${EnergyContract.EnergyEntry.COLUMN_ID} DESC"
        val selection = "${EnergyContract.EnergyEntry.COLUMN_HOUSE_ID} = ?"
        val selectionArgs = arrayOf(houseId)

        val cursor = db.query(
            EnergyContract.EnergyEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder,
            "1"
        )

        val previousReading: Int? = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(EnergyContract.EnergyEntry.COLUMN_READING))
        } else {
            null
        }

        cursor.close()
        return previousReading
    }

    private fun updateHistoricalValues(houseId: String) {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            EnergyContract.EnergyEntry.COLUMN_READING,
            EnergyContract.EnergyEntry.COLUMN_COST
        )

        val sortOrder = "${EnergyContract.EnergyEntry.COLUMN_ID} DESC"
        val selection = "${EnergyContract.EnergyEntry.COLUMN_HOUSE_ID} = ?"
        val selectionArgs = arrayOf(houseId)

        val cursor = db.query(
            EnergyContract.EnergyEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder,
        )

        val historicalValues = StringBuilder()
        with(cursor) {
            while (moveToNext()) {
                val historicalReading = getInt(getColumnIndexOrThrow(EnergyContract.EnergyEntry.COLUMN_READING))
                val historicalCost = getInt(getColumnIndexOrThrow(EnergyContract.EnergyEntry.COLUMN_COST))

                historicalValues.append("Reading: $historicalReading, Cost: $historicalCost\n")
            }
        }

        cursor.close()

        binding.historical.text = historicalValues.toString()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}