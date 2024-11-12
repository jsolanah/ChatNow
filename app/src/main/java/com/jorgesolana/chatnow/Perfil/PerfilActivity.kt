package com.jorgesolana.chatnow.Perfil

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hbb20.CountryCodePicker
import com.jorgesolana.chatnow.MainActivity
import com.jorgesolana.chatnow.Modelo.Usuario
import com.jorgesolana.chatnow.R

class PerfilActivity : AppCompatActivity() {

    // Definición de las vistas
    private lateinit var p_imagen : ImageView
    private lateinit var p_n_usuario : TextView
    private lateinit var p_email : TextView
    private lateinit var p_nombres : EditText
    private lateinit var p_apellidos : EditText
    private lateinit var p_profesion : EditText
    private lateinit var p_domicilio : EditText
    private lateinit var p_edad : EditText
    private lateinit var p_telefono : TextView
    private lateinit var btn_guardar : Button
    private lateinit var  editar_imagen : ImageView
    private lateinit var p_proveedor : TextView
    private lateinit var editar_telefono : ImageView
    private lateinit var btn_verificar : MaterialButton

    // Variables de Firebase
    var user : FirebaseUser?=null
    var reference : DatabaseReference?=null

    // Variables para teléfono
    private var codigoTelefono = ""
    private var numeroTel = ""
    private var codigo_numero_telefono = ""

    // ProgressDialog para mostrar mientras se realizan tareas largas
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        // Inicializamos las vistas y variables
        inicializarVariables()
        // Obtenemos los datos del usuario desde Firebase
        obtenerDatos()
        // Comprobamos el estado de la cuenta (verificada o no)
        estadoCuenta()

