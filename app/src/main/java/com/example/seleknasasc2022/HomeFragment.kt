package com.example.seleknasasc2022

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seleknasasc2022.databinding.CardBookLayoutBinding
import com.example.seleknasasc2022.databinding.FragmentHomeBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {
    private lateinit var bind: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomeBinding.inflate(layoutInflater, container, false)

        getBooks("")

        bind.homeSearch.addTextChangedListener { getBooks(it.toString()) }

        return bind.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getBooks(search: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("http://10.0.2.2:5000/Api/Book?searchText=$search").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer ${Session.token}")

            val inputS = conn.inputStream.bufferedReader().readText()
            val arrayJson = JSONArray(inputS)
            val rescode = conn.responseCode

            if (rescode in 200..299) {
                GlobalScope.launch(Dispatchers.Main) {
                    bind.homeRv.adapter = object : RecyclerView.Adapter<CardHolder>() {
                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
                            val inflater = CardBookLayoutBinding.inflate(layoutInflater, parent, false)
                            return CardHolder(inflater)
                        }

                        override fun getItemCount(): Int = arrayJson.length()

                        override fun onBindViewHolder(holder: CardHolder, position: Int) {
                            val sample = arrayJson.getJSONObject(position)

                            holder.binding.bookTitle.text = sample.getString("name")
                            holder.binding.bookAuthor.text = sample.getString("authors")
                            holder.binding.bookImage.setImageResource(R.drawable.bookplaceholder)

                            holder.itemView.setOnClickListener {
//                                ==================================================
//                                Using constructor to send data fragment to fragment
//                                ==================================================
//                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
//                                transaction.replace(R.id.main, DetailFragment(sample.getString("id")))
//                                transaction.addToBackStack(null)
//                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                                transaction.commit()
//                                ===================================================

//                                Using arguments to send data fragment to fragment
                                val dataId = sample.getString("id")
                                val bundle = Bundle()
                                bundle.putString("bookId", dataId)
                                val detailFragment = DetailFragment("")
                                detailFragment.arguments = bundle
                                requireActivity().supportFragmentManager.beginTransaction().apply {
                                    replace(R.id.main, detailFragment)
                                    addToBackStack(null)
                                    commit()
                                }
                            }
                        }
                    }
                    bind.homeRv.layoutManager = GridLayoutManager(context, 2)
                }
            } else {
                Log.d("Loading failed!", "Failed to load book!")
            }
        }
    }

    class CardHolder(val binding: CardBookLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}