package com.example.seleknasasc2022

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.seleknasasc2022.databinding.ActivityRegisterBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {
    private lateinit var bind : ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.signupBtn.setOnClickListener {
            // (?=.*?[A-Z]) = At least one upper case
            // (?=.*?[a-z]) = At least one lower case
            // (?=.*?[0-9]) = At least one digit
            // (?=.*?[^A-Za-z0-9]) = At least one special char, not included on the first 3 condition
            // .{8,} = At least 8 char
            val regex = Regex("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[^A-Za-z0-9]).{8,}$")
            if (!regex.containsMatchIn(bind.signupPass.text.toString())) {
                bind.signupPass.error = "Password is not strong!"
                return@setOnClickListener
            }

            if (bind.signupConfPass.text.toString() != bind.signupPass.text.toString()) {
                bind.signupConfPass.error = "Cofirm password not same with password!"
                return@setOnClickListener
            }

            registerProcess()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun registerProcess() {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("http://10.0.2.2:5000/Api/Users").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")

            val data = JSONObject().apply {
                put("name", bind.signupName.text.toString())
                put("password", bind.signupConfPass.text.toString())
                put("email", bind.signupEmail.text.toString())
            }

            val outputS = conn.outputStream
            outputS.write(data.toString().toByteArray())
            outputS.flush()
            outputS.close()

            val resCode = conn.responseCode

            if (resCode in 200..299) {
                Log.d("Register Berhasil", conn.inputStream.bufferedReader().readText())
                finish()
            } else {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Gagal Register!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}