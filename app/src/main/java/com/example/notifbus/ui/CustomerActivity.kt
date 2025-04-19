package com.example.notifbus.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notifbus.R
import com.example.notifbus.adapter.CustomerAdapter
import com.example.notifbus.databinding.ActivityCustomerBinding
import com.example.notifbus.model.Customer
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


class CustomerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCustomerBinding
    private lateinit var connection: Connection
    private lateinit var sessionLogin: SessionLogin

    private lateinit var progressBar: ProgressDialog
    private lateinit var mCustomemr: MutableList<Customer>
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connection = Connection()
        sessionLogin = SessionLogin(this)
        customerAdapter = CustomerAdapter()

        progressBar = ProgressDialog(this)
        progressBar.setMessage("Loading..")
        progressBar.setCanceledOnTouchOutside(false)
        progressBar.setCancelable(false)

        binding.rvListCustomer.layoutManager = LinearLayoutManager(this)
        binding.rvListCustomer.setHasFixedSize(false)

        showListCustomer()

        binding.imgBackCustomer.setOnClickListener {
            finish()
        }

        binding.cvCreateCustomer.setOnClickListener {
            dialogCustomer(null)
        }
    }

    private fun dialogCustomer(customer: Customer?){
        val dialogAdd = Dialog(this)
        dialogAdd.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAdd.setContentView(R.layout.dialog_add_customer)
        dialogAdd.setCanceledOnTouchOutside(false)

        val btnClose = dialogAdd.findViewById<ImageView>(R.id.imgCloseDialogAddCustomer)
        val btnTambah = dialogAdd.findViewById<Button>(R.id.btnAddDialogCustomer)
        val etNama = dialogAdd.findViewById<EditText>(R.id.etNamaLengkapDialogAddCustomer)
        val etBDate = dialogAdd.findViewById<EditText>(R.id.etTglLahirDialogAddCustomer)
        val etKelamin = dialogAdd.findViewById<EditText>(R.id.etKelaminDialogAddCustomer)
        val etPhone = dialogAdd.findViewById<EditText>(R.id.etNoHpDialogAddCustomer)
        val etEmail = dialogAdd.findViewById<EditText>(R.id.etEmailDialogAddCustomer)

        if(customer != null){
            etNama.setText(customer.name)
            etBDate.setText(customer.bDate)
            etPhone.setText(customer.noHp)
            etKelamin.setText(customer.gender)
            etEmail.setText(customer.email)

            btnTambah.text = "UBAH"
        }

        btnClose.setOnClickListener {
            dialogAdd.dismiss()
        }

        etKelamin.setOnClickListener{
            val dialogGender = AlertDialog.Builder(this)
            dialogGender.setTitle("Silahkan pilih kelamin anda..")
            val arrayGender = arrayOf("Laki-Laki", "Perempuan")
            dialogGender.setItems(arrayGender){_, p ->
                when(p){
                    0 -> {
                        etKelamin.setText("Laki-Laki")
                    }
                    1 -> {
                        etKelamin.setText("Perempuan")
                    }
                }
            }
            dialogGender.show()
        }

        etBDate.setOnClickListener {
            val tglLahir: Calendar = Calendar.getInstance()
            val date =
                OnDateSetListener { view1: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tglLahir.set(Calendar.YEAR, year)
                    tglLahir.set(Calendar.MONTH, monthOfYear)
                    tglLahir.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val strFormatDefault = "yyyy-MM-dd"
                    val simpleDateFormat =
                        SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    etBDate.setText(simpleDateFormat.format(tglLahir.time))
                }

            DatePickerDialog(
                this, date,
                tglLahir.get(Calendar.YEAR),
                tglLahir.get(Calendar.MONTH),
                tglLahir.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTambah.setOnClickListener {
            if(etNama.text.isEmpty() || etEmail.text.isEmpty() || etBDate.text.isEmpty() || etKelamin.text.isEmpty() || etPhone.text.isEmpty()){
                Toast.makeText(this, "Semua wajib diisi!!", Toast.LENGTH_SHORT).show()
            }else{
                progressBar.show()
                lifecycleScope.launch {
                    val resultSql = withContext(Dispatchers.IO){
                        createCustomer(customer?.custId?: 0, etNama.text.toString(), etBDate.text.toString(), etKelamin.text.toString(), etPhone.text.toString(), etEmail.text.toString())
                    }
                    when (resultSql){
                        "Berhasil" -> {
                            progressBar.dismiss()
                            dialogAdd.dismiss()
                            customerAdapter.setData(mCustomemr)
//                            Toast.makeText(this@CustomerActivity, "Data berhasil ditambahkan...", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            progressBar.dismiss()
                            Toast.makeText(this@CustomerActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
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

    private fun showListCustomer(){
        progressBar.show()
        lifecycleScope.launch {
            val resultSql = withContext(Dispatchers.IO){
                getDataCustomer()
            }
            when(resultSql){
                "Berhasil" -> {
                    progressBar.dismiss()
                    customerAdapter.setData(mCustomemr)
                    binding.rvListCustomer.adapter = customerAdapter
                    customerAdapter.setOnItemClickCallback(object : CustomerAdapter.OnItemClickCallback{
                        override fun onItemClicked(customer: Customer) {
                            dialogCustomer(customer)
                        }
                    })
                    val swipeHandler = object : SwipeToDeleteCallback(this@CustomerActivity) {
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            lifecycleScope.launch {
                                progressBar.show()
                                val result = withContext(Dispatchers.IO){
                                    deleteItemCustomer(mCustomemr[viewHolder.adapterPosition].custId!!.toInt())
                                }
                                when(result){
                                    "Berhasil" -> {
                                        progressBar.dismiss()
                                        customerAdapter.setData(mCustomemr)
                                    }
                                    else -> {
                                        progressBar.dismiss()
                                        customerAdapter.setData(mCustomemr)
                                        Toast.makeText(this@CustomerActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                    val itemTouchHelper = ItemTouchHelper(swipeHandler)
                    itemTouchHelper.attachToRecyclerView(binding.rvListCustomer)
                }
                else -> {
                    progressBar.dismiss()
                    Toast.makeText(this@CustomerActivity, resultSql.replace("java.sql.SQLException: ", ""), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteItemCustomer(custId: Int):String{
        mCustomemr = mutableListOf()
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "DELETE S_Customer WHERE CustId = ? " +
                        "Select * From S_Customer Order By CustId"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, custId)
                val result = preparedStatement.executeQuery()
                mCustomemr.clear()
                while(result.next()){
                    val customer = Customer().apply {
                        this.custId = result.getInt("CustId")
                        this.name = result.getString("CustName")
                        this.bDate = result.getString("CustBDate")
                        this.gender = result.getString("CustGender")
                        this.noHp = result.getString("CustPhone")
                        this.email = result.getString("CustEmail")
                    }
                    mCustomemr.add(customer)
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
        mCustomemr = mutableListOf()
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "Select * From S_Customer Order By CustId"
                val statement = conn.createStatement()
                val result = statement.executeQuery(query)
                mCustomemr.clear()
                while(result.next()){
                    val customer = Customer().apply {
                        this.custId = result.getInt("CustId")
                        this.name = result.getString("CustName")
                        this.bDate = result.getString("CustBDate")
                        this.gender = result.getString("CustGender")
                        this.noHp = result.getString("CustPhone")
                        this.email = result.getString("CustEmail")
                    }
                    mCustomemr.add(customer)
                }
                "Berhasil"
            }catch (e:SQLException){
                e.printStackTrace()
                e.toString()
            }
        }
        return "Tidak terhubung ke server...."
    }

    private fun createCustomer(custId: Int, name: String, bDate: String, gender: String, noHp: String, email: String):String{
        val conn = connection.connection(this)
        if (conn != null){
            return try {
                val query = "EXEC USP_S_Customer_Update @CustId = ?, @CustName = ?, @CustBDate = ?, @CustGender = ?, @CustPhone = ?, @CustEmail = ?, @LastModBy = ?"
                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setInt(1, custId)
                preparedStatement.setString(2, name)
                preparedStatement.setString(3, bDate)
                preparedStatement.setString(4, gender)
                preparedStatement.setString(5, noHp)
                preparedStatement.setString(6, email)
                preparedStatement.setString(7, sessionLogin.getUserName())
                val result = preparedStatement.executeQuery()
                mCustomemr.clear()
                while(result.next()){
                    val customer = Customer().apply {
                        this.custId = result.getInt("CustId")
                        this.name = result.getString("CustName")
                        this.bDate = result.getString("CustBDate")
                        this.gender = result.getString("CustGender")
                        this.noHp = result.getString("CustPhone")
                        this.email = result.getString("CustEmail")
                    }
                    mCustomemr.add(customer)
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