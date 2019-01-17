package org.d1scw0rld.bookbag.dto;

import java.util.List;

public class ParentResult implements Parent<Result>
{
   /* Create an instance variable for your list of children */
   
   private String sName;
   
   private List<Result> lsChildren;

   /**
    * Your constructor and any other accessor
    *  methods should go here.
    */
   
   public ParentResult(String sName, List<Result> lsChildren)
   {
      this.setName(sName);
      this.lsChildren = lsChildren;
   }

   @Override
   public List<Result> getChildList()
   {
      return lsChildren;
   }

   @Override
   public boolean isInitiallyExpanded()
   {
      return true;
   }

   public void setName(String sName)
   {
      this.sName = sName;
   }
   
   public void addChildResult(Result result)
   {
      lsChildren.add(result);
   }

   @Override
   public String getName()
   {
      return sName;
   }
}
