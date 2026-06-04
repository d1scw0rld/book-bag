package org.d1scw0rld.bookbag.fileselector;

/**
 * This class contains information about the file name and type.
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
   private final String fileName;

   /** Defines the type of file. Can be one of UP_FOLDER, DIRECTORY or FILE */
   private final int fileType;

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
         throw new IllegalArgumentException("Illegal type of file");
      }
      this.fileName = fileName;
      this.fileType = fileType;
   }

   @Override
   public int compareTo(final FileData another)
   {
      if(fileType != another.fileType)
      {
         return fileType - another.fileType;
      }
      return fileName.compareTo(another.fileName);
   }

   String getFileName()
   {
      return fileName;
   }

   int getFileType()
   {
      return fileType;
   }
}
