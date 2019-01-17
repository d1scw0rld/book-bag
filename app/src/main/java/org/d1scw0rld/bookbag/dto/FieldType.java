package org.d1scw0rld.bookbag.dto;

/**
 * Created by Iasen on 12.7.2016 г..
 */

public class FieldType
{
   public int iID = 0;
   public String sName;
   public boolean isVisible = false;
   public byte iType = 0;
   public int iInputType = 0;
   public String sDigit = "";
//   public boolean isMultiline = false;

   public final static byte TYPE_TEXT = 1,
                            TYPE_TEXT_AUTOCOMPLETE = 2,
                            TYPE_MONEY = 3,
                            TYPE_MULTIFIELD = 4,
                            TYPE_SPINNER = 5,
                            TYPE_MULTI_SPINNER = 6,
                            TYPE_DATE = 7,
                            TYPE_RATING = 8,
                            TYPE_CHECK_BOX = 9;
   
   
   public FieldType(int iID, String sName, boolean isVisible, byte iType)
   {
      this.iID = iID;
      this.sName = sName;
//      this(iID, sName);
      this.isVisible = isVisible;
      this.iType = iType;
   }

   public FieldType(int iID, String sName, byte iType)
   {
      this.iID = iID;
      this.sName = sName;      
//      this(iID, sName);
      this.iType = iType;
   }

   public FieldType setVisibility(boolean isVisible)
   {
      this.isVisible = isVisible;
      return this;
   }
   
   public void setDigit(String sDigit)
   {
      this.sDigit = sDigit;
   }
   
   public FieldType setInputType(int iInputType)
   {
      this.iInputType = iInputType;
      return this;
   }

   
}
