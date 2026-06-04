package org.d1scw0rld.bookbag.dto;

import androidx.annotation.NonNull;

public class Price
{
   public int value = 0;
   public long currencyId = 0;
   
   public Price()
   {}
   
   public Price(String priceString)
   {
      this();
      
      if(priceString.isEmpty())
         return;
         
      String[] parts = priceString.split("\\|");
      value = Integer.parseInt(parts[0]);
      if(parts.length > 1)
         currencyId = Long.parseLong(parts[1]);
   }
   
   public Price(int value, int currency)
   {
      this.value = value;
      this.currencyId = currency;
   }

   @NonNull
   @Override
   public String toString()
   {
      if(value == 0)
         return "";
      else
         return value + "|" + currencyId;
   }
}
