package welove520.com.photocategory.algorithm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.content_main2.*
import welove520.com.photocategory.PhotoListModel
import welove520.com.photocategory.R
import java.util.*


class Main2Activity : AppCompatActivity() {

    private var photoPath: PhotoListModel? = null

    private val mShortAnimationDuration: Long = 1000

    private var next: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)

        photoPath = intent.getSerializableExtra("photoList") as PhotoListModel

        fl_main.setOnClickListener { view ->
            var path = photoPath!!.photoList.get(Random().nextInt(photoPath!!.photoList.size)).photoPath
            next = !next
            showContentOrLoadingIndicator(path, next, nextImageView, prevImageView)
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        nextImageView.visibility = View.GONE
    }


    /**
     * Cross-fades between [.nextView] and [.preView].
     */
    private fun showContentOrLoadingIndicator(path: String, contentLoaded: Boolean, nextView: ImageView, preView: ImageView) {
        // Decide which view to hide and which to show.
        val showView = if (contentLoaded) nextView else preView
        val hideView = if (contentLoaded) preView else nextView
        Glide.with(applicationContext)
                .load(path)
                .into(showView)
        // Set the "show" view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        showView.alpha = 0f
        showView.visibility = View.VISIBLE

        // Animate the "show" view to 100% opacity, and clear any animation listener set on
        // the view. Remember that listeners are not limited to the specific animation
        // describes in the chained method calls. Listeners are set on the
        // ViewPropertyAnimator object for the view, which persists across several
        // animations.
        showView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null)

        // Animate the "hide" view to 0% opacity. After the animation ends, set its visibility
        // to GONE as an optimization step (it won't participate in layout passes, etc.)
        hideView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        hideView.visibility = View.GONE
                        var path = photoPath!!.photoList.get(Random().nextInt(photoPath!!.photoList.size)).photoPath
                        next = !next
                        if (nextImageView == null) return
                        if (prevImageView == null) return
                        Handler().postDelayed({
                            showContentOrLoadingIndicator(path, next, nextImageView, prevImageView)
                        }, (Random().nextInt(2000)).toLong())
                    }
                })
    }

}
