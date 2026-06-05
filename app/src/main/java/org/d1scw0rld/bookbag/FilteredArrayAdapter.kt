package org.d1scw0rld.bookbag;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;

import java.util.ArrayList;
import java.util.List;

public class FilteredArrayAdapter<T> extends ArrayAdapter<T>
{
   private final Context context;
   private final ArrayList<T> items;
   private FieldFilter  nameFilter;

   public FilteredArrayAdapter(Context context, int viewResourceId, ArrayList<T> items)
   {
      super(context, viewResourceId, items);
      this.items = items;
      this.context = context;
   }

   @NonNull
   public View getView(int position, View convertView, @NonNull ViewGroup parent)
   {
      TextView view = (TextView) super.getView(position, convertView, parent);
      T item = getItem(position);
      assert item != null;
      view.setText(item.toString());
      return view;
   }

   @NonNull
   @Override
   public Filter getFilter()
   {
      if(nameFilter == null)
         nameFilter = new FieldFilter(items);
      return nameFilter;
   }

   private class FieldFilter extends Filter
   {
      private final ArrayList<T> suggestions;

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
            ArrayList<T> filteredSuggestions = new ArrayList<>();

            for(T object : suggestions)
            {
               if(object.toString()
                        .toLowerCase(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0))
                        .startsWith(filterSeq))
                  filteredSuggestions.add(object);
            }
            result.count = filteredSuggestions.size();
            result.values = filteredSuggestions;
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
         }
         else
            notifyDataSetInvalidated();
      }
   }
}
