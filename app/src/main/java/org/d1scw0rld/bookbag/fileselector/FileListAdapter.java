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
   private final ArrayList<FileData> mFileDataArray;

   private final Context mContext;
   
   FileListAdapter(Context context, List<FileData> aFileDataArray)
   {
      mFileDataArray = (ArrayList<FileData>) aFileDataArray;
      mContext = context;
   }

   @Override
   public int getCount()
   {
      return mFileDataArray.size();
   }

   @Override
   public Object getItem(int position)
   {
      return mFileDataArray.get(position);
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      FileData tempFileData = mFileDataArray.get(position);

      final ViewHolder holder;
      int imgRes = -1;
      if (convertView == null) 
      {
         convertView = View.inflate(mContext, R.layout.row_file, null);
         holder = new ViewHolder();
         holder.tvFileName = convertView.findViewById(R.id.tv_file_name);
         holder.ivFileType = convertView.findViewById(R.id.iv_file_type);
         convertView.setTag(holder);
      } 
      else 
      {
         holder = (ViewHolder) convertView.getTag();
      }
      
      holder.tvFileName.setText(tempFileData.getFileName());
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
      holder.ivFileType.setImageResource(imgRes);
      
      return convertView;      
   }
   
   static class ViewHolder 
   {
      TextView tvFileName;
      ImageView ivFileType;
   }
}
