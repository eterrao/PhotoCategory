package welove520.com.photocategory.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import welove520.com.photocategory.utils.model.DirImage;
import welove520.com.photocategory.utils.model.GroupImage;
import welove520.com.photocategory.utils.model.PickData;


/**
 * Created by wanbo on 2017/1/3.
 */

public class PickPreferences {

    private static PickPreferences mInstance = null;
    private final SharedPreferences mSharedPreferences;
    private Context context;

    private static final String IMAGE_LIST = "image_list";
    private static final String DIR_NAMES = "dir_names";
    private static final String PICK_DATA = "pick_data";
    private static final String CURRENT_DIR_NAME = "current_dir_name";

    private GroupImage listImage;
    private DirImage dirImage;
    private PickData pickData;

    public static PickPreferences getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PickPreferences.class) {
                if (mInstance == null) {
                    mInstance = new PickPreferences(context);
                }
            }
        }
        return mInstance;
    }

    private PickPreferences(Context context) {
        this.context = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean saveImageList(GroupImage images){
        listImage = images;
        Editor editor = mSharedPreferences.edit();
        editor.putString(IMAGE_LIST, images.toJson());
        boolean result = editor.commit();
        return result;
    }

    public GroupImage getListImage(){
        if(listImage == null) {
            String ss = mSharedPreferences.getString(IMAGE_LIST, "");
            if(TextUtils.isEmpty(ss)) {
                return null;
            } else {
//                listImage = PickGson.fromJson(GroupImage.class, ss);
                try {
                    listImage = (GroupImage) JSONHandler.parse(ss, GroupImage.class);
                } catch (Exception e) {
                    Log.e(PickConfig.TAG, "", e);
                }
            }
        }
        return listImage;
    }

    public boolean saveDirNames(DirImage images){
        dirImage = images;
        Editor editor = mSharedPreferences.edit();
        editor.putString(DIR_NAMES, images.toJson());
        boolean result = editor.commit();
        return result;
    }

    public DirImage getDirImage(){
        if(dirImage == null) {
            String ss = mSharedPreferences.getString(DIR_NAMES, "");
            if(TextUtils.isEmpty(ss)) {
                return null;
            } else {
//                dirImage = PickGson.fromJson(DirImage.class, ss);
                try {
                    dirImage = (DirImage) JSONHandler.parse(ss, DirImage.class);
                } catch (Exception e) {
                    Log.e(PickConfig.TAG, "", e);
                }
            }
        }
        return dirImage;
    }

    public boolean saveCurrentDirName(String dirName){
        Editor editor = mSharedPreferences.edit();
        editor.putString(CURRENT_DIR_NAME, dirName);
        boolean result = editor.commit();
        return result;
    }

    public String getCurrentDirName(){
        return mSharedPreferences.getString(CURRENT_DIR_NAME, "");
    }
}
