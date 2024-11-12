package com.jorgesolana.chatnow.Perfil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jorgesolana.chatnow.Modelo.Usuario
import com.jorgesolana.chatnow.R

class PerfilVisitado : AppCompatActivity() {

    // Vistas de la actividad
    private lateinit var PV_ImagenU : ImageView
    private lateinit var PV_NombreU : TextView
    private lateinit var PV_EmailU : TextView
    private lateinit var PV_nombres  : TextView
    private lateinit var PV_apellidos : TextView
    private lateinit var PV_profesion : TextView
    private lateinit var PV_telefono : TextView
    private lateinit var PV_edad : TextView
    private lateinit var PV_domicilio : TextView
    private lateinit var PV_proveedor : TextView
    // Botones de la actividad
    private lateinit var Btn_llamar : Button
    private lateinit var Btn_enviar_sms : Button
    // Variables de Firebase
    var uid_usuario_visitado = ""
    var user : FirebaseUser?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_visitado)
        // Inicializamos las vistas y obtenemos el UID del usuario visitado
        inicializarVistas()
        obtenerUid()
        // Leemos la información del usuario visitado desde Firebase
        leerInformacionUsuario()

        Btn_llamar.setOnClickListener {
            // Verificamos si tenemos permiso para hacer llamadas
            if (ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                realizarLlamada()
            }else{
                requestCallPhonePermiso.launch(Manifest.permission.CALL_PHONE)
            }
        }

        Btn_enviar_sms.setOnClickListener {
            // Verificamos si tenemos permiso para enviar SMS
            if (ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                enviarSms()
            }else{
                requestSendMessagePermiso.launch(Manifest.permission.SEND_SMS)
            }

        }
    }

    // Método para inicializar las vistas de la actividad
    private fun inicializarVistas(){

        PV_ImagenU = findViewById(R.id.PV_ImagenU)
        PV_NombreU = findViewById(R.id.PV_NombreU)
        PV_EmailU = findViewById(R.id.PV_EmailU)
        PV_nombres = findViewById(R.id.PV_nombres)
        PV_apellidos = findViewById(R.id.PV_apellidos)
        PV_profesion = findViewById(R.id.PV_profesion)
        PV_telefono = findViewById(R.id.PV_telefono)
        PV_edad = findViewById(R.id.PV_edad)
        PV_domicilio = findViewById(R.id.PV_domicilio)
        PV_proveedor = findViewById(R.id.PV_proveedor)

        Btn_llamar = findViewById(R.id.Btn_llamar)
        Btn_enviar_sms = findViewById(R.id.Btn_enviar_sms)
        // Obtenemos el usuario actual desde Firebase
        user = FirebaseAuth.getInstance().currentUser
    }
    // Método para obtener el UID del usuario visitado desde el Intent
    private fun obtenerUid(){
        intent = intent
        uid_usuario_visitado = intent.getStringExtra("uid").toString()
    }
    // Método para leer la información del usuario visitado desde Firebase
    private fun leerInformacionUsuario(){
        val reference = FirebaseDatabase.getInstance().reference
            .child("Usuarios")
            .child(uid_usuario_visitado) // Referencia a la base de datos de Firebase del usuario visitado


        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario : Usuario? = snapshot.getValue(Usuario::class.java)
                // Actualizamos las vistas con la información del usuario visitado
                PV_NombreU.text = usuario!!.getN_Usuario()
                PV_EmailU.text = usuario!!.getEmail()
                PV_nombres.text = usuario!!.getNombres()
                PV_apellidos.text = usuario!!.getApellidos()
                PV_profesion.text = usuario!!.getProfesion()
                PV_telefono.text = usuario!!.getTelefono()
                PV_edad.text = usuario!!.getEdad()
                PV_domicilio.text = usuario!!.getDomicilio()
                PV_proveedor.text = usuario!!.getProveedor()
                // Cargamos la imagen de perfil usando Glide
                Glide.with(applicationContext).load(usuario.getImagen())
                    .placeholder(R.drawable.imagen_usuario_visitado) // Imagen de perfil por defecto
                    .into(PV_ImagenU)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    // Método para realizar una llamada al número del usuario visitado
    private fun realizarLlamada() {
        val numeroUsuario = PV_telefono.text.toString()
        // Verificamos si el usuario tiene un número telefónico
        if (numeroUsuario.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.no_telefono), Toast.LENGTH_SHORT).show()
        }else{
            // Iniciamos la llamada
            val intent = Intent(Intent.ACTION_CALL)
            intent.setData(Uri.parse("tel:$numeroUsuario"))
            startActivity(intent)
        }
    }
    // Método para enviar un SMS al número del usuario visitado
    private fun enviarSms() {
        val numeroUsuario = PV_telefono.text.toString()
        // Verificamos si el usuario tiene un número telefónico
        if (numeroUsuario.isEmpty()){
            Toast.makeText(applicationContext, getString(R.string.no_telefono), Toast.LENGTH_SHORT).show()
        }else{
            // Iniciamos el envío de SMS
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("smsto:$numeroUsuario"))
            intent.putExtra("sms_body", "")
            startActivity(intent)
        }
    }
    // Solicita permiso para realizar llamadas
    private val requestCallPhonePermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ Permiso_concedido->
            if (Permiso_concedido){
                realizarLlamada()
            }else{
                // Si el permiso no es concedido, mostramos un mensaje
                Toast.makeText(applicationContext, getString(R.string.no_permiso_llamada),Toast.LENGTH_SHORT).show()
            }
        }
    // Solicita permiso para enviar SMS
    private val requestSendMessagePermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){Permiso_concedido->
            if (Permiso_concedido){
                enviarSms()
            }
            else{
                // Si el permiso no es concedido, mostramos un mensaje
                Toast.makeText(applicationContext, getString(R.string.no_permiso_sms),Toast.LENGTH_SHORT).show()
            }

        }
}