package org.d1scw0rld.bookbag.dto;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.d1scw0rld.bookbag.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BooksAdapter extends ExpandableRecyclerAdapter<BooksAdapter.BookListItem>
{
   private static final float INITIAL_POSITION = 0f,
                              ROTATED_POSITION = 180f;
   private int iAllChildrenCount;

   private String sFilter = "";

   private final ArrayList<BookListItem> alListItemsNotFiltered;

   private OnClickListener onBookClickListener = null;
   private OnLongClickListener onBookLongClickListener = null;
   private OnClickListener onHeaderClickListener   = null;

   public BooksAdapter(Context context, ArrayList<ParentResult> alParentsResults)
   {
      super(context);

      setItems(generateItems(alParentsResults));
      alListItemsNotFiltered = new ArrayList<>(allItems);
   }

   @Override
   public long getItemId(int i)
   {
      return visibleItems.get(i).id;
   }

   private List<BookListItem> generateItems(ArrayList<ParentResult> alParentsResults)
   {
      iAllChildrenCount = 0;
      List<BookListItem> items = new ArrayList<>();
      for(ParentResult parentResult : alParentsResults)
      {
         items.add(new BookListItem(parentResult.getName()));
         iAllChildrenCount += parentResult.getChildList()
                                           .size();
         for(Result result : parentResult.getChildList())
         {
            items.add(new BookListItem(result.id, result.content));
         }
      }

      return items;
   }

   public void setHeaderClickListener(OnClickListener onHeaderClickListener)
   {
      this.onHeaderClickListener = onHeaderClickListener;
   }

   public static class BookListItem extends ExpandableRecyclerAdapter.ListItem
   {
      public long id = -1;

      BookListItem(String group)
      {
         super(TYPE_HEADER, group);
      }

      BookListItem(long id, String item)
      {
         super(TYPE_ITEM, item);

         this.id = id;
      }
   }

   public class HeaderViewHolder extends ExpandableRecyclerAdapter<BookListItem>.HeaderViewHolder
   {
      View view;

      TextView name;

      private final ImageView arrow;

      HeaderViewHolder(View view)
      {
         super(view);

         arrow = view.findViewById(R.id.iv_arrow);
         name = view.findViewById(R.id.tv_header);
         this.view = view;
      }

      public void bind(int position)
      {
         super.bind(position);

         name.setText(visibleItems.get(position).sText);
      }

      @Override
      public void setExpanded(boolean expanded)
      {
         super.setExpanded(expanded);
         arrow.setRotation(expanded ? ROTATED_POSITION : INITIAL_POSITION);
      }

      @Override
      public void onExpansionToggled(boolean expanded)
      {
         RotateAnimation rotateAnimation;
         if(expanded)
         {
            // rotate counterclockwise
            rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                                                  INITIAL_POSITION,
                                                  RotateAnimation.RELATIVE_TO_SELF,
                                                  0.5f,
                                                  RotateAnimation.RELATIVE_TO_SELF,
                                                  0.5f);

         }
         else
         {
            // rotate clockwise
            rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
                                                  INITIAL_POSITION,
                                                  RotateAnimation.RELATIVE_TO_SELF,
                                                  0.5f,
                                                  RotateAnimation.RELATIVE_TO_SELF,
                                                  0.5f);

         }

         rotateAnimation.setDuration(200);
         rotateAnimation.setFillAfter(true);
         arrow.startAnimation(rotateAnimation);
      }
   }

   public class ItemViewHolder extends ExpandableRecyclerAdapter<BookListItem>.ViewHolder
   {
      public View view;

      TextView name;

      ItemViewHolder(View view)
      {
         super(view);

         name = view.findViewById(R.id.tv_item);
         this.view = view;
      }

      void bind(int position)
      {
         String sText = visibleItems.get(position).sText;
         Spannable spContent = new SpannableString(sText);
         int iFilteredStart = sText.toLowerCase(Locale.getDefault())
                                   .indexOf(sFilter.toLowerCase(Locale.getDefault()));
         int iFilterEnd;
         if(iFilteredStart < 0)
         {
            iFilteredStart = 0;
            iFilterEnd = 0;
         }
         else
         {
            iFilterEnd = iFilteredStart + sFilter.length();
         }
         spContent.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.accent)),
                           iFilteredStart,
                           iFilterEnd,
                           Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
         name.setText(spContent);
      }
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
   {
      switch(viewType)
      {
         case TYPE_HEADER:
            return new HeaderViewHolder(inflate(R.layout.item_header, parent));
         case TYPE_ITEM:
         default:
            return new ItemViewHolder(inflate(R.layout.item_book, parent));
      }
   }

   @Override
   public void onBindViewHolder(@NonNull ExpandableRecyclerAdapter<BookListItem>.ViewHolder holder,
                                int position)
   {
      switch(getItemViewType(position))
      {
         case TYPE_HEADER:
            ((HeaderViewHolder) holder).bind(position);
            ((HeaderViewHolder) holder).view.setOnClickListener(v-> {
               onHeaderClickListener.onClick(v);
               ((HeaderViewHolder) holder).handleClick();
            });
            break;
         case TYPE_ITEM:
         default:
            ((ItemViewHolder) holder).bind(position);
            ((ItemViewHolder) holder).view.setOnClickListener(onBookClickListener);
            ((ItemViewHolder) holder).view.setOnLongClickListener(onBookLongClickListener);
            break;
      }
   }

   public void filter(String charText)
   {
      iAllChildrenCount = 0;

      charText = charText.toLowerCase(Locale.getDefault());
      ArrayList<BookListItem> alBookListItemsTmp = new ArrayList<>();
      sFilter = charText;
      visibleItems.clear();
      if(charText.length() == 0)
      {
         setItems(alListItemsNotFiltered);
         for(ListItem listItem : alListItemsNotFiltered)
         {
            if(listItem.ItemType == TYPE_ITEM)
            {
               iAllChildrenCount++;
            }
         }
      }
      else
      {
         for(BookListItem bookListItem : alListItemsNotFiltered)
         {
            if(bookListItem.ItemType == TYPE_HEADER || bookListItem.sText.toLowerCase(Locale.getDefault())
                                                                         .contains(charText))
            {
               /*
                * If the last and the next items are headers remove the last item - it has not subitems
                */

               if(bookListItem.ItemType == TYPE_HEADER
                     && alBookListItemsTmp.size() > 0
                     && alBookListItemsTmp.get(alBookListItemsTmp.size() - 1).ItemType == TYPE_HEADER)
               {
                  alBookListItemsTmp.remove(alBookListItemsTmp.size() - 1);
               }
               if(bookListItem.ItemType == TYPE_ITEM)
               {
                  iAllChildrenCount++;
               }
               alBookListItemsTmp.add(bookListItem);
            }
         }
         if(alBookListItemsTmp.size() > 0
               && alBookListItemsTmp.get(alBookListItemsTmp.size() - 1).ItemType == TYPE_HEADER)
         {
            alBookListItemsTmp.remove(alBookListItemsTmp.size() - 1);
         }
         setItems(alBookListItemsTmp);
      }

      expandAll();
   }

   public int getAllChildrenCount()
   {
      return iAllChildrenCount;
   }

   public void setBookClickListener(OnClickListener onClickListener)
   {
      this.onBookClickListener = onClickListener;
   }

   public void setBookLongClickListener(OnLongClickListener onLongClickListener)
   {
      this.onBookLongClickListener = onLongClickListener;
   }

   public void removeAt(int iClickedItemNdx)
   {
      removeItemAt(iClickedItemNdx);
      iAllChildrenCount--;
   }

   @Override
   protected void removeItemAt(int visiblePosition)
   {
      alListItemsNotFiltered.remove(visibleItems.get(visiblePosition));
      super.removeItemAt(visiblePosition);
      if(visibleItems.get(visiblePosition - 1).ItemType == TYPE_HEADER && (visiblePosition == visibleItems.size() || visibleItems.get(visiblePosition).ItemType == TYPE_HEADER))
      {
         super.removeItemAt(visiblePosition - 1);
      }
   }
}