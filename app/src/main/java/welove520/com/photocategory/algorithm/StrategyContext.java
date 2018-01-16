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

public class StrategyContext {

    ICategoryStrategy strategy;

    public StrategyContext(ICategoryStrategy strategy) {
        this.strategy = strategy;
    }

    public Set<Integer> getNearbyPhotosCategory(List<Photo> photosList) {
        return strategy != null ? strategy.getNearbyPhotos(photosList) : null;
    }
}
