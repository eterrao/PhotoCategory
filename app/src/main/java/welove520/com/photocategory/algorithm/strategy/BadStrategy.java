package welove520.com.photocategory.algorithm.strategy;

import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import welove520.com.photocategory.Photo;
import welove520.com.photocategory.algorithm.ICategoryStrategy;

/**
 * Created by Raomengyang on 18-1-16.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class BadStrategy implements ICategoryStrategy {

    @Override
    public Set<Integer> getNearbyPhotos(List<Photo> photosList) {

        Set<Integer> tagList = new LinkedHashSet<>();
        HashMap<Long, Long> nearbyPhotoCounterMap = new HashMap<>();
        int tempTag = 1;
        HashMap<Integer, List<Photo>> sortedByLocation = new HashMap<>();
        for (int index = 0; index < photosList.size() - 1; index++) {
            Photo cursorPhoto = photosList.get(index);
            if (cursorPhoto.getLatitude() != 0 || cursorPhoto.getLongitude() != 0) {
                LatLng curLat = new LatLng(cursorPhoto.getLatitude(), cursorPhoto.getLongitude());
                for (int j = index + 1; j < photosList.size(); j++) {
                    Photo nearbyPhoto = photosList.get(j);
                    LatLng nearbyLat = new LatLng(nearbyPhoto.getLatitude(), nearbyPhoto.getLongitude());
                    float distanceM = AMapUtils.calculateLineDistance(curLat, nearbyLat);
                    if (distanceM <= 500) {
                        if (cursorPhoto.getPhotoTag() <= 0 && nearbyPhoto.getPhotoTag() <= 0) {
                            cursorPhoto.setPhotoTag(tempTag);
                            nearbyPhoto.setPhotoTag(tempTag);
                            tagList.add(tempTag);
                            List<Photo> list = new ArrayList<Photo>();
                            list.add(cursorPhoto);
                            list.add(nearbyPhoto);
                            sortedByLocation.put(tempTag, list);
                            tempTag++;
                        } else {
                            if (cursorPhoto.getPhotoTag() > 0) {
                                int tag = cursorPhoto.getPhotoTag();
                                nearbyPhoto.setPhotoTag(tag);
                                List<Photo> photoList = sortedByLocation.get(tag);
                                photoList.add(nearbyPhoto);
                                sortedByLocation.put(tag, photoList);
                                tagList.add(tag);
                            } else {
                                if (nearbyPhoto.getPhotoTag() > 0) {
                                    int tag = nearbyPhoto.getPhotoTag();
                                    cursorPhoto.setPhotoTag(tag);
                                    List<Photo> photoList = sortedByLocation.get(tag);
                                    photoList.add(cursorPhoto);
                                    sortedByLocation.put(tag, photoList);
                                    tagList.add(tag);
                                }
                            }
                        }
                        Log.e("log_tag", "distance : " + distanceM + " m");
                        nearbyPhotoCounterMap.put(cursorPhoto.getId(), nearbyPhoto.getId());

                    }
                }
            }
        }
        Log.e("log_tag", "nearbyPhotoCounterMap : " + nearbyPhotoCounterMap.size());
        Log.e("log_tag", "sortedByLocation : " + sortedByLocation.size());
        return tagList;
    }
}
