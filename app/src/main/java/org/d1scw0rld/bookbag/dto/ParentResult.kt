package org.d1scw0rld.bookbag.dto;

import java.util.List;

public class ParentResult implements Parent<Result>
{
   private String name;
   
   private final List<Result> children;

   public ParentResult(String name, List<Result> children)
   {
      this.setName(name);
      this.children = children;
   }

   @Override
   public List<Result> getChildList()
   {
      return children;
   }

   @Override
   public boolean isInitiallyExpanded()
   {
      return true;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   public void addChildResult(Result result)
   {
      children.add(result);
   }

   @Override
   public String getName()
   {
      return name;
   }
}
