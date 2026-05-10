package com.example.lr2v

import android.content.Context
import java.io.FileNotFoundException
import java.io.IOException

object StorageHelper {

    private const val FILE_NAME = "pizza_orders.txt"

    fun saveOrder(context: Context, orderText: String): Boolean {
        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_APPEND).use { output ->
                output.write((orderText + "\n\n--------------------\n\n").toByteArray())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun readOrders(context: Context): String {
        return try {
            context.openFileInput(FILE_NAME).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            ""
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    fun updateOrders(context: Context, newText: String): Boolean {
        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(newText.toByteArray())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun deleteOrders(context: Context): Boolean {
        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write("".toByteArray())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}