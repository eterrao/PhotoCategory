package welove520.com.photocategory;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.BusinessArea;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.bumptech.glide.Glide;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
        implements NavigationView.OnNavigationItemSelectedListener,
        GeocodeSearch.OnGeocodeSearchListener {
    private static final int PERMISSION_REQUEST_CODE = 7;
    private static final String TAG = MainActivityJava.class.getSimpleName();

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
    @BindView(R.id.map_view)
    MapView mapView;

    private PickPreferences pickPreferences;
    private List<String> allPhotos;
    private PhotoRVAdapter photoRVAdapter;

    private PhotoDao photoDao = null;
    private Query<Photo> photosQuery = null;
    private List<Photo> photos;
    private GeocodeSearch geocoderSearch;
    private ProgressDialog progDialog;
    private AMap aMap;
    private MarkerOptions markerOption;

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

        mapView.onCreate(savedInstanceState);
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        progDialog = new ProgressDialog(this);

        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    /**
     * 在地图上添加marker
     *
     * @param latlng
     */
    private void addMarkersToMap(LatLng latlng, String path) {

        markerOption = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
                .position(latlng)
                .draggable(false);
        aMap.addMarker(markerOption).showInfoWindow();
//        customMarker.showInfoWindow();
    }

    private int computSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
        float srcWidth = options.outWidth;//20
        float srcHeight = options.outHeight;//10
        int sampleSize = 1;
        if (srcWidth > reqWidth || srcHeight > reqHeight) {
            int withRatio = Math.round(srcWidth / reqWidth);
            int heightRatio = Math.round(srcHeight / reqHeight);
            sampleSize = Math.min(withRatio, heightRatio);
        }
        return sampleSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
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
                        pickPhotoHelper.getImages();
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
                                String photoPath = photoPathList.get(index);
                                ExifInterface exifInterface = null;
                                try {
                                    exifInterface = new ExifInterface(photoPath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Double altitude = exifInterface.getAltitude(0.0);
                                float[] latArray = new float[2];
                                exifInterface.getLatLong(latArray);
                                Photo photo = new Photo();
                                photo.setPhotoName(new File(photoPath).getName());
                                photo.setPhotoPath(photoPath);
                                photo.setLatitude(Double.parseDouble(latArray[0] + ""));
                                photo.setLongitude(Double.parseDouble(latArray[1] + ""));
                                photoList.add(photo);
                                LatLng latLonPoint = new LatLng(photo.getLatitude(), photo.getLongitude());
                                addMarkersToMap(latLonPoint, photoPath);// 往地图上添加marker
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
                Photo photo = photosList.get((Integer) v.getTag());
//                Photo photo = new Photo();
//                photo.setPhotoPath(photoPath);
//                photo.setPhotoName(new File(photoPath).getName());

//                String photoDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
//                photo.setId(Long.parseLong(v.getTag() + ""));
//                photo.setLatitude(Double.parseDouble(latArray[0] + ""));
//                photo.setLongitude(Double.parseDouble(latArray[1] + ""));
//                photo.setPhotoDate(new Date());
//
//                photoDao.insert(photo);
//                showDialog();
                // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                LatLonPoint latLonPoint = new LatLonPoint(photo.getLatitude(), photo.getLongitude());
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);
            }
        });
        photoRVAdapter.setHasStableIds(true);
        rvPhoto.setAdapter(photoRVAdapter);
        DaoSession daoSession = ((MyApplication) getApplication()).getDaoSession();
        photoDao = daoSession.getPhotoDao();
        photosQuery = photoDao.queryBuilder().orderAsc(PhotoDao.Properties.PhotoName).build();
        photos = photosQuery.list();
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        Log.e("log_tag", "getPoint = " + result.getRegeocodeQuery().getPoint().toString());
        Toast.makeText(this, "getPoint = " + result.getRegeocodeQuery().getPoint().toString(), Toast.LENGTH_SHORT).show();
        dismissDialog();

        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
