package com.example.uas_iot.data
import com.google.gson.annotations.SerializedName
data class SensorModel(
    @SerializedName("suhu") val suhu: Double,
    @SerializedName("kelembapan") val kelembapan: Int,
    @SerializedName("gas") val gas: Int,
    @SerializedName("status_hujan") val status_hujan: String,
    @SerializedName("status_cahaya") val status_cahaya: String, // ⚠️ Dipetakan langsung ke teks "Cerah"
    @SerializedName("status_atap") val status_atap: String
)
