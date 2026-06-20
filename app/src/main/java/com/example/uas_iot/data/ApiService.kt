package com.example.uas_iot.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("pantaucuaca/baca_data.php")
    fun getRealtimeSensor(): Call<SensorModel>

    @GET("pantaucuaca/baca_riwayat.php")
    fun getRiwayatSensor(): Call<List<RiwayatModel>>

    @POST("pantaucuaca/update_kontrol.php")
    fun updateKontrolAtap(@Body payload: Map<String, String>): Call<ControlResponse>
}