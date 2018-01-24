package welove520.com.photocategory

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_item_duplicated.view.*

/**
 * Created by Raomengyang on 18-1-24.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

class SQLRVAdapter : RecyclerView.Adapter<SQLRVAdapter.ViewHolder>() {

    private var sqlList: List<Photo>? = null

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bind(sqlList!![position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = View.inflate(parent!!.context, R.layout.layout_item_duplicated, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sqlList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(photo: Photo) {
            Glide.with(itemView.context)
                    .load(photo.photoPath)
                    .centerCrop()
                    .into(itemView.iv_photo)
            itemView.tv_duplicated_name.text = photo!!.photoName
        }
    }

    fun setPhotoList(photoList: List<Photo>) {
        this.sqlList = photoList
    }
}