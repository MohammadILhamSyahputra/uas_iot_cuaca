package com.example.uas_iot.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.uas_iot.R
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.uas_iot.data.ApiService
import com.example.uas_iot.data.ControlResponse
import com.example.uas_iot.data.SensorModel
import com.google.android.material.materialswitch.MaterialSwitch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {

    private lateinit var tvSuhu: TextView
    private lateinit var tvKelembaban: TextView
    private lateinit var tvGas: TextView
    private lateinit var tvHujan: TextView
    private lateinit var tvCahaya: TextView
    private lateinit var tvStatusAtap: TextView
    private lateinit var switchMode: MaterialSwitch
    private lateinit var btnBuka: Button
    private lateinit var btnTutup: Button

    private lateinit var apiService: ApiService
    private val handler = Handler(Looper.getMainLooper())
    private var statusAtapManual = "BUKA"

    private val sensorRunnable = object : Runnable {
        override fun run() {
            ambilDataRealtime()
            handler.postDelayed(this, 2000) // Refresh data tiap 2 detik
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
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvSuhu = view.findViewById(R.id.tv_suhu)
        tvKelembaban = view.findViewById(R.id.tv_kelembaban)
        tvGas = view.findViewById(R.id.tv_gas)
        tvHujan = view.findViewById(R.id.tv_hujan)
        tvCahaya = view.findViewById(R.id.tv_cahaya)
//        tvStatusAtap = view.findViewById(R.id.tv_status_atap)
        switchMode = view.findViewById(R.id.switch_mode)
        btnBuka = view.findViewById(R.id.btn_buka)
        btnTutup = view.findViewById(R.id.btn_tutup)

        // Inisialisasi Retrofit (Ganti IP sesuai IP Laptop/XAMPP kamu)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.146.68.249/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            btnBuka.isEnabled = isChecked
            btnTutup.isEnabled = isChecked
            kirimPerintahKontrol()
        }

        btnBuka.setOnClickListener {
            statusAtapManual = "BUKA"
            kirimPerintahKontrol()
        }

        btnTutup.setOnClickListener {
            statusAtapManual = "TUTUP"
            kirimPerintahKontrol()
        }

        // TRAMBAHAN: Pastikan tombol terkunci otomatis saat pertama kali aplikasi dibuka
//        btnBuka.isEnabled = switchMode.isChecked
//        btnTutup.isEnabled = switchMode.isChecked

        return view
    }

    private fun ambilDataRealtime() {
        apiService.getRealtimeSensor().enqueue(object : Callback<SensorModel> {
            override fun onResponse(call: Call<SensorModel>, response: Response<SensorModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    tvSuhu.text = "Suhu: ${data.suhu} °C"
                    tvKelembaban.text = "Kelembapan: ${data.kelembapan} %"
                    tvGas.text = "Polusi: ${data.gas} PPM"
                    tvCahaya.text = "Cahaya: ${data.status_cahaya}"
                    tvHujan.text = "Cuaca: ${data.status_hujan}"
//                    tvStatusAtap.text = "Status Atap Mekanik: ${data.status_atap}"
                }
            }
            override fun onFailure(call: Call<SensorModel>, t: Throwable) {}
        })
    }

    private fun kirimPerintahKontrol() {
        val mode = "MANUAL"
        val payload = mapOf(
            "mode_kontrol" to mode,
            "perintah_atap" to statusAtapManual
        )

        apiService.updateKontrolAtap(payload).enqueue(object : Callback<ControlResponse> {
            override fun onResponse(call: Call<ControlResponse>, response: Response<ControlResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Kontrol: $mode ($statusAtapManual)", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ControlResponse>, t: Throwable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(sensorRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(sensorRunnable)
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment DashboardFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            DashboardFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}