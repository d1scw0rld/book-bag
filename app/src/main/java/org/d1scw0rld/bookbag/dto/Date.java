package org.d1scw0rld.bookbag.dto;

public class Date
{
   public int iDay, 
              iMonth, 
              iYear; 

   public Date()
   {
      iDay = 1;
      iMonth = 1;
      iYear = 1900;
   }
   
   public Date(int iDay,
                int iMonth,
                int iYear)
   {
      this.iDay = iDay;
      this.iMonth = iMonth;
      this.iYear = iYear;
   }
   
   
   public Date(int iDate)
   {
      fromInt(iDate);
   }
   
   public Date(Date oDate)
   {
      this(oDate.iDay, oDate.iMonth, oDate.iYear);
   }  
   
   public int toInt()
   {
      return iYear*10000 + iMonth*100 + iDay;
   }
   
   public void fromInt(int iDate)
   {
      iDay = iDate % 100;
      iMonth = (iDate / 100) % 100;
      iYear = iDate / 10000;
   }
   
   @Override
   public String toString()
   {
      return String.format("%02d", iDay) + "/" + String.format("%02d", iMonth) + "/" + iYear;
   }
   
   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;

      if(iDay != ((Date)o).iDay) return false;
      if(iMonth != ((Date)o).iMonth) return false;
      if(iYear != ((Date)o).iYear) return false;
      
      return true;
   }      

}
