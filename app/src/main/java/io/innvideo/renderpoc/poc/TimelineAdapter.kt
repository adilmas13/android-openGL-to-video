package io.innvideo.renderpoc.poc

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.innvideo.renderpoc.R
import kotlinx.android.synthetic.main.adapter_timeline.view.*

class TimelineAdapter(val list: List<Bitmap>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.itemView.ivTimeline.setImageBitmap(list[position])
    }

    inner class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}