package com.example.metarsearch.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.metarsearch.databinding.ActivityMainBinding
import com.example.metarsearch.utils.isNetworkAvailable
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        progressDialog = ProgressDialog(this)
            .apply {
                setMessage("Loading..Please wait")
            }


        binding.btnSearch.setOnClickListener {

            val s = binding.edtSerach.text

            if (s.toString().isNotEmpty() && s.toString().startsWith("ED")) {
                progressDialog.show()

                if (!isNetworkAvailable(this)) {
                    setChachedData(s.toString().uppercase())
                } else {
                    mainViewModel.search(s.toString().uppercase())
                }


            } else Toast.makeText(
                this@MainActivity,
                "Invalid station! Only German stations!",
                Toast.LENGTH_SHORT
            ).show()
        }

        mainViewModel.rawTxt.observe(this, {
            progressDialog.dismiss()
            binding.rawData.text = it
            val editor = this.getSharedPreferences("metar", Context.MODE_PRIVATE)
            editor.edit().putString("${binding.edtSerach.text}_RAW", it).apply()
        })

        mainViewModel.decodedTxt.observe(this, {
            progressDialog.dismiss()
            binding.decodeTxt.text = it
            val editor = this.getSharedPreferences("metar", Context.MODE_PRIVATE)
            editor.edit().putString("${binding.edtSerach.text}_DECODE", it).apply()
        })


    }

    private fun setChachedData(stationId: String) {
        val RAW = this.getSharedPreferences("metar", Context.MODE_PRIVATE)
            .getString("${stationId}_RAW", "No saved data found")
        val decode = this.getSharedPreferences("metar", Context.MODE_PRIVATE)
            .getString("${stationId}_DECODE", "No saved data found")

        binding.rawData.text = RAW
        binding.decodeTxt.text = decode

        if (progressDialog.isShowing) progressDialog.dismiss()

    }

    override fun onResume() {
        super.onResume()
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

}
