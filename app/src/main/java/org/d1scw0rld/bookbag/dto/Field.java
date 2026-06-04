package org.d1scw0rld.bookbag.dto;

/**
 * Created by Iasen on 12.7.2016 г..
 */

public class Field
{
   public  int     id;
   public  String  name;
   public  boolean isVisible  = false;
   public  byte    type;
   public  int     inputType = 0;

   public final static byte TYPE_TEXT              = 1,
                            TYPE_TEXT_AUTOCOMPLETE = 2,
                            TYPE_MONEY             = 3,
                            TYPE_MULTIFIELD        = 4,
                            TYPE_SPINNER           = 5,
                            TYPE_MULTI_SPINNER     = 6,
                            TYPE_DATE              = 7,
                            TYPE_RATING            = 8,
                            TYPE_CHECK_BOX         = 9;
   
   
   public Field(int id, String name, boolean isVisible, byte type)
   {
      this.id = id;
      this.name = name;
      this.isVisible = isVisible;
      this.type = type;
   }

   public Field(int id, String name, byte type)
   {
      this.id = id;
      this.name = name;      
      this.type = type;
   }

   public Field setVisibility(boolean isVisible)
   {
      this.isVisible = isVisible;
      return this;
   }
   
   public Field setInputType(int inputType)
   {
      this.inputType = inputType;
      return this;
   }
}
