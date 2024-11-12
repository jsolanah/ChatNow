package com.jorgesolana.chatnow

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class Inicio : AppCompatActivity() {


    private lateinit var btn_ir_logeo: MaterialButton
    private lateinit var btn_login_google : MaterialButton

    var firebaseUser : FirebaseUser?=null
    private lateinit var auth : FirebaseAuth


    private lateinit var progressDialog : ProgressDialog
    private lateinit var mGoogleSignInClient : GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        btn_login_google = findViewById(R.id.btn_login_google)
        btn_ir_logeo = findViewById(R.id.btn_ir_logeo)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)



        btn_ir_logeo.setOnClickListener{
            val intent = Intent(this@Inicio, LoginActivity::class.java)
            //Toast.makeText(applicationContext, "Login", Toast.LENGTH_SHORT).show()
            startActivity(intent)

        }

        btn_login_google.setOnClickListener {
            empezarInicioGoogle()

        }


    }

    private fun empezarInicioGoogle() {
        val googleSignIntent = mGoogleSignInClient.signInIntent
        googleSignInARl.launch(googleSignIntent)
    }

    private val googleSignInARl = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){resultado ->
        if (resultado.resultCode == RESULT_OK){
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                autenticarGoogleFirebase(account.idToken)
            }catch (e : Exception){
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()

            }
        }else{
            Toast.makeText(applicationContext, "Cancelado", Toast.LENGTH_SHORT).show()

        }



    }

    private fun autenticarGoogleFirebase(idToken: String?) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credencial)
            .addOnSuccessListener { authResult ->
                if (authResult.additionalUserInfo!!.isNewUser){
                    guardarInfoBD()

                }else{
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }.addOnFailureListener{e->
                Toast.makeText(applicationContext, "a${e.message}", Toast.LENGTH_SHORT).show()


            }



    }

    private fun guardarInfoBD() {
        progressDialog.setMessage("Se esta registrando su informacon...")
        progressDialog.show()

        val uidGoogle = auth.uid
        val correoGoogle = auth.currentUser?.email
        val n_google = auth.currentUser?.displayName
        val nombre_usuario_G : String = n_google.toString()



        val hashmap = HashMap<String, Any?>()
        hashmap["uid"] = uidGoogle
        hashmap["n_usuario"] = nombre_usuario_G
        hashmap["email"] = correoGoogle
        hashmap["imagen"] = ""
        hashmap["buscar"] = nombre_usuario_G.lowercase()

        //Nuevos datos
        hashmap["nombres"] = ""
        hashmap["apellidos"] = ""
        hashmap["edad"] = ""
        hashmap["profesion"] = ""
        hashmap["domicilio"] = ""
        hashmap["telefono"] = ""
        hashmap["estado"] = "offline"
        hashmap["proveedor"] = "Google"

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uidGoogle!!)
            .setValue(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                Toast.makeText(applicationContext, "Se ha registrado exitosamente", Toast.LENGTH_SHORT).show()
                finishAffinity()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()

            }




    }

    private fun comprobarSesion(){
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser!=null){
            val intent = Intent(this@Inicio, MainActivity::class.java)
            Toast.makeText(applicationContext, "La sesion está activa", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        comprobarSesion()
        super.onStart()
    }

}