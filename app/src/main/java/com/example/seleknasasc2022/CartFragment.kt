package com.example.seleknasasc2022

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seleknasasc2022.databinding.CardCartLayoutBinding
import com.example.seleknasasc2022.databinding.FragmentCartBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class CartFragment : Fragment() {
    private lateinit var bind : FragmentCartBinding

    var startDay = 0
    var startMonth = 0
    var startYear = 0

    var endDay = 0
    var endMonth = 0
    var endYear = 0

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n", "SimpleDateFormat", "CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentCartBinding.inflate(layoutInflater, container, false)

        bind.startBorrow.setOnClickListener {
            val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { datePicker, Y, M, D ->
                startMonth = M + 1
                startDay = D
                startYear = Y

                bind.startBorrowText.text = "${startDay} ${DateFormatSymbols().months[startMonth - 1]} ${startYear}"

                val startDate = SimpleDateFormat("dd-MM-yyyy").parse("$startDay-$startMonth-$startYear")
                val cal = Calendar.getInstance()
                cal.time = startDate
                cal.add(Calendar.DATE, 3)

                endMonth = cal.get(Calendar.MONTH) + 1
                endDay = cal.get(Calendar.DATE)
                endYear = cal.get(Calendar.YEAR)

                bind.endBorrowText.text = "${cal.get(Calendar.DAY_OF_MONTH)} ${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"

            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        bind.cartBooking.setOnClickListener {
            val shared = requireActivity().getSharedPreferences("cart", Activity.MODE_PRIVATE).getString("bookCart", "[]")
            val cartArray = JSONArray(shared)

            if (cartArray.length() == 0) {
                Toast.makeText(context, "Cart do not empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val arrayBook = JSONArray()

            for (e in 0 until cartArray.length()) {
                arrayBook.apply {
                    put(cartArray.getJSONObject(e).getString("id"))
                }
            }

            val formater = DateTimeFormatter.ofPattern("d.M.u")

            val sendData = JSONObject().apply {
                put("start", LocalDate.parse("$startDay-$startMonth-$startYear", formater))
                put("end", LocalDate.parse("$endDay-$endMonth-$endYear", formater))
                put("bookIds", arrayBook)
            }

            Log.d("GagalMasuk", sendData.toString())
            return@setOnClickListener

            GlobalScope.launch(Dispatchers.IO) {
                val conn = URL("http://10.0.2.2:5000/Api/Borrowing").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${Session.token}")

                val formater = DateTimeFormatter.ofPattern("d.M.u")

                val sendData = JSONObject().apply {
                    put("start", LocalDate.parse("$startDay-$startMonth-$startYear", formater))
                    put("end", LocalDate.parse("$endDay-$endMonth-$endYear", formater))
                    put("bookIds", arrayBook)
                }

                val outputS = conn.outputStream
                outputS.write(sendData.toString().toByteArray())
                outputS.flush()
                outputS.close()

                val rescode = conn.responseCode

                if (rescode in 200..299) {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Complete send data!", Toast.LENGTH_SHORT).show()
                        requireActivity().getSharedPreferences("cart", Activity.MODE_PRIVATE).edit().clear()
                    }
                } else {
                    Log.d("GagalMasuk", conn.errorStream.bufferedReader().readText())
                }
            }

        }

        getCart()

        return bind.root
    }

    private fun getCart() {
        val shared = requireActivity().getSharedPreferences("cart", Activity.MODE_PRIVATE).getString("bookCart", "[]")
        val cartArray = JSONArray(shared)

        bind.cartRv.adapter = object : RecyclerView.Adapter<CartHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): CartHolder {
                val inflate = CardCartLayoutBinding.inflate(layoutInflater, parent, false)
                return CartHolder(inflate)
            }

            override fun getItemCount(): Int = cartArray.length()

            @SuppressLint("NotifyDataSetChanged")
            override fun onBindViewHolder(holder: CartHolder, position: Int) {
                val getObject = cartArray.getJSONObject(position)
                holder.binding.cartTitle.text = getObject.getString("name")
                holder.binding.cartAuthor.text = getObject.getString("authors")
                holder.binding.cartIsbn.text = "ISBN-10: ${getObject.getString("isbn")}"
                holder.binding.cartAvailable.text = "Available: ${getObject.getString("available")}"

                holder.binding.btnRemove.setOnClickListener {
                    cartArray.remove(position)
                    notifyDataSetChanged()

                    val sharedDel = requireActivity().getSharedPreferences("cart", Activity.MODE_PRIVATE)
                    val editor = sharedDel.edit()

                    editor.putString("bookCart", cartArray.toString())
                    editor.apply()
                }
            }
        }
        bind.cartRv.layoutManager = LinearLayoutManager(requireContext())
    }

    class CartHolder(val binding: CardCartLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}