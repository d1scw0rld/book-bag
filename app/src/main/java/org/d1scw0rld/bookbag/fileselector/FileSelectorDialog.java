package org.d1scw0rld.bookbag.fileselector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.d1scw0rld.bookbag.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

/**
 * Create the file selection dialog. This class will create a custom dialog for
 * file selection which can be used to save files.
 */
public class FileSelectorDialog extends DialogFragment
{
   private String currentFileName;

   /** The list of files and folders which you can choose from */
   private ListView fileListView;

   /** Spinner by which to select the file type filtering */
   private Spinner filterSpinner;

   /**
    * Indicates current location in the directory structure displayed in the
    * dialog.
    */
   private File currentLocation;

   /**
    * The file selector dialog.
    */
   private Context context;

   private String[] fileFilters;

   private FileOperation operation;

   private View view;

   private Toolbar toolbar;

   private EditText fileNameEditText;

   /** Save or Load file listener. */
   OnHandleFileListener onHandleFileListener = null;

   public FileSelectorDialog()
   {}

   public static FileSelectorDialog newInstance(final File currentFile,
                                                final FileOperation operation,
                                                final OnHandleFileListener onHandleFileListener,
                                                final String[] fileFilters)
   {
      FileSelectorDialog fileSelectorDialig = new FileSelectorDialog();

      fileSelectorDialig.onHandleFileListener = onHandleFileListener;
      fileSelectorDialig.fileFilters = fileFilters;
      fileSelectorDialig.operation = operation;

      if(currentFile == null)
      {
         final File sdCard = Environment.getExternalStorageDirectory();
         fileSelectorDialig.currentLocation = sdCard.canRead() ? sdCard : Environment.getRootDirectory();
      }
      else
      {
         if(currentFile.isDirectory())
            fileSelectorDialig.currentLocation = currentFile;
         else
         {
            fileSelectorDialig.currentLocation = currentFile.getParentFile();
            if(operation == FileOperation.SAVE)
               fileSelectorDialig.currentFileName = currentFile.getName();
         }
      }

      return fileSelectorDialig;
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState)
   {
      // Use the Builder class for convenient dialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
      context = getContext();

      view = View.inflate(context, R.layout.dialog_file, null);
      builder.setView(view);

      fileNameEditText = view.findViewById(R.id.fileName);
      if(currentFileName != null)
         fileNameEditText.setText(currentFileName);

      toolbar = view.findViewById(R.id.dlg_toolbar);
      toolbar.setTitle(currentLocation.getName());
      toolbar.setOnMenuItemClickListener(item -> {
         if(item.getItemId() == R.id.action_new_folder)
         {
            openNewFolderDialog();
            return true;
         }
         else if(item.getItemId() == R.id.action_save || item.getItemId() == R.id.action_load)
         {
            handleSaveOrLoad();
            return true;
         }

         return false;
      });

      switch (operation)
      {
         case SAVE:
            toolbar.inflateMenu(R.menu.menu_dialog_save);
            break;
         case LOAD:
            toolbar.inflateMenu(R.menu.menu_dialog_load);
            fileNameEditText.setEnabled(false);
            break;
      }

      prepareFilterSpinner(fileFilters);
      prepareFilesList();

      return builder.create();
   }

   @Override
   public void onStart()
   {
      super.onStart();
      Dialog dialog = getDialog();
      if (dialog != null && dialog.getWindow() != null)
      {
         dialog.getWindow().setLayout(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT
         );
         View rootView = dialog.findViewById(R.id.saveFileDialog);
         if (rootView != null)
         {
            rootView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            ViewParent parent = rootView.getParent();
            while (parent instanceof View parentView)
            {
               ViewGroup.LayoutParams layoutParams = parentView.getLayoutParams();
               if (layoutParams != null)
               {
                  layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                  parentView.requestLayout();
               }
               parent = parent.getParent();
            }
            rootView.requestLayout();
         }
      }
   }

