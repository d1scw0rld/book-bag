package org.d1scw0rld.bookbag.dto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class FileUtils
{
   /**
    * Creates the specified <code>toFile</code> as a byte for byte copy of the
    * <code>fromFile</code>. If <code>toFile</code> already exists, then it will
    * be replaced with a copy of <code>fromFile</code>. The name and path of
    * <code>toFile</code> will be that of <code>toFile</code>.<br/>
    * <br/>
    * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
    * this function.</i>
    * 
    * @param fromFile
    *           - FileInputStream for the file to copy from.
    * @param toFile
    *           - FileInputStream for the file to copy to.
    */
   public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException
   {
      FileChannel fromChannel = null;
      FileChannel toChannel = null;
      try
      {
         fromChannel = fromFile.getChannel();
         toChannel = toFile.getChannel();
         fromChannel.transferTo(0, fromChannel.size(), toChannel);
      } 
      finally
      {
         try
         {
            if(fromChannel != null)
            {
               fromChannel.close();
            }
         } 
         finally
         {
            if(toChannel != null)
            {
               toChannel.close();
            }
         }
      }
   }
   
   public static void copyFile(File fromFile, File toFile) throws IOException
   {
      FileUtils.copyFile(new FileInputStream(fromFile), new FileOutputStream(toFile));
   }
   
   public static void copyFile(String fromFile, String toFile) throws IOException
   {
      FileUtils.copyFile(new FileInputStream(fromFile), new FileOutputStream(toFile));
   }
   
   public static void verifyStoragePermissions(Activity activity)
   {
      // Check if we have write permission
      int permission = ActivityCompat.checkSelfPermission(activity,
                                                          Manifest.permission.WRITE_EXTERNAL_STORAGE);

      if(permission != PackageManager.PERMISSION_GRANTED)
      {
         // We don't have permission so prompt the user
         ActivityCompat.requestPermissions(activity,
                                           new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                           1);
      }
   }
}
