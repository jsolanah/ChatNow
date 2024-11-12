package com.jorgesolana.chatnow

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistroActivity : AppCompatActivity() {

    // Variables para las vistas y Firebase
    private lateinit var R_Et_nombre_usuario : EditText
    private lateinit var R_Et_email : EditText
    private lateinit var R_Et_contrasenia : EditText
    private lateinit var R_Et_r_contrasenia : EditText
    private lateinit var Btn_registrar : Button
    private lateinit var progressDialog : ProgressDialog

    private lateinit var auth : FirebaseAuth
    private lateinit var reference : DatabaseReference
    var uid : String = "" // ID único del usuario que se generará al registrarse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        inicializarVariables()
        // Cuando el usuario hace clic en "Registrar", validamos los datos
        Btn_registrar.setOnClickListener{
            validarDatos();
        }
    }
    // Método para inicializar las vistas
    private fun inicializarVariables(){
        R_Et_nombre_usuario = findViewById(R.id.R_Et_nombre_usuario)
        R_Et_email = findViewById(R.id.R_Et_email)
        R_Et_contrasenia = findViewById(R.id.R_Et_contrasenia)
        R_Et_r_contrasenia = findViewById(R.id.R_Et_r_contrasenia)
        Btn_registrar = findViewById(R.id.Btn_registrar)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.registrando_informacion))
        progressDialog.setCanceledOnTouchOutside(false)
    }
    // Método para validar los datos ingresados por el usuario
    private fun validarDatos() {
        // Obtenemos los datos ingresados en los EditText
        val nombre_usuario : String = R_Et_nombre_usuario.text.toString()
        val email : String = R_Et_email.text.toString()
        val contrasenia : String = R_Et_contrasenia.text.toString()
        val r_contrasenia : String = R_Et_r_contrasenia.text.toString()
        // Verificamos que los campos no estén vacíos
        if (nombre_usuario.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.ingrese_usuario), Toast.LENGTH_SHORT).show()
        }else if (email.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.ingrese_correo), Toast.LENGTH_SHORT).show()
        }else if (contrasenia.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.ingrese_contrasena), Toast.LENGTH_SHORT).show()
        }else if (r_contrasenia.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.repita_contrasena), Toast.LENGTH_SHORT).show()
        }else if (!contrasenia.equals(r_contrasenia)){
            Toast.makeText(applicationContext, getString(R.string.no_coinciden_contrasena), Toast.LENGTH_SHORT).show()
        }else{
            registrarUsuario(email, contrasenia)
        }
    }
    // Método para registrar al usuario en Firebase
    private fun registrarUsuario(email: String, contrasenia: String) {
        progressDialog.setMessage("Espere por favor")
        progressDialog.show()
        // Usamos FirebaseAuth para crear el usuario con el correo y la contraseña proporcionados
        auth.createUserWithEmailAndPassword(email, contrasenia)
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    uid = auth.currentUser!!.uid
                    reference = FirebaseDatabase.getInstance().reference.child("Usuarios").child(uid)
                    // Creamos un mapa con los datos del usuario a almacenar en Firebase
                    val hashmap = HashMap<String, Any>()
                    val n_nombre_usuario : String = R_Et_nombre_usuario.text.toString()
                    val n_email : String = R_Et_email.text.toString()

                    hashmap["uid"] = uid
                    hashmap["n_usuario"] = n_nombre_usuario
                    hashmap["email"] = n_email
                    hashmap["imagen"] = ""
                    hashmap["buscar"] = n_nombre_usuario.lowercase()
                    hashmap["nombres"] = ""
                    hashmap["apellidos"] = ""
                    hashmap["edad"] = ""
                    hashmap["profesion"] = ""
                    hashmap["domicilio"] = ""
                    hashmap["telefono"] = ""
                    hashmap["estado"] = "offline"
                    hashmap["proveedor"] = "Email"
                    // Actualizamos los datos del usuario en Firebase Realtime Database
                    reference.updateChildren(hashmap).addOnCompleteListener { task2 ->
                        if (task2.isSuccessful){
                            val intent = Intent(this@RegistroActivity, MainActivity::class.java)
                            Toast.makeText(applicationContext, getString(R.string.registro_exitoso), Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                            finish()

                        } else {
                            Toast.makeText(applicationContext, getString(R.string.error_actualizar_usuario), Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener{e->
                        Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()

                    }

                }else{
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext,getString( R.string.error), Toast.LENGTH_SHORT).show()

                }

            }.addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

}