package org.d1scw0rld.bookbag.dto;

import java.util.ArrayList;

/**
 * Created by Iasen on 12.7.2016 г..
 */
public class Book
{
//   public int iAuthor1ID,
//              iAuthor2ID,
//              iAuthor3ID,
//              iSeriesID,
//              iVolume = 0,
//              iCategoryID,
//              iLanguageID,
//              iPublisherID,
//              iPublicationLocationID,
//              iPublicationDate = 0,
//              iPages = 0,
//              iStatusID,
//              iRatingID,
//              iFormatID,
//              iLocationID,
////              iPrice = 0,
////              iValue = 0,
//              iConditionID,
//              iDueDate,
//              iReadDate,
//              iEdition;

   public long iID = 0;
   
   public Changeable<String> csTitle,
                             csDescription,
                             csPrice,
                             csValue,
                             csISBN,
                             csWeb;
;
   
   public Changeable<Integer> ciVolume,
                              ciPages,
                              ciPublicationDate,
                              ciEdition,
                              ciReadDate,
                              ciDueDate,
                              ciRating;
   
//   public String sTitle,
//                 sDescription,
//                 sISBN,
//                 sWeb,
//                 sPrice,
//                 sValue;
   
   public ArrayList<Field> alFields = new ArrayList<>();

   public Book()
   {
      csTitle = new Changeable<String>("");
      csDescription  = new Changeable<String>("");
      csPrice = new Changeable<String>("");
      csValue = new Changeable<String>("");
      csISBN = new Changeable<String>("");
      csWeb = new Changeable<String>("");

      ciVolume = new Changeable<Integer>(0);
      ciPages = new Changeable<Integer>(0);
      ciPublicationDate = new Changeable<Integer>(0);
      ciEdition = new Changeable<Integer>(0);
      ciReadDate = new Changeable<Integer>(0);
      ciDueDate = new Changeable<Integer>(0);
      ciRating = new Changeable<Integer>(0);
   }

   public Book(int iID,
               String sTitle,
               String sDescription,
               int iVolume,
               int iPublicationDate,
               int iPages,
//               int iPrice,
//               int iValue,
               String sPrice,
               String sValue,
               int iDueDate,
               int iReadDate,
               int iEdition,
               String sISBN,
               String sWeb)
   {
      this.iID = iID;
      this.csTitle = new Changeable<String>(sTitle);
//      this.csTitle.value = sTitle;
//      this.sTitle = sTitle;
//      this.sDescription = sDescription;
      csDescription = new Changeable<String>(sDescription);
//      this.iVolume = iVolume;
      ciVolume = new Changeable<Integer>(iVolume);
//    this.iPublicationDate = iPublicationDate;
      ciPublicationDate = new Changeable<Integer>(iPublicationDate);
//    this.iPages = iPages;
      ciPages = new Changeable<Integer>(iPages);
//      this.iPrice = iPrice;
//    this.sPrice = sPrice;
      csPrice = new Changeable<String>(sPrice);
//      this.iValue = iValue;
//      this.sValue = sValue;
      csValue = new Changeable<String>(sValue);
//      this.iReadDate = iReadDate;
      ciReadDate = new Changeable<Integer>(iReadDate);
//    this.iDueDate = iDueDate;
      ciDueDate = new Changeable<Integer>(iDueDate);
//      this.iEdition = iEdition;
      ciEdition = new Changeable<Integer>(iEdition);
//      this.sISBN = sISBN;
      csISBN = new Changeable<String>(sISBN);
//      this.sWeb = sWeb;
      csWeb = new Changeable<String>(sWeb);
   }
}
