package org.d1scw0rld.bookbag;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Changeable;
import org.d1scw0rld.bookbag.dto.Property;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.fields.AutoCompleteTextViewX;
import org.d1scw0rld.bookbag.fields.EditTextX;
import org.d1scw0rld.bookbag.fields.FieldAutoCompleteTextView;
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

class PropertiesFactory
{
   private Book book;
   private  Context context;
   private FieldEditTextUpdatableClearable fBookTitle = null;
   private View vPrevious = null;


   private static final PropertiesFactory ourInstance = new PropertiesFactory();

   static PropertiesFactory getInstance()
   {
      return ourInstance;
   }

   private PropertiesFactory()
   {
   }

   public void init(Book book, Context context)
   {
      this.book = book;
      this.context = context;
   }


   public void addFieldText(LinearLayout rootView, Field field)
   {
      switch(field.iID)
      {
         case DBAdapter.FLD_TITLE:
            addFieldText(rootView, field, book.csTitle);
            break;

         case DBAdapter.FLD_DESCRIPTION:
            addFieldText(rootView, field, book.csDescription);
            break;

         case DBAdapter.FLD_VOLUME:
            addFieldText(rootView, field, book.ciVolume);
            break;

         case DBAdapter.FLD_PAGES:
            addFieldText(rootView, field, book.ciPages);
            break;

         case DBAdapter.FLD_EDITION:
            addFieldText(rootView, field, book.ciEdition);
            break;

         case DBAdapter.FLD_ISBN:
            addFieldText(rootView, field, book.csISBN);
            break;

         case DBAdapter.FLD_WEB:
            addFieldText(rootView, field, book.csWeb);
            break;

         default:
      }

   }

   private <T> void addFieldText(ViewGroup rootView, Field field, final Changeable<T> cValue)
   {
      final FieldEditTextUpdatableClearable fieldEditTextUpdatableClearable = new FieldEditTextUpdatableClearable(context);

      fieldEditTextUpdatableClearable.setTitle(field.sName);
      fieldEditTextUpdatableClearable.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldEditTextUpdatableClearable.setText(cValue.toString());
      fieldEditTextUpdatableClearable.setHint(field.sName);
      fieldEditTextUpdatableClearable.setInputType(field.iInputType);
      if(field.iID == DBAdapter.FLD_TITLE)
      {
         fBookTitle = fieldEditTextUpdatableClearable;
         vPrevious = fieldEditTextUpdatableClearable.findViewById(R.id.editTextX);
      }
      fieldEditTextUpdatableClearable.setUpdateListener(new EditTextX.OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText et)
         {
            Class<?> c = cValue.getGenericType();
            Class<?> clazz;
            Object object;
            try
            {
               clazz = Class.forName(c.getName());
               Constructor<?> ctor = clazz.getConstructor(String.class);
               object = ctor.newInstance(et.getText()
                                           .toString()
                                           .trim());

            }
            catch(ClassNotFoundException
                  | NoSuchMethodException
                  | SecurityException
                  | InstantiationException
                  | IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e)
            {
               e.printStackTrace();
               return;
            }

            cValue.value = (T) object;

//            if(t instanceof Integer)
//            {
//               t = (T) Integer.valueOf(et.getText().toString());
//               cValue.value = t;
//            }
//            else if(t instanceof String)
//            {
//               t = (T) et.getText().toString().trim();
//               cValue.value = t;
//
//            }
         }
      });
      rootView.addView(fieldEditTextUpdatableClearable);
      if(!field.isVisible && cValue.isEmpty())
         hideField(fieldEditTextUpdatableClearable, field.sName);
   }

   private void addAutocompleteField(ViewGroup rootView, final Field field)
   {
      final FieldAutoCompleteTextView fieldAutoCompleteTextView = new FieldAutoCompleteTextView(context);
      fieldAutoCompleteTextView.setTitle(field.sName);
      fieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldAutoCompleteTextView.setHint(field.sName);
      View view = new View(context);
      view.setNextFocusDownId(fieldAutoCompleteTextView.getId());

      final ArrayList<Property> alFieldsOfType = dbAdapter.getFieldValues(field.iID, true);
      Property property = new Property(field.iID);

      for(int i = 0; property.iID == 0 && i < book.alProperties.size(); i++)
      {
         if(field.iID == book.alProperties.get(i).iFieldTypeID)
            property = book.alProperties.get(i);
      }

      if(property.iID == 0) // The book has not such a property
         book.alProperties.add(property);
      else
         fieldAutoCompleteTextView.setText(property.sValue);

      fieldAutoCompleteTextView.setTag(property);

//      FilteredArrayAdapter oArrayAdapter = new FilteredArrayAdapter(this, android.R.layout.select_dialog_item, alFieldsOfType);
      FilteredArrayAdapter oArrayAdapter = new FilteredArrayAdapter(context, R.layout.dropdown, alFieldsOfType);
//      FilteredArrayAdapter oArrayAdapter = new FilteredArrayAdapter(this, android.R.layout.simple_expandable_list_item_2, alFieldsOfType);
      fieldAutoCompleteTextView.setAdapter(oArrayAdapter);
      fieldAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
         public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId)
         {
            Property fldSelected = (Property) adapter.getItemAtPosition(position);
            ((Property) fieldAutoCompleteTextView.getTag()).copy(fldSelected);
         }
      });
      fieldAutoCompleteTextView.setUpdateListener(new AutoCompleteTextViewX.OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText et)
         {
            boolean isFound = false;
            for(Property f : alFieldsOfType)
            {
               if(et.getText()
                    .toString()
                    .trim()
                    .equalsIgnoreCase(f.sValue))
               {
                  isFound = true;
                  ((Property) fieldAutoCompleteTextView.getTag()).copy(f);
                  break;
               }
            }
            if(!isFound)
            {
               ((Property) fieldAutoCompleteTextView.getTag()).iID = 0;
               ((Property) fieldAutoCompleteTextView.getTag()).sValue = et.getText()
                                                                          .toString();
            }
         }
      });

      rootView.addView(fieldAutoCompleteTextView);
      if(!field.isVisible && property.sValue.trim()
                                            .isEmpty())
         hideField(fieldAutoCompleteTextView, field.sName);
   }


   private void hideField(View view, String sName)
   {

      view.setVisibility(View.GONE);

      pmHiddenFields.getMenu()
                    .add(Menu.NONE, pmHiddenFields.getMenu().size(), 0, sName);
      hmHiddenFileds.put(pmHiddenFields.getMenu()
                                       .getItem(pmHiddenFields.getMenu().size() - 1), view);
   }
}
