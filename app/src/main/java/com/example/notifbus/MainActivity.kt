package com.example.notifbus

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notifbus.adapter.ListKeluhanAdapter
import com.example.notifbus.databinding.ActivityMainBinding
import com.example.notifbus.model.Keluhan
import com.example.notifbus.server.Connection
import com.example.notifbus.tools.SessionLogin
import com.example.notifbus.ui.CustomerActivity
import com.example.notifbus.ui.TIcketActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionLogin: SessionLogin
    private lateinit var connection: Connection

    private lateinit var mKeluhan: MutableList<Keluhan>
    private lateinit var listKeluhanAdapter: ListKeluhanAdapter

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionLogin = SessionLogin(this)
        connection = Connection()
        listKeluhanAdapter = ListKeluhanAdapter()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(false)

        binding.tvNameUser.text = sessionLogin.getFullName()

        binding.rvKeluhanCustomer.layoutManager = LinearLayoutManager(this)
        binding.rvKeluhanCustomer.setHasFixedSize(false)

        if (sessionLogin.getGendre() == "Laki-Laki"){
            binding.imgProfile.setImageResource(R.drawable.boy)
        }else{
            binding.imgProfile.setImageResource(R.drawable.woman)
        }

        requestNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.e("FCM", "Token: $token")
        }

        showKeluhan()

        binding.imgLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Apakah yakin ingin logout?")
            builder.setPositiveButton("Ya"){_,_->
                sessionLogin.logoutUser()
                finishAffinity()
            }
            builder.setNegativeButton("Males"){p,_->
                p.dismiss()
            }
            builder.create().show()
        }

        binding.cvMenuTicket.setOnClickListener{
            intentClass(TIcketActivity())
        }

        binding.cvMenuCustomer.setOnClickListener {
            intentClass(CustomerActivity())
        }
    }

    private fun showKeluhan(){
        lifecycleScope.launch {
            mKeluhan = mutableListOf()
            progressDialog.show()
            val resultSql = withContext(Dispatchers.IO){
                getDataKeluhan()
            }
            when(resultSql){
                "Berhasil" -> {
                    progressDialog.dismiss()
                    listKeluhanAdapter.setData(mKeluhan)
                    binding.rvKeluhanCustomer.adapter = listKeluhanAdapter
                    listKeluhanAdapter.setOnItemClickCallback(object :ListKeluhanAdapter.OnItemClickCallback{
                        override fun onItemClicked(keluhan: Keluhan) {
                            progressDialog.show()
                            lifecycleScope.launch {
                                val resultSqlOpen = withContext(Dispatchers.IO){
                                    openKeluhan(keluhan.kId!!.toInt())
                                }
                                when(resultSqlOpen){
                                    "Berhasil"->{
                                        progressDialog.dismiss()
                                        listKeluhanAdapter.setData(mKeluhan)
                                        val dialogKeluhan = Dialog(this@MainActivity)
                                        dialogKeluhan.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                        dialogKeluhan.setContentView(R.layout.dialog_show_keluhan)

                                        val etKeluhan = dialogKeluhan.findViewById<EditText>(R.id.etDialogKeluhan)
                                        val btnApprove = dialogKeluhan.findViewById<Button>(R.id.btnApproveDialogKeluhan)
                                        val etSaran = dialogKeluhan.findViewById<EditText>(R.id.etDialogSaranKritik)
                                        val tvKepuasan = dialogKeluhan.findViewById<TextView>(R.id.tvDialogTingkatKepuasan)

                                        val etApprove = dialogKeluhan.findViewById<EditText>(R.id.etDialogApprove)
                                        val btnSubmit = dialogKeluhan.findViewById<Button>(R.id.btnApproveDialogApprove)

                                        val clDialogKeluhan = dialogKeluhan.findViewById<ConstraintLayout>(R.id.clDialogKeluhan)
                                        val clDialogApprove = dialogKeluhan.findViewById<ConstraintLayout>(R.id.clDialogApprove)

                                        etKeluhan.setText(keluhan.kKeluhan.toString())
                                        etSaran.setText(keluhan.kSaran.toString())
                                        tvKepuasan.setText(keluhan.kKepuasan.toString())

                                        btnApprove.setOnClickListener {
                                            clDialogKeluhan.visibility = View.GONE
                                            clDialogApprove.visibility = View.VISIBLE
                                        }

                                        btnSubmit.setOnClickListener {
                                            if(etApprove.text.isEmpty()){
                                                Toast.makeText(this@MainActivity, "Wajib mengisi tanggapan", Toast.LENGTH_SHORT).show()
                                            }else{
                                                progressDialog.show()
                                                lifecycleScope.launch {
                                                    val resultSql3 = withContext(Dispatchers.IO){
                                                        approveKeluhan(keluhan.kId!!, etApprove.text.toString())
                                                    }
                                                    when(resultSql3){
                                                        "Berhasil" -> {
                                                            progressDialog.dismiss()
                                                            dialogKeluhan.dismiss()
                                                            Toast.makeText(this@MainActivity, "Tanggapan anda sudah dikirim..", Toast.LENGTH_SHORT).show()
                                                        }
                                                        else -> {
                                                            progressDialog.dismiss()
                                                            Toast.makeText(this@MainActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        dialogKeluhan.show()
                                        dialogKeluhan.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                        dialogKeluhan.window?.setBackgroundDrawable(
                                            ColorDrawable(
                                                Color.TRANSPARENT)
                                        )
                                        dialogKeluhan.window?.attributes?.windowAnimations = R.style.DialogAnimation
                                        dialogKeluhan.window?.setGravity(Gravity.CENTER)
                                    }
                                    else -> {
                                        progressDialog.dismiss()
                                        Toast.makeText(this@MainActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    })
                }
                else -> {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun approveKeluhan(kId: Int, approveReason: String):String{
        val conn = connection.connection(this)
        if(conn != null){
            try {
                val query = "EXEC USP_S_Keluhan_Update @KId = ?, @KApprove = 1, @KApproveReason = ?, @UserId = ?"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, kId)
                preparedStatement.setString(2, approveReason)
                preparedStatement.setInt(3, sessionLogin.getUserId())
                preparedStatement.executeQuery()
                return "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                return e.toString()
            }
        }
        return "Gagal terhubung dengan server..."
    }

    private fun openKeluhan(kId: Int):String{
        val conn = connection.connection(this)
        if(conn != null){
            try {
                val query = "EXEC USP_S_Keluhan_Update @KId = ?, @KRead = 1"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, kId)
                val result = preparedStatement.executeQuery()
                mKeluhan.clear()
                while (result.next()){
                    val keluhan = Keluhan().apply {
                        this.fullName = result.getString("CustName")
                        this.kId = result.getInt("KId")
                        this.noTicket = result.getString("TTicketNo")
                        this.dari = result.getString("TDari")
                        this.tujuan = result.getString("TTujuan")
                        this.kKeluhan = result.getString("KKeluhan")
                        this.kSaran = result.getString("KSaran")
                        this.kKepuasan = result.getString("KKepuasan")
                        this.kApprove = result.getInt("KApprove")
                        this.kRead = result.getInt("KRead")
                        this.kCreateDate = result.getString("KCreateDate")
                    }
                    mKeluhan.add(keluhan)
                }
                return "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                return e.toString()
            }
        }
        return "Gagal terhubung dengan server..."
    }

    private fun getDataKeluhan():String{
        val conn = connection.connection(this)
        if (conn != null){
            try {
                val query = "SELECT SC.CustName, ST.TTicketNo, ST.TDari, ST.TTujuan, SK.* FROM S_Keluhan SK\n" +
                        "JOIN S_Ticket ST ON ST.TId = SK.TId\n" +
                        "JOIN S_Customer SC ON SC.CustId = ST.TId"
                val statement = conn.createStatement()
                val result = statement.executeQuery(query)
                mKeluhan.clear()
                while (result.next()){
                    val keluhan = Keluhan().apply {
                        this.fullName = result.getString("CustName")
                        this.kId = result.getInt("KId")
                        this.noTicket = result.getString("TTicketNo")
                        this.dari = result.getString("TDari")
                        this.tujuan = result.getString("TTujuan")
                        this.kKeluhan = result.getString("KKeluhan")
                        this.kSaran = result.getString("KSaran")
                        this.kKepuasan = result.getString("KKepuasan")
                        this.kApprove = result.getInt("KApprove")
                        this.kRead = result.getInt("KRead")
                        this.kCreateDate = result.getString("KCreateDate")
                    }
                    mKeluhan.add(keluhan)
                }
                return "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                return e.toString()
            }
        }
        return "Gagal terhubung dengan server..."
    }

    private fun intentClass(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

    private fun requestNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if(!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}