        // Acción cuando el botón de guardar es presionado
        btn_guardar.setOnClickListener{
            // Actualizamos la información del perfil
            actualizarInformacion()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        // Acción cuando se desea editar la imagen de perfil
        editar_imagen.setOnClickListener{
            val intent = Intent(applicationContext, EditarImagenPerfil::class.java)
            startActivity(intent)
        }

        // Acción cuando se desea editar el teléfono
        editar_telefono.setOnClickListener{
            establecerNumTel()

        }
        // Acción cuando se presiona el botón de verificar cuenta
        btn_verificar.setOnClickListener {
            if (user!!.isEmailVerified){
                cuentaVerificada()
            }else{
                confirmarEnvio()
            }
        }

    }
    // Método para confirmar si el usuario quiere enviar el correo de verificación
    private fun confirmarEnvio() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.verificar_cuenta))
            .setMessage(getString(R.string.correo_verificacion)+" ${user!!.email}")
            .setPositiveButton(getString(R.string.enviar)){d, e ->
                enviarEmailConfirmacion()
            }
            .setNegativeButton(getString(R.string.opcion_cancelar)){d, e ->
                d.dismiss()
            }.show()
    }

    // Método para enviar el correo de verificación al usuario
    private fun enviarEmailConfirmacion() {
        progressDialog.setMessage(getString(R.string.envio_correo_verificacion) + " ${user!!.email}")
        progressDialog.show()

        user!!.sendEmailVerification().addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(applicationContext, getString(R.string.revise_correo) +"  ${user!!.email}", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener{e->
            progressDialog.dismiss()
            Toast.makeText(applicationContext, getString(R.string.fallo_operacion)+ " ${e.message}}", Toast.LENGTH_SHORT).show()
        }

    }
    // Método para actualizar el estado de verificación de la cuenta en la UI
    private fun estadoCuenta(){
        if (user!!.isEmailVerified){
            btn_verificar.text = getString(R.string.verificado)
        }else{
            btn_verificar.text = getString(R.string.no_verificaddo)
        }
    }
    // Método para establecer el número de teléfono del usuario
    private fun establecerNumTel() {
        val establecer_telefono : EditText
        val selectorCodigoPais : CountryCodePicker
        val btn_aceptar_telefono : MaterialButton

        // Dialogo para ingresar el número de teléfono
        val dialog = Dialog(this@PerfilActivity)
        dialog.setContentView(R.layout.cuadro_d_establecer_telefono)
        establecer_telefono = dialog.findViewById(R.id.establecer_telefono)
        selectorCodigoPais = dialog.findViewById(R.id.selectorCodigoPais)
        btn_aceptar_telefono = dialog.findViewById(R.id.btn_aceptar_telefono)

        btn_aceptar_telefono.setOnClickListener {
            // Concatenamos el código de país con el número de teléfono
            codigoTelefono = selectorCodigoPais.selectedCountryCodeWithPlus
            numeroTel = establecer_telefono.text.toString().trim()
            codigo_numero_telefono = codigoTelefono + numeroTel

            if (numeroTel.isEmpty()){
                Toast.makeText(applicationContext, getString(R.string.ingrese_numero_telefonico) , Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else{
                p_telefono.text = codigo_numero_telefono
                dialog.dismiss()
            }
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

    }

    // Inicializa las vistas y variables de Firebase
    private fun inicializarVariables(){
        p_imagen = findViewById(R.id.P_imagen)
        p_n_usuario = findViewById(R.id.P_n_usuario)
        p_email = findViewById(R.id.P_email)
        p_nombres = findViewById(R.id.p_nombre)
        p_apellidos = findViewById(R.id.p_apellido)
        p_profesion = findViewById(R.id.p_profesion)
        p_domicilio = findViewById(R.id.p_domicilio)
        p_edad = findViewById(R.id.p_edad)
        p_telefono = findViewById(R.id.p_telefono)
        btn_guardar = findViewById(R.id.btn_guardar)
        editar_imagen = findViewById(R.id.editar_imagen)
        p_proveedor = findViewById(R.id.p_proveedor)
        editar_telefono = findViewById(R.id.editar_telefono)
        btn_verificar = findViewById(R.id.btn_verificar)
        // Inicializamos el ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.esperar) )
        progressDialog.setCanceledOnTouchOutside(false)
        // Inicializamos Firebase
        user = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().reference.child("Usuarios").child(user!!.uid)

    }

    // Método para obtener los datos del usuario desde Firebase
    private fun obtenerDatos(){
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    // Obtenemos los datos de Firebase
                    val usuario : Usuario?= snapshot.getValue(Usuario::class.java)
                    val str_n_usuario = usuario!!.getN_Usuario()
                    val str_email = usuario.getEmail()
                    val str_proveedor = usuario.getProveedor()
                    val str_nombre = usuario.getNombres()
                    val str_apellido = usuario.getApellidos()
                    val str_profesion = usuario.getProfesion()
                    val str_domicilio = usuario.getDomicilio()
                    val str_edad = usuario.getEdad()
                    val str_telefono = usuario.getTelefono()

                    // Seteamos la informacion en las vistas
                    p_n_usuario.text = str_n_usuario
                    p_email.text = str_email
                    p_proveedor.text = str_proveedor
                    p_nombres.setText(str_nombre)
                    p_apellidos.setText(str_apellido)
                    p_profesion.setText(str_profesion)
                    p_domicilio.setText(str_domicilio)
                    p_edad.setText(str_edad)
                    p_telefono.setText(str_telefono)
                    // Cargamos la imagen de perfil usando Glide
                    Glide.with(applicationContext).load(usuario.getImagen()).placeholder(R.drawable.ic_item_usuario).into(p_imagen)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    // Método para actualizar la información del usuario en Firebase
    private fun actualizarInformacion(){
        val str_nombres = p_nombres.text.toString()
        val str_apellidos = p_apellidos.text.toString()
        val str_domicilio = p_domicilio.text.toString()
        val str_profesion = p_profesion.text.toString()
        val str_edad = p_edad.text.toString()
        val str_telefono = p_telefono.text.toString()
        // Creamos un mapa con los nuevos valores
        val hasmap = HashMap<String, Any>()
        hasmap["nombres"] = str_nombres
        hasmap["apellidos"] = str_apellidos
        hasmap["profesion"] = str_profesion
        hasmap["domicilio"] = str_domicilio
        hasmap["edad"] = str_edad
        hasmap["telefono"] = str_telefono
        // Actualizamos los datos en la base de datos
        reference!!.updateChildren(hasmap).addOnCompleteListener{task ->
            if (task.isSuccessful){
                Toast.makeText(applicationContext, getString(R.string.datos_actualizados) , Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext, getString(R.string.datos_no_actualizados), Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener{e ->
            Toast.makeText(applicationContext, getString(R.string.error) +" ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    // Método para manejar el estado de la cuenta cuando está verificada
    private fun cuentaVerificada(){

        val BtnEntendidoVerificado : MaterialButton
        val dialog = Dialog(this@PerfilActivity)

        dialog.setContentView(R.layout.cuadro_d_cuenta_verificada)

        BtnEntendidoVerificado = dialog.findViewById(R.id.btn_entendidoVerificado)
        BtnEntendidoVerificado.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

    }

    /*private fun actualizarEstado(estado : String){
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios")
            .child(user!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado
        reference!!.updateChildren(hashMap)

    }

    override fun onResume() {
        super.onResume()
        actualizarEstado("online")
    }

    override fun onPause() {
        super.onPause()
        actualizarEstado("offline")
    }*/

}