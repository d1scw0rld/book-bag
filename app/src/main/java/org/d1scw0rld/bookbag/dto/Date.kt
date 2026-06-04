package org.d1scw0rld.bookbag.dto;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Date
{
   public int day, 
              month, 
              year; 

   public Date()
   {
      day = 1;
      month = 1;
      year = 1900;
   }
   
   public Date(int day,
                int month,
                int year)
   {
      this.day = day;
      this.month = month;
      this.year = year;
   }
   
   
   public Date(int dateValue)
   {
      fromInt(dateValue);
   }
   
   public Date(Date otherDate)
   {
      this(otherDate.day, otherDate.month, otherDate.year);
   }
   
   public int toInt()
   {
      return year*10000 + month*100 + day;
   }
   
   private void fromInt(int dateValue)
   {
      day = dateValue % 100;
      month = (dateValue / 100) % 100;
      year = dateValue / 10000;
   }

   @NonNull
   @Override
   public String toString()
   {
      return String.format(Locale.getDefault(), "%02d", day) + "/" + String.format(Locale.getDefault(), "%02d", month) + "/" + year;
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (this == other) return true;
      if (other == null || getClass() != other.getClass()) return false;

      Date otherDate = (Date) other;
      if(day != otherDate.day) return false;
      if(month != otherDate.month) return false;
      return year == otherDate.year;
   }      

}
