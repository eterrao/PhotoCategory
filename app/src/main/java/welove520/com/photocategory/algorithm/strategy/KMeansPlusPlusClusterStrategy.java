package welove520.com.photocategory.algorithm.strategy;

import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.ArrayList;
import java.util.List;

import welove520.com.photocategory.Photo;
import welove520.com.photocategory.algorithm.ICategoryStrategy;

/**
 * Created by Raomengyang on 18-1-17.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class KMeansPlusPlusClusterStrategy implements ICategoryStrategy {

    private static final String TAG = KMeansPlusPlusClusterer.class.getSimpleName();
    private int k = 1;
    private List<Photo> classifiedPhotoList = new ArrayList<>();

    public KMeansPlusPlusClusterStrategy(int k) {
        this.k = k;
    }

    @Override
    public List<Photo> getNearbyPhotos(List<Photo> photoList) {
        if (photoList != null && photoList.size() > 0) {
            classifiedPhotoList.clear();
            KMeansPlusPlusClusterer<Photo> kMeansPlusPlusClusterer = new KMeansPlusPlusClusterer<>(k);
            List<CentroidCluster<Photo>> clusterResults = kMeansPlusPlusClusterer.cluster(photoList);
            if (clusterResults != null && clusterResults.size() > 0) {
                for (int index = 0; index < clusterResults.size(); index++) {
                    List<Photo> photoResult = clusterResults.get(index).getPoints();
                    double dist = findFarthestPairPoints(photoResult);
                    if (dist > 500) {
                        ++k;
                        getNearbyPhotos(photoList);
                        break;
                    } else {
                        for (Photo point : photoResult) {
                            point.setPhotoTag(index);
                            point.setTagCount(clusterResults.size());
                            classifiedPhotoList.add(point);
                        }
                    }
                }
                return classifiedPhotoList;
            }
        }
        return photoList;
    }

    public double findFarthestPairPoints(List<Photo> list) {
        double distance = 0;
        LatLng farthestPoint1 = null;
        LatLng farthestPoint2 = null;
        for (int i = 0; i < list.size(); i++) {
            LatLng latLng1 = new LatLng(list.get(i).getLatitude(), list.get(i).getLongitude());
            for (int j = i + 1; j < list.size(); j++) {
                LatLng latLng2 = new LatLng(list.get(j).getLatitude(), list.get(j).getLongitude());
                double tempDistance = AMapUtils.calculateLineDistance(latLng1, latLng2);
                if (distance < tempDistance) {
                    distance = tempDistance;
                    farthestPoint1 = latLng1;
                    farthestPoint2 = latLng2;
                }
            }
        }
        Log.e(TAG, " k value  ==> " + k);
        Log.e(TAG, " farthest distance ==> " + distance);
        Log.e(TAG, " farthest distance point1 ==> " + (farthestPoint1 != null ? farthestPoint1.toString() : null)
                + " , point2 ===> " + (farthestPoint2 != null ? farthestPoint2.toString() : null));
        return distance;
    }
}
