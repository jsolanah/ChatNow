package com.jorgesolana.chatnow.Chat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.jorgesolana.chatnow.Adaptador.AdaptadorChat
import com.jorgesolana.chatnow.Modelo.Chat
import com.jorgesolana.chatnow.Modelo.Usuario

import com.jorgesolana.chatnow.Perfil.PerfilVisitado
import com.jorgesolana.chatnow.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.Normalizer

class MensajesActivity : AppCompatActivity() {
    // Variables de UI y Firebase
    private lateinit var et_mensaje : EditText
    private lateinit var ib_enviar : ImageButton
    private lateinit var ib_adjuntar : ImageButton
    private lateinit var imagen_perfil_chat : ImageView
    private lateinit var n_usuario_chat : TextView
    var uid_usuario_seleccionado : String = ""
    var firebaseUser : FirebaseUser ?= null
    private var imagenUri : Uri?= null
    lateinit var rv_chat : RecyclerView
    var chatAdapter : AdaptadorChat ?= null
    var chatList : List<Chat> ?= null
    var reference : DatabaseReference?= null
    var seenListener : ValueEventListener ?= null

    // Inicialización de la vista
    private fun inicializarVistas(){
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        et_mensaje = findViewById(R.id.et_mensaje)
        ib_enviar = findViewById(R.id.ib_enviar)
        ib_adjuntar = findViewById(R.id.ib_adjuntar)
        imagen_perfil_chat = findViewById(R.id.imagen_perfil_chat)
        n_usuario_chat = findViewById(R.id.n_usuario_chat)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        rv_chat = findViewById(R.id.RV_chats)
        rv_chat.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        rv_chat.layoutManager = linearLayoutManager
    }

    // Obtener el UID del usuario con el que se está chateando
    private fun obtenerUid(){
        intent = intent
        uid_usuario_seleccionado = intent.getStringExtra("uid_usuario").toString()
    }

    // Leer la información del usuario seleccionado y recuperar mensajes
    private fun leerInfoUsuarioSeleccionado(){
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios")
            .child(uid_usuario_seleccionado)
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario : Usuario? = snapshot.getValue(Usuario::class.java)
                n_usuario_chat.text = usuario!!.getN_Usuario()
                Glide.with(applicationContext).load(usuario.getImagen())
                    .placeholder(R.drawable.ic_item_usuario)
                    .into(imagen_perfil_chat)

                recuperarMensajes(firebaseUser!!.uid, uid_usuario_seleccionado, usuario.getImagen())

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }

