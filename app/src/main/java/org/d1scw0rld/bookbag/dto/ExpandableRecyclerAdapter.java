package org.d1scw0rld.bookbag.dto;

import android.content.Context;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class ExpandableRecyclerAdapter<T extends ExpandableRecyclerAdapter.ListItem> extends RecyclerView.Adapter<ExpandableRecyclerAdapter<T>.ViewHolder>
{
   protected Context mContext;
   protected List<T> allItems = new ArrayList<>();
   protected List<T> visibleItems = new ArrayList<>();
   private List<Integer> indexList = new ArrayList<>();
   private SparseIntArray expandMap = new SparseIntArray();
   private int mode;
   
//   private boolean isAllExpanded = false;

   protected static final int TYPE_HEADER = 1000, 
                              TYPE_ITEM = 1001;

//   private static final int ARROW_ROTATION_DURATION = 150;

   public static final int MODE_NORMAL = 0;
   public static final int MODE_ACCORDION = 1;

   public ExpandableRecyclerAdapter(Context context)
   {
      mContext = context;
      
   }

   public static class ListItem
   {
      public int ItemType;
      public String sText;

      public ListItem(int itemType)
      {
         ItemType = itemType;
      }
      
      public ListItem(int itemType, String sName)
      {
         ItemType = itemType;
         this.sText = sName;
      }
      
   }

   @Override
   public long getItemId(int i)
   {
      return i;
   }

   @Override
   public int getItemCount()
   {
      return visibleItems == null ? 0 : visibleItems.size();
   }

   protected View inflate(int resourceID, ViewGroup viewGroup)
   {
      return LayoutInflater.from(mContext).inflate(resourceID, viewGroup, false);
   }

   public class ViewHolder extends RecyclerView.ViewHolder
   {
      public ViewHolder(View view)
      {
         super(view);
      }
   }

   public class HeaderViewHolder extends ViewHolder
   {
//      ImageView arrow;

//      public HeaderViewHolder(View view, final ImageView arrow)
      public HeaderViewHolder(View view)
      {
         super(view);

//         this.arrow = arrow;

         view.setOnClickListener(new View.OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
               handleClick();
            }
         });
      }

      protected void handleClick()
      {
         if(toggleExpandedItems(getLayoutPosition(), false))
         {
//            openArrow(arrow);
            setExpanded(true);
            onExpansionToggled(false);
         }
         else
         {
//            closeArrow(arrow);
            setExpanded(false);
            onExpansionToggled(true);
         }
      }

      public void bind(int position)
      {
//         arrow.setRotation(isExpanded(position) ? 90 : 0);
         setExpanded(isExpanded(position));
      }
      
      /**
       * Setter method for expanded state, used for initialization of expanded state.
       * changes to the state are given in {@link #onExpansionToggled(boolean)}
       *
       * @param expanded true if expanded, false if not
       */
      @UiThread
      public void setExpanded(boolean expanded) 
      {
      }
      
      
      /**
       * Callback triggered when expansion state is changed, but not during
       * initialization.
       * <p>
       * Useful for implementing animations on expansion.
       * 
       * @param expanded
       *           true if view is expanded before expansion is toggled, false if
       *           not
       */
      @UiThread
      public void onExpansionToggled(boolean expanded)
      {
      }      
   }

   public boolean toggleExpandedItems(int position, boolean notify)
   {
      if(isExpanded(position))
      {
         collapseItems(position, notify);
         return false;
      }
      else
      {
         expandItems(position, notify);

         if(mode == MODE_ACCORDION)
         {
            collapseAllExcept(position);
         }

         return true;
      }
   }

   public void expandItems(int position, boolean notify)
   {
      int count = 0;
      int index = indexList.get(position);
      int insert = position;

      for(int i = index + 1; i < allItems.size() && allItems.get(i).ItemType != TYPE_HEADER; i++)
      {
         insert++;
         count++;
         visibleItems.add(insert, allItems.get(i));
         indexList.add(insert, i);
      }

      notifyItemRangeInserted(position + 1, count);

      int allItemsPosition = indexList.get(position);
      expandMap.put(allItemsPosition, 1);

      if(notify)
      {
         notifyItemChanged(position);
      }
   }

   public void collapseItems(int position, boolean notify)
   {
      int count = 0;
      int index = indexList.get(position);

      for(int i = index + 1; i < allItems.size()
               && allItems.get(i).ItemType != TYPE_HEADER; i++)
      {
         count++;
         visibleItems.remove(position + 1);
         indexList.remove(position + 1);
      }

      notifyItemRangeRemoved(position + 1, count);

      int allItemsPosition = indexList.get(position);
      expandMap.delete(allItemsPosition);

      if(notify)
      {
         notifyItemChanged(position);
      }
   }

   public class StaticViewHolder extends ViewHolder
   {
      public StaticViewHolder(View view)
      {
         super(view);
      }
   }

   public class ItemViewHolder extends ViewHolder
   {
      public ItemViewHolder(View view)
      {
         super(view);
      }
   }

   protected boolean isExpanded(int position)
   {
      int allItemsPosition = indexList.get(position);
      return expandMap.get(allItemsPosition, -1) >= 0;
   }

   @Override
   public int getItemViewType(int position)
   {
      return visibleItems.get(position).ItemType;
   }

   public void setItems(List<T> items)
   {
      allItems = items;
      List<T> visibleItems = new ArrayList<>();
      expandMap.clear();
      indexList.clear();

      for(int i = 0; i < items.size(); i++)
      {
         if(items.get(i).ItemType == TYPE_HEADER)
         {
            indexList.add(i);
            visibleItems.add(items.get(i));
         }
      }

      this.visibleItems = visibleItems;
      notifyDataSetChanged();
   }

   protected void notifyItemInserted(int allItemsPosition, int visiblePosition)
   {
      incrementIndexList(allItemsPosition, visiblePosition, 1);
      incrementExpandMapAfter(allItemsPosition, 1);

      if(visiblePosition >= 0)
      {
         notifyItemInserted(visiblePosition);
      }
   }

   protected void removeItemAt(int visiblePosition)
   {
      int allItemsPosition = indexList.get(visiblePosition);

      allItems.remove(allItemsPosition);
      visibleItems.remove(visiblePosition);

      incrementIndexList(allItemsPosition, visiblePosition, -1);
      incrementExpandMapAfter(allItemsPosition, -1);

      notifyItemRemoved(visiblePosition);
   }

   private void incrementExpandMapAfter(int position, int direction)
   {
      SparseIntArray newExpandMap = new SparseIntArray();

      for(int i = 0; i < expandMap.size(); i++)
      {
         int index = expandMap.keyAt(i);
         newExpandMap.put(index < position ? index : index + direction, 1);
      }

      expandMap = newExpandMap;
   }

   private void incrementIndexList(int allItemsPosition,
                                   int visiblePosition,
                                   int direction)
   {
      List<Integer> newIndexList = new ArrayList<>();

      for(int i = 0; i < indexList.size(); i++)
      {
         if(i == visiblePosition)
         {
            if(direction > 0)
            {
               newIndexList.add(allItemsPosition);
            }
            else
               continue;
         }

         int val = indexList.get(i);
         newIndexList.add(val < allItemsPosition ? val : val + direction);
      }

      indexList = newIndexList;
   }

   public void collapseAll()
   {
      collapseAllExcept(-1);
   }

   public void collapseAllExcept(int position)
   {
      for(int i = visibleItems.size() - 1; i >= 0; i--)
      {
         if(i != position && getItemViewType(i) == TYPE_HEADER)
         {
            if(isExpanded(i))
            {
               collapseItems(i, true);
            }
         }
      }
   }

   public void expandAll()
   {
      if(visibleItems.size() == allItems.size())
         return;
      
      for(int i = visibleItems.size() - 1; i >= 0; i--)
      {
         if(getItemViewType(i) == TYPE_HEADER)
         {
            if(!isExpanded(i))
            {
               expandItems(i, true);
            }
         }
      }
   }

//   @SuppressLint("NewApi")
//   public static void openArrow(View view)
//   {
//      view.animate().setDuration(ARROW_ROTATION_DURATION).rotation(90);
//   }
//
//   @SuppressLint("NewApi")
//   public static void closeArrow(View view)
//   {
//      view.animate().setDuration(ARROW_ROTATION_DURATION).rotation(0);
//   }

   public int getMode()
   {
      return mode;
   }

   public void setMode(int mode)
   {
      this.mode = mode;
   }
   

//   public boolean isAllExpanded()
//   {
//      return isAllExpanded;
//   }
//   
//
//   public void setAllExpanded(boolean isAllExpanded)
//   {
//      this.isAllExpanded = isAllExpanded;
//   }
}