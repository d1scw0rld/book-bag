package org.d1scw0rld.bookbag;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.d1scw0rld.bookbag.dto.Field;

import java.util.ArrayList;
import java.util.List;

public class FilteredArrayAdapter<T> extends ArrayAdapter
{
   //      private final String MY_DEBUG_TAG = "FilteredArrayAdapter";
   private ArrayList<T> items;
   private ArrayList<T> suggestions;
   FieldFilter nameFilter;

   public FilteredArrayAdapter(Context context, int viewResourceId, ArrayList<T> items)
   {
      super(context, viewResourceId, items);
      this.items = items;
      suggestions = new ArrayList<>();
      suggestions.addAll(items);
   }

   @NonNull
   public View getView(int position, View convertView, @NonNull ViewGroup parent)
   {
      TextView view = (TextView) super.getView(position, convertView, parent);
//      view.setText(getItem(position).sValue);
      view.setText(getItem(position).toString());
      return view;
   }

   @NonNull
   @Override
   public Filter getFilter()
   {
      if(nameFilter == null)
         nameFilter = new FieldFilter<>(items);
      return nameFilter;
//          return nameFilter;
   }

   private class FieldFilter<T> extends Filter
   {
      private ArrayList<T> alSourceObjects;

      FieldFilter(List<T> objects)
      {
         alSourceObjects = new ArrayList<T>();
         synchronized(this)
         {
            alSourceObjects.addAll(objects);
         }
      }

      @Override
      protected FilterResults performFiltering(CharSequence charSequence)
      {
         String filterSeq = charSequence.toString().toLowerCase();

         FilterResults result = new FilterResults();
         if(filterSeq != null && filterSeq.length() > 0)
         {
            ArrayList<T> filter = new ArrayList<T>();

            for(T object : alSourceObjects)
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
            // add all objects
            synchronized(this)
            {
               result.values = alSourceObjects;
               result.count = alSourceObjects.size();
            }
         }
         return result;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults)
      {
         ArrayList<?> filtered = (ArrayList<?>) filterResults.values;
         if(filterResults != null && filterResults.count > 0)
         {
            notifyDataSetChanged();
            clear();
//            for(int i = 0, l = filtered.size(); i < l; i++)
            addAll(filtered);
//            for(T object : filtered)
//            {
////               add(filtered.get(i));
//               add(object);
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

//      Filter nameFilter = new Filter()
//      {
//         @Override
//         public String convertResultToString(Object resultValue)
//         {
//            return ((Field)(resultValue)).sValue;
//         }
//
//         @Override
//         protected FilterResults performFiltering(CharSequence constraint)
//         {
//            if(constraint != null)
//            {
//               suggestions.clear();
//               for (Field oField : items)
//               {
//                  if(oField.sValue.toLowerCase(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0)).startsWith(constraint.toString().toLowerCase()))
//                  {
//                     suggestions.add(oField);
//                  }
//               }
//
//               FilterResults filterResults = new FilterResults();
//               filterResults.values = suggestions;
//               filterResults.count = suggestions.size();
//               return filterResults;
//            }
//            else
//            {
//               return new FilterResults();
//            }
//         }
//
//         @Override
//         protected void publishResults(CharSequence constraint, FilterResults results)
//         {
//            ArrayList<Field> filteredList = (ArrayList<Field>) results.values;
//            if(results != null && results.count > 0)
//            {
//               clear();
//               for (Field c : filteredList)
//               {
//                  add(c);
//               }
//
//               notifyDataSetChanged();
//            }
//         }
//      };
}
