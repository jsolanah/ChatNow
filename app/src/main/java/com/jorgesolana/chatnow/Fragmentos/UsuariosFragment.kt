package com.jorgesolana.chatnow.Fragmentos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jorgesolana.chatnow.Adaptador.AdaptadorUsuario
import com.jorgesolana.chatnow.Modelo.Usuario
import com.jorgesolana.chatnow.R



class UsuariosFragment : Fragment() {
    //Adaptador de usuarios, lista de usuarios, referencia al RecyclerView y al campo de búsqueda
    private var usuarioAdaptador : AdaptadorUsuario?=null
    private var usuarioLista : List<Usuario>?=null
    private var rvUsuario : RecyclerView?=null
    private lateinit var et_buscar_usuario : EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Configura la vista del fragmento, RecyclerView y la barra de búsqueda
        val view : View =  inflater.inflate(R.layout.fragment_usuarios, container, false)
        rvUsuario = view.findViewById(R.id.RV_usuarios)
        rvUsuario!!.setHasFixedSize(true) // Configura RecyclerView
        rvUsuario!!.layoutManager = LinearLayoutManager(context)
        et_buscar_usuario = view.findViewById(R.id.et_buscar_usuario)

        // Inicializa la lista de usuarios y obtiene datos de la base de datos
        usuarioLista = ArrayList()
        obtenerUsuariosDB()

        // Configura un listener para el campo de búsqueda de usuarios
        et_buscar_usuario.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            // Realiza la búsqueda cuando el texto cambia
            override fun onTextChanged(b_usuario: CharSequence?, start: Int, before: Int, count: Int) {
                buscarUsuario(b_usuario.toString().lowercase())
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        return view
    }

    // Función para obtener los usuarios de la base de datos
    private fun obtenerUsuariosDB() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild("n_usuario")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (usuarioLista as ArrayList<Usuario>).clear()
                // Si no hay texto en el campo de búsqueda, muestra todos los usuarios
                if (et_buscar_usuario.text.toString().isEmpty()){
                    for (sh in snapshot.children){
                        val usuario : Usuario?= sh.getValue(Usuario::class.java)
                        // Evita mostrar el usuario actual
                        if (!(usuario!!.getUid()).equals(firebaseUser)){
                            (usuarioLista as ArrayList<Usuario>).add(usuario)
                        }
                    }
                    // Configura el adaptador con la lista de usuarios obtenida
                    usuarioAdaptador = AdaptadorUsuario(context!!, usuarioLista!!, false)
                    rvUsuario!!.adapter = usuarioAdaptador
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    // Función para buscar usuarios en la base de datos a partir del texto ingresado
    private fun buscarUsuario(buscarUsuario : String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val consulta = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild(getString(R.string.buscar))
            .startAt(buscarUsuario).endAt(buscarUsuario + "\uf8ff")
        consulta.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot){
                (usuarioLista as ArrayList<Usuario>).clear()
                // Agrega usuarios que coincidan con la búsqueda
                for (sh in snapshot.children){
                    val usuario : Usuario?= sh.getValue(Usuario::class.java)
                    if (!(usuario!!.getUid()).equals(firebaseUser)){
                        (usuarioLista as ArrayList<Usuario>).add(usuario)
                    }
                }
                // Actualiza el adaptador con los resultados de la búsqueda
                usuarioAdaptador = AdaptadorUsuario(context!!, usuarioLista!!, false)
                rvUsuario!!.adapter = usuarioAdaptador
            }
            override fun onCancelled(error: DatabaseError){
            }
        })
    }
}