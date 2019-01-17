package org.d1scw0rld.bookbag.fileselector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.d1scw0rld.bookbag.R;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.support.v7.widget.AppCompatEditText;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Create the file selection dialog. This class will create a custom dialog for
 * file selection which can be used to save files.
 */
public class FileSelectorDialog extends DialogFragment
{
   private String sCurrentFile;
   
   /** The list of files and folders which you can choose from */
   private ListView mFileListView;

   /** Spinner by which to select the file type filtering */
   private Spinner mFilterSpinner;

   /**
    * Indicates current location in the directory structure displayed in the
    * dialog.
    */
   private File mCurrentLocation;

   /**
    * The file selector dialog.
    */
   // private final Dialog mDialog;
   private Context mContext;

   private String[] fileFilters;

   private FileOperation operation;

   private View v;
   
   private Toolbar toolbar;
   
   private EditText etFileName;

   /** Save or Load file listener. */
   OnHandleFileListener mOnHandleFileListener = null;

   public FileSelectorDialog()
   {}

   public static FileSelectorDialog newInstance(final File flCurrentFile,
                                                final FileOperation operation,
                                                final OnHandleFileListener onHandleFileListener,
                                                final String[] fileFilters)
   {
      FileSelectorDialog frag = new FileSelectorDialog();

      frag.mOnHandleFileListener = onHandleFileListener;
      frag.fileFilters = fileFilters;
      frag.operation = operation;

      if(flCurrentFile == null)
      {
         final File sdCard = Environment.getExternalStorageDirectory();
         frag.mCurrentLocation = sdCard.canRead() ? sdCard : Environment.getRootDirectory(); 
      }
      else
      {
         if(flCurrentFile.isDirectory())
            frag.mCurrentLocation = flCurrentFile;   
         else
         {
            frag.mCurrentLocation = flCurrentFile.getParentFile();
            if(operation == FileOperation.SAVE)
               frag.sCurrentFile = flCurrentFile.getName();
         }
      }
      
      return frag;
   }

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState)
   {
      // Use the Builder class for convenient dialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
      mContext = getContext();

//      LayoutInflater inflater = getActivity().getLayoutInflater();
      v = View.inflate(mContext, R.layout.dialog_file, null);
      builder.setView(v);

      etFileName = (EditText) v.findViewById(R.id.fileName);
      if(sCurrentFile != null)
         etFileName.setText(sCurrentFile);
      
      toolbar = (Toolbar) v.findViewById(R.id.dlg_toolbar);
      toolbar.setTitle(mCurrentLocation.getName());
      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
      {
         @Override
         public boolean onMenuItemClick(MenuItem item)
         {
            if(item.getItemId() == R.id.action_new_folder)
            {
               openNewFolderDialog();
               return true;
            }
            
            return false;
         }
      });

      int str_id = 0;
      switch (operation)
      {
         case SAVE:
            str_id = R.string.save;
            toolbar.inflateMenu(R.menu.menu_dialog_save);
         break;
         case LOAD:
            str_id = R.string.load;
            toolbar.inflateMenu(R.menu.menu_dialog_load);
            etFileName.setEnabled(false);
         break;
      }
      builder.setPositiveButton(str_id, new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int id)
         {
            final String text = getSelectedFileName();
            if(checkFileName(text))
            {
               final String filePath = getCurrentLocation().getAbsolutePath()
                                       + File.separator
                                       + text;
               final File file = new File(filePath);
               int messageText = 0;
               // Check file access rights.
               switch (operation)
               {
                  case SAVE:
                     if((file.exists()) && (!file.canWrite()))
                     {
                        messageText = R.string.msg_cnt_sv_fl;
                     }
                  break;
                  case LOAD:
                     if(!file.exists())
                     {
                        messageText = R.string.msg_msn_fl;
                     }
                     else if(!file.canRead())
                     {
                        messageText = R.string.msg_acc_dnd;
                     }
                  break;
               }
               if(messageText != 0)
               {
                  // Access denied.
                  final Toast t = Toast.makeText(mContext,
                                                 messageText,
                                                 Toast.LENGTH_SHORT);
                  t.setGravity(Gravity.CENTER, 0, 0);
                  t.show();
               }
               else
               {
                  // Access granted.
                  mOnHandleFileListener.handleFile(filePath);
                  dismiss();
               }
            }     
         }
      });
      builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int id)
         {
            dismiss();
         }
      });
      
      prepareFilterSpinner(fileFilters);
      prepareFilesList();
      
      // Create the AlertDialog object and return it
      return builder.create();
   }
   
   /**
    * This method prepares a filter's list with the String's array
    * 
    * @param aFilesFilter
    *           - array of filters, the elements of the array will be used as
    *           elements of the spinner
    */
   private void prepareFilterSpinner(String[] fitlesFilter)
   {
//       mFilterSpinner = (Spinner) v.findViewById(R.id.fileFilter);
      mFilterSpinner = (Spinner) toolbar.findViewById(R.id.action_select_type);
      if(fitlesFilter == null || fitlesFilter.length == 0)
      {
         fitlesFilter = new String[] { FileUtils.FILTER_ALLOW_ALL };
         mFilterSpinner.setEnabled(false);
      }
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                                                              R.layout.layout_drop_title,
                                                              fitlesFilter);
      adapter.setDropDownViewResource(R.layout.layout_drop_list);


      mFilterSpinner.setAdapter(adapter);
      OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener()
      {

         @Override
         public void onItemSelected(AdapterView<?> aAdapter,
                                    View aView,
                                    int arg2,
                                    long arg3)
         {
            TextView textViewItem = (TextView) aView;
            String filtr = textViewItem.getText().toString();
            makeList(mCurrentLocation, filtr);
            toolbar.setTitle(mCurrentLocation.getName());

         }

         @Override
         public void onNothingSelected(AdapterView<?> arg0)
         {}
      };
      mFilterSpinner.setOnItemSelectedListener(onItemSelectedListener);
   }   

   /**
    * This method prepares the mFileListView
    * 
    */
   private void prepareFilesList()
   {
      mFileListView = (ListView) v.findViewById(R.id.fileList);
   
      mFileListView.setOnItemClickListener(new OnItemClickListener()
      {
   
         @Override
         public void onItemClick(final AdapterView<?> parent,
                                 final View view,
                                 final int position,
                                 final long id)
         {
            // Check if "../" item should be added.
//            etFileName.setText("");
            if(id == 0)
            {
               final String parentLocation = mCurrentLocation.getParent();
               if(parentLocation != null)
               { // text == "../"
                  String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText()
                                                                                   .toString();
                  mCurrentLocation = new File(parentLocation);
                  makeList(mCurrentLocation, fileFilter);
                  toolbar.setTitle(mCurrentLocation.getName());
               }
               else
               {
                  onItemSelect(parent, position);
               }
            }
            else
            {
               onItemSelect(parent, position);
            }
         }
      });
      String filtr = mFilterSpinner.getSelectedItem().toString();
      makeList(mCurrentLocation, filtr);
   
   }

   /** Opens a dialog for creating a new folder. */
   private void openNewFolderDialog()
   {
      AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
      alert.setTitle(R.string.btn_new_fld);
      alert.setMessage(" ");
      final AppCompatEditText input = new AppCompatEditText(mContext);
      alert.setView(input);
      alert.setPositiveButton(R.string.create, new DialogInterface.OnClickListener()
      {
         @Override
         public void onClick(final DialogInterface dialog, final int whichButton)
         {
            File file = new File(mCurrentLocation.getAbsolutePath()
                                 + File.separator
                                 + input.getText().toString());
            if(file.mkdir())
            {
               Toast t = Toast.makeText(mContext,
                                        R.string.msg_fld_crt_ok,
                                        Toast.LENGTH_SHORT);
               t.setGravity(Gravity.CENTER, 0, 0);
               t.show();
            }
            else
            {
               Toast t = Toast.makeText(mContext,
                                        R.string.msg_fld_crt_err,
                                        Toast.LENGTH_SHORT);
               t.setGravity(Gravity.CENTER, 0, 0);
               t.show();
            }
            String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
            makeList(mCurrentLocation, fileFilter);
         }
      });
      alert.show();
   }

   /** Set onClick() event handler for the cancel button. */
   public String getSelectedFileName()
   {
      return etFileName.getText().toString();
   }

   /**
    * Handle the file list item selection.
    * 
    * Change the directory on the list or change the name of the saved file
    * if the user selected a file.
    * 
    * @param parent
    *           First parameter of the onItemClick() method of
    *           OnItemClickListener. It's a value of text property of the
    *           item.
    * @param position
    *           Third parameter of the onItemClick() method of
    *           OnItemClickListener. It's the index on the list of the
    *           selected item.
    */
   private void onItemSelect(final AdapterView<?> parent, final int position)
   {
      final String itemText = ((FileData) parent.getItemAtPosition(position)).getFileName();
      final String itemPath = mCurrentLocation.getAbsolutePath()
                              + File.separator
                              + itemText;
      final File itemLocation = new File(itemPath);
   
      if(!itemLocation.canRead())
      {
         Toast.makeText(mContext, "Access denied!!!", Toast.LENGTH_SHORT)
              .show();
      }
      else if(itemLocation.isDirectory())
      {
         mCurrentLocation = itemLocation;
         String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText()
                                                                          .toString();
         makeList(mCurrentLocation, fileFilter);
         toolbar.setTitle(mCurrentLocation.getName());
      }
      else if(itemLocation.isFile())
         etFileName.setText(itemText);
   }

   /**
    * Set button name and click handler for Save or Load button.
    * 
    * @param operation
    *           Performed file operation.
    */
   public File getCurrentLocation()
   {
      return mCurrentLocation;
   }

   /**
    * The method that fills the list with a directories contents.
    * 
    * @param location
    *           Indicates the directory whose contents should be displayed in
    *           the dialog.
    * @param fitlesFilter
    *           The filter specifies the type of file to be displayed
    */
   private void makeList(final File location, final String fitlesFilter)
   {
      final ArrayList<FileData> fileList = new ArrayList<FileData>();
      final String parentLocation = location.getParent();
      if(parentLocation != null)
      {
         // First item on the list.
         fileList.add(new FileData("../", FileData.UP_FOLDER));
      }
      File listFiles[] = location.listFiles();
      if(listFiles != null)
      {
         ArrayList<FileData> fileDataList = new ArrayList<FileData>();
         for(int index = 0; index < listFiles.length; index++)
         {
            File tempFile = listFiles[index];
            if(FileUtils.accept(tempFile, fitlesFilter))
            {
               int type = tempFile.isDirectory() ? FileData.FOLDER : FileData.FILE;
               fileDataList.add(new FileData(listFiles[index].getName(),
                                             type));
            }
         }
         fileList.addAll(fileDataList);
         Collections.sort(fileList);
      }
      // Fill the list with the contents of fileList.
      if(mFileListView != null)
      {
         FileListAdapter adapter = new FileListAdapter(mContext, fileList);
         mFileListView.setAdapter(adapter);
      }
   }

   /**
    * Check if file name is correct, e.g. if it isn't empty.
    * 
    * @return False, if file name is empty true otherwise.
    */
   boolean checkFileName(String text)
   {
      if(text.length() == 0)
      {
         final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
         builder.setTitle(R.string.information);
         builder.setMessage(R.string.msg_fl_nm_frs);
         builder.setNeutralButton(android.R.string.ok, null);
         builder.show();
         return false;
      }
      return true;
   }
}
