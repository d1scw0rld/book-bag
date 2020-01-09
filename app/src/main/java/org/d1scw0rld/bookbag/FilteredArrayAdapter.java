package org.d1scw0rld.bookbag;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FilteredArrayAdapter<T> extends ArrayAdapter<T>
{
   private ArrayList<T> items;
   private FieldFilter  nameFilter;

   public FilteredArrayAdapter(Context context, int viewResourceId, ArrayList<T> items)
   {
      super(context, viewResourceId, items);
      this.items = items;
   }

   @NonNull
   public View getView(int position, View convertView, @NonNull ViewGroup parent)
   {
      TextView view = (TextView) super.getView(position, convertView, parent);
      T t = getItem(position);
      assert t!= null;
      view.setText(t.toString());
      return view;
   }

   @NonNull
   @Override
   public Filter getFilter()
   {
      if(nameFilter == null)
         nameFilter = new FieldFilter(items);
      return nameFilter;
//          return nameFilter;
   }

   private class FieldFilter extends Filter
   {
      private ArrayList<T> suggestions;

      FieldFilter(List<T> objects)
      {
         suggestions = new ArrayList<>();
         synchronized(this)
         {
            suggestions.addAll(objects);
         }
      }

      @Override
      protected FilterResults performFiltering(CharSequence charSequence)
      {
         FilterResults result = new FilterResults();
         if(charSequence != null && charSequence.length() > 0)
         {
            String filterSeq = charSequence.toString().toLowerCase();
            ArrayList<T> filter = new ArrayList<>();

            for(T object : suggestions)
            {
               // the filtering itself:
               if(object.toString()
                        .toLowerCase()
                        .startsWith(filterSeq))
                  filter.add(object);
            }
            result.count = filter.size();
            result.values = filter;
         }
         else
         {
            synchronized(this)
            {
               result.values = suggestions;
               result.count = suggestions.size();
            }
         }
         return result;
      }
      @SuppressWarnings("unchecked")
      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults)
      {
         if(filterResults != null && filterResults.count > 0)
         {
            ArrayList<T> filtered = (ArrayList<T>) filterResults.values;
            notifyDataSetChanged();
            clear();
            addAll(filtered);
//            for(Object o: filtered)
//            {
//               add((T) o);
//            }
         }
         else
            notifyDataSetInvalidated();


//            if(filterResults != null && filterResults.count > 0) {
//               notifyDataSetChanged();
//            }
//            else {
//               notifyDataSetInvalidated();
//            }
      }
   }
}