//                result.getRegeocodeAddress().getFormatAddress()
                List<BusinessArea> addressName = result.getRegeocodeAddress().getBusinessAreas();
                for (BusinessArea bussinessArea :
                        addressName) {
                    Log.e(TAG, "address area = " + bussinessArea.getName());
                }
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        AMapUtil.convertToLatLng(latLonPoint), 15));
//                regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
//                Toast.makeText(this, addressName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.no_result, Toast.LENGTH_SHORT).show();
            }
        } else {
            showerror(this, rCode);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        Log.e("log_tag", "getPoint = " + geocodeResult.toString());
//        Toast.makeText(this, "getPoint = " + geocodeResult.getRegeocodeQuery().getPoint().toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示进度条对话框
     */
    public void showDialog() {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在获取地址");
        progDialog.show();
    }

    /**
     * 隐藏进度条对话框
     */
    public void dismissDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
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
                    holder.tvPhotoName.setText(photo.getPhotoName());
                    holder.tvPhotoGeoInfo.setText("lat = " + photo.getLatitude() + " , longitude = " + photo.getLongitude());
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

        @Override
        public long getItemId(int position) {
            return position;
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

    public static void showerror(Context context, int rCode) {
        try {
            switch (rCode) {
                //服务错误码
                case 1001:
                    throw new AMapException(AMapException.AMAP_SIGNATURE_ERROR);
                case 1002:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_KEY);
                case 1003:
                    throw new AMapException(AMapException.AMAP_SERVICE_NOT_AVAILBALE);
                case 1004:
                    throw new AMapException(AMapException.AMAP_DAILY_QUERY_OVER_LIMIT);
                case 1005:
                    throw new AMapException(AMapException.AMAP_ACCESS_TOO_FREQUENT);
                case 1006:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_IP);
                case 1007:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_DOMAIN);
                case 1008:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_SCODE);
                case 1009:
                    throw new AMapException(AMapException.AMAP_USERKEY_PLAT_NOMATCH);
                case 1010:
                    throw new AMapException(AMapException.AMAP_IP_QUERY_OVER_LIMIT);
                case 1011:
                    throw new AMapException(AMapException.AMAP_NOT_SUPPORT_HTTPS);
                case 1012:
                    throw new AMapException(AMapException.AMAP_INSUFFICIENT_PRIVILEGES);
                case 1013:
                    throw new AMapException(AMapException.AMAP_USER_KEY_RECYCLED);
                case 1100:
                    throw new AMapException(AMapException.AMAP_ENGINE_RESPONSE_ERROR);
                case 1101:
                    throw new AMapException(AMapException.AMAP_ENGINE_RESPONSE_DATA_ERROR);
                case 1102:
                    throw new AMapException(AMapException.AMAP_ENGINE_CONNECT_TIMEOUT);
                case 1103:
                    throw new AMapException(AMapException.AMAP_ENGINE_RETURN_TIMEOUT);
                case 1200:
                    throw new AMapException(AMapException.AMAP_SERVICE_INVALID_PARAMS);
                case 1201:
                    throw new AMapException(AMapException.AMAP_SERVICE_MISSING_REQUIRED_PARAMS);
                case 1202:
                    throw new AMapException(AMapException.AMAP_SERVICE_ILLEGAL_REQUEST);
                case 1203:
                    throw new AMapException(AMapException.AMAP_SERVICE_UNKNOWN_ERROR);
                    //sdk返回错误
                case 1800:
                    throw new AMapException(AMapException.AMAP_CLIENT_ERRORCODE_MISSSING);
                case 1801:
                    throw new AMapException(AMapException.AMAP_CLIENT_ERROR_PROTOCOL);
                case 1802:
                    throw new AMapException(AMapException.AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION);
                case 1803:
                    throw new AMapException(AMapException.AMAP_CLIENT_URL_EXCEPTION);
                case 1804:
                    throw new AMapException(AMapException.AMAP_CLIENT_UNKNOWHOST_EXCEPTION);
                case 1806:
                    throw new AMapException(AMapException.AMAP_CLIENT_NETWORK_EXCEPTION);
                case 1900:
                    throw new AMapException(AMapException.AMAP_CLIENT_UNKNOWN_ERROR);
                case 1901:
                    throw new AMapException(AMapException.AMAP_CLIENT_INVALID_PARAMETER);
                case 1902:
                    throw new AMapException(AMapException.AMAP_CLIENT_IO_EXCEPTION);
                case 1903:
                    throw new AMapException(AMapException.AMAP_CLIENT_NULLPOINT_EXCEPTION);
                    //云图和附近错误码
                case 2000:
                    throw new AMapException(AMapException.AMAP_SERVICE_TABLEID_NOT_EXIST);
                case 2001:
                    throw new AMapException(AMapException.AMAP_ID_NOT_EXIST);
                case 2002:
                    throw new AMapException(AMapException.AMAP_SERVICE_MAINTENANCE);
                case 2003:
                    throw new AMapException(AMapException.AMAP_ENGINE_TABLEID_NOT_EXIST);
                case 2100:
                    throw new AMapException(AMapException.AMAP_NEARBY_INVALID_USERID);
                case 2101:
                    throw new AMapException(AMapException.AMAP_NEARBY_KEY_NOT_BIND);
                case 2200:
                    throw new AMapException(AMapException.AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR);
                case 2201:
                    throw new AMapException(AMapException.AMAP_CLIENT_USERID_ILLEGAL);
                case 2202:
                    throw new AMapException(AMapException.AMAP_CLIENT_NEARBY_NULL_RESULT);
                case 2203:
                    throw new AMapException(AMapException.AMAP_CLIENT_UPLOAD_TOO_FREQUENT);
                case 2204:
                    throw new AMapException(AMapException.AMAP_CLIENT_UPLOAD_LOCATION_ERROR);
                    //路径规划
                case 3000:
                    throw new AMapException(AMapException.AMAP_ROUTE_OUT_OF_SERVICE);
                case 3001:
                    throw new AMapException(AMapException.AMAP_ROUTE_NO_ROADS_NEARBY);
                case 3002:
                    throw new AMapException(AMapException.AMAP_ROUTE_FAIL);
                case 3003:
                    throw new AMapException(AMapException.AMAP_OVER_DIRECTION_RANGE);
                    //短传分享
                case 4000:
                    throw new AMapException(AMapException.AMAP_SHARE_LICENSE_IS_EXPIRED);
                case 4001:
                    throw new AMapException(AMapException.AMAP_SHARE_FAILURE);
                default:
                    Toast.makeText(context, "错误码：" + rCode, Toast.LENGTH_LONG).show();
                    logError("查询失败", rCode);
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            logError(e.getMessage(), rCode);
        }
    }

    private static void logError(String info, int errorCode) {
        print("                                   错误信息                                     ");
        print(info);
        print("错误码: " + errorCode);
        print("                                                                               ");
        print("如果需要更多信息，请根据错误码到以下地址进行查询");
        print("  http://lbs.amap.com/api/android-sdk/guide/map-tools/error-code/");
        print("如若仍无法解决问题，请将全部log信息提交到工单系统，多谢合作");
    }


    private static void print(String s) {
        Log.i(TAG, s);
    }
}