   private void handleSaveOrLoad()
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
            final Toast toast = Toast.makeText(context,
                    messageText,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
         }
         else
         {
            // Access granted.
            onHandleFileListener.handleFile(filePath);
            dismiss();
         }
      }
   }

   /**
    * This method prepares a filter's list with the String's array
    *
    * @param filesFilter
    *           - array of filters, the elements of the array will be used as
    *           elements of the spinner
    */
   private void prepareFilterSpinner(String[] filesFilter)
   {
      filterSpinner = toolbar.findViewById(R.id.action_select_type);
      if(filesFilter == null || filesFilter.length == 0)
      {
         filesFilter = new String[] { FileUtils.FILTER_ALLOW_ALL };
         filterSpinner.setEnabled(false);
      }
      ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
              R.layout.layout_drop_title,
              filesFilter);
      adapter.setDropDownViewResource(R.layout.layout_drop_list);


      filterSpinner.setAdapter(adapter);
      OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener()
      {

         @Override
         public void onItemSelected(AdapterView<?> aAdapter,
                                    View aView,
                                    int position,
                                    long row)
         {
            TextView textViewItem = (TextView) aView;
            String filter = textViewItem.getText().toString();
            makeList(currentLocation, filter);
            toolbar.setTitle(currentLocation.getName());

         }

         @Override
         public void onNothingSelected(AdapterView<?> arg0)
         {}
      };
      filterSpinner.setOnItemSelectedListener(onItemSelectedListener);
   }

   /**
    * This method prepares the mFileListView
    *
    */
   private void prepareFilesList()
   {
      fileListView = view.findViewById(R.id.fileList);

      fileListView.setOnItemClickListener((parent, view, position, id) -> {
         // Check if "../" item should be added.
         if(id == 0)
         {
            final String parentLocation = currentLocation.getParent();
            if(parentLocation != null)
            { // text == "../"
               String fileFilter = ((TextView) filterSpinner.getSelectedView()).getText()
                       .toString();
               currentLocation = new File(parentLocation);
               makeList(currentLocation, fileFilter);
               toolbar.setTitle(currentLocation.getName());
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
      });
      String filter = filterSpinner.getSelectedItem().toString();
      makeList(currentLocation, filter);

   }

   /** Opens a dialog for creating a new folder. */
   private void openNewFolderDialog()
   {
      AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
      alert.setTitle(R.string.btn_new_fld);
      alert.setMessage(" ");
      final AppCompatEditText input = new AppCompatEditText(context);
      alert.setView(input);
      alert.setPositiveButton(R.string.create, (dialog, whichButton) -> {
         String folderName = input.getText().toString().trim();

         if (folderName.isEmpty()) {
            // Avoid creating an empty-named directory
            Toast.makeText(context, R.string.msg_fld_crt_err, Toast.LENGTH_SHORT).show();
            return;
         }

         File file = new File(currentLocation.getAbsolutePath() + File.separator + folderName);

         // Use mkdirs() for robust folder creation, and assign message dynamically
         int messageResId = file.mkdirs() ? R.string.msg_fld_crt_ok : R.string.msg_fld_crt_err;

         Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();

         String fileFilter = ((TextView) filterSpinner.getSelectedView()).getText().toString();
         makeList(currentLocation, fileFilter);
      });
      alert.show();
   }

   /** Set onClick() event handler for the cancel button. */
   public String getSelectedFileName()
   {
      return fileNameEditText.getText().toString();
   }

   /**
    * Handle the file list item selection.
    * <p>
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
      final String itemPath = currentLocation.getAbsolutePath()
              + File.separator
              + itemText;
      final File itemLocation = new File(itemPath);

      if(!itemLocation.canRead())
      {
         Toast.makeText(context, "Access denied!!!", Toast.LENGTH_SHORT)
                 .show();
      }
      else if(itemLocation.isDirectory())
      {
         currentLocation = itemLocation;
         String fileFilter = ((TextView) filterSpinner.getSelectedView()).getText()
                 .toString();
         makeList(currentLocation, fileFilter);
         toolbar.setTitle(currentLocation.getName());
      }
      else if(itemLocation.isFile())
         fileNameEditText.setText(itemText);
   }

   public File getCurrentLocation()
   {
      return currentLocation;
   }

   /**
    * The method that fills the list with a directories contents.
    *
    * @param location
    *           Indicates the directory whose contents should be displayed in
    *           the dialog.
    * @param filesFilter
    *           The filter specifies the type of file to be displayed
    */
   private void makeList(final File location, final String filesFilter)
   {
      final ArrayList<FileData> fileList = new ArrayList<>();
      final String parentLocation = location.getParent();
      if(parentLocation != null)
      {
         // First item on the list.
         fileList.add(new FileData("../", FileData.UP_FOLDER));
      }
      File[] listFiles = location.listFiles();
      if(listFiles != null)
      {
         ArrayList<FileData> fileDataList = new ArrayList<>();
         for(File tempFile : listFiles)
         {
            if(FileUtils.accept(tempFile, filesFilter))
            {
               int type = tempFile.isDirectory() ? FileData.FOLDER : FileData.FILE;
               fileDataList.add(new FileData(tempFile.getName(),
                       type));
            }
         }
         fileList.addAll(fileDataList);
         Collections.sort(fileList);
      }
      // Fill the list with the contents of fileList.
      if(fileListView != null)
      {
         FileListAdapter adapter = new FileListAdapter(context, fileList);
         fileListView.setAdapter(adapter);
      }
   }

   /**
    * Check if file name is correct, e.g. if it isn't empty.
    *
    * @return False, if file name is empty true otherwise.
    */
   boolean checkFileName(String text)
   {
      if(text.isEmpty())
      {
         final AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle(R.string.information);
         builder.setMessage(R.string.msg_fl_nm_frs);
         builder.setNeutralButton(android.R.string.ok, null);
         builder.show();
         return false;
      }
      return true;
   }
}
