package welove520.com.photocategory.utils;


import welove520.com.photocategory.R;

/**
 * Created by wanbo on 2016/12/30.
 * ContinuousPickMultiPhotoWorker 配置文件
 */

public class PickConfig {

    // TAG
    public static final String TAG = "ContinuousPickMultiPhotoWorker";
    // intent data
    public static final String INTENT_PICK_DATA = "intent_pick_Data";
    // intent dirName
    public static final String INTENT_DIR_NAME = "intent_dir_name";
    // intent img path
    public static final String INTENT_IMG_PATH = "intent_img_path";
    // intent img list
    public static final String INTENT_IMG_LIST = "intent_img_list";
    // intent camera uri
    public static final String INTENT_CAMERA_URI = "intent_camera_uri";
    // intent img select list
    public static final String INTENT_IMG_LIST_SELECT = "intent_img_list_select";
    // take photo mode
    public static final int MODE_TAKE_PHOTO = 1;
    // choose single photo mode
    public static final int MODE_SINGLE_PHOTO = 2;
    // choose multi photo mode continuous
    public static final int MODE_MULTI_PHOTO_CONTINUOUS = 3;
    // choose multi photo mode slide
    public static final int MODE_MULTI_PHOTO_SLIDE = 4;
    // all photos
    public static final String ALL_PHOTOS = "All Photos";
    // Camera type
    public static final int CAMERA_TYPE = -1;
    // space
    public static final int ITEM_SPACE = 4;
    // intent requestCode
    public static final int PICK_PHOTO_DATA = 0x5521;
    // intent requestCode
    public static final int CAMERA_PHOTO_DATA = 0x9949;
    // default size
    public static final int DEFAULT_PICK_SIZE = 9;
    // max size
    public static final int MAX_PICK_SIZE = 100;
    // default theme color
    public static final int DEFAULT_THEME_COLOR = R.color.colorPrimary;
    // default span count
    public static final int DEFAULT_SPAN_COUNT = 4;

    public static final int FROM_CHAT = 10;
    public static final int FROM_ALBUM = 11;
}
