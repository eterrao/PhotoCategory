package welove520.com.photocategory;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;

/**
 * Created by Raomengyang on 17-12-26.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */


@Entity(indexes = {@Index(value = "photoName, photoDate DESC", unique = true)})
public class Photo implements Clusterable, Serializable {
    private static final long serialVersionUID = 3003712021798271649L;

    @Id
    private Long id;

    @NotNull
    private String photoName;
    private String photoDate;
    private Double latitude;
    private Double longitude;
    private String photoPath;
    private String photoClassify;
    private String photoAddress;
    private int photoTag;
    private int tagCount;

    @Generated(hash = 1043664727)
    public Photo() {
    }

    @Generated(hash = 1794596164)
    public Photo(Long id, @NotNull String photoName, String photoDate, Double latitude, Double longitude, String photoPath, String photoClassify, String photoAddress, int photoTag,
                 int tagCount) {
        this.id = id;
        this.photoName = photoName;
        this.photoDate = photoDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
        this.photoClassify = photoClassify;
        this.photoAddress = photoAddress;
        this.photoTag = photoTag;
        this.tagCount = tagCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }


    public String getPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(String photoDate) {
        this.photoDate = photoDate;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getPhotoClassify() {
        return photoClassify;
    }

    public void setPhotoClassify(String photoClassify) {
        this.photoClassify = photoClassify;
    }

    public String getPhotoAddress() {
        return photoAddress;
    }

    public void setPhotoAddress(String photoAddress) {
        this.photoAddress = photoAddress;
    }

    public int getPhotoTag() {
        return photoTag;
    }

    public void setPhotoTag(int photoTag) {
        this.photoTag = photoTag;
    }

    public int getTagCount() {
        return tagCount;
    }

    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
    }


    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", photoName='" + photoName + '\'' +
                ", photoDate='" + photoDate + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", photoPath='" + photoPath + '\'' +
                ", photoClassify='" + photoClassify + '\'' +
                ", photoAddress='" + photoAddress + '\'' +
                ", photoTag=" + photoTag +
                ", tagCount=" + tagCount +
                '}';
    }

    @Override
    public double[] getPoint() {
        return new double[]{latitude, longitude};
    }
}
