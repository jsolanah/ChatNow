package com.jorgesolana.chatnow

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jorgesolana.chatnow.Fragmentos.ChatFragment
import com.jorgesolana.chatnow.Fragmentos.UsuariosFragment
import com.jorgesolana.chatnow.Modelo.Chat
import com.jorgesolana.chatnow.Modelo.Usuario
import com.jorgesolana.chatnow.Perfil.PerfilActivity

class MainActivity : AppCompatActivity() {

    // Variables necesarias para manejar la base de datos y el usuario autenticado
    var reference : DatabaseReference?=null
    var firebaseUser : FirebaseUser ?= null
    private lateinit var nombre_usuario : TextView
    var msg = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarComponentes()
        obtenerDato()
    }
    // Método para inicializar las vistas y componentes necesarios
    fun inicializarComponentes(){
        // Inicializamos la barra de herramientas (Toolbar)
        val toolbar : Toolbar = findViewById(R.id.toolbarName)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        // Obtenemos el usuario actual de Firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseUser!!.uid)
        nombre_usuario = findViewById(R.id.nombre_usuario)
        val tabLayout : TabLayout = findViewById(R.id.TabLayoutMain)
        val viewPager : ViewPager = findViewById(R.id.ViewPagerMain)
        // Obtenemos los chats desde la base de datos y actualizamos la vista
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var contChatsNoVistos = 0
                val chatsNoVistosSet = mutableSetOf<String>() // Usamos un Set para evitar duplicados
                // Iteramos por los chats en la base de datos
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat != null && chat.getReceptor().equals(firebaseUser!!.uid) && !chat.isVisto()) {
                        chat.getEmisor()?.let { chatsNoVistosSet.add(it) } // Agregamos el ID del emisor al Set
                    }
                }

                contChatsNoVistos = chatsNoVistosSet.size // El tamaño del Set es el número de chats no vistos
                // Si hay chats no vistos, lo indicamos en el título del tab
                if (contChatsNoVistos == 0) {
                    viewPagerAdapter.addItem(ChatFragment(), getString(R.string.chats))
                } else {
                    viewPagerAdapter.addItem(ChatFragment(), getString(R.string.chats) +"[$contChatsNoVistos]")
                }
                // Agregamos el fragmento de usuarios
                viewPagerAdapter.addItem(UsuariosFragment(), getString(R.string.usuarios))
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    // Método para obtener el nombre de usuario y actualizar la vista
    fun obtenerDato(){
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var usuario : Usuario? = snapshot.getValue(Usuario::class.java)
                    nombre_usuario.text = usuario!!.getN_Usuario()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    // Adaptador para los fragmentos en el ViewPager
    class ViewPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {

        private val listaFragmentos : MutableList<Fragment> = ArrayList()
        private val listaTitulos : MutableList<String> = ArrayList()

        override fun getCount(): Int {
            return listaFragmentos.size
        }

        override fun getItem(position: Int): Fragment {
            return listaFragmentos[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return listaTitulos[position]
        }
        // Método para agregar un fragmento y su título
        fun addItem(fragment: Fragment, titulo : String){
            listaFragmentos.add(fragment)
            listaTitulos.add(titulo)
        }
    }

    // Método para crear el menú de opciones
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_principal, menu)
        return true
    }
    // Método para manejar la selección de opciones del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_perfil->{
                val intent = Intent(applicationContext, PerfilActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_acerca_de->{
                infoApp()
                return true
                return true
            }

            R.id.menu_salir->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, Inicio::class.java)
                //Toast.makeText(applicationContext, "Has cerrado sesion", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                return true
            }else -> super.onOptionsItemSelected(item)
        }
    }
    // Método para mostrar información de la app
    private fun infoApp(){
        val entendidoInfo : Button
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.cuadro_d_info_app)

        entendidoInfo = dialog.findViewById(R.id.EntendidoInfo)
        entendidoInfo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }

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
        actualizarEstado("offline")
    }*/

}