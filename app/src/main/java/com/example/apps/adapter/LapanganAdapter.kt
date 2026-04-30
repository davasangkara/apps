package com.example.apps.adapter

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apps.R
import com.example.apps.model.Lapangan
import java.text.NumberFormat
import java.util.Locale

class LapanganAdapter(
    private var lapanganList: List<Lapangan>,
    private val onFavoriteClick: (Lapangan, Boolean) -> Unit = { _, _ -> },
    private val onItemClick: (Lapangan) -> Unit = {}
) : RecyclerView.Adapter<LapanganAdapter.LapanganViewHolder>() {

    private val favoriteIds = mutableSetOf<String>()
    private var lastAnimatedPosition = -1

    private val sportColors = mapOf(
        "futsal"      to "#1E6B4A",
        "badminton"   to "#1A5276",
        "basket"      to "#784212",
        "voli"        to "#1B2631",
        "tenis"       to "#145A32",
        "renang"      to "#1A5276",
        "sepakbola"   to "#1E6B4A",
        "mini soccer" to "#1E6B4A",
        "bela diri"   to "#4A235A",
        "bowling"     to "#784212",
        "fitness"     to "#922B21",
        "tenis meja"  to "#0E6655",
        "baseball"    to "#784212",
        "panahan"     to "#1C2833",
        "biliar"      to "#212F3C",
        "yoga"        to "#7D6608",
        "default"     to "#212F3C"
    )

    private val sportAccentColors = mapOf(
        "futsal"      to "#2ECC71",
        "badminton"   to "#3498DB",
        "basket"      to "#E67E22",
        "voli"        to "#5D6D7E",
        "tenis"       to "#27AE60",
        "renang"      to "#2980B9",
        "sepakbola"   to "#2ECC71",
        "mini soccer" to "#2ECC71",
        "bela diri"   to "#9B59B6",
        "bowling"     to "#E67E22",
        "fitness"     to "#E74C3C",
        "tenis meja"  to "#1ABC9C",
        "baseball"    to "#D35400",
        "panahan"     to "#566573",
        "biliar"      to "#7F8C8D",
        "yoga"        to "#F1C40F",
        "default"     to "#566573"
    )

    inner class LapanganViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView         = itemView.findViewById(R.id.cardLapangan)
        val headerBg: View             = itemView.findViewById(R.id.viewHeaderBg)
        val accentBar: View            = itemView.findViewById(R.id.viewAccentBar)
        val sportLabel: TextView       = itemView.findViewById(R.id.tvSportLabel)
        val namaText: TextView         = itemView.findViewById(R.id.tvNamaLapangan)
        val jenisChip: TextView        = itemView.findViewById(R.id.tvJenisOlahraga)
        val hargaText: TextView        = itemView.findViewById(R.id.tvHarga)
        val jamText: TextView          = itemView.findViewById(R.id.tvJamTersedia)
        val imgLapangan: ImageView     = itemView.findViewById(R.id.imgLapangan)
        val tvHargaBadge: TextView     = itemView.findViewById(R.id.tvHargaBadge)
        val btnFavorite: ImageView     = itemView.findViewById(R.id.btnFavorite)
        val statusDot: View            = itemView.findViewById(R.id.viewStatusDot)
        val tvAvailable: TextView      = itemView.findViewById(R.id.tvAvailable)
        val tvSportInitial: TextView   = itemView.findViewById(R.id.tvSportInitial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapanganViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lapangan, parent, false)
        return LapanganViewHolder(view)
    }

    override fun onBindViewHolder(holder: LapanganViewHolder, position: Int) {
        val lapangan = lapanganList[position]
        val key = lapangan.jenisOlahraga.lowercase()

        val bgColor     = sportColors[key]     ?: sportColors["default"]!!
        val accentColor = sportAccentColors[key] ?: sportAccentColors["default"]!!

        // Header
        holder.headerBg.setBackgroundColor(Color.parseColor(bgColor))
        holder.accentBar.setBackgroundColor(Color.parseColor(accentColor))
        holder.tvHargaBadge.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(accentColor))

        // Sport initial (2 chars, uppercase)
        val initial = lapangan.jenisOlahraga.take(2).uppercase()
        holder.tvSportInitial.text    = initial
        holder.sportLabel.text        = lapangan.jenisOlahraga.uppercase()

        // Name & chip
        holder.namaText.text   = lapangan.namaLapangan
        holder.jenisChip.text  = lapangan.jenisOlahraga.uppercase()
        holder.jenisChip.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(accentColor))

        // Time
        holder.jamText.text = lapangan.jamTersedia

        // Availability indicator (simple: always available in mock)
        holder.statusDot.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor("#2ECC71"))
        holder.tvAvailable.text = "Tersedia"

        // Price
        val hargaRaw = lapangan.harga
            .replace("Rp", "").replace(".", "").replace(",", "").trim()
        val hargaInt = hargaRaw.toLongOrNull() ?: 0L
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        if (hargaInt > 0) {
            holder.hargaText.text    = "Rp ${formatter.format(hargaInt)} / jam"
            holder.tvHargaBadge.text = "Rp ${formatter.format(hargaInt)}"
        } else {
            holder.hargaText.text    = lapangan.harga
            holder.tvHargaBadge.text = lapangan.harga
        }

        // Image
        if (lapangan.gambar.isNotEmpty() && lapangan.gambar.startsWith("http")) {
            holder.imgLapangan.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(lapangan.gambar)
                .centerCrop()
                .placeholder(R.drawable.ic_sport_placeholder)
                .error(R.drawable.ic_sport_placeholder)
                .into(holder.imgLapangan)
        } else {
            holder.imgLapangan.visibility = View.GONE
        }

        // Favorite
        val isFav = favoriteIds.contains(lapangan.id)
        holder.btnFavorite.setImageResource(
            if (isFav) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )
        holder.btnFavorite.setColorFilter(
            if (isFav) Color.parseColor(accentColor)
            else Color.parseColor("#B0BEC5")
        )
        holder.btnFavorite.setOnClickListener {
            val nowFav = !favoriteIds.contains(lapangan.id)
            if (nowFav) favoriteIds.add(lapangan.id) else favoriteIds.remove(lapangan.id)
            notifyItemChanged(position)
            onFavoriteClick(lapangan, nowFav)
        }

        // Item click
        holder.cardView.setOnClickListener { onItemClick(lapangan) }

        // Entry animation
        if (position > lastAnimatedPosition) {
            holder.cardView.alpha = 0f
            holder.cardView.translationY = 40f
            holder.cardView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .setStartDelay((position * 60L).coerceAtMost(400L))
                .setInterpolator(DecelerateInterpolator())
                .start()
            lastAnimatedPosition = position
        }
    }

    override fun getItemCount(): Int = lapanganList.size

    fun updateList(newList: List<Lapangan>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = lapanganList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) =
                lapanganList[o].id == newList[n].id
            override fun areContentsTheSame(o: Int, n: Int) =
                lapanganList[o] == newList[n]
        })
        lapanganList = newList
        diff.dispatchUpdatesTo(this)
    }

    fun getFavoriteIds(): Set<String> = favoriteIds.toSet()
}
