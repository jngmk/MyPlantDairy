package edu.uc.jeong.myplantdiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import edu.uc.jeong.myplantdiary.ui.main.EventFragment
import edu.uc.jeong.myplantdiary.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
        detector = GestureDetectorCompat(this, DiaryGestureListener())  // wire up inner class to our activity
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (detector.onTouchEvent(event)) {
            // event was handled, nobody else needs to handle it
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    inner class DiaryGestureListener: GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var diffX = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F  // if null return 0.0
            var diffY = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if (Math.abs(diffX) > Math.abs(diffY)) {
                // this is a left or right swipe
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // right swipe
                        this@MainActivity.onSWipeRight()
                    } else {
                        // left swipe
                        this@MainActivity.onSWipeLeft()
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            } else {
                // this is either a bottom or top swipe
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // top swipe
                        this@MainActivity.onSWipeTop()
                    } else {
                        // bottom swipe
                        this@MainActivity.onSWipeBottom()
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            }
        }
    }

    private fun onSWipeBottom() {

    }

    private fun onSWipeTop() {

    }

    private fun onSWipeLeft() {

    }

    private fun onSWipeRight() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, EventFragment.newInstance())
            .commitNow()
    }
}
