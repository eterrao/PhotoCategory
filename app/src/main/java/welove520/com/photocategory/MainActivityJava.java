package welove520.com.photocategory;

import android.Manifest;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import welove520.com.photocategory.utils.PermissionManager;
import welove520.com.photocategory.utils.PickConfig;
import welove520.com.photocategory.utils.PickPhotoHelper;
import welove520.com.photocategory.utils.PickPreferences;
import welove520.com.photocategory.utils.model.DirImage;
import welove520.com.photocategory.utils.model.GroupImage;

public class MainActivityJava extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_CODE = 7;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_photo)
    RecyclerView rvPhoto;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private PickPreferences pickPreferences;
    private List<String> allPhotos;
    private PhotoRVAdapter photoRVAdapter;

    private PhotoDao photoDao = null;
    private Query<Photo> photosQuery = null;
    private List<Photo> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (PermissionManager.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        } else {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            PermissionManager.checkPermissions(permissions, PERMISSION_REQUEST_CODE, this);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        initPhotoRecyclerView();
    }

    private void initPhotoRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPhoto.setLayoutManager(layoutManager);
        initPickHelper();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void initPickHelper() {
        pickPreferences = PickPreferences.getInstance(this);
        PickPhotoHelper helper = new PickPhotoHelper(this);
        pickPreferences.saveCurrentDirName(PickConfig.ALL_PHOTOS);
        Observable.just(helper)
                .map(new Func1<PickPhotoHelper, Object>() {
                    @Override
                    public Object call(PickPhotoHelper pickPhotoHelper) {
                        pickPhotoHelper.getImagesAsync();
                        return null;
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        DirImage dirImage = pickPreferences.getDirImage();
                        int listSize = dirImage.dirName.size();
                        GroupImage groupImage = PickPreferences.getInstance(MainActivityJava.this).getListImage();
                        if (groupImage != null && groupImage.getGroupMap() != null && groupImage.getGroupMap().size() > 0) {
                            List<String> photoPathList = groupImage.getGroupMap().get(PickConfig.ALL_PHOTOS);
                            List<Photo> photoList = new ArrayList<>();
                            for (int index = 0; index < photoPathList.size(); index++) {
                                Photo photo = new Photo();
                                photo.setPhotoPath(photoPathList.get(index));
                                photoList.add(photo);
                            }
                            initRVAdapter(photoList);
                        }
                    }
                });

    }

    private void initRVAdapter(final List<Photo> photosList) {
        photoRVAdapter = new PhotoRVAdapter(photosList);
        photoRVAdapter.setOnItemClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoPath = photosList.get((Integer) v.getTag()).getPhotoPath();
                Photo photo = new Photo();
                photo.setPhotoPath(photoPath);
                photo.setPhotoName(new File(photoPath).getName());
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(photoPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Double altitude = exifInterface.getAltitude(0.0);
                float[] latArray = new float[2];
                exifInterface.getLatLong(latArray);
                String photoDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                photo.setId(Long.parseLong(v.getTag() + ""));
                photo.setLatitude(Double.parseDouble(latArray[0] + ""));
                photo.setLongitude(Double.parseDouble(latArray[1] + ""));
                photo.setPhotoDate(new Date());

                photoDao.insert(photo);
            }
        });
        photoRVAdapter.setHasStableIds(true);
        rvPhoto.setAdapter(photoRVAdapter);
        DaoSession daoSession = ((MyApplication) getApplication()).getDaoSession();
        photoDao = daoSession.getPhotoDao();
        photosQuery = photoDao.queryBuilder().orderAsc(PhotoDao.Properties.PhotoName).build();
        photos = photosQuery.list();
        if (photos != null && photos.size() > 0) {
            photoRVAdapter.setPhotos(photos);
        }
    }

    public class PhotoRVAdapter extends RecyclerView.Adapter<PhotoRVAdapter.ViewHolder> {

        private List<Photo> photoList;
        private View.OnClickListener onItemClickedListener;

        public PhotoRVAdapter(List<Photo> photosList) {
            if (photosList != null) {
                if (this.photoList != null) {
                    this.photoList.addAll(photosList);
                } else {
                    this.photoList = photosList;
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_photo, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder != null) {
                if (photoList != null && photoList.size() > 0) {
                    Photo photo = photoList.get(position);

                    Glide.with(holder.ivPhoto.getContext())
                            .load(photo.getPhotoPath())
                            .centerCrop()
                            .into(holder.ivPhoto);
                }
                if (onItemClickedListener != null) {
                    holder.itemView.setTag(position);
                    holder.itemView.setOnClickListener(onItemClickedListener);
                }
            }
        }

        public void setOnItemClickedListener(View.OnClickListener onItemClickedListener) {
            this.onItemClickedListener = onItemClickedListener;
        }

        @Override
        public int getItemCount() {
            return photoList != null ? photoList.size() : 0;
        }

        public void setPhotos(List<Photo> photos) {
            photoList = photos;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.iv_photo)
            ImageView ivPhoto;
            @BindView(R.id.tv_photo_name)
            TextView tvPhotoName;
            @BindView(R.id.tv_photo_description)
            TextView tvPhotoDescription;
            @BindView(R.id.tv_photo_date)
            TextView tvPhotoDate;
            @BindView(R.id.tv_photo_geo_info)
            TextView tvPhotoGeoInfo;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

}
