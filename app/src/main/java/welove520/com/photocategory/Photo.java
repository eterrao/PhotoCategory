package welove520.com.photocategory;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created by Raomengyang on 17-12-26.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */


@Entity(indexes = {@Index(value = "photoName, photoDate DESC", unique = true)})
public class Photo {

    @Id
    private Long id;

    @NotNull
    private String photoName;
    private Date photoDate;
    private Double latitude;
    private Double longitude;
    private String photoPath;

    @Generated(hash = 1043664727)
    public Photo() {
    }

    @Generated(hash = 1384252396)
    public Photo(Long id, @NotNull String photoName, Date photoDate, Double latitude,
            Double longitude, String photoPath) {
        this.id = id;
        this.photoName = photoName;
        this.photoDate = photoDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
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


    public Date getPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(Date photoDate) {
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

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", photoName='" + photoName + '\'' +
                ", photoDate=" + photoDate +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
