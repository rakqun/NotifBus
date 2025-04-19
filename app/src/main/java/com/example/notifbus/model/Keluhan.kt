package com.example.notifbus.model

data class Keluhan (
    var kId: Int? = null,
    var noTicket: String? = null,
    var dari: String? = null,
    var tujuan: String? = null,
    var fullName: String? = null,
    var kKeluhan: String? = null,
    var kSaran: String? = null,
    var kKepuasan: String? = null,
    var kApprove: Int? = null,
    var kRead: Int? = null,
    var kCreateDate: String? = null
        )