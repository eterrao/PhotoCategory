package welove520.com.photocategory;

import com.amap.api.services.geocoder.GeocodeSearch;

/**
 * Created by Raomengyang on 18-1-18.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public abstract class OnCustomGeocodeSearchListener<T> implements GeocodeSearch.OnGeocodeSearchListener {

    public T callbackObj;

    public OnCustomGeocodeSearchListener(T callbackObj) {
        this.callbackObj = callbackObj;
    }

    public T getCallbackObj() {
        return callbackObj;
    }

    public void setCallbackObj(T callbackObj) {
        this.callbackObj = callbackObj;
    }
}