    // Recuperar y mostrar el historial de mensajes
    private fun recuperarMensajes(emisorUid : String, receptorUid : String, receptorImagen : String?){
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList<Chat>).clear()
                for (sn in snapshot.children){
                    val chat = sn.getValue(Chat::class.java)

                    if (chat!!.getReceptor().equals(emisorUid) && chat.getEmisor().equals(receptorUid)
                        || chat.getReceptor().equals(receptorUid) && chat.getEmisor().equals(emisorUid)){
                        (chatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = AdaptadorChat(this@MensajesActivity,(chatList as ArrayList<Chat>), receptorImagen!!)
                    rv_chat.adapter = chatAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }

    // Marcar mensajes como vistos
    private fun mensajeVisto(usuarioUid : String){
        reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceptor().equals(firebaseUser!!.uid) && chat!!.getEmisor().equals(usuarioUid)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["visto"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    //Inicializa la actividad cuando es creada.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)
        inicializarVistas()
        obtenerUid()
        leerInfoUsuarioSeleccionado()

        ib_enviar.setOnClickListener{
            val mensaje = et_mensaje.text.toString()
            if (mensaje.isEmpty()){
                Toast.makeText(applicationContext, getString(R.string.imagen_enviada), Toast.LENGTH_SHORT).show()
            }else{
                enviarMensaje(firebaseUser!!.uid, uid_usuario_seleccionado, mensaje)
                et_mensaje.setText("")
            }

        }

        ib_adjuntar.setOnClickListener{

            abrirGaleria()
        }

        mensajeVisto(uid_usuario_seleccionado)


    }

    // Método para enviar un mensaje de texto
    private fun enviarMensaje(uid_emisor: String, uid_receptor: String, mensaje: String) {

        val reference = FirebaseDatabase.getInstance().reference
        val mensajeKey = reference.push().key
        val infoMensaje = HashMap<String, Any?>()
        infoMensaje["id_mensaje"] = mensajeKey
        infoMensaje["emisor"] = uid_emisor
        infoMensaje["receptor"] = uid_receptor
        infoMensaje["mensaje"] = mensaje
        infoMensaje["url"] = ""
        infoMensaje["visto"] = false
        reference.child("Chats").child(mensajeKey!!).setValue(infoMensaje).addOnCompleteListener{tarea->
            if (tarea.isSuccessful){
                val listaMensajesEmisor = FirebaseDatabase.getInstance().reference.child("ListaMensajes")
                    .child(firebaseUser!!.uid)
                    .child(uid_usuario_seleccionado)

                listaMensajesEmisor.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(!snapshot.exists()){
                            listaMensajesEmisor.child("uid").setValue(uid_usuario_seleccionado)
                        }
                        val listaMensajesReceptor = FirebaseDatabase.getInstance().reference.child("ListaMensajes")
                            .child(uid_usuario_seleccionado)
                            .child(firebaseUser!!.uid)
                        listaMensajesReceptor.child("uid").setValue(firebaseUser!!.uid)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        val usuarioReference = FirebaseDatabase.getInstance().reference
            .child("Usuarios").child(firebaseUser!!.uid)
        usuarioReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario = snapshot.getValue(Usuario::class.java)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    // Método para adjuntar y enviar imágenes
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galeriaARL.launch(intent)
    }

    //Actividad de resultado de la galería - Gestiona la selección y envío de una imagen desde la galería.
    private val galeriaARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {resultado ->
            if (resultado.resultCode == RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data

                val cargandoImagen = ProgressDialog(this@MensajesActivity)
                cargandoImagen.setMessage(getString(R.string.enviando_imagen))
                cargandoImagen.setCanceledOnTouchOutside(false)
                cargandoImagen.show()


                val carpetaImagenes = FirebaseStorage.getInstance().reference.child("Imágenes de mensajes")
                val reference = FirebaseDatabase.getInstance().reference
                val idMensaje = reference.push().key
                val nombreImagen = carpetaImagenes.child("$idMensaje.jpg")

                val uploadTask : StorageTask<*>
                uploadTask = nombreImagen.putFile(imagenUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful){
                        task.exception?.let{
                            throw it
                        }
                    }
                    return@Continuation nombreImagen.downloadUrl

                }).addOnCompleteListener{task->
                    if (task.isSuccessful){
                        cargandoImagen.dismiss()
                        val downloadUrl = task.result
                        val url = downloadUrl.toString()

                        val infoMensajeImagen = HashMap<String, Any?>()
                        infoMensajeImagen["id_mensaje"] = idMensaje
                        infoMensajeImagen["emisor"] = firebaseUser!!.uid
                        infoMensajeImagen["receptor"] = uid_usuario_seleccionado
                        infoMensajeImagen["mensaje"] = "Se ha enviado la imagen"
                        infoMensajeImagen["url"] = url
                        infoMensajeImagen["visto"] = false

                        reference.child("Chats").child(idMensaje!!).setValue(infoMensajeImagen)
                            .addOnCompleteListener { tarea->
                                if (tarea.isSuccessful){
                                    val usuarioReference = FirebaseDatabase.getInstance().reference
                                        .child("Usuarios").child(firebaseUser!!.uid)
                                    usuarioReference.addValueEventListener(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val usuario = snapshot.getValue(Usuario::class.java)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }
                                    })

                                }
                            }

                        reference.child("Chats").child(idMensaje!!).setValue(infoMensajeImagen)
                            .addOnCompleteListener{tarea->
                                if (tarea.isSuccessful){
                                    val listaMensajesEmisor = FirebaseDatabase.getInstance().reference.child("ListaMensajes")
                                        .child(firebaseUser!!.uid)
                                        .child(uid_usuario_seleccionado)

                                    listaMensajesEmisor.addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if(!snapshot.exists()){
                                                listaMensajesEmisor.child("uid").setValue(uid_usuario_seleccionado)
                                            }
                                            val listaMensajesReceptor = FirebaseDatabase.getInstance().reference.child("ListaMensajes")
                                                .child(uid_usuario_seleccionado)
                                                .child(firebaseUser!!.uid)
                                            listaMensajesReceptor.child("uid").setValue(firebaseUser!!.uid)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }


                                    })
                                }
                            }
                        //Toast.makeText(applicationContext, "La imagen se ha envaido con éxito ", Toast.LENGTH_SHORT).show()

                    }
                }
            }
            else{
                Toast.makeText(applicationContext, getString(R.string.cancelado_usuario), Toast.LENGTH_SHORT).show()
            }

        }
    )

    //Infla el menú de opciones
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_visitar_perfil, menu)
        return true
    }

    //Maneja las selecciones de ítems del menú.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_visitar->{
                val intent = Intent(applicationContext, PerfilVisitado::class.java)
                intent.putExtra("uid", uid_usuario_seleccionado)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Métodos para actualizar el estado del usuario
    /*private fun actualizarEstado(estado : String){
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios")
            .child(firebaseUser!!.uid)
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
        reference!!.removeEventListener(seenListener!!)
        actualizarEstado("offline")
    }*/




}