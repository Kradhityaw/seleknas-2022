package com.example.seleknasasc2022

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.seleknasasc2022.databinding.FragmentDetailBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DetailFragment(private val idBook: String) : Fragment() {
    private lateinit var bind : FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentDetailBinding.inflate(layoutInflater, container, false)
        getBook(arguments?.getString("bookId").toString())
        return bind.root
    }

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    fun getBook(id: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("http://10.0.2.2:5000/Api/Book/$id").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer ${Session.token}")

            val inputS = conn.inputStream.bufferedReader().readText()
            val dataObject = JSONObject(inputS)
            val resCode = conn.responseCode

            if (resCode in 200..299) {
                GlobalScope.launch(Dispatchers.Main) {
                    bind.detailTitle.text = dataObject.getString("name")
                    bind.detailAuthor.text = dataObject.getString("authors")
                    bind.detailISBN.text = "${makeBoldText("ISBN-10")}: ${dataObject.getString("isbn")}"
                    bind.detailPublisher.text = "${makeBoldText("Publisher")}: ${dataObject.getString("publisher")}"
                    bind.detailAvailable.text = "${makeBoldText("Available")}: ${dataObject.getString("available")}"
                    bind.detailDescription.text = dataObject.getString("description")

                    bind.cartBtn.setOnClickListener {
                        val shared = requireActivity().getSharedPreferences("cart", Activity.MODE_PRIVATE)
                        val editor = shared.edit()

                        val cartArray = JSONArray(shared.getString("bookCart", "[]"))

                        for (e in 0 until cartArray.length()) {
                            val getter = cartArray.getJSONObject(e)
                            if (getter.getString("id") == dataObject.getString("id")) {
                                Toast.makeText(context, "Book Is Available In Your Cart!", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }
                        }

                        cartArray.put(dataObject)

                        editor.putString("bookCart", cartArray.toString())
                        editor.apply()
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal Memuat Data!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun makeBoldText(text: String) : SpannableString {
        val spanable = SpannableString(text)
        val boldSpan = StyleSpan(Typeface.BOLD)
        spanable.setSpan(boldSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spanable
    }

}