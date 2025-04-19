package com.example.notifbus.ui

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.notifbus.R
import com.example.notifbus.databinding.ActivityKeluhanPassangerBinding
import com.example.notifbus.model.Ticket
import com.example.notifbus.server.Connection
import com.example.notifbus.tools.SessionLogin
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

class KeluhanPassangerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeluhanPassangerBinding

    private lateinit var connection: Connection
    private lateinit var sessionLogin: SessionLogin

    private var selectedText = ""

    private var keluhan = ""
    private var saran = ""
    private var kepuasan = ""
    private var reasonApprove = ""
    private var approve = 0

    private lateinit var progressBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityKeluhanPassangerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var token = ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            token = task.result
            Log.e("TOKEN FCM", token, task.exception)
        }

        sessionLogin = SessionLogin(this)
        connection = Connection()

        progressBar = ProgressDialog(this)
        progressBar.setMessage("Loading..")
        progressBar.setCanceledOnTouchOutside(false)
        progressBar.setCancelable(false)

        binding.tvNameUser.text = sessionLogin.getCustName()

        lifecycleScope.launch {
            progressBar.show()
            val resultSql = withContext(Dispatchers.IO){
                getTicket(sessionLogin.getTId())
            }
            when(resultSql){
                "Berhasil" -> {
                    progressBar.dismiss()
                    if (kepuasan == ""){
                        showDialog(token)
                    }else{
                        binding.tvBalasanAdmin.visibility = View.VISIBLE
                        if (approve == 1){
                            binding.lottieKeluhan.visibility = View.GONE
                            binding.tvEndSession.visibility = View.VISIBLE
                            binding.tvBalasanAdmin.text = reasonApprove
                        }
                        binding.tvKeluhanKamu.text = "Keluhan: $keluhan & Saran: $saran \n Kepuasan: $kepuasan"
                    }
                }
                "OUT" ->{
                    sessionLogin.logoutUser()
                    finishAffinity()
                }
                else -> {
                    progressBar.dismiss()
                    Toast.makeText(this@KeluhanPassangerActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDialog(token: String){
        val dialogKeluhan = Dialog(this)
        dialogKeluhan.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogKeluhan.setContentView(R.layout.dialog_sarankritik)

        val etKeluhan = dialogKeluhan.findViewById<EditText>(R.id.etKeluhan)
        val btnSubmit = dialogKeluhan.findViewById<Button>(R.id.btnSubmitDialogSaran)
        val etSaran = dialogKeluhan.findViewById<EditText>(R.id.etSaranKritik)
        val cgKepuasan = dialogKeluhan.findViewById<ChipGroup>(R.id.cgKepuasan)

        cgKepuasan.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val selectedChip = cgKepuasan.findViewById<Chip>(checkedId)

                if (selectedChip != null) {
                    selectedText = selectedChip.text.toString()
                }
            }
        }

        btnSubmit.setOnClickListener {
            if(selectedText == ""){
                Toast.makeText(this@KeluhanPassangerActivity, "Kepuasan Wajib diisi!!", Toast.LENGTH_SHORT).show()
            }else{
                lifecycleScope.launch {
                    progressBar.show()
                    val resultSql = withContext(Dispatchers.IO){
                        createTicket(sessionLogin.getTId(), token, etKeluhan.text.toString(), etSaran.text.toString(), selectedText)
                    }
                    when(resultSql){
                        "Berhasil" -> {
                            progressBar.dismiss()
                            dialogKeluhan.dismiss()
                            keluhan = etKeluhan.text.toString()
                            saran = etSaran.text.toString()
                            binding.tvKeluhanKamu.text = "Keluhan: $keluhan & Saran: $saran \n Kepuasan: $kepuasan"
                        }
                        else -> {
                            progressBar.dismiss()
                            Toast.makeText(this@KeluhanPassangerActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        dialogKeluhan.show()
        dialogKeluhan.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogKeluhan.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogKeluhan.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialogKeluhan.window?.setGravity(Gravity.CENTER)
    }

    private fun getSelectedChipIndex(chipGroup: ChipGroup, checkedId: Int): Int {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip // Gunakan safe cast
            if (chip?.id == checkedId) {
                return i
            }
        }
        return -1
    }

    private fun createTicket(tId: Int, kDevice: String, kKeluhan: String, kSaran: String, kKepuasan: String):String{
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "EXEC USP_S_Keluhan_Update @TId = ?, @KDevice = ?, @KKeluhan = ?, @KSaran = ?, @KKepuasan = ?"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, tId)
                preparedStatement.setString(2, kDevice)
                preparedStatement.setString(3, kKeluhan)
                preparedStatement.setString(4, kSaran)
                preparedStatement.setString(5, kKepuasan)
                preparedStatement.execute()
                "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }

    private fun getApproveKeluhan(tId: Int, kDevice: String, kKeluhan: String, kSaran: String, kKepuasan: String):String{
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "EXEC USP_S_Keluhan_Update @TId = ?, @KDevice = ?, @KKeluhan = ?, @KSaran = ?, @KKepuasan = ?"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, tId)
                preparedStatement.setString(2, kDevice)
                preparedStatement.setString(3, kKeluhan)
                preparedStatement.setString(4, kSaran)
                preparedStatement.setString(5, kKepuasan)
                preparedStatement.execute()
                "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }

    private fun getTicket(tId: Int):String{
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "EXEC USP_S_Ticket_Query @TId = ?"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, tId)
                val result = preparedStatement.executeQuery()
                while (result.next()){
                    keluhan = result.getString("KKeluhan")
                    saran = result.getString("KSaran")
                    kepuasan = result.getString("KKepuasan")
                    reasonApprove = result.getString("KApproveReason")
                    approve = result.getInt("KApprove")
                }
                "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                e.toString().replace("java.sql.SQLException: ", "")
            }
        }
        return "Tidak terhubung ke server...."
    }
}