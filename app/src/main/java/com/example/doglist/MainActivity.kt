package com.example.doglist

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doglist.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DogAdapter
    private val dogImage = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.searchViewDogs.setOnQueryTextListener(this)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = DogAdapter(dogImage)
        binding.recyclerViewDogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDogs.adapter = adapter
    }

    private fun getRetrofit() : Retrofit
    {
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/breed/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun searchByName(query : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val call : Response<DogResponse> = getRetrofit().create(APIService::class.java).getDogsByBreeds("$query/images")
            val puppies : DogResponse? = call.body()
            runOnUiThread{
                if (call.isSuccessful){
                    //Show recyclerView
                    val images : List<String> = puppies?.images ?: emptyList()
                    dogImage.clear()
                    dogImage.addAll(images)
                    adapter.notifyDataSetChanged()
                }else{
                    //Show error
                    dogImage.clear()
                    showError()
                }
                hideKeyBoard()
            }
        }
    }

    private fun hideKeyBoard() {
        val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.applicationUI.windowToken, 0)
    }

    private fun showError()
    {
        Toast.makeText(this, "No hemos podido recuperar tu imagen", Toast.LENGTH_LONG).show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrEmpty()){
            searchByName(query.lowercase())
        }
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return true
    }
}