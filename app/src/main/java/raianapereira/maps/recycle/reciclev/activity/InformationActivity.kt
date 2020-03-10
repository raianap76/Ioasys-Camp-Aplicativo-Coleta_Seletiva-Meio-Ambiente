package raianapereira.maps.recycle.reciclev.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_information.*
import raianapereira.maps.recycle.reciclev.R

class InformationActivity : AppCompatActivity() ,GestureDetector.OnGestureListener{
    private var step : Int = 0
    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100
    private lateinit var gestureDetector: GestureDetector

    /**
     * Seta o listener do botão proximo e instancia o GestureDetector.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_information)
        bttnStep.setOnClickListener {
            proxInfo()
        }
        gestureDetector = GestureDetector(this)
    }

    /**
     * Define qual imagem e texto de informações será exibida para o usúario em determinada etapa e starta a activity Mapa ao clicar em próximo ou realizar swipe para esquerda na ultima etapa.
     */

    fun proxInfo()
    {
        if (step == 0) {
            imageStepOne.visibility = View.GONE
            imageCenter.setImageResource(R.mipmap.destwo)
            txtTitle.setText(R.string.cidadao)
            val params = txtTitle.layoutParams as ConstraintLayout.LayoutParams
            params.marginEnd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,75F,this.resources.displayMetrics).toInt()
            txtTitle.layoutParams = params
            txtPrincipal.setText(R.string.steptwoPrincipal)
            imagestep.setImageResource(R.mipmap.steptwo)
            step++
        }
        else if (step == 1)
        {
            imageCenter.setImageResource(R.mipmap.desthree)
            txtTitle.setText(R.string.empresa)
            val params = txtTitle.layoutParams as ConstraintLayout.LayoutParams
            params.marginEnd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,90F,this.resources.displayMetrics).toInt()
            txtTitle.layoutParams = params
            txtPrincipal.setText(R.string.stepthreePrincipal)
            imagestep.setImageResource(R.mipmap.stepthree)
            step++
        }
        else
        {
            startActivity(Intent(this,LogarCadastrar::class.java))
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
        proxInfo()
    }

    /**
     * Se realizar um clique na tela, a ação será passada para o método onTouchEvent do GestureDetector
     */

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}

