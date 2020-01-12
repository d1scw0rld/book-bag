package org.d1scw0rld.bookbag.dto;

import android.support.annotation.NonNull;

public class Property
{
   public int    iFieldTypeID;
   public long   iID = 0;
   public String sValue = "";

   public Property(int iFieldTypeID)
   {
      this.iFieldTypeID = iFieldTypeID;
   }

   public Property(int iFieldTypeID, String sValue)
   {
      this.iFieldTypeID = iFieldTypeID;
      this.sValue = sValue;
   }

   public Property(long iID, int iFieldTypeID, String sValue)
   {
      this.iID = iID;
      this.iFieldTypeID = iFieldTypeID;
      this.sValue = sValue;
   }

   public void copy(Property f)
   {
      iID = f.iID;
      iFieldTypeID = f.iFieldTypeID;
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
      Property f = (Property) o;
      return o != null && iID == f.iID && iFieldTypeID == f.iFieldTypeID && sValue.equalsIgnoreCase(f.sValue);
   }
}
