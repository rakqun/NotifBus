package com.example.notifbus.model

data class Ticket (
    var tId: Int? = null,
    var ticketNo: String? = null,
    var custName: String? = null,
    var custPhone: String? = null,
    var custEmail: String? = null,
    var tDari: String? = null,
    var tTujuan: String? = null,
    var tTglKeberangkatan: String? = null
)