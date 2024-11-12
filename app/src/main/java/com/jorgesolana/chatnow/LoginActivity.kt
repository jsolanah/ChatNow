package com.jorgesolana.chatnow

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Variables para las vistas y Firebase
    private lateinit var et_email : EditText
    private lateinit var et_contrasenia : EditText
    private lateinit var btn_login: Button
    private lateinit var auth : FirebaseAuth
    private lateinit var txt_ir_registros : TextView
    private lateinit var txt_olvide_password : TextView
    private lateinit var progressDialog : ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Inicializamos las vistas y variables
        inicializarVariables()
        // Acción para redirigir al usuario a la pantalla de recuperación de contraseña
        txt_olvide_password.setOnClickListener {
            startActivity(Intent(this@LoginActivity, Olvide_password::class.java))
        }
        // Acción para intentar el login
        btn_login.setOnClickListener{
            validarDatos()
        }
        // Acción para intentar el login
        txt_ir_registros.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistroActivity::class.java)
            startActivity(intent) }

    }

    // Método para inicializar las vistas y objetos
    private fun inicializarVariables(){
        et_email = findViewById(R.id.Et_email)
        et_contrasenia = findViewById(R.id.Et_contrasenia)
        btn_login = findViewById(R.id.Btn_login)
        auth = FirebaseAuth.getInstance()
        txt_ir_registros = findViewById(R.id.txt_ir_registros)
        txt_olvide_password = findViewById(R.id.Txt_olvide_password)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Iniciando sesion")
        progressDialog.setCanceledOnTouchOutside(false)

    }
    // Método para validar los datos ingresados por el usuario
    private fun validarDatos(){
        val email : String = et_email.text.toString()
        val contrasenia :  String = et_contrasenia.text.toString()

        if (email.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.ingrese_correo), Toast.LENGTH_SHORT).show()
        }
        if (contrasenia.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.ingrese_contrasena), Toast.LENGTH_SHORT).show()
        }else{
            loginUsuario(email, contrasenia)
        }

    }
    // Método para intentar hacer login con Firebase
    private fun loginUsuario(email : String, contrasenia : String){
        progressDialog.setMessage(getString(R.string.esperar))
        progressDialog.show()
        auth.signInWithEmailAndPassword(email, contrasenia)
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    //Toast.makeText(applicationContext, "Ha iniciado sesion", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }else{
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, getString(R.string.error), Toast.LENGTH_SHORT).show()

                }

            }.addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "{${e.message}}", Toast.LENGTH_SHORT).show()

            }
    }

}