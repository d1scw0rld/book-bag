package org.d1scw0rld.bookbag.fileselector;

/**
 * This class contais information about the file name and type
 */
public class FileData implements Comparable<FileData>
{

   /** Constant that specifies the object is a reference to the parent */
   static final int UP_FOLDER = 0;
   /** Constant that specifies the object is a folder */
   static final int FOLDER = 1;
   /** Constant that specifies the object is a file */
   static final int FILE = 2;

   /** The file's name */
   final private String mFileName;

   /** Defines the type of file. Can be one of UP_FOLDER, DIRECTORY or FILE */
   final private int mFileType;

   /**
    * This class holds information about the file.
    * 
    * @param fileName
    *           - file name
    * @param fileType
    *           - file type - can be UP_FOLDER, DIRECTORY or FILE
    * @throws IllegalArgumentException
    *            - when illegal type (different than UP_FOLDER, DIRECTORY or
    *            FILE)
    */
   FileData(final String fileName, final int fileType)
   {

      if(fileType != UP_FOLDER && fileType != FOLDER && fileType != FILE)
      {
         throw new IllegalArgumentException("Illegel type of file");
      }
      this.mFileName = fileName;
      this.mFileType = fileType;
   }

   @Override
   public int compareTo(final FileData another)
   {
      if(mFileType != another.mFileType)
      {
         return mFileType - another.mFileType;
      }
      return mFileName.compareTo(another.mFileName);
   }

   String getFileName()
   {
      return mFileName;
   }

   int getFileType()
   {
      return mFileType;
   }
}
