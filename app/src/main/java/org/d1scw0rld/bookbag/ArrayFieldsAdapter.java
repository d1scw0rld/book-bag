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

import org.d1scw0rld.bookbag.dto.Field;

public class ArrayFieldsAdapter extends ArrayAdapter<Field>
{
//   private final String MY_DEBUG_TAG = "ArrayFieldsAdapter";
   private ArrayList<Field> items;
   private ArrayList<Field> suggestions;
   private Context context;

   public ArrayFieldsAdapter(Context context, int viewResourceId, ArrayList<Field> items) 
   {
       super(context, viewResourceId, items);
       this.context = context;
       this.items = items;
       this.suggestions = new ArrayList<>();
   }

   @NonNull
   public View getView(int position, View convertView, @NonNull ViewGroup parent)
   {
      TextView view = (TextView) super.getView(position, convertView, parent);
      // Replace text with my own
      view.setText(getItem(position).sValue);
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
         return ((Field)(resultValue)).sValue;
      }
      
      @Override
      protected FilterResults performFiltering(CharSequence constraint) 
      {
         if(constraint != null) 
         {
            suggestions.clear();
            for (Field oField : items) 
            {
               if(oField.sValue.toLowerCase(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0)).startsWith(constraint.toString().toLowerCase()))
               {
                  suggestions.add(oField);
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
         ArrayList<Field> filteredList = (ArrayList<Field>) results.values;
         if(results != null && results.count > 0) 
         {
            clear();
            for (Field c : filteredList) 
            {
               add(c);
            }
            
            notifyDataSetChanged();
         }
      }
   };
}