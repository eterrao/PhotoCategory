package welove520.com.photocategory;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.BusinessArea;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.bumptech.glide.Glide;

import org.greenrobot.greendao.query.Query;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import welove520.com.photocategory.algorithm.Main2Activity;
import welove520.com.photocategory.algorithm.StrategyContext;
import welove520.com.photocategory.algorithm.strategy.KMeansPlusPlusClusterStrategy;
import welove520.com.photocategory.tensorflow.ImageClassifier;
import welove520.com.photocategory.utils.PermissionManager;
import welove520.com.photocategory.utils.PickConfig;
import welove520.com.photocategory.utils.PickPhotoHelper;
import welove520.com.photocategory.utils.PickPreferences;
import welove520.com.photocategory.utils.model.GroupImage;

public class MainActivityJava extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GeocodeSearch.OnGeocodeSearchListener, AMap.OnMyLocationChangeListener, SeekBar.OnSeekBarChangeListener, AMap.OnCameraChangeListener {
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
    @BindView(R.id.sb_zoom)
    SeekBar sbZoom;
    @BindView(R.id.rv_recommend_photo)
    RecyclerView rvRecommendPhoto;

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
    private UiSettings mUiSettings;//定义一个UiSettings对象


    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    private ArrayList<Photo> photoList;
    private RecommendPhotoRVAdapter recommendAdapter;


    private ImageClassifier classifier;
    private int kValue = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initTensorFlowClassifier();
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

            mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
            mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
            mUiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示

