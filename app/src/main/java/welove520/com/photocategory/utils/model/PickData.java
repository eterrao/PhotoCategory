package welove520.com.photocategory.utils.model;

import android.util.Log;


import java.io.Serializable;

import welove520.com.photocategory.utils.PickConfig;

/**
 * Created by wanbo on 2016/12/30.
 */

public class PickData implements Serializable {

    private int mode;//拍照、选择单张、选择多张
    private boolean crop;//是否需要裁剪
    /**
     * {@link CropImageView.CropMode}
     */
    private int cropMode;
    /**
     * {@link CropImageView.CropMode == RATIO_CUSTOM}时需要传入
     */
    private int cropWidth;
    /**
     * {@link CropImageView.CropMode == RATIO_CUSTOM}时需要传入
     */
    private int cropHeight;

    private int pickPhotoSize;
    private int themeColor;
    private int spanCount;
    private boolean isShowCamera;
    private boolean isShowOriginal;
    private int from;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public int getCropMode() {
        return cropMode;
    }

    public void setCropMode(int cropMode) {
        this.cropMode = cropMode;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = cropHeight;
    }

    public int getPickPhotoSize() {
        return pickPhotoSize;
    }

    public void setPickPhotoSize(int pickPhotoSize) {
        if(pickPhotoSize > 0 && pickPhotoSize <= PickConfig.MAX_PICK_SIZE){
            this.pickPhotoSize = pickPhotoSize;
        }else {
            Log.e(PickConfig.TAG,"Untrue size : photo size must between 1 and 100");
            this.pickPhotoSize = PickConfig.MAX_PICK_SIZE;
        }
    }

    public int getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }

    public int getSpanCount() {
        return spanCount;
    }

    public void setSpanCount(int spanCount) {
        if(spanCount > 0 && spanCount <= PickConfig.DEFAULT_SPAN_COUNT ) {
            this.spanCount = spanCount;
        }else {
            Log.e(PickConfig.TAG,"Untrue count : span count must between 1 and 4");
            this.spanCount = PickConfig.DEFAULT_SPAN_COUNT;
        }
    }

    public boolean isShowCamera() {
        return isShowCamera;
    }

    public void setShowCamera(boolean showCamera) {
        isShowCamera = showCamera;
    }

    public boolean isShowOriginal() {
        return isShowOriginal;
    }

    public void setShowOriginal(boolean showOriginal) {
        isShowOriginal = showOriginal;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }
}
