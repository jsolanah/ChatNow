package com.jorgesolana.chatnow.Adaptador

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.jorgesolana.chatnow.Modelo.Chat
import com.jorgesolana.chatnow.R

class AdaptadorChat (contexto : Context, chatLista : List<Chat>, imageUrl  : String)
    : RecyclerView.Adapter<AdaptadorChat.ViewHolder?>() {
    // Variables para contexto, lista de chats e imagen de perfil del usuario.
    private val contexto : Context
    private val chatLista : List<Chat>
    private val imageUrl : String
    // Usuario actual autenticado en Firebase.
    var firebaseUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    // Inicialización de variables en el bloque init.
    init {
        this.contexto = contexto
        this.chatLista = chatLista
        this.imageUrl = imageUrl
    }

    // ViewHolder interno: define los elementos de la vista del mensaje.
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var imagen_perfil_mensaje : ImageView ?= null
        var txt_ver_mensaje : TextView?=null
        var imagen_enviada_izquierdo : ImageView?= null
        var txt_mensaje_visto : TextView ?= null

        var imagen_enviada_derecho : ImageView?= null

        // Inicializa las vistas a partir de sus identificadores en el XML.
        init {
            imagen_perfil_mensaje = itemView.findViewById(R.id.imagen_perfil_mensaje)
            txt_mensaje_visto = itemView.findViewById(R.id.txt_mensaje_visto)
            txt_ver_mensaje = itemView.findViewById(R.id.txt_ver_mensaje)
            imagen_enviada_izquierdo = itemView.findViewById(R.id.imagen_enviada_izquierdo)
            imagen_enviada_derecho = itemView.findViewById(R.id.imagen_enviada_derecha)
        }
    }

    // Inflar el diseño de cada mensaje según el emisor (mensaje a la derecha o izquierda).
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1){
            val view : View = LayoutInflater.from(contexto).inflate(com.jorgesolana.chatnow.R.layout.item_mensaje_derecho, parent, false)
            ViewHolder(view)

        }else{
            val view : View = LayoutInflater.from(contexto).inflate(com.jorgesolana.chatnow.R.layout.item_mensaje_izquierdo, parent, false)
            ViewHolder(view)
        }

    }

    // Devuelve el tamaño de la lista de chats.
    override fun getItemCount(): Int {
        return chatLista.size
    }

    // Vincula los datos de cada mensaje al ViewHolder correspondiente.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat : Chat = chatLista[position]
        // Carga la imagen de perfil en el mensaje.
        Glide.with(contexto).load(imageUrl).placeholder(R.drawable.ic_imagen_chat).into(holder.imagen_perfil_mensaje!!)

        // Verifica si el mensaje es una imagen o texto.
        if (chat.getMensaje().equals("Se ha enviado la imagen") && !chat.getUrl().equals("")){
            // Configura la vista para el mensaje de imagen enviado por el usuario actual.
            if (chat.getEmisor().equals(firebaseUser!!.uid)){
                holder.txt_ver_mensaje!!.visibility = View.GONE
                holder.imagen_enviada_derecho!!.visibility = View.VISIBLE
                Glide.with(contexto).load(chat.getUrl()).placeholder(R.drawable.ic_imagen_enviada).into(holder.imagen_enviada_derecho!!)

                // Configura opciones al hacer clic en la imagen.
                holder.imagen_enviada_derecho!!.setOnClickListener{
                    val opciones = arrayOf<CharSequence>(contexto.getString(R.string.opcion_imagen),contexto.getString(R.string.opcion_eliminar), contexto.getString(R.string.opcion_cancelar))
                    val builder : androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle(contexto.getString(R.string.que_realizar))
                    builder.setItems(opciones, DialogInterface.OnClickListener{
                            dialogInterface, i ->
                        if (i == 0){
                            visualizarImagen(chat.getUrl())
                        }
                        else if (i == 1){
                            eliminarMensaje(position, holder)
                        }
                    })
                    builder.show()

                }
            }
            //Condición para el usuario el cuál nos envia una imagen como mensaje
            else if (!chat.getEmisor().equals(firebaseUser!!.uid)){
                holder.txt_ver_mensaje!!.visibility = View.GONE
                holder.imagen_enviada_izquierdo!!.visibility = View.VISIBLE
                Glide.with(contexto).load(chat.getUrl()).placeholder(R.drawable.ic_imagen_enviada).into(holder.imagen_enviada_izquierdo!!)

                holder.imagen_enviada_izquierdo!!.setOnClickListener {
                    val opciones = arrayOf<CharSequence>(contexto.getString(R.string.opcion_imagen), contexto.getString(R.string.opcion_cancelar))
                    val builder : androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle(contexto.getString(R.string.que_realizar))
                    builder.setItems(opciones, DialogInterface.OnClickListener{
                            dialogInterface, i ->
                        if (i == 0){
                            visualizarImagen(chat.getUrl())
                        }
                    })
                    builder.show()
                }

            }
        }
        //Si el mensaje contiene sólo texto
        else{
            holder.txt_ver_mensaje!!.text = chat.getMensaje()
            if (firebaseUser!!.uid == chat.getEmisor()){
                holder.txt_ver_mensaje!!.setOnClickListener {
                    val opciones = arrayOf<CharSequence>(contexto.getString(R.string.opcion_eliminar), contexto.getString(R.string.opcion_cancelar))
                    val builder : androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle(contexto.getString(R.string.que_realizar))
                    builder.setItems(opciones, DialogInterface.OnClickListener {
                            dialogInterface, i ->
                        if (i == 0){
                            eliminarMensaje(position, holder)
                        }

                    })
                    builder.show()
                }
            }
        }

        //Muestra el estado del mensaje (Enviado o Visto) para el último mensaje en la lista.
        if (position == chatLista.size-1){
            if (chat.isVisto()){
                holder.txt_mensaje_visto!!.text = contexto.getString(R.string.mensaje_visto)
                if (chat.getMensaje().equals("Se ha enviado la imagen") && !chat.getUrl().equals("")){
                    val lp : RelativeLayout.LayoutParams = holder.txt_mensaje_visto!!.layoutParams as RelativeLayout.LayoutParams
                    lp!!.setMargins(0,245, 10,0)
                    holder.txt_mensaje_visto!!.layoutParams = lp
                }
            }else{
                holder.txt_mensaje_visto!!.text = contexto.getString(R.string.mensaje_enviado)
                if (chat.getMensaje().equals("Se ha enviado la imagen") && !chat.getUrl().equals("")){
                    val lp : RelativeLayout.LayoutParams = holder.txt_mensaje_visto!!.layoutParams as RelativeLayout.LayoutParams
                    lp!!.setMargins(0,245, 10,0)
                    holder.txt_mensaje_visto!!.layoutParams = lp
                }
            }
        }else{
            holder.txt_mensaje_visto!!.visibility = View.GONE
        }

    }

    // Determina el tipo de vista para el mensaje según el emisor (derecha o izquierda).
    override fun getItemViewType(position: Int): Int {
        return if(chatLista[position].getEmisor().equals(firebaseUser!!.uid)){
            1
        }else{
            0
        }
    }

    // Muestra la imagen en una vista de diálogo.
    private fun visualizarImagen(imagen : String?){
        val Img_visualizar : PhotoView
        val Btn_cerrar_v : Button

        val dialog = Dialog(contexto)

        dialog.setContentView(R.layout.visualizar_imagen_completa)

        Img_visualizar = dialog.findViewById(R.id.Img_visualizar)
        Btn_cerrar_v = dialog.findViewById(R.id.Btn_cerrar_v)

        Glide.with(contexto).load(imagen).placeholder(R.drawable.ic_imagen_enviada).into(Img_visualizar)

        Btn_cerrar_v.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)



    }

    // Elimina un mensaje de Firebase según su posición en la lista de chat.
    private fun eliminarMensaje(position: Int, holder : AdaptadorChat.ViewHolder){

        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(chatLista.get(position).getId_Mensaje()!!)
            .removeValue()
            .addOnCompleteListener { tarea->
                if (tarea.isSuccessful){
                    Toast.makeText(holder.itemView.context, contexto.getString(R.string.mensaje_eliminado), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(holder.itemView.context, contexto.getString(R.string.mensaje_no_eliminado), Toast.LENGTH_SHORT).show()
                }
            }
    }
}