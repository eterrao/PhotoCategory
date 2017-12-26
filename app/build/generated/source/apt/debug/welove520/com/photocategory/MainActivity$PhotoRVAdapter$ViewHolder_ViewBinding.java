// Generated code from Butter Knife. Do not modify!
package welove520.com.photocategory;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity$PhotoRVAdapter$ViewHolder_ViewBinding implements Unbinder {
  private MainActivity.PhotoRVAdapter.ViewHolder target;

  @UiThread
  public MainActivity$PhotoRVAdapter$ViewHolder_ViewBinding(MainActivity.PhotoRVAdapter.ViewHolder target,
      View source) {
    this.target = target;

    target.setIvPhoto(Utils.findRequiredViewAsType(source, R.id.iv_photo, "field 'ivPhoto'", ImageView.class));
    target.setTvPhotoName(Utils.findRequiredViewAsType(source, R.id.tv_photo_name, "field 'tvPhotoName'", TextView.class));
    target.setTvPhotoDescription(Utils.findRequiredViewAsType(source, R.id.tv_photo_description, "field 'tvPhotoDescription'", TextView.class));
    target.setTvPhotoDate(Utils.findRequiredViewAsType(source, R.id.tv_photo_date, "field 'tvPhotoDate'", TextView.class));
    target.setTvPhotoGeoInfo(Utils.findRequiredViewAsType(source, R.id.tv_photo_geo_info, "field 'tvPhotoGeoInfo'", TextView.class));
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity.PhotoRVAdapter.ViewHolder target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.setIvPhoto(null);
    target.setTvPhotoName(null);
    target.setTvPhotoDescription(null);
    target.setTvPhotoDate(null);
    target.setTvPhotoGeoInfo(null);
  }
}
