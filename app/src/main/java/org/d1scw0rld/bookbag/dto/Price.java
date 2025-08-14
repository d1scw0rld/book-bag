package org.d1scw0rld.bookbag.dto;

import androidx.annotation.NonNull;

public class Price
{
   public int iValue = 0;
   public long iCurrencyID = 0;
   
   public Price()
   {}
   
   public Price(String sPrice)
   {
      this();
      
      if(sPrice.isEmpty())
         return;
         
      String[] sParts = sPrice.split("\\|");
      iValue = Integer.parseInt(sParts[0]);
      if(sParts.length > 1)
         iCurrencyID = Long.parseLong(sParts[1]);
   }
   
   public Price(int iValue, int iCurrency)
   {
      this.iValue = iValue;
      this.iCurrencyID = iCurrency;
   }

   @NonNull
   @Override
   public String toString()
   {
      if(iValue == 0)
         return "";
      else
         return iValue + "|" + iCurrencyID;
   }
}
