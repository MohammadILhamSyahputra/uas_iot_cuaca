package com.example.uas_iot.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.uas_iot.R
import android.widget.TextView
import com.example.uas_iot.data.ApiService
import com.example.uas_iot.data.RiwayatModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.os.Handler
import android.os.Looper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RiwayatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RiwayatFragment : Fragment() {
    private lateinit var tvListRiwayat: TextView
    private lateinit var chartSuhu: LineChart
    private lateinit var chartKelembaban: LineChart
    private lateinit var apiService: ApiService

    private val handler = Handler(Looper.getMainLooper())
    private val riwayatRunnable = object : Runnable {
        override fun run() {
            ambilDataRiwayat()
            handler.postDelayed(this, 3000) // Mengambil data baru & update grafik tiap 3 detik
        }
    }
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riwayat, container, false)
        tvListRiwayat = view.findViewById(R.id.tv_list_riwayat)
        chartSuhu = view.findViewById(R.id.chart_suhu)
        chartKelembaban = view.findViewById(R.id.chart_kelembaban)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.146.68.249/") // Ganti IP sesuai IP Laptop/XAMPP kamu
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)

        ambilDataRiwayat()

        return view
    }

    private fun ambilDataRiwayat() {
        apiService.getRiwayatSensor().enqueue(object : Callback<List<RiwayatModel>> {
            override fun onResponse(call: Call<List<RiwayatModel>>, response: Response<List<RiwayatModel>>) {
                if (response.isSuccessful && response.body() != null) {
                    val listRiwayat = response.body()!!

                    // 1. Tampilkan List Teks Log Riwayat
                    val stringBuilder = StringBuilder()
                    for (item in listRiwayat) {
                        stringBuilder.append("🕒 Jam: ${item.waktu}\n")
                        stringBuilder.append("🌡️ Suhu: ${item.suhu}°C | Kelembaban: ${item.kelembapan}%\n")
                        stringBuilder.append("🌧️ Status Hujan: ${item.status_hujan}\n")
                        stringBuilder.append("--------------------------------------------------\n\n")
                    }

                    if (listRiwayat.isEmpty()) {
                        tvListRiwayat.text = "Belum ada data log riwayat."
                        return
                    } else {
                        tvListRiwayat.text = stringBuilder.toString()
                    }

                    // 2. Buat Data Array Grafik (Dibalik .reversed() agar kronologis dari kiri ke kanan)
                    val entriesSuhu = ArrayList<Entry>()
                    val entriesKelembaban = ArrayList<Entry>()

                    val dataKronologis = listRiwayat.reversed()
                    for (i in dataKronologis.indices) {
                        entriesSuhu.add(Entry(i.toFloat(), dataKronologis[i].suhu.toFloat()))
                        entriesKelembaban.add(Entry(i.toFloat(), dataKronologis[i].kelembapan.toFloat()))
                    }

                    // 3. Set Up Grafik Suhu
                    val dataSetSuhu = LineDataSet(entriesSuhu, "Suhu (°C)").apply {
                        color = Color.parseColor("#EF4444") // Merah
                        setCircleColor(Color.parseColor("#EF4444"))
                        lineWidth = 2f
                        circleRadius = 4f
                        setDrawValues(false) // Tampilkan angka di tiap titik
                        valueTextSize = 10f
                    }
                    chartSuhu.data = LineData(dataSetSuhu)
//                    chartSuhu.animateX(1000)
                    chartSuhu.invalidate()

                    // 4. Set Up Grafik Kelembapan
                    val dataSetKelembaban = LineDataSet(entriesKelembaban, "Kelembapan (%)").apply {
                        color = Color.parseColor("#3B82F6") // Biru
                        setCircleColor(Color.parseColor("#3B82F6"))
                        lineWidth = 2f
                        circleRadius = 4f
                        setDrawValues(false)
                        valueTextSize = 10f
                    }
                    chartKelembaban.data = LineData(dataSetKelembaban)
//                    chartKelembaban.animateX(1000)
                    chartKelembaban.invalidate()
                }
            }

            override fun onFailure(call: Call<List<RiwayatModel>>, t: Throwable) {
                tvListRiwayat.text = "Gagal memuat data riwayat."
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(riwayatRunnable) // Mulai looping saat halaman dibuka
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(riwayatRunnable) // Hentikan looping saat pindah menu agar RAM tidak bocor
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment RiwayatFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            RiwayatFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}