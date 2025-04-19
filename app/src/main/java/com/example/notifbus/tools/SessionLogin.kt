package com.example.notifbus.tools

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.notifbus.ui.LoginActivity

class SessionLogin(var context: Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor
    var PRIVATE_MODE = 0

    fun createLoginSession(userId: Int, userName: String, fullName: String, gendre: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putInt(KEY_USERID, userId)
        editor.putString(KEY_USERNAME, userName)
        editor.putString(KEY_FULLNAME, fullName)
        editor.putString(KEY_GENDRE, gendre)
        editor.commit()
    }

    fun createLoginSessionToken(userId: Int, userName: String, email:String, ticketNo: String, tId: Int) {
        editor.putBoolean(IS_LOGIN_TOKEN, true)
        editor.putInt(KEY_CUSTID, userId)
        editor.putString(KEY_CUSTNAME, userName)
        editor.putString(KEY_CUSTEMAIL, email)
        editor.putString(KEY_TICKETNO, ticketNo)
        editor.putInt(KEY_TID, tId)
        editor.commit()
    }

    fun checkLogin() {
        if (!isLoggedIn()) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGIN, false)
    fun getUserId(): Int = pref.getInt(KEY_USERID, 0)
    fun getUserName(): String = pref.getString(KEY_USERNAME, "").toString()
    fun getFullName(): String = pref.getString(KEY_FULLNAME, "").toString()
    fun getGendre(): String = pref.getString(KEY_GENDRE, "").toString()

    fun isLoggedInToken(): Boolean = pref.getBoolean(IS_LOGIN_TOKEN, false)
    fun getCustId(): Int = pref.getInt(KEY_CUSTID, 0)
    fun getTId(): Int = pref.getInt(KEY_TID, 0)
    fun getCustName(): String = pref.getString(KEY_CUSTNAME, "").toString()
    fun getCustEmail(): String = pref.getString(KEY_CUSTEMAIL, "").toString()
    fun getTicketNo(): String = pref.getString(KEY_TICKETNO, "").toString()

    companion object {
        private const val PREF_NAME = "Prefrence"
        private const val IS_LOGIN = "IsLoggedIn"
        const val KEY_USERID = "userId"
        const val KEY_USERNAME = "userName"
        const val KEY_FULLNAME = "fullName"
        const val KEY_GENDRE = "gendre"

        private const val IS_LOGIN_TOKEN = "IsLoginToken"
        const val KEY_CUSTID = "custId"
        const val KEY_CUSTNAME = "custName"
        const val KEY_CUSTEMAIL = "custEmail"
        const val KEY_TICKETNO = "ticketNo"
        const val KEY_TID = "tId"
    }

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}