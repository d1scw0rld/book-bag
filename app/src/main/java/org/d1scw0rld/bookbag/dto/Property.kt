package org.d1scw0rld.bookbag.dto;

import androidx.annotation.NonNull;

public class Property
{
   public int    fieldTypeId;
   public long   id = 0;
   public String value = "";

   public Property(int fieldTypeId)
   {
      this.fieldTypeId = fieldTypeId;
   }

   public Property(int fieldTypeId, String value)
   {
      this.fieldTypeId = fieldTypeId;
      this.value = value;
   }

   public Property(long id, int fieldTypeId, String value)
   {
      this.id = id;
      this.fieldTypeId = fieldTypeId;
      this.value = value;
   }

   public void copy(Property other)
   {
      id = other.id;
      fieldTypeId = other.fieldTypeId;
      value = other.value;
   }

   @NonNull
   @Override
   public String toString()
   {
      return value;
   }

   @Override
   public boolean equals(Object other)
   {
      if (this == other) return true;
      if (other == null || getClass() != other.getClass()) return false;
      Property property = (Property) other;
      return id == property.id && fieldTypeId == property.fieldTypeId && value.equalsIgnoreCase(property.value);
   }
}
