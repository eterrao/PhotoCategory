package welove520.com.photocategory;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Raomengyang on 18-1-8.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class PhotoListModel implements Serializable {
    private static final long serialVersionUID = 2254394795527420270L;

    private List<Photo> photoList;

    private String albumName;

    private long albumTag;

    private long albumId;

    public List<Photo> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<Photo> photoList) {
        this.photoList = photoList;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public long getAlbumTag() {
        return albumTag;
    }

    public void setAlbumTag(long albumTag) {
        this.albumTag = albumTag;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    @Override
    public String toString() {
        return "PhotoListModel{" +
                "photoList=" + photoList +
                ", albumName='" + albumName + '\'' +
                ", albumTag=" + albumTag +
                ", albumId=" + albumId +
                '}';
    }
}