            MyLocationStyle myLocationStyle;
            myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
            myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
            myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        }

        sbZoom.setProgress((int) aMap.getCameraPosition().zoom);
        sbZoom.setOnSeekBarChangeListener(this);
    }

    private void initTensorFlowClassifier() {
        try {
            classifier = new ImageClassifier(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.");
        }
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(Photo photo) {
        if (photo == null) return;
        LatLng latLng = new LatLng(photo.getLatitude(), photo.getLongitude());
        int width = 100;
        int height = 100;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);
        Paint brushPaint = new Paint();
        brushPaint.setStyle(Paint.Style.FILL);
        brushPaint.setColor(Color.WHITE);
        brushPaint.setAntiAlias(true);
        brushPaint.setTextSize(20);
        brushPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(photo.getPhotoTag() + "\n" + photo.getPhotoAddress()), canvas.getWidth() / 2, canvas.getHeight() / 2, brushPaint);
        markerOption = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(latLng)
                .draggable(false);
        aMap.addMarker(markerOption).showInfoWindow();
    }

    private Bitmap getClipBitmap(String srcImagePath, float clipWidth, float clipHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcImagePath, options);
        //根据原始图片的宽高比和期望的输出图片的宽高比计算最终输出的图片的宽和高
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        float minWidth = clipWidth;
        float minHeight = clipHeight;
        float srcRatio = srcWidth / srcHeight;
        float outRatio = minWidth / minHeight;
        float actualOutWidth = srcWidth;
        float actualOutHeight = srcHeight;

        if (srcRatio > outRatio) {
            actualOutHeight = minHeight;
            actualOutWidth = actualOutHeight * srcRatio;
        } else if (srcRatio < outRatio) {
            actualOutWidth = minWidth;
            actualOutHeight = actualOutWidth / srcRatio;
        } else {
            actualOutWidth = minWidth;
            actualOutHeight = minHeight;
        }
        options.inSampleSize = computSampleSize(options, actualOutWidth, actualOutHeight);
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap = null;
        try {
            scaledBitmap = BitmapFactory.decodeFile(srcImagePath, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (scaledBitmap == null) {
            return null;//压缩失败
        }
        //生成最终输出的bitmap
        Bitmap actualOutBitmap = Bitmap.createScaledBitmap(scaledBitmap, (int) actualOutWidth, (int) actualOutHeight, true);
        if (actualOutBitmap != scaledBitmap) {
            scaledBitmap.recycle();
        }

//        //处理图片旋转问题
//        ExifInterface exif = null;
//        try {
//            exif = new ExifInterface(srcImagePath);
//            int orientation = exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION, 0);
//            Matrix matrix = new Matrix();
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
//                matrix.postRotate(90);
//            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
//                matrix.postRotate(180);
//            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//                matrix.postRotate(270);
//            }
//            actualOutBitmap = Bitmap.createBitmap(actualOutBitmap, 0, 0,
//                    actualOutBitmap.getWidth(), actualOutBitmap.getHeight(), matrix, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
        return actualOutBitmap;
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

    private void drawRoundRect(Canvas canvas, Paint paint, float width, float height) {
        float mMargin = 15f;
        float mRadius = 5;
        float right = width - mMargin;
        float bottom = height - mMargin;
        canvas.drawRoundRect(new RectF(mMargin, mMargin, right, bottom), mRadius, mRadius, paint);
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
        if (classifier != null) {
            classifier.close();
        }
        super.onDestroy();
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    private void initPhotoRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPhoto.setLayoutManager(layoutManager);
        LinearLayoutManager recommendLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvRecommendPhoto.setLayoutManager(recommendLayoutManager);
        recommendAdapter = new RecommendPhotoRVAdapter();
        rvRecommendPhoto.setAdapter(recommendAdapter);
        recommendAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                PhotoListModel photoListModel = new PhotoListModel();
                photoListModel.setPhotoList(photoList);
                Intent intent = new Intent(MainActivityJava.this, DuplicatedActivity.class);
                intent.putExtra("photo_list", photoListModel);
                startActivity(intent);
            }
        });

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
                        GroupImage groupImage = PickPreferences.getInstance(MainActivityJava.this).getListImage();
                        if (groupImage != null && groupImage.getGroupMap() != null && groupImage.getGroupMap().size() > 0) {
                            List<String> photoPathList = groupImage.getGroupMap().get(PickConfig.ALL_PHOTOS);
                            if (photoList == null) {
                                photoList = new ArrayList<>();
                            } else {
                                photoList.clear();
                            }
                            for (int index = 0; index < photoPathList.size(); index++) {
                                String photoPath = photoPathList.get(index);
                                ExifInterface exifInterface = null;
                                try {
                                    exifInterface = new ExifInterface(photoPath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                float[] latArray = new float[2];
                                exifInterface.getLatLong(latArray);
                                Photo photo = new Photo();
                                File file = new File(photoPath);
                                if (file != null && file.exists()) {
                                    photo.setPhotoName(file.getName());
                                    photo.setPhotoDate(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
                                }
                                photo.setPhotoPath(photoPath);
                                photo.setLatitude(Double.parseDouble(latArray[0] + ""));
                                photo.setLongitude(Double.parseDouble(latArray[1] + ""));
                                photo.setId((long) index);
                                if (photo.getLatitude() == 0 && photo.getLongitude() == 0) {
                                } else {
                                    photoList.add(photo);
                                    addMarkersToMap(photo);// 往地图上添加marker
                                }
                            }
                            initRVAdapter(photoList);
                        }
                    }
                });

    }

    private void classifyPhoto(String photoPath, Photo photo) {
        Bitmap clippedBitmap = getClipBitmap(photoPath, ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
        if (clippedBitmap != null) {
            Bitmap bitmap =
                    Bitmap.createBitmap(clippedBitmap,
                            0, 0, ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
            String classifierResult = classifyFrame(bitmap);
            photo.setPhotoClassify(classifierResult);
            Log.e(TAG, " classify result :" + classifierResult);
        }
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
//                photoDao.insert(photo);
//                showDialog();
                PhotoListModel photoListModel = new PhotoListModel();
                photoListModel.setPhotoList(photoList);
                Intent intent = new Intent(MainActivityJava.this, Main2Activity.class);
                intent.putExtra("photoList", photoListModel);
                startActivity(intent);
            }
        });
        photoRVAdapter.setHasStableIds(true);
        rvPhoto.setAdapter(photoRVAdapter);
