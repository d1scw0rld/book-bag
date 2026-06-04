package org.d1scw0rld.bookbag.fileselector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import org.d1scw0rld.bookbag.R

/**
 * Adapter used to display a files list
 */
class FileListAdapter(
    private val context: Context,
    private var fileDataList: List<FileData>,
) : BaseAdapter() {

    override fun getCount(): Int = fileDataList.size

    override fun getItem(position: Int): FileData = fileDataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context ?: context)
            .inflate(R.layout.row_file, parent, false).apply {
                tag = ViewHolder(
                    findViewById(R.id.tv_file_name),
                    findViewById(R.id.iv_file_type),
                )
            }

        val holder = view.tag as ViewHolder
        val tempFileData = getItem(position)

        holder.fileNameTextView.text = tempFileData.fileName

        val imgRes = when (tempFileData.fileType) {
            FileType.UP_FOLDER -> R.drawable.ic_folder_open
            FileType.FOLDER -> R.drawable.ic_folder
            FileType.FILE -> R.drawable.ic_file
        }
        holder.fileTypeImageView.setImageResource(imgRes)

        return view
    }

    /**
     * Updates the data set and refreshes the list without reconstructing the adapter.
     */
    fun updateData(newFileDataList: List<FileData>) {
        fileDataList = newFileDataList
        notifyDataSetChanged()
    }

    private class ViewHolder(
        val fileNameTextView: TextView,
        val fileTypeImageView: ImageView,
    )
}
