package org.d1scw0rld.bookbag;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.os.ConfigurationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.d1scw0rld.bookbag.fields.FieldMultiText.Item;

public class ArrayItemsAdapter extends ArrayAdapter<Item> 
{
//   private final String MY_DEBUG_TAG = "ArrayFieldsAdapter";
//   private ArrayList<? extends Item> items;
   private Context context;
   private ArrayList<Item> items;
   private ArrayList<Item> suggestions;
//   private int viewResourceId;

   public ArrayItemsAdapter(Context context, int viewResourceId, ArrayList<Item> alDictionaryFields) 
   {
       super(context, viewResourceId, alDictionaryFields);
//       this.items = alDictionaryFields1;
//       this.itemsAll = (ArrayList<Item>) alDictionaryFields.clone();
       this.context = context;
       this.items = alDictionaryFields;
       this.suggestions = new ArrayList<Item>();
//       this.viewResourceId = viewResourceId;
   }

   public View getView(int position, View convertView, ViewGroup parent) 
   {
      TextView view = (TextView) super.getView(position, convertView, parent);
      // Replace text with my own
      view.setText(getItem(position).getValue());
      return view;         
      
   }

   @Override
   public Filter getFilter() 
   {
       return nameFilter;
   }

   Filter nameFilter = new Filter() 
   {
      @Override
      public String convertResultToString(Object resultValue) 
      {
         String str = ((Item)(resultValue)).getValue(); 
         return str;
      }
      
      @Override
      protected FilterResults performFiltering(CharSequence constraint) 
      {
         if(constraint != null) 
         {
            suggestions.clear();
            for (Item oField : items) 
            {
               if(oField.getValue().toLowerCase(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0)).startsWith(constraint.toString().toLowerCase()))
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
         ArrayList<Item> filteredList = (ArrayList<Item>) results.values;
         if(results != null && results.count > 0) 
         {
            clear();
            for (Item c : filteredList) 
            {
               add(c);
            }
            
            notifyDataSetChanged();
         }
      }
   };
}