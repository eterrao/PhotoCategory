package welove520.com.photocategory

import android.Manifest
import android.media.ExifInterface
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.item_photo.view.*
import org.greenrobot.greendao.query.Query
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import welove520.com.photocategory.utils.PermissionManager
import welove520.com.photocategory.utils.PickConfig
import welove520.com.photocategory.utils.PickPhotoHelper
import welove520.com.photocategory.utils.PickPreferences
import java.io.File

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var pickPreferences: PickPreferences? = null
    private var photoRVAdapter: PhotoRVAdapter? = null

    private var photoDao: PhotoDao? = null

    private var notesQuery: Query<Photo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        if (PermissionManager.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            fab!!.setOnClickListener { view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }

            val toggle = ActionBarDrawerToggle(
                    this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer_layout!!.addDrawerListener(toggle)
            toggle.syncState()
            nav_view!!.setNavigationItemSelectedListener(this)
            initPhotoRecyclerView()
        } else {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
            PermissionManager.checkPermissions(permissions, PERMISSION_REQUEST_CODE, this)
        }

        val daoSession: DaoSession = (application as App).daoSession
        photoDao = daoSession.photoDao
        notesQuery = photoDao!!.queryBuilder().orderAsc(PhotoDao.Properties.PhotoName).build()
        val notes = notesQuery!!.list()
        photoRVAdapter!!.setPhotos(notes)
    }

    private fun initPhotoRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        rv_photo!!.layoutManager = layoutManager
        initPickHelper()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            true
        } else if (id == R.id.action_add) {
            true
        } else super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun initPickHelper() {
        pickPreferences = PickPreferences.getInstance(this)
        val helper = PickPhotoHelper(this)
        pickPreferences!!.saveCurrentDirName(PickConfig.ALL_PHOTOS)
        Observable.just(helper)
                .map { pickPhotoHelper ->
                    pickPhotoHelper.getImagesAsync()
                    null
                }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action1<Nothing?> {
                    val dirImage = pickPreferences!!.dirImage
                    val listSize = dirImage!!.dirName.size
                    val groupImage = PickPreferences.getInstance(this@MainActivity).listImage
                    if (groupImage!!.getGroupMap() != null && groupImage.getGroupMap().size > 0) {
                        val photosList = groupImage.getGroupMap()[PickConfig.ALL_PHOTOS]
                        val photoList: MutableList<Photo> = ArrayList()
                        for (photoPath: String in photosList!!) {
                            var photo = Photo()
                            photo.photoPath = photoPath
                            photoList.add(photo)
                        }
                        initRVAdapter(photoList!!)
                    }
                })
    }

    private fun initRVAdapter(photosList: MutableList<Photo>) {
        photoRVAdapter = PhotoRVAdapter(photosList)
        photoRVAdapter!!.setHasStableIds(true)
        //        photoRVAdapter.setOnItemClickListener(new MainAlbumRVAdapter.OnAlbumItemClickListener() {
        //            @Override
        //            public void onClick(View view, int type, String photoPath) {
        //                albumEditView.onItemClicked(view, type, photoPath);
        //            }
        //        });
        rv_photo!!.adapter = photoRVAdapter
    }

    private inner class PhotoRVAdapter(photosList: MutableList<Photo>?) : RecyclerView.Adapter<PhotoRVAdapter.ViewHolder>() {

        private var photoList: MutableList<Photo>? = null

        init {
            if (photosList != null) {
                if (this.photoList != null) {
                    this.photoList!!.addAll(photosList)
                } else this.photoList = photosList
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = View.inflate(parent.context, R.layout.item_photo, null)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            if (photoList!!.size > 0) {
                holder!!.bind(photoList!![position])
            }
        }


        override fun getItemCount(): Int {
            return if (photoList != null) photoList!!.size else 0
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivPhoto = itemView.iv_photo
            var tvPhotoName = itemView.tv_photo_name
            var tvPhotoDescription = itemView.tv_photo_description
            var tvPhotoDate = itemView.tv_photo_date
            var tvPhotoGeoInfo = itemView.tv_photo_geo_info

            fun bind(photo: Photo) {
                Glide.with(ivPhoto.context)
                        .load(photo)
                        .into(ivPhoto)

                var exifInterface = ExifInterface(photo.photoPath)
                val altitude = exifInterface.getAltitude(0.0)
                val latArray = FloatArray(2)
                exifInterface.getLatLong(latArray)
                tvPhotoName.text = (File(photo.photoPath).name)
                val photoDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
                Log.e("log_tag", "longtitude : " + latArray[0]
                        + ",  : " + latArray[1] + " , attrs : " + photoDate)
                tvPhotoDate.text = (photoDate)
                tvPhotoGeoInfo.text = ("latitude: " + latArray[0]
                        + ", longitude  : " + latArray[1])
                var photo = Photo()
                photo.photoName = File(photo.photoPath).name
                photo.latitude = latArray[0].toDouble()
                photo.longitude = latArray[1].toDouble()
                Log.e("log_tag", "photo string= " + photo.toString())

            }
        }

        fun setPhotos(photos: MutableList<Photo>) {
            this.photoList = photos
        }
    }

    companion object {
        private val PERMISSION_REQUEST_CODE = 7
    }

}
