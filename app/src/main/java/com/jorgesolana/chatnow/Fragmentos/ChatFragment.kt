package com.jorgesolana.chatnow.Fragmentos

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jorgesolana.chatnow.Adaptador.AdaptadorUsuario
import com.jorgesolana.chatnow.Modelo.ListaChats
import com.jorgesolana.chatnow.Modelo.Usuario
import com.jorgesolana.chatnow.R

class ChatFragment : Fragment() {

    //Adaptador para mostrar usuarios, listas de usuarios y chats
    private var usuarioAdaptador : AdaptadorUsuario?=null
    private var usuarioLista : List<Usuario>?=null
    private var usuarioListaChats : List<ListaChats>?= null
    // Referencia al RecyclerView y al usuario autenticado
    lateinit var RV_ListaChats : RecyclerView
    private var firebaseUser : FirebaseUser?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Configura la vista del fragmento, RecyclerView y la obtención del usuario
        val view : View = inflater.inflate(R.layout.fragment_chat, container, false)

        RV_ListaChats = view.findViewById(R.id.RV_ListaChats)
        RV_ListaChats.setHasFixedSize(true)
        RV_ListaChats.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usuarioListaChats = ArrayList()
        // Configura un listener para obtener los chats del usuario desde Firebase
        val ref = FirebaseDatabase.getInstance().reference.child("ListaMensajes").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (usuarioListaChats as ArrayList).clear()
                // Agrega chats a la lista y actualiza la UI
                for (dataSnapShot in snapshot.children){
                    val chatList = dataSnapShot.getValue(ListaChats::class.java)
                    (usuarioListaChats as ArrayList).add(chatList!!)
                }
                recuperarListaChats() // Actualiza la lista de chats
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        // Obtiene el token de notificación del usuario
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { tarea->
                if (tarea.isSuccessful){
                    if (tarea.result != null && !TextUtils.isEmpty(tarea.result)){
                        val token : String = tarea.result!!
                        //actualizarToken(token)
                    }
                }
            }

        return view
    }

    // Función que recupera y muestra la lista de usuarios con los chats activos
    private fun recuperarListaChats(){
        usuarioLista = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usuarioLista as ArrayList).clear()
                // Verifica los chats y agrega los usuarios correspondientes
                for (dataSnapshot in snapshot.children){
                    val user = dataSnapshot.getValue(Usuario::class.java)
                    for (cadaLista in usuarioListaChats!!){
                        if (user!!.getUid().equals(cadaLista.getUid())){
                            (usuarioLista as ArrayList).add(user!!)
                        }
                    }
                    // Configura el adaptador para mostrar los usuarios con chats
                    usuarioAdaptador = AdaptadorUsuario(context!!, (usuarioLista as ArrayList<Usuario>), true)
                    RV_ListaChats.adapter = usuarioAdaptador
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}