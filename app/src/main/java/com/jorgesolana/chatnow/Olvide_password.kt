package com.jorgesolana.chatnow

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class Olvide_password : AppCompatActivity() {

    // Variables para manejar las vistas y Firebase
    private lateinit var L_Et_email : EditText
    private lateinit var Btn_enviar_correo : MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_olvide_password)
        InicializarVistas()

        Btn_enviar_correo.setOnClickListener {
            ValidarInformacion()
        }
    }
    // Método para inicializar las vistas
    private fun InicializarVistas(){
        L_Et_email = findViewById(R.id.L_Et_email)
        Btn_enviar_correo = findViewById(R.id.Btn_enviar_correo)
        firebaseAuth = FirebaseAuth.getInstance()
        //Configuramos el progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.esperar))
        progressDialog.setCanceledOnTouchOutside(false)
    }
    // Método para validar el correo ingresado por el usuario
    private fun ValidarInformacion() {
        //Obtener el email
        email = L_Et_email.text.toString().trim()
        if (email.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.ingrese_correo), Toast.LENGTH_SHORT).show()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(applicationContext, getString(R.string.correo_no_valido), Toast.LENGTH_SHORT).show()
        }else{
            RecuperarPassword()
        }

    }
    // Método para enviar el correo de recuperación de contraseña
    private fun RecuperarPassword() {
        progressDialog.setMessage(getString(R.string.envio_cambio_contrasena)+ " $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
            }
    }

}