package welove520.com.photocategory.algorithm;

import java.util.List;
import java.util.Set;

import welove520.com.photocategory.Photo;

/**
 * Created by Raomengyang on 18-1-16.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public interface ICategoryStrategy {

    Set<Integer> getNearbyPhotos(List<Photo> photosList);
}
