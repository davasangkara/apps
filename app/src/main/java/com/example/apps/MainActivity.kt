package com.example.apps

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apps.adapter.LapanganAdapter
import com.example.apps.model.Lapangan
import com.example.apps.network.RetrofitInstance
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var rvLapangan: RecyclerView
    private lateinit var layoutLoading: FrameLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button
    private lateinit var etSearch: EditText
    private lateinit var btnSort: ImageView
    private lateinit var llFilterChips: LinearLayout

    private lateinit var adapter: LapanganAdapter
    private var allData: List<Lapangan> = emptyList()
    private var currentFilter = "Semua"
    private var currentQuery = ""

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        setupRecyclerView()
        setupSearch()
        setupSort()
        loadData()
    }

    private fun bindViews() {
        rvLapangan    = findViewById(R.id.rvLapangan)
        layoutLoading = findViewById(R.id.layoutLoading)
        layoutError   = findViewById(R.id.layoutError)
        layoutEmpty   = findViewById(R.id.layoutEmpty)
        tvTotal       = findViewById(R.id.tvTotal)
        tvError       = findViewById(R.id.tvError)
        btnRetry      = findViewById(R.id.btnRetry)
        etSearch      = findViewById(R.id.etSearch)
        btnSort       = findViewById(R.id.btnSort)
        llFilterChips = findViewById(R.id.llFilterChips)
        btnRetry.setOnClickListener { loadData() }
    }

    private fun setupRecyclerView() {
        adapter = LapanganAdapter(
            lapanganList = emptyList(),
            onFavoriteClick = { lapangan, saved ->
                val msg = if (saved) "${lapangan.namaLapangan} disimpan" else "Dihapus dari tersimpan"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            },
            onItemClick = { lapangan ->
                Toast.makeText(this, lapangan.namaLapangan, Toast.LENGTH_SHORT).show()
            }
        )
        rvLapangan.layoutManager = LinearLayoutManager(this)
        rvLapangan.adapter = adapter
        rvLapangan.setHasFixedSize(false)
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                currentQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSort() {
        btnSort.setOnClickListener {
            val options = arrayOf("Harga Terendah", "Harga Tertinggi", "Nama A-Z", "Nama Z-A")
            AlertDialog.Builder(this)
                .setTitle("Urutkan")
                .setItems(options) { _, which ->
                    allData = when (which) {
                        0 -> allData.sortedBy { it.harga.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
                        1 -> allData.sortedByDescending { it.harga.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
                        2 -> allData.sortedBy { it.namaLapangan }
                        3 -> allData.sortedByDescending { it.namaLapangan }
                        else -> allData
                    }
                    applyFilters()
                }
                .show()
        }
    }

    private fun buildFilterChips(data: List<Lapangan>) {
        llFilterChips.removeAllViews()
        val types = listOf("Semua") + data.map { it.jenisOlahraga }.distinct().sorted()
        types.forEach { type ->
            val chip = TextView(this).apply {
                text = type
                textSize = 11f
                setTextColor(if (type == currentFilter) Color.WHITE else Color.parseColor("#9AA3AD"))
                setPadding(28, 16, 28, 16)
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAllCaps = false
                background = if (type == currentFilter)
                    resources.getDrawable(R.drawable.bg_chip_active, theme)
                else
                    resources.getDrawable(R.drawable.bg_chip_inactive, theme)
                setOnClickListener {
                    currentFilter = type
                    buildFilterChips(allData)
                    applyFilters()
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 8 }
            llFilterChips.addView(chip, params)
        }
    }

    private fun applyFilters() {
        var filtered = allData
        if (currentFilter != "Semua") {
            filtered = filtered.filter { it.jenisOlahraga.equals(currentFilter, ignoreCase = true) }
        }
        if (currentQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.namaLapangan.contains(currentQuery, ignoreCase = true) ||
                        it.jenisOlahraga.contains(currentQuery, ignoreCase = true)
            }
        }
        adapter.updateList(filtered)
        if (filtered.isEmpty()) {
            rvLapangan.visibility  = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvLapangan.visibility  = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }
        val total = allData.size
        val shown = filtered.size
        tvTotal.text = if (currentFilter == "Semua" && currentQuery.isBlank())
            "$total lapangan tersedia"
        else
            "$shown dari $total lapangan"
    }

    private fun loadData(fromSwipe: Boolean = false) {
        if (!fromSwipe) {
            layoutLoading.visibility = View.VISIBLE
            layoutError.visibility   = View.GONE
            rvLapangan.visibility    = View.GONE
            layoutEmpty.visibility   = View.GONE
        }
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { RetrofitInstance.api.getLapangan() }
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    allData = data
                    buildFilterChips(data)
                    applyFilters()
                    layoutLoading.visibility = View.GONE
                    layoutError.visibility   = View.GONE
                    rvLapangan.visibility    = View.VISIBLE
                } else {
                    showError("Gagal memuat data (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Koneksi gagal")
            }
        }
    }

    private fun showError(message: String) {
        layoutLoading.visibility = View.GONE
        rvLapangan.visibility    = View.GONE
        layoutEmpty.visibility   = View.GONE
        layoutError.visibility   = View.VISIBLE
        tvError.text             = message
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}