package com.example.seleknasasc2022

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.seleknasasc2022.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.loginBtn.setOnClickListener {
            loginProcess()
            Log.d("Cek Login", "OK")
        }

        bind.signupBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun loginProcess() {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("http://10.0.2.2:5000/Api/Auth").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")

            val dataLogin = JSONObject().apply {
                put("email", bind.loginEmail.text.toString())
                put("password", bind.loginPassword.text.toString())
            }

            val outputS = conn.outputStream
            outputS.write(dataLogin.toString().toByteArray())
            outputS.flush()
            outputS.close()

            val inputS = conn.inputStream.bufferedReader().readText()

            val rescode = conn.responseCode

            if (rescode in 200..299) {
                Log.d("Login Success", inputS)
                val getData = JSONObject(inputS)
                Session.token = getData.getString("token")

                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            } else {
                Log.d("Login Error", conn.errorStream.bufferedReader().readText())
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Email or Password is invalid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}