package com.example.notifbus.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.notifbus.MainActivity
import com.example.notifbus.R
import com.example.notifbus.databinding.ActivityLoginBinding
import com.example.notifbus.server.Connection
import com.example.notifbus.tools.SessionLogin
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.util.Base64

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var sessionLogin: SessionLogin
    private lateinit var connection: Connection

    private var userId = 0
    private var userNam = ""
    private var fullName = ""
    private var gendre = ""

    private var custEmail = ""
    private var custId = 0
    private var custName = ""
    private var ticketNo = ""
    private var tId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionLogin = SessionLogin(this)
        connection = Connection()

        val progressBar = ProgressDialog(this)
        progressBar.setMessage("Loading..")
        progressBar.setCancelable(false)
        progressBar.setCanceledOnTouchOutside(false)

        var token = ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            token = task.result
            Log.e("TOKEN FCM", token, task.exception)
        }

        if (sessionLogin.isLoggedIn()){
            intentClass(MainActivity())
        }

        if (sessionLogin.isLoggedInToken()){
            intentClass(KeluhanPassangerActivity())
        }

        val customerCondition = false

        if (customerCondition){
            binding.cvNoTicket.visibility = View.VISIBLE
            binding.cvLogin.visibility = View.GONE
        }else{
            binding.cvLogin.visibility = View.VISIBLE
            binding.cvNoTicket.visibility = View.GONE
        }

        binding.etKelaminRegistrasi.setOnClickListener{
            val dialogGender = AlertDialog.Builder(this)
            dialogGender.setTitle("Silahkan pilih kelamin anda..")
            val arrayGender = arrayOf("Laki-Laki", "Perempuan")
            dialogGender.setItems(arrayGender){_, p ->
                when(p){
                    0 -> {
                        binding.etKelaminRegistrasi.setText("Laki-Laki")
                    }
                    1 -> {
                        binding.etKelaminRegistrasi.setText("Perempuan")
                    }
                }
            }
            dialogGender.show()
        }

        binding.tvLogin.setOnClickListener {
            binding.etUsernameLogin.setText("")
            binding.etPassLogin.setText("")
            binding.lottieLogin.visibility = View.VISIBLE
            binding.cvLogin.visibility = View.VISIBLE
            binding.cvRegistrasi.visibility = View.GONE
        }

        binding.tvRegistrasi.setOnClickListener {
            binding.etNamaLengkapRegistrasi.setText("")
            binding.etUsernameRegistrasi.setText("")
            binding.etKelaminRegistrasi.setText("")
            binding.etNomorHpRegistrasi.setText("")
            binding.etEmailRegistrasi.setText("")
            binding.etPassRegistrasi.setText("")
            binding.lottieLogin.visibility = View.GONE
            binding.cvLogin.visibility = View.GONE
            binding.cvRegistrasi.visibility = View.VISIBLE
        }

        binding.btnLogin.setOnClickListener {

            if (binding.etUsernameLogin.text.toString().isEmpty() || binding.etPassLogin.text.toString().isEmpty()){
                binding.tvErrorLogin.visibility = View.VISIBLE
                binding.tvErrorLogin.text = "Semua wajib diisi!!"
            }else{
                binding.tvErrorLogin.visibility = View.GONE
                lifecycleScope.launch {
                    progressBar.show()
                    val resultSql = withContext(Dispatchers.IO){
                        checkLogin(binding.etUsernameLogin.text.toString(), binding.etPassLogin.text.toString(), token)
                    }
                    when (resultSql){
                        "Berhasil" -> {
                            progressBar.dismiss()
                            binding.tvErrorLogin.visibility = View.GONE
                            sessionLogin.createLoginSession(userId, userNam, fullName, gendre)
                            intentClass(MainActivity())
                            finish()
                        }
                        "Gagal" -> {
                            progressBar.dismiss()
                            binding.tvErrorLogin.visibility = View.VISIBLE
                            binding.tvErrorLogin.text = "Gagal terhubung dengan server..."
                        }
                        else -> {
                            progressBar.dismiss()
                            binding.tvErrorLogin.visibility = View.VISIBLE
                            binding.tvErrorLogin.text = resultSql.replace("java.sql.SQLException: ", "")
                        }
                    }
                }
            }
        }

        binding.btnRegistrasi.setOnClickListener {
            progressBar.show()
            val etNamaLengkap = binding.etNamaLengkapRegistrasi.text
            val etUsername = binding.etUsernameRegistrasi.text
            val etKelamin = binding.etKelaminRegistrasi.text
            val etNoHp = binding.etNomorHpRegistrasi.text
            val etEmail = binding.etEmailRegistrasi.text
            val etPassword = binding.etPassRegistrasi.text
            if (etNamaLengkap.isEmpty() || etUsername.isEmpty() || etKelamin.isEmpty() || etNoHp.isEmpty() || etEmail.isEmpty() || etPassword.isEmpty()){
                binding.tvErrorRegistrasi.visibility = View.VISIBLE
                binding.tvErrorRegistrasi.text = "Semua data wajib dilengkapi!!"
                progressBar.dismiss()
            }else{
                binding.tvErrorRegistrasi.visibility = View.GONE
                lifecycleScope.launch {
                    val resultSql = withContext(Dispatchers.IO){
                        registrasiUser(etNamaLengkap.toString(), etUsername.toString(), etKelamin.toString(), etNoHp.toString(), etEmail.toString(), etPassword.toString())
                    }
                    when (resultSql){
                        "Berhasil" -> {
                            progressBar.dismiss()
                            binding.etNamaLengkapRegistrasi.setText("")
                            binding.etUsernameRegistrasi.setText("")
                            binding.etKelaminRegistrasi.setText("")
                            binding.etNomorHpRegistrasi.setText("")
                            binding.etEmailRegistrasi.setText("")
                            binding.etPassRegistrasi.setText("")

                            binding.lottieLogin.visibility = View.VISIBLE
                            binding.cvLogin.visibility = View.VISIBLE
                            binding.cvRegistrasi.visibility = View.GONE
                        }
                        "Gagal" -> {
                            progressBar.dismiss()
                            binding.tvErrorRegistrasi.visibility = View.VISIBLE
                            binding.tvErrorRegistrasi.text = "Gagal terhubung dengan server..."
                        }
                        else -> {
                            progressBar.dismiss()
                            binding.tvErrorRegistrasi.visibility = View.VISIBLE
                            binding.tvErrorRegistrasi.text = resultSql.replace("java.sql.SQLException: ", "")
                        }
                    }
                }
            }
        }

        binding.btnMasuk.setOnClickListener {
            if (binding.etNoTicket.text.isEmpty()){
                binding.tvErrorTicket.visibility = View.VISIBLE
                binding.tvErrorTicket.text = "Harap masukan nomor ticket"
            }else{
                binding.tvErrorTicket.visibility = View.GONE
                lifecycleScope.launch {
                    progressBar.show()
                    val resultSql = withContext(Dispatchers.IO){
                        checkTicket(binding.etNoTicket.text.toString(), "", "")
                    }
                    when(resultSql){
                        "Berhasil" -> {
                            progressBar.dismiss()
                            ticketNo = binding.etNoTicket.text.toString()
                            binding.cvOtp.visibility = View.VISIBLE
                            binding.cvNoTicket.visibility = View.GONE
                            binding.tvKirimUlangOtp.visibility = View.INVISIBLE
                            Handler().postDelayed({
                                binding.tvKirimUlangOtp.visibility = View.VISIBLE
                            },60000)
                        }
                        else -> {
                            progressBar.dismiss()
                            binding.tvErrorTicket.visibility = View.VISIBLE
                            binding.tvErrorTicket.text = resultSql.replace("java.sql.SQLException: ", "")
                        }
                    }
                }
            }
        }

        binding.tvKirimUlangOtp.setOnClickListener {
            lifecycleScope.launch {
                progressBar.show()
                val resultSql = withContext(Dispatchers.IO){
                    checkTicket(ticketNo, "", "")
                }
                when(resultSql){
                    "Berhasil" -> {
                        progressBar.dismiss()
                        binding.tvKirimUlangOtp.visibility = View.INVISIBLE
                        Handler().postDelayed({
                            binding.tvKirimUlangOtp.visibility = View.VISIBLE
                        },60000)
                    }
                    else -> {
                        progressBar.dismiss()
                        binding.tvErrorOtp.visibility = View.VISIBLE
                        binding.tvErrorOtp.text = resultSql.replace("java.sql.SQLException: ", "")
                    }
                }
            }
        }

        binding.btnMasukOtp.setOnClickListener {
            lifecycleScope.launch {
                progressBar.show()
                val resultSql = withContext(Dispatchers.IO){
                    checkTicket(ticketNo, "CheckOtp", binding.etOtp.text.toString())
                }
                when(resultSql){
                    "Berhasil" -> {
                        progressBar.dismiss()
                        sessionLogin.createLoginSessionToken(custId, custName, custEmail, ticketNo, tId)
                        intentClass(KeluhanPassangerActivity())
                        finish()
                    }
                    else -> {
                        progressBar.dismiss()
                        binding.tvErrorOtp.visibility = View.VISIBLE
                        binding.tvErrorOtp.text = resultSql.replace("java.sql.SQLException: ", "")
                    }
                }
            }
        }
    }

    private fun checkTicket(ticketN: String, action: String, otp: String):String{
        val connect = connection.connection(this)
        if (connect != null){
            return try {
                val query = "EXEC USP_S_Ticket_SignIn @TicketNo = ?, @Action = ?, @Otp = ?"
                val preparedStatement = connect.prepareStatement(query)
                preparedStatement.setString(1, ticketN)
                preparedStatement.setString(2, action)
                preparedStatement.setString(3, otp)
                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()){
                    custEmail = resultSet.getString("CustEmail")
                    custId = resultSet.getInt("CustId")
                    custName = resultSet.getString("CustName")
                    ticketNo = resultSet.getString("TTicketNo")
                    tId = resultSet.getInt("TId")
                }
                "Berhasil"
            }catch (e: SQLException){
                e.toString()
            }
        }
        return "Gagal terhubung dengan server..."
    }

    private fun checkLogin(userName: String, password: String, tokenDevice: String): String{
        val connect = connection.connection(this)
        if (connect != null){
            return try {
                val query = "EXEC USP_S_User_Login @UserName = ?, @Password = ?, @TokenDevice = ?"
                val preparedStatement = connect.prepareStatement(query)
                preparedStatement.setString(1, userName)
                preparedStatement.setString(2, encryptPassword(password))
                preparedStatement.setString(3, tokenDevice)
                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()){
                    userId = resultSet.getInt("UserId")
                    userNam = resultSet.getString("UserName")
                    fullName = resultSet.getString("FullName")
                    gendre = resultSet.getString("Gender")
                }
                "Berhasil"
            }catch (e: SQLException){
                e.toString()
            }
        }
        return "Gagal"
    }

    private fun registrasiUser(fullName: String, userName: String, kelamin: String, noHp: String, email:String, password: String): String{
        val connect = connection.connection(this)
        if (connect != null){
            try {
                val query = "EXEC USP_S_User_Update @FullName = ?, @UserName = ?, @Gender = ?, @Phone = ?, @Email = ?, @Password = ?"
                val preparedStatement = connect.prepareStatement(query)
                preparedStatement.setString(1, fullName)
                preparedStatement.setString(2, userName)
                preparedStatement.setString(3, kelamin)
                preparedStatement.setString(4, noHp)
                preparedStatement.setString(5, email)
                preparedStatement.setString(6, encryptPassword(password))
                preparedStatement.execute()
                return "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                return e.toString()
            }
        }
        return "Gagal"
    }

    private fun encryptPassword(password: String): String = Base64.getEncoder().encodeToString(password.toByteArray())

    private fun intentClass(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}