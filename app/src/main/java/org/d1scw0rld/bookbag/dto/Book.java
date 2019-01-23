package org.d1scw0rld.bookbag.dto;

import java.util.ArrayList;

/**
 * Created by Iasen on 12.7.2016 г..
 */
public class Book
{
   public long iID = 0;
   
   public Changeable<String> csTitle,
                             csDescription,
                             csPrice,
                             csValue,
                             csISBN,
                             csWeb;

   public Changeable<Integer> ciVolume,
                              ciPages,
                              ciPublicationDate,
                              ciEdition,
                              ciReadDate,
                              ciDueDate,
                              ciRating;

   public ArrayList<Field> alFields = new ArrayList<>();

   public Book()
   {
      csTitle = new Changeable<>("");
      csDescription  = new Changeable<>("");
      csPrice = new Changeable<>("");
      csValue = new Changeable<>("");
      csISBN = new Changeable<>("");
      csWeb = new Changeable<>("");

      ciVolume = new Changeable<>(0);
      ciPages = new Changeable<>(0);
      ciPublicationDate = new Changeable<>(0);
      ciEdition = new Changeable<>(0);
      ciReadDate = new Changeable<>(0);
      ciDueDate = new Changeable<>(0);
      ciRating = new Changeable<>(0);
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
      this.csTitle = new Changeable<>(sTitle);
      csDescription = new Changeable<>(sDescription);
      ciVolume = new Changeable<>(iVolume);
      ciPublicationDate = new Changeable<>(iPublicationDate);
      ciPages = new Changeable<>(iPages);
      csPrice = new Changeable<>(sPrice);
      csValue = new Changeable<>(sValue);
      ciReadDate = new Changeable<>(iReadDate);
      ciDueDate = new Changeable<>(iDueDate);
      ciEdition = new Changeable<>(iEdition);
      csISBN = new Changeable<>(sISBN);
      csWeb = new Changeable<>(sWeb);
   }
}
