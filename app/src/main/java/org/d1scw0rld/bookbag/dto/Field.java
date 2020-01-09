package org.d1scw0rld.bookbag.dto;

import android.support.annotation.NonNull;

public class Field
{
   public int iTypeID;
   public long iID = 0;
   public String sValue = "";

   public Field(int iTypeID)
   {
      this.iTypeID = iTypeID;
   }

   public Field(int iTypeID, String sValue)
   {
      this.iTypeID = iTypeID;
      this.sValue = sValue;
   }

   public Field(long iID, int iTypeID, String sValue)
   {
      this.iID = iID;
      this.iTypeID = iTypeID;
      this.sValue = sValue;
   }

   public void copy(Field f)
   {
      iID = f.iID;
      iTypeID = f.iTypeID;
      sValue = f.sValue;
   }

   @NonNull
   @Override
   public String toString()
   {
      return sValue;
   }


//   @Override
//   public String getValue()
//   {
//      return sValue;
//   }

   @Override
   public boolean equals(Object o)
   {
      Field f = (Field) o;
      return o != null && iID == f.iID && iTypeID == f.iTypeID && sValue.equalsIgnoreCase(f.sValue);
   }
}
