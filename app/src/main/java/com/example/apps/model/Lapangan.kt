package com.example.apps.model

import com.google.gson.annotations.SerializedName

data class Lapangan(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("nama_lapangan")
    val namaLapangan: String = "",

    @SerializedName("jenis_olahraga")
    val jenisOlahraga: String = "",

    @SerializedName("harga")
    val harga: String = "",

    @SerializedName("jam_tersedia")
    val jamTersedia: String = "",

    @SerializedName("gambar")
    val gambar: String = ""
)