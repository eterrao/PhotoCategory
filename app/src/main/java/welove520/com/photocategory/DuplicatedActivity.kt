package welove520.com.photocategory

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_duplicated.*
import kotlinx.android.synthetic.main.content_duplicated.*
import org.greenrobot.greendao.query.Query

class DuplicatedActivity : AppCompatActivity() {

    private val LOG_TAG: String? = "LOG_TAG"

    private lateinit var sqlRVAdapter: SQLRVAdapter
    private lateinit var duplicatedRVAdapter: DuplicatedRVAdapter

    private lateinit var photoListModel: PhotoListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duplicated)
        setSupportActionBar(toolbar)
        photoListModel = intent.getSerializableExtra("photo_list") as PhotoListModel
        val daoSession = (application as MyApplication).daoSession
        photoDao = daoSession.photoDao
        initView()
    }

    private fun initView() {
        initPhotoListRecyclerView()
        initSQLiteRecyclerView()
    }

    private fun initPhotoListRecyclerView() {
        duplicatedRVAdapter = DuplicatedRVAdapter { photo: Photo ->
            clickEvent(photo)
        }
        rv_duplicated.adapter = duplicatedRVAdapter
        rv_duplicated.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        photosQuery = photoDao!!.queryBuilder().orderAsc(PhotoDao.Properties.PhotoName).build();
        var photoNameList = ArrayList<String>()
        var sqlPhotoNameList = ArrayList<String>()
        for (photo in photoListModel.photoList) {
            photoNameList.add(photo.photoName)
        }
        for (photo in photosQuery!!.list()) {
            sqlPhotoNameList.add(photo.photoName)
        }
        var allPhotoList = ArrayList<String>(photoNameList)
        allPhotoList.addAll(sqlPhotoNameList)
        var intersectionList = ArrayList<String>(photoNameList)
        intersectionList.retainAll(sqlPhotoNameList)
        var finalPhotoList = ArrayList<String>(allPhotoList)
        finalPhotoList.removeAll(intersectionList)
        var needPhotoList = ArrayList<Photo>()
        for (photoName in finalPhotoList) {
            for (photoMD5 in photoListModel.photoList) {
                if (photoName == photoMD5.photoName) {
                    Log.e(LOG_TAG, " common photo ===> " + photoMD5.photoName)
                    needPhotoList.add(photoMD5)
                }
            }
        }
        duplicatedRVAdapter.setPhotoList(needPhotoList)
    }

    private var photosQuery: Query<Photo>? = null

    private fun initSQLiteRecyclerView() {
        rv_in_sqlite.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        sqlRVAdapter = SQLRVAdapter()
        rv_in_sqlite.adapter = sqlRVAdapter
        photosQuery = photoDao!!.queryBuilder().orderAsc(PhotoDao.Properties.PhotoName).build();
        var photos = photosQuery!!.list()
        sqlRVAdapter.setPhotoList(photos)
    }

    private var photoDao: PhotoDao? = null

    private fun clickEvent(photo: Photo) {
        Log.e(LOG_TAG, "photo ====> " + photo.toString())
        photoDao!!.insertOrReplace(photo)
        var photos = photosQuery!!.list()
        sqlRVAdapter.setPhotoList(photos)
        sqlRVAdapter.notifyDataSetChanged()
    }
}

