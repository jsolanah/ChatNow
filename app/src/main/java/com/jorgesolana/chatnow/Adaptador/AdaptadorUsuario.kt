package com.jorgesolana.chatnow.Adaptador

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jorgesolana.chatnow.Chat.MensajesActivity
import com.jorgesolana.chatnow.Modelo.Chat
import com.jorgesolana.chatnow.Modelo.Usuario
import com.jorgesolana.chatnow.R
import java.text.Normalizer

class AdaptadorUsuario (context : Context, listaUsuarios : List<Usuario>, chatLeido : Boolean) : RecyclerView.Adapter<AdaptadorUsuario.ViewHolder?>(){

    // Contexto de la aplicación y lista de usuarios que se va a mostrar en el RecyclerView
    private val contexto : Context
    private val listaUsuarios : List<Usuario>
    // Variable que indica si se ha leído el último mensaje del chat
    private var chatLeido : Boolean
    // Almacena el último mensaje enviado en el chat
    var ultimoMensaje : String = ""

    // Inicializa las variables de contexto, lista de usuarios y estado de chat leído
    init {
        this.contexto = context
        this.listaUsuarios = listaUsuarios
        this.chatLeido = chatLeido
    }

    // ViewHolder que contiene y vincula los elementos de vista que se usarán para cada usuario en el RecyclerView
    class ViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        var nombre_usuario : TextView
        //var email_usuario : TextView
        var imagen_usuario : ImageView
        //var imagen_online : ImageView
        //var imagen_offline : ImageView
        var Txt_ultimo_mensaje : TextView

        init {
            // Asocia los elementos de la vista con sus correspondientes identificadores de layout
            nombre_usuario = itemView.findViewById(R.id.item_nombre_usuario)
            imagen_usuario = itemView.findViewById(R.id.item_imagen)
            Txt_ultimo_mensaje = itemView.findViewById(R.id.Txt_ultimo_mensaje)

        }
    }

    // Crea y devuelve el ViewHolder para un nuevo elemento en el RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(contexto).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    // Devuelve el número total de elementos en la lista de usuarios
    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    // Asocia los datos de cada usuario con el ViewHolder correspondiente
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario : Usuario = listaUsuarios[position]
        // Asigna el nombre y la imagen del usuario actual a los elementos del ViewHolder
        holder.nombre_usuario.text = usuario.getN_Usuario()
        Glide.with(contexto).load(usuario.getImagen()).placeholder(R.drawable.ic_item_usuario).into(holder.imagen_usuario)

        // Configura un click listener para cada elemento, que abre la actividad de mensajes con el usuario seleccionado
        holder.itemView.setOnClickListener{
            val intent = Intent(contexto, MensajesActivity::class.java)
            intent.putExtra("uid_usuario", usuario.getUid())
            //Toast.makeText(context, "El usuario seleccioonado es: " +usuario.getN_Usuario(), Toast.LENGTH_SHORT).show()
            contexto.startActivity(intent)

        }

        // Muestra el último mensaje si el chat ha sido leído, en caso contrario oculta el campo de texto de último mensaje
        if (chatLeido){
            obtenerUltimoMensaje(usuario.getUid(), holder.Txt_ultimo_mensaje)
        }else{
            holder.Txt_ultimo_mensaje.visibility = View.GONE
        }
    }

    // Obtiene el último mensaje del usuario seleccionado y lo muestra en el TextView `Txt_ultimo_mensaje`
    private fun obtenerUltimoMensaje(ChatUsuarioUid: String?, txtUltimoMensaje: TextView) {
        // Variable para almacenar el último mensaje (por defecto un mensaje indicativo)
        ultimoMensaje = "defaultMensaje"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        // Escucha cambios en la base de datos para cargar el último mensaje entre el usuario actual y el usuario en la lista
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val chat : Chat?= dataSnapshot.getValue(Chat::class.java)
                    // Verifica si el chat pertenece al usuario actual y al usuario seleccionado
                    if (firebaseUser!= null && chat!= null){
                        if (chat.getReceptor() == firebaseUser!!.uid &&
                            chat.getEmisor() == ChatUsuarioUid ||
                            chat.getReceptor() == ChatUsuarioUid &&
                            chat.getEmisor() == firebaseUser!!.uid){
                            ultimoMensaje = chat.getMensaje()!!
                            // Actualiza el último mensaje según el contenido del chat
                        }
                    }
                }

                // Muestra el último mensaje en el TextView, con formato dependiendo de su contenido
                when(ultimoMensaje){
                    "defaultMensaje" -> txtUltimoMensaje.text = contexto.getString(R.string.no_mensaje)
                    "Se ha enviado la imagen" -> txtUltimoMensaje.text = contexto.getString(R.string.imagen_enviada)
                    else-> txtUltimoMensaje.text = ultimoMensaje
                }
                ultimoMensaje =  "defaultMensaje"
            }

            // Método llamado en caso de error de acceso a la base de datos
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


}