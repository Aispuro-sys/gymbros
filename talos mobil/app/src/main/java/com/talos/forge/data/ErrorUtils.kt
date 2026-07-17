package com.talos.forge.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException

object ErrorUtils {
    private val gson = Gson()

    fun getErrorMessage(e: Exception): String {
        return if (e is HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    val json = gson.fromJson(errorBody, JsonObject::class.java)
                    json?.get("error")?.asString ?: e.message()
                } else {
                    e.message()
                }
            } catch (_: Exception) {
                e.message()
            }
        } else {
            e.message ?: "Error desconocido"
        }
    }
}
