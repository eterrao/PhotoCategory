package welove520.com.photocategory.algorithm.strategy;

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
    @Override
    public List<Photo> getNearbyPhotos(List<Photo> photoList) {
        if (photoList != null && photoList.size() > 0) {
            KMeansPlusPlusClusterer<Photo> kMeansPlusPlusClusterer = new KMeansPlusPlusClusterer<>(8);
            List<CentroidCluster<Photo>> clusterResults = kMeansPlusPlusClusterer.cluster(photoList);
            List<Photo> classifiedPhotoList = new ArrayList<>();
            for (int index = 0; index < clusterResults.size(); index++) {
                for (Photo point : clusterResults.get(index).getPoints()) {
                    point.setPhotoTag(index);
                    classifiedPhotoList.add(point);
                }
            }
            return classifiedPhotoList;
        }
        return photoList;
    }
}
