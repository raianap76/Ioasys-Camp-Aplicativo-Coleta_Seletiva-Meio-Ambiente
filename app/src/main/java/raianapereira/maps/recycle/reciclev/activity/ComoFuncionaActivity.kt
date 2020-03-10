package raianapereira.maps.recycle.reciclev.activity

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_como_funciona.*
import raianapereira.maps.recycle.reciclev.R

class ComoFuncionaActivity : AppCompatActivity(),GestureDetector.OnGestureListener {

    private var etapa = 0
    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100
    private lateinit var gestureDetector: GestureDetector

    /**
     * Gerencia a tela de como funciona.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_como_funciona)
        bttnStepInformation.setOnClickListener {
            next()
        }
        gestureDetector = GestureDetector(this)
        backToMap.setOnClickListener {
            finish()
        }
    }

    /**
     * Define qual imagem e texto de informações será exibida para o usúario em determinada etapa e starta a activity Mapa na ultima etapa.
     */

    fun next()
    {
        if (etapa == 0)
        {
            imgPrincipalInformation.setImageResource(R.mipmap.organico)
            txtTituloInformation.setText(R.string.separacao)
            txtHeadInformation.setText(R.string.separacaotexto)
            etapa++
        }
        else if (etapa == 1)
        {
            imgPrincipalInformation.setImageResource(R.mipmap.lixo)
            txtTituloInformation.setText(R.string.limpeza)
            txtHeadInformation.setText(R.string.limpezatexto)
            etapa++
        }
        else if (etapa == 2)
        {
            imgPrincipalInformation.setImageResource(R.mipmap.screen)
            txtTituloInformation.setText(R.string.contaminacao)
            txtHeadInformation.setText(R.string.contaminacaotexto)
            etapa++
        }
        else if (etapa == 3)
        {
            imgPrincipalInformation.setImageResource(R.mipmap.sacola)
            txtTituloInformation.setText(R.string.separacaototal)
            txtHeadInformation.setText(R.string.separacaototaltexto)
            etapa++
        }
        else if (etapa == 4)
        {
            imgPrincipalInformation.setImageResource(R.mipmap.pessoas)
            txtTituloInformation.setText(R.string.descarte)
            txtHeadInformation.setText(R.string.descartetexto)
            etapa++
        }
        else
        {
            finish()
        }
    }

    override fun onShowPress(p0: MotionEvent?) = Unit

    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

    override fun onDown(p0: MotionEvent?): Boolean = false

    /**
     * Configura o listener de swipe para esquerda.
     */

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        val difY = p1.y - p0.y
        val difX = p1.x - p0.x
        if (Math.abs(difX) > Math.abs(difY) )
        {
            if (Math.abs(difX) > SWIPE_THRESHOLD && Math.abs(p2) >  SWIPE_VELOCITY_THRESHOLD)
            {
                if (difX <= 0)
                {
                    SwipeLeftListener()
                }
            }
        }
        return (false)
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) = Unit

    /**
     * Listener do swipe para esquerda que chama o método de próxima etapa.
     */

    fun SwipeLeftListener()
    {
        next()
    }

    /**
     * Se realizar um clique na tela, a ação será passada para o método onTouchEvent do GestureDetector
     */

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}
