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
class DuplicatedRVAdapter(val onItemClickListener: (Photo) -> Unit) : RecyclerView.Adapter<DuplicatedRVAdapter.ViewHolder>() {

    private var photoList: List<Photo>? = null

    fun setPhotoList(list: List<Photo>) {
        this.photoList = list
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bind(photoList!![position])
    }

    override fun getItemCount(): Int {
        return photoList!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var view = View.inflate(parent!!.context, R.layout.layout_item_duplicated, null)
        return ViewHolder(view, onItemClickListener)
    }

    class ViewHolder(val view: View, val itemClickListener: (Photo) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(photo: Photo) {
            Glide.with(view.context)
                    .load(photo.photoPath)
                    .centerCrop()
                    .into(view.iv_photo)
            view.tv_duplicated_name.text = photo.photoName
            view.setOnClickListener {
                itemClickListener(photo)
            }
        }
    }
}