package com.example.notifbus.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notifbus.R
import com.example.notifbus.adapter.ListPenumpangAdapter
import com.example.notifbus.adapter.ListTicketAdapter
import com.example.notifbus.databinding.ActivityTicketBinding
import com.example.notifbus.model.Customer
import com.example.notifbus.model.Ticket
import com.example.notifbus.server.Connection
import com.example.notifbus.tools.SessionLogin
import com.example.notifbus.tools.SwipeToDeleteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TIcketActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTicketBinding
    private lateinit var connection: Connection
    private lateinit var sessionLogin: SessionLogin

    private lateinit var progressBar: ProgressDialog
    private lateinit var mTicket : MutableList<Ticket>
    private lateinit var mCustomer : MutableList<Customer>

    private lateinit var listPenumpangAdapter: ListPenumpangAdapter
    private lateinit var listTicketAdapter: ListTicketAdapter

    private var selectPenumpang: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connection = Connection()
        sessionLogin = SessionLogin(this)
        listTicketAdapter = ListTicketAdapter()

        progressBar = ProgressDialog(this)
        progressBar.setMessage("Loading..")
        progressBar.setCanceledOnTouchOutside(false)
        progressBar.setCancelable(false)

        binding.rvListTIcket.layoutManager = LinearLayoutManager(this)
        binding.rvListTIcket.setHasFixedSize(false)

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                getDataCustomer()
            }
        }

        showListTicket()

        binding.imgBackTicket.setOnClickListener {
            finish()
        }

        binding.cvCreateTicket.setOnClickListener {
            dialogTicket()
        }
    }

    private fun dialogTicket(){
        val dialogAdd = Dialog(this)
        dialogAdd.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAdd.setContentView(R.layout.dialog_add_ticket)
        dialogAdd.setCanceledOnTouchOutside(false)

        val btnClose = dialogAdd.findViewById<ImageView>(R.id.imgCloseDialogAddTicket)
        val btnTambah = dialogAdd.findViewById<Button>(R.id.btnAddDialogTicket)
        val etNama = dialogAdd.findViewById<EditText>(R.id.etPilihCustDialogAddTicket)
        val etPhone = dialogAdd.findViewById<EditText>(R.id.etNoHpDialogAddTicket)
        val etDari = dialogAdd.findViewById<EditText>(R.id.etDariDialogAddTicket)
        val etTujuan = dialogAdd.findViewById<EditText>(R.id.etTujuanDialogAddTicket)
        val etHarga = dialogAdd.findViewById<EditText>(R.id.etHargaDialogAddTicket)
        val etTglKeberangkatan = dialogAdd.findViewById<EditText>(R.id.etTglBerangkatDialogAddTicket)

        btnClose.setOnClickListener {
            dialogAdd.dismiss()
        }

        etNama.setOnClickListener {
            val dialogListPenumpang = Dialog(this)
            dialogListPenumpang.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogListPenumpang.setContentView(R.layout.dialog_list_penumpang)

            val btnCloseListPenumpang = dialogListPenumpang.findViewById<ImageView>(R.id.imgCloseDialogListPenumpang)
            val rvListPenumpang = dialogListPenumpang.findViewById<RecyclerView>(R.id.rvListPilihCust)

            btnCloseListPenumpang.setOnClickListener {
                dialogListPenumpang.dismiss()
            }

            rvListPenumpang.layoutManager = LinearLayoutManager(this)
            rvListPenumpang.setHasFixedSize(false)

            listPenumpangAdapter = ListPenumpangAdapter(mCustomer)
            rvListPenumpang.adapter = listPenumpangAdapter
            listPenumpangAdapter.setOnItemClickCallback(object : ListPenumpangAdapter.OnItemClickCallback{
                override fun onItemClicked(customer: Customer) {
                    selectPenumpang = customer
                    etNama.setText(customer.name)
                    etPhone.setText(customer.noHp)
                    dialogListPenumpang.dismiss()
                }
            })

            dialogListPenumpang.show()
            dialogListPenumpang.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialogListPenumpang.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogListPenumpang.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialogListPenumpang.window?.setGravity(Gravity.CENTER)
        }

        etTglKeberangkatan.setOnClickListener {
            val tglLahir: Calendar = Calendar.getInstance()
            val date =
                DatePickerDialog.OnDateSetListener { view1: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tglLahir.set(Calendar.YEAR, year)
                    tglLahir.set(Calendar.MONTH, monthOfYear)
                    tglLahir.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val strFormatDefault = "yyyy-MM-dd"
                    val simpleDateFormat =
                        SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    etTglKeberangkatan.setText(simpleDateFormat.format(tglLahir.time))
                }

            DatePickerDialog(
                this, date,
                tglLahir.get(Calendar.YEAR),
                tglLahir.get(Calendar.MONTH),
                tglLahir.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTambah.setOnClickListener {
            if(etNama.text.isEmpty() || etDari.text.isEmpty() || etTujuan.text.isEmpty() || etHarga.text.isEmpty() || etTglKeberangkatan.text.isEmpty()){
                Toast.makeText(this, "Semua wajib diisi!!", Toast.LENGTH_SHORT).show()
            }else{
                progressBar.show()
                lifecycleScope.launch {
                    val resultSql = withContext(Dispatchers.IO){
                        createTicket(selectPenumpang?.custId!!.toInt(), etDari.text.toString(), etTujuan.text.toString(), etHarga.text.toString(), etTglKeberangkatan.text.toString())
                    }
                    when(resultSql){
                        "Berhasil" -> {
                            progressBar.dismiss()
                            listTicketAdapter.setData(mTicket)

                            dialogAdd.dismiss()
                        }
                        else -> {
                            progressBar.dismiss()
                            listTicketAdapter.setData(mTicket)
                            Toast.makeText(this@TIcketActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        dialogAdd.show()
        dialogAdd.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogAdd.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAdd.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialogAdd.window?.setGravity(Gravity.CENTER)
    }

    private fun showListTicket(){
        progressBar.show()
        lifecycleScope.launch {
            val resultSql = withContext(Dispatchers.IO){
                getDataTicket()
            }
            when(resultSql){
                "Berhasil" -> {
                    progressBar.dismiss()
                    listTicketAdapter.setData(mTicket)
                    binding.rvListTIcket.adapter = listTicketAdapter
                    listTicketAdapter.setOnItemClickCallback(object : ListTicketAdapter.OnItemClickCallback{
                        override fun onItemClicked(ticket: Ticket) {
                            val dialogGender = AlertDialog.Builder(this@TIcketActivity)
                            dialogGender.setTitle("Send Ticket By")
                            val arrayGender = arrayOf("Email", "Whatsapp")
                            dialogGender.setItems(arrayGender){_, p ->
                                when(p){
                                    0 -> {
                                        sendEmail(ticket.custEmail.toString(), "Hallo ${ticket.custName} Ticket Kamu Berhasil dipesan..", "")
                                    }
                                    1 -> {

                                    }
                                }
                            }
                            dialogGender.show()
                        }
                    })
                    val swipeHandler = object : SwipeToDeleteCallback(this@TIcketActivity) {
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            lifecycleScope.launch {
                                progressBar.show()
                                val result = withContext(Dispatchers.IO){
                                    voidItemTicket(mTicket[viewHolder.adapterPosition].tId!!.toInt())
                                }
                                when(result){
                                    "Berhasil" -> {
                                        progressBar.dismiss()
                                        listTicketAdapter.setData(mTicket)
                                    }
                                    else -> {
                                        progressBar.dismiss()
                                        Toast.makeText(this@TIcketActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                    val itemTouchHelper = ItemTouchHelper(swipeHandler)
                    itemTouchHelper.attachToRecyclerView(binding.rvListTIcket)
                }
                else -> {
                    progressBar.dismiss()
                    Toast.makeText(this@TIcketActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun createTicket(tCustId: Int, tDari: String, tTujuan: String, tHarga: String, tTglKeberangkatan: String):String{
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "EXEC USP_S_Ticket_Update @CustId = ?, @TDari = ?, @TTujuan = ?, @THarga = ?, @TTglKeberangkatan = ?, @CreatedBy = ?"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, tCustId)
                preparedStatement.setString(2, tDari)
                preparedStatement.setString(3, tTujuan)
                preparedStatement.setString(4, tHarga)
                preparedStatement.setString(5, tTglKeberangkatan)
                preparedStatement.setString(6, sessionLogin.getUserName())
                val result = preparedStatement.executeQuery()
                mTicket.clear()
                while(result.next()){
                    val ticket  = Ticket().apply {
                        this.tId = result.getInt("TId")
                        this.custName = result.getString("CustName")
                        this.custPhone = result.getString("CustPhone")
                        this.custEmail = result.getString("CustEmail")
                        this.ticketNo = result.getString("TTicketNo")
                        this.tDari = result.getString("TDari")
                        this.tTujuan = result.getString("TTujuan")
                        this.tTglKeberangkatan = result.getString("TTglKeberangkatan")
                    }
                    mTicket.add(ticket)
                }
                "Berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }

    private fun voidItemTicket(tId: Int):String{
        mTicket = mutableListOf()
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "UPDATE S_Ticket Set VoidBy = ?, VoidDate = GETDATE(), VoidRemarks = '' WHERE TId = ? " +
                        "SELECT SC.CustName, SC.CustPhone,SC.CustEmail, ST.* FROM S_Ticket ST\n" +
                        "                        JOIN S_Customer SC ON SC.CustId = ST.CustId\n" +
                        "                        WHERE ST.VoidBy IS NULL\n" +
                        "                        ORDER BY st.TId"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setString(1, sessionLogin.getUserName())
                preparedStatement.setInt(2, tId)
                val result = preparedStatement.executeQuery()
                mTicket.clear()
                while(result.next()){
                    val ticket  = Ticket().apply {
                        this.tId = result.getInt("TId")
                        this.custName = result.getString("CustName")
                        this.custPhone = result.getString("CustPhone")
                        this.custEmail = result.getString("CustEmail")
                        this.ticketNo = result.getString("TTicketNo")
                        this.tDari = result.getString("TDari")
                        this.tTujuan = result.getString("TTujuan")
                        this.tTglKeberangkatan = result.getString("TTglKeberangkatan")
                    }
                    mTicket.add(ticket)
                }
                "Berhasil"
            }catch (e:SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }

    private fun getDataCustomer():String{
        mCustomer = mutableListOf()
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "Select * From S_Customer Order By CustId"
                val statement = conn.createStatement()
                val result = statement.executeQuery(query)
                mCustomer.clear()
                while(result.next()){
                    val customer = Customer().apply {
                        this.custId = result.getInt("CustId")
                        this.name = result.getString("CustName")
                        this.bDate = result.getString("CustBDate")
                        this.gender = result.getString("CustGender")
                        this.noHp = result.getString("CustPhone")
                        this.email = result.getString("CustEmail")
                    }
                    mCustomer.add(customer)
                }
                "Berhasil"
            }catch (e:SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }

    private fun getDataTicket():String{
        mTicket = mutableListOf()
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "SELECT SC.CustName, SC.CustPhone,SC.CustEmail, ST.* FROM S_Ticket ST\n" +
                        "JOIN S_Customer SC ON SC.CustId = ST.CustId\n" +
                        "WHERE ST.VoidBy IS NULL\n" +
                        "ORDER BY st.TId"
                val statement = conn.createStatement()
                val result = statement.executeQuery(query)
                mTicket.clear()
                while(result.next()){
                    val ticket  = Ticket().apply {
                        this.tId = result.getInt("TId")
                        this.custName = result.getString("CustName")
                        this.custPhone = result.getString("CustPhone")
                        this.custEmail = result.getString("CustEmail")
                        this.ticketNo = result.getString("TTicketNo")
                        this.tDari = result.getString("TDari")
                        this.tTujuan = result.getString("TTujuan")
                        this.tTglKeberangkatan = result.getString("TTglKeberangkatan")
                    }
                    mTicket.add(ticket)
                }
                "Berhasil"
            }catch (e:SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }
}