//        DaoSession daoSession = ((MyApplication) getApplication()).getDaoSession();
//        photoDao = daoSession.getPhotoDao();
//        photosQuery = photoDao.queryBuilder().orderAsc(PhotoDao.Properties.PhotoName).build();
//        photos = photosQuery.list();

        StrategyContext strategyContext = new StrategyContext(new KMeansPlusPlusClusterStrategy(kValue));
        final List<Photo> list = strategyContext.getNearbyPhotosCategory(photosList);
        final Map<Integer, Photo> photoMap = new LinkedHashMap<>();
        List<Photo> recommendList = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            Photo photo = list.get(index);
            if (!photoMap.containsKey(photo.getPhotoTag())) {
                photoMap.put(photo.getPhotoTag(), photo);
                recommendList.add(photo);
            }
        }
        recommendAdapter.setRecommendList(recommendList);

        for (int index = 0; index < photoMap.size(); index++) {
            final GeocodeSearch geocoderSearch = new GeocodeSearch(this);
            Photo photo = photoMap.get(index);
            LatLonPoint latLonPoint = new LatLonPoint(photo.getLatitude(), photo.getLongitude());
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
            int order = index;
            geocoderSearch.setOnGeocodeSearchListener(new OnCustomGeocodeSearchListener<Integer>(order) {
                @Override
                public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                    Log.e("log_tag", "index = " + getCallbackObj() + " , getPoint = " + result.getRegeocodeQuery().getPoint().toString());
                    Log.e(TAG, "index : " + getCallbackObj() + " format address : " + result.getRegeocodeAddress().getFormatAddress());
                    Log.e(TAG, "index : " + getCallbackObj() + " format getBusinessAreas : " + result.getRegeocodeAddress().getBusinessAreas());
                    Log.e(TAG, "index : " + getCallbackObj() + " format getBuilding : " + result.getRegeocodeAddress().getBuilding());
                    if (rCode == AMapException.CODE_AMAP_SUCCESS) {
                        if (result != null) {
                            if (result.getRegeocodeAddress() != null) {
                                List<BusinessArea> addressName = result.getRegeocodeAddress().getBusinessAreas();
                                if (addressName != null && addressName.size() > 0) {
                                    if (list != null && list.size() > 0) {
                                        for (Photo photo : photosList) {
                                            if (photo.getPhotoTag() - getCallbackObj() == 0) {
                                                photo.setPhotoAddress(result.getRegeocodeAddress().getBusinessAreas().get(0).getName());
                                            }
                                        }
                                        photoRVAdapter.setPhotos(list);
                                    }
                                    Log.e(TAG, "index = " + getCallbackObj() + " business address  = " + result.getRegeocodeAddress().getBusinessAreas().get(0).getName());
                                } else {
                                    if (list != null && list.size() > 0) {
                                        for (Photo photo : photosList) {
                                            if (photo.getPhotoTag() - getCallbackObj() == 0) {
                                                photo.setPhotoAddress(result.getRegeocodeAddress().getFormatAddress());
                                            }
                                        }
                                        photoRVAdapter.setPhotos(list);
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, getApplicationContext().getResources().getString(R.string.no_result));
                        }
                    } else showerror(getApplicationContext(), rCode);
                }

                @Override
                public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                }
            });
        }
        photoRVAdapter.setPhotos(list);
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
                if (addressName != null && addressName.size() > 0) {
                    Toast.makeText(this, "business address  = " + result.getRegeocodeAddress().getBusinessAreas().get(0).getName(), Toast.LENGTH_SHORT).show();
                }
                for (BusinessArea bussinessArea : addressName) {
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //设置希望展示的地图缩放级别
        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(progress);
        aMap.moveCamera(mCameraUpdate);
        if (photoList != null && photoList.size() > 0) {
            for (int index = 0; index < photoList.size(); index++) {
                Photo photo = photoList.get(index);
                addMarkersToMap(photo);
            }

            recommendAdapter.notifyDataSetChanged();
//            if (tagList != null && tagList.size() > 0) {
//                List<Photo> recommendList = new ArrayList<>(3);
//                Iterator<Integer> iterator = tagList.iterator();
//                while (iterator.hasNext()) {
//                    Integer tag = iterator.next();
//                    if (tag != null) {
//                        for (int index = 0; index < photoList.size(); index++) {
//                            Photo photo = photoList.get(index);
//                            if (photo.getPhotoTag() == tag) {
//                                recommendList.add(photo);
//                            }
//                        }
//                    }
//                }
//                recommendAdapter.setRecommendList(recommendList);
//            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.e(TAG, "zoom : " + cameraPosition.zoom);
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.e(TAG, "zoom finished : " + cameraPosition.zoom);

    }

    @Override
    public void onMyLocationChange(Location location) {
        Log.e(TAG, " location ===> " + location.toString());
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
                    holder.tvPhotoDate.setText(photo.getPhotoDate());
                    holder.tvPhotoDescription.setText("address: " + photo.getPhotoAddress());
                    holder.tvPhotoTag.setText(String.valueOf(photo.getPhotoTag() + "\n" + photo.getPhotoAddress()));
                    holder.tvPhotoClassify.setText(photo.getPhotoClassify());
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
            if (photos != null) {
                if (this.photoList != null) {
                    this.photoList.clear();
                    this.photoList.addAll(photos);
                } else {
                    this.photoList = photos;
                }
                notifyDataSetChanged();
            }
        }

        public List<Photo> getPhotoList() {
            return photoList;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.iv_photo)
            ImageView ivPhoto;
            @BindView(R.id.tv_photo_tag)
            TextView tvPhotoTag;
            @BindView(R.id.tv_photo_name)
            TextView tvPhotoName;
            @BindView(R.id.tv_photo_description)
            TextView tvPhotoDescription;
            @BindView(R.id.tv_photo_date)
            TextView tvPhotoDate;
            @BindView(R.id.tv_photo_classify)
            TextView tvPhotoClassify;
            @BindView(R.id.tv_photo_geo_info)
            TextView tvPhotoGeoInfo;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    public static class RecommendPhotoRVAdapter extends RecyclerView.Adapter<RecommendPhotoRVAdapter.RecommendViewHolder> {

        private List<Photo> recommendList = new ArrayList<>(3);
        private OnItemClickListener onItemClickListener;

        public void setRecommendList(List<Photo> recommendList) {
            if (recommendList != null && recommendList.size() > 0) {
                this.recommendList.clear();
                this.recommendList.addAll(recommendList);
                notifyDataSetChanged();
            }
        }

        @Override
        public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_recommend_photo, null);
            return new RecommendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecommendViewHolder holder, final int position) {
            Photo photo = recommendList.get(position);
            Glide.with(holder.ivRecommend.getContext())
                    .load(photo.getPhotoPath())
                    .placeholder(R.drawable.ic_launcher)
                    .into(holder.ivRecommend);
            holder.tvRecommend.setText(photo.getPhotoAddress());
            holder.ivRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return recommendList != null ? recommendList.size() : 0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        static class RecommendViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.iv_recommend)
            ImageView ivRecommend;
            @BindView(R.id.tv_recommend)
            TextView tvRecommend;

            RecommendViewHolder(View view) {
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

    static class ViewHolder {
        @BindView(R.id.iv_photo_in_map)
        ImageView ivPhotoInMap;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    /**
     * Classifies a frame from the preview stream.
     */
    private String classifyFrame(Bitmap bitmap) {
        if (classifier == null) {
            Toast.makeText(this, "Uninitialized Classifier or invalid context.", Toast.LENGTH_SHORT).show();
            return null;
        }
        String textToShow = null;
        if (bitmap != null) {
            textToShow = classifier.classifyFrame(bitmap);
        }
        bitmap.recycle();
        return textToShow;
    }
}
