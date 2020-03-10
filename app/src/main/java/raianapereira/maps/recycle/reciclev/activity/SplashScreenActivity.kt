package raianapereira.maps.recycle.reciclev.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import raianapereira.maps.recycle.reciclev.R


class SplashScreenActivity : Activity() {

    /**
     * Inicializa a Splash Screen com duração de 1 segundo enquanto a activity Mapa e inicializada.
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed(
            {
                startActivity(Intent(this,InformationActivity::class.java))
                finish()
            },1000)
    }
}
