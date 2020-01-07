package org.d1scw0rld.bookbag;

import java.util.ArrayList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.os.ConfigurationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class ArrayItemsAdapter extends ArrayAdapter<IItem>
{
//   private final String MY_DEBUG_TAG = "FilteredArrayAdapter";
   private Context context;
   private ArrayList<IItem> items;
   private ArrayList<IItem> suggestions;

   public ArrayItemsAdapter(Context context, int viewResourceId, ArrayList<IItem> items)
   {
       super(context, viewResourceId, items);
       this.context = context;
       this.items = items;
       this.suggestions = new ArrayList<>();
       suggestions.addAll(items);
   }

   @NonNull
   public View getView(int position, View convertView, @NonNull ViewGroup parent)
   {
      TextView view = (TextView) super.getView(position, convertView, parent);
      view.setText(getItem(position).getValue());
      return view;         
   }

   @NonNull
   @Override
   public Filter getFilter() 
   {
       return nameFilter;
   }

   private Filter nameFilter = new Filter()
   {
      @Override
      public String convertResultToString(Object resultValue) 
      {
         return ((IItem)(resultValue)).getValue();
      }
      
      @Override
      protected FilterResults performFiltering(CharSequence constraint) 
      {
         if(constraint != null) 
         {
            suggestions.clear();
            for (IItem item : items)
            {
               if(item.getValue().toLowerCase(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0)).startsWith(constraint.toString().toLowerCase()))
               {
                  suggestions.add(item);
               }
            }
            
            FilterResults filterResults = new FilterResults();
            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
         } 
         else 
         {
            return new FilterResults();
         }
      }
       
      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) 
      {
         ArrayList<IItem> filteredList = (ArrayList<IItem>) results.values;
         if(results != null && results.count > 0) 
         {
            clear();
            for (IItem item : filteredList)
            {
               add(item);
            }
            
            notifyDataSetChanged();
         }
      }
   };
}