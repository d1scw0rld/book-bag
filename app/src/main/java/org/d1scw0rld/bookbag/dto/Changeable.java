package org.d1scw0rld.bookbag.dto;

import android.support.annotation.NonNull;

public class Changeable<T>
{
   public T value;

   Changeable(T value)
   {
      this.value = value;
   }

   @NonNull
   public String toString() 
   {
      return value.toString();
   }
   
   public Class getGenericType()
   {
      return value.getClass();
   }
   
//   public void set(T value)
//   {
//      this.value = value;
//   }

   public boolean equals(Object other) 
   {
      if (other instanceof Changeable) 
         return value.equals(((Changeable)other).value);
      else 
         return value.equals(other);
   }

   public int hashCode() 
   {
      return value.hashCode();
   }
   
   public boolean isEmpty()
   {
      if(value instanceof String)
         return ((String) value).trim().isEmpty();
      else if(value instanceof Integer)
         return ((Integer)value) == 0;
      return true;
   }
}