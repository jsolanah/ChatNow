package com.jorgesolana.chatnow.Perfil

import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jorgesolana.chatnow.R

class EditarImagenPerfil : AppCompatActivity() {

    //Referencias a los botones, ImageView, URI de la imagen, autenticación de Firebase, y un diálogo de progreso
    private lateinit var btnElegirImagen : Button
    private lateinit var btnActualizarImagen : Button
    private var imageUri : Uri?= null
    private lateinit var imagenPerfilActualizar : ImageView

    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    var user : FirebaseUser?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_imagen_perfil)

        // Inicialización de vistas
        btnActualizarImagen = findViewById(R.id.btnActualizarImagen)
        btnElegirImagen = findViewById(R.id.btnElegirImagen)
        imagenPerfilActualizar = findViewById(R.id.imagenPerfilActualizar)

        // Configuración del diálogo de progreso
        progressDialog = ProgressDialog(this@EditarImagenPerfil)
        progressDialog.setTitle(getString(R.string.esperar))
        progressDialog.setCanceledOnTouchOutside(false)

        // Autenticación de Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        // Acción al presionar el botón de elegir imagen
        btnElegirImagen.setOnClickListener{
            mostrarDialogo() // Muestra un diálogo para seleccionar la imagen
        }
        btnActualizarImagen.setOnClickListener{
            validarImagen()// Valida si se ha seleccionado una imagen
        }

    }

    // Función para validar si se ha seleccionado una imagen
    private fun validarImagen(){
        if (imageUri == null){
            Toast.makeText(applicationContext, getString(R.string.imagen_necesaria), Toast.LENGTH_SHORT).show()
        }else{
            subirImagen() // Si hay imagen seleccionada, sube la imagen
        }
    }

    // Función para subir la imagen seleccionada a Firebase Storage
    private fun subirImagen(){
        progressDialog.setMessage(getString(R.string.actualizando_imagen))
        progressDialog.show()
        val rutaImagen = "Perfil_usuario"+firebaseAuth.uid
        val referenceStorage = FirebaseStorage.getInstance().getReference(rutaImagen)
        // Subir imagen a Firebase Storage
        referenceStorage.putFile(imageUri!!).addOnSuccessListener { tarea->
            val uriTarea : Task<Uri> = tarea.storage.downloadUrl
            while (!uriTarea.isSuccessful);
            val urlImagen = "${uriTarea.result}"
            actualizarImagenBD(urlImagen )// Actualizar la URL de la imagen en la base de datos
        }.addOnFailureListener{e->
            Toast.makeText(applicationContext, getString(R.string.fallo_imagen) + " ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    // Función para actualizar la URL de la imagen en la base de datos de Firebase
    private fun actualizarImagenBD(urlImagen: String) {
        progressDialog.setMessage(getString(R.string.actualizando_imagen))
        val hashMap : HashMap<String, Any> = HashMap()
        if (imageUri!=null){
            hashMap["imagen"] = urlImagen // Establecer la nueva URL de la image
        }

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(firebaseAuth.uid!!).updateChildren(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            //Toast.makeText(applicationContext, "Su imagen a sido actualizada", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, PerfilActivity::class.java)
            startActivity(intent)

        }.addOnFailureListener{e->
            Toast.makeText(applicationContext, getString(R.string.fallo_imagen)+" ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    // Función para abrir la galería y seleccionar una imagen
    private fun abrirGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galeriaActivityResultLauncher.launch(intent)
    }

    // Resultados de la selección de imagen desde la galería
    private val galeriaActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ resultado ->
            if (resultado.resultCode == RESULT_OK){
                val data = resultado.data
                imageUri = data!!.data
                imagenPerfilActualizar.setImageURI(imageUri) // Establecer la imagen seleccionada

            }else {
                Toast.makeText(applicationContext, getString(R.string.cancelado_usuario), Toast.LENGTH_SHORT).show()

            }
        }
    )

    // Función para abrir la cámara y capturar una imagen
    private fun abrirCamara(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Titulo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        camaraActivityResultLauncher.launch(intent)
    }

    // Resultados de la captura de imagen con la cámara
    private val camaraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado_camara ->
            if (resultado_camara.resultCode == RESULT_OK){
                imagenPerfilActualizar.setImageURI(imageUri)
            }else{
                Toast.makeText(applicationContext, getString(R.string.cancelado_usuario), Toast.LENGTH_SHORT).show()
            }
        }

    // Función para mostrar un diálogo que permita seleccionar entre abrir la galería o la cámara
    private fun mostrarDialogo(){
        val btn_abrir_galeria : Button
        val btn_abrir_camara : Button

        val dialog = Dialog(this@EditarImagenPerfil)

        dialog.setContentView(R.layout.cuadro_d_seleccionar)

        btn_abrir_galeria = dialog.findViewById(R.id.btn_abrir_galeria)
        btn_abrir_camara = dialog.findViewById(R.id.btn_abrir_camara)

        btn_abrir_galeria.setOnClickListener{
            abrirGaleria() // Llamar a la función de abrir galería
            dialog.dismiss()
        }
        btn_abrir_camara.setOnClickListener{
            abrirCamara()
            dialog.dismiss()
        }
        dialog.show()
    }

}