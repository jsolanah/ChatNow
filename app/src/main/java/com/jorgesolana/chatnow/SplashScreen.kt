package com.jorgesolana.chatnow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer

class SplashScreen : AppCompatActivity() {
    // Método que se ejecuta cuando la actividad es creada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        mostrarBienvenida()
    }
    // Función que maneja la cuenta regresiva para la splash screen
    fun mostrarBienvenida(){
        object : CountDownTimer(3000,1000){
            override fun onTick(millisUntilFinished: Long) {

            }
            override fun onFinish() {
                val intent = Intent(applicationContext, Inicio::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }

}