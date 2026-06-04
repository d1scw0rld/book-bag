package org.d1scw0rld.bookbag.fileselector;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.d1scw0rld.bookbag.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter used to display a files list
 */
public class FileListAdapter extends BaseAdapter
{

   /** Array of FileData objects that will be used to display a list */
   private final ArrayList<FileData> fileDataList;

   private final Context context;
   
   FileListAdapter(Context context, List<FileData> fileDataList)
   {
      this.fileDataList = (ArrayList<FileData>) fileDataList;
      this.context = context;
   }

   @Override
   public int getCount()
   {
      return fileDataList.size();
   }

   @Override
   public Object getItem(int position)
   {
      return fileDataList.get(position);
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      FileData tempFileData = fileDataList.get(position);

      final ViewHolder holder;
      int imgRes = -1;
      if (convertView == null) 
      {
         convertView = View.inflate(context, R.layout.row_file, null);
         holder = new ViewHolder();
         holder.fileNameTextView = convertView.findViewById(R.id.tv_file_name);
         holder.fileTypeImageView = convertView.findViewById(R.id.iv_file_type);
         convertView.setTag(holder);
      } 
      else 
      {
         holder = (ViewHolder) convertView.getTag();
      }
      
      holder.fileNameTextView.setText(tempFileData.getFileName());
      switch (tempFileData.getFileType())
      {
         case FileData.UP_FOLDER:
         {
            imgRes = R.drawable.ic_folder_open;
            break;
         }
         case FileData.FOLDER:
         {
            imgRes = R.drawable.ic_folder;
            break;
         }
         case FileData.FILE:
         {
            imgRes = R.drawable.ic_file;
            break;
         }
      }
      holder.fileTypeImageView.setImageResource(imgRes);
      
      return convertView;      
   }
   
   static class ViewHolder 
   {
      TextView fileNameTextView;
      ImageView fileTypeImageView;
   }
}
