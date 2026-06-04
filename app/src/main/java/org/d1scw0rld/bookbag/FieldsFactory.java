package org.d1scw0rld.bookbag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.TextView;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Changeable;
import org.d1scw0rld.bookbag.dto.Date;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.dto.Price;
import org.d1scw0rld.bookbag.dto.Property;
import org.d1scw0rld.bookbag.fields.FieldAutoCompleteTextView;
import org.d1scw0rld.bookbag.fields.FieldCheckBox;
import org.d1scw0rld.bookbag.fields.FieldDate;
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable;
import org.d1scw0rld.bookbag.fields.FieldMoney;
import org.d1scw0rld.bookbag.fields.FieldMultiSpinner;
import org.d1scw0rld.bookbag.fields.FieldMultiText;
import org.d1scw0rld.bookbag.fields.FieldRating;
import org.d1scw0rld.bookbag.fields.FieldSpinner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

public class FieldsFactory extends BaseObservable<FieldsFactory.Listener>
{
   private final Context context;
   private final Book book;
   private final DBAdapter dbAdapter;

   private View vPrevious = null;

   public FieldsFactory(Context context, Book book, DBAdapter dbAdapter)
   {
      this.context = context;
      this.book = book;
      this.dbAdapter = dbAdapter;
   }

   public void addFieldText(ViewGroup rootView, Field field)
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

   public  <T> void addFieldText(ViewGroup rootView, Field field, final Changeable<T> cValue)
   {
      final FieldEditTextUpdatableClearable fieldEditTextUpdatableClearable = new FieldEditTextUpdatableClearable(context);

      fieldEditTextUpdatableClearable.setTitle(field.sName);
      fieldEditTextUpdatableClearable.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldEditTextUpdatableClearable.setText(cValue.toString());
      fieldEditTextUpdatableClearable.setHint(field.sName);
      fieldEditTextUpdatableClearable.setInputType(field.iInputType);
      if(field.iID == DBAdapter.FLD_TITLE)
      {
         vPrevious = fieldEditTextUpdatableClearable.findViewById(R.id.editTextX);
      }
      fieldEditTextUpdatableClearable.setUpdateListener(editText -> {
         Class<?> c = cValue.getGenericType();
         Class<?> clazz;
         Object object;
         try
         {
            clazz = Class.forName(c.getName());
            Constructor<?> ctor = clazz.getConstructor(String.class);
            object = ctor.newInstance(editText.getText()
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
      });
      rootView.addView(fieldEditTextUpdatableClearable);
      if(!field.isVisible && cValue.isEmpty())
         hideField(fieldEditTextUpdatableClearable, field.sName);
   }

   public void addAutocompleteField(ViewGroup rootView, final Field field)
   {
      final FieldAutoCompleteTextView fieldAutoCompleteTextView = new FieldAutoCompleteTextView(context);
      fieldAutoCompleteTextView.setTitle(field.sName);
      fieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldAutoCompleteTextView.setHint(field.sName);
      View view = new View(context);
      view.setNextFocusDownId(fieldAutoCompleteTextView.getId());

      final ArrayList<Property> propertyValues = getPropertyValues(field.iID, true);
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

      FilteredArrayAdapter<Property> filteredArrayAdapter = new FilteredArrayAdapter<>(context, R.layout.dropdown, propertyValues);
      fieldAutoCompleteTextView.setAdapter(filteredArrayAdapter);
      fieldAutoCompleteTextView.setOnItemClickListener((adapter, view1, position, rowId) -> {
         Property fldSelected = (Property) adapter.getItemAtPosition(position);
         ((Property) fieldAutoCompleteTextView.getTag()).copy(fldSelected);
      });
      fieldAutoCompleteTextView.setUpdateListener(editText -> {
         boolean isFound = false;
         for(Property p : propertyValues)
         {
            if(editText.getText()
                 .toString()
                 .trim()
                 .equalsIgnoreCase(p.sValue))
            {
               isFound = true;
               ((Property) fieldAutoCompleteTextView.getTag()).copy(p);
               break;
            }
         }
         if(!isFound)
         {
            ((Property) fieldAutoCompleteTextView.getTag()).iID = 0;
            ((Property) fieldAutoCompleteTextView.getTag()).sValue = editText.getText()
                                                                       .toString();
         }
      });

      rootView.addView(fieldAutoCompleteTextView);
      if(!field.isVisible && property.sValue.trim()
                                            .isEmpty())
         hideField(fieldAutoCompleteTextView, field.sName);
   }

   public void addFieldSpinner(ViewGroup rootView, Field field)
   {
      final FieldSpinner fieldSpinner = new FieldSpinner(context);

      fieldSpinner.setTitle(field.sName);
      fieldSpinner.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));

      Property property = new Property(field.iID);
      final ArrayList<Property> propertyValues = getPropertyValues(field.iID);

      for(int i = 0; i < book.alProperties.size() && (property.iID == 0 || property.iFieldTypeID != field.iID); i++)
      {
         if(book.alProperties.get(i).iFieldTypeID == field.iID)
            property = book.alProperties.get(i);
      }
      if(property.iID == 0) // The book has not such a property
         book.alProperties.add(property);

      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.spinner_item)
      {
         @NonNull
         @Override
         public View getView(int position, View convertView, @NonNull ViewGroup parent)
         {
            View view = super.getView(position, convertView, parent);
            view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()); // Removing leading pad
            if (position == 0)
               ((TextView) view.findViewById(android.R.id.text1)).setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.text, null));
            return view;
         }

         @Override
         public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
         {
            View view;
            if (position == 0)
            {
               TextView textView = new TextView(getContext());
               textView.setHeight(0);
               textView.setVisibility(View.GONE);
               view = textView;
            }
            else
               view = super.getDropDownView(position, null, parent);

            parent.setVerticalScrollBarEnabled(false);
            return view;
         }
      };
      arrayAdapter.setDropDownViewResource(R.layout.dropdown);
      arrayAdapter.add(field.sName);
      for(Property propertyOfType : propertyValues)
      {
         arrayAdapter.add(propertyOfType.sValue);
      }

      fieldSpinner.setAdapter(arrayAdapter);
      int iSelected = 0;
      for(int i = 0; i < propertyValues.size(); i++)
      {
         if(propertyValues.get(i)
                          .equals(property))
            iSelected = i + 1;
      }
      fieldSpinner.setSelection(iSelected);
      fieldSpinner.setTag(property);
      fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            if(pos > 0)
               ((Property) fieldSpinner.getTag()).copy(propertyValues.get(pos - 1));
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });

      rootView.addView(fieldSpinner);
      if(!field.isVisible && property.iID == 0)
         hideField(fieldSpinner, field.sName);
   }

   public void addFieldMultiText(ViewGroup rootView, final Field field)
   {
      final FieldMultiText fieldMultiText = new FieldMultiText(context);
      fieldMultiText.setId(View.generateViewId());
      if(vPrevious != null)
         vPrevious.setNextFocusDownId(R.id.et_author_1);
      String[] tsNames = field.sName.split("\\|");
      fieldMultiText.setTitle(tsNames.length > 1 ? tsNames[1] : field.sName);
      fieldMultiText.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldMultiText.setHint(tsNames[0]);

      final ArrayList<Property> propertyValues = getPropertyValues(field.iID, true);
      final ArrayList<Property> alItemsValues = new ArrayList<>(propertyValues);

      final FilteredArrayAdapter<Property> filteredArrayAdapter = new FilteredArrayAdapter<>(context, R.layout.dropdown, alItemsValues);

      fieldMultiText.setOnAddRemoveListener(new FieldMultiText.OnAddRemoveFieldListener()
      {
         @Override
         public void onFieldRemove(View view)
         {
            book.alProperties.remove((Property) view.getTag());
         }

         @Override
         public void onAddNewField(View view)
         {
            Property fldNew = new Property(field.iID);
            book.alProperties.add(fldNew);
            view.setTag(fldNew);
         }

         @Override
         public void onFieldUpdated(View view, String value)
         {
            boolean isExists = false;
            for(Property property : propertyValues)
            {
               if(property.sValue.trim()
                                 .equalsIgnoreCase(value.trim()))
               {
                  ((Property) view.getTag()).copy(property);
                  isExists = true;
                  break;
               }
            }
            if(!isExists)
            {
               ((Property) view.getTag()).iID = 0;
               ((Property) view.getTag()).sValue = value;
            }
         }

         @Override
         public void onItemSelect(View view, Property selection)
         {
            ((Property) view.getTag()).copy(selection);
         }
      });

      fieldMultiText.setItems(filteredArrayAdapter, book.alProperties);

      rootView.addView(fieldMultiText);
      fieldMultiText.clearFocus();

      if(!field.isVisible && hasNotPropertiesOfType(field.iID))
         hideField(fieldMultiText, field.sName);
   }

   public void addFieldMultiSpinner(ViewGroup rootView, final Field field)
   {
      final FieldMultiSpinner fieldMultiSpinner = new FieldMultiSpinner(context);
      String[] tsNames = field.sName.split("\\|");
      fieldMultiSpinner.setTitle(tsNames.length > 1 ? tsNames[1] : field.sName);
      fieldMultiSpinner.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldMultiSpinner.setHint(tsNames.length > 1 ? tsNames[1] : field.sName);

      final ArrayList<Property> propertyValues = getPropertyValues(field.iID);
      ArrayList<FieldMultiSpinner.Item> alItems = new ArrayList<>();
      for(Property property : propertyValues)
      {
         FieldMultiSpinner.Item item = new FieldMultiSpinner.Item(property.sValue);
         item.setSelected(book.alProperties.contains(property));
         alItems.add(item);
      }

      fieldMultiSpinner.setItems(alItems);
      fieldMultiSpinner.setOnUpdateListener(item -> {
         boolean isFound = false;
         for(Property propertyValue : propertyValues)
         {
            if(propertyValue.sValue.equalsIgnoreCase(item.getTitle()))
            {
               isFound = true;
               if(item.isSelected())
                  book.alProperties.add(propertyValue);
               else
                  book.alProperties.remove(propertyValue);
               break;
            }
         }
         if(!isFound)
         {
            Property newPropertyValue = new Property(field.iID, item.getTitle());
            propertyValues.add(newPropertyValue);
            book.alProperties.add(newPropertyValue);
         }
      });

      rootView.addView(fieldMultiSpinner);

      if(!field.isVisible && hasNotPropertiesOfType(field.iID))
         hideField(fieldMultiSpinner, tsNames.length > 1 ? tsNames[1] : field.sName);

   }

   @SuppressWarnings("unchecked")
   public void addFieldMoney(ViewGroup rootView, Field field)
   {
      final FieldMoney fieldMoney = new FieldMoney(context);
      fieldMoney.setTitle(field.sName);
      fieldMoney.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldMoney.setHint(field.sName);

      switch(field.iID)
      {
         case DBAdapter.FLD_PRICE:
            fieldMoney.setTag(book.csPrice);
            break;

         case DBAdapter.FLD_VALUE:
            fieldMoney.setTag(book.csValue);
            break;

         default:
            return;
      }

      final Price price = new Price(((Changeable<String>) fieldMoney.getTag()).value);
      if(price.iValue != 0)
         fieldMoney.setValue(price.iValue);

      final ArrayList<Property> alCurrencies = getPropertyValues(DBAdapter.FLD_CURRENCY);
      int iSelected = 0;
      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.spinner_item);
      for(int i = 0; i < alCurrencies.size(); i++)
      {
         arrayAdapter.add(alCurrencies.get(i).sValue);
         if(price.iCurrencyID == alCurrencies.get(i).iID)
            iSelected = i;
      }

      fieldMoney.setAdapter(arrayAdapter);
      fieldMoney.setSelection(iSelected);

      fieldMoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            price.iCurrencyID = alCurrencies.get(pos).iID;
            ((Changeable<String>) fieldMoney.getTag()).value = price.toString();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });

      fieldMoney.setUpdateListener(editText -> {
         String sValue = editText.getText()
                           .toString();
         int iValue;
         if(sValue.isEmpty() || sValue.matches("-|,|-,"))
            iValue = 0;
         else
         {
            String[] tsValue = sValue.split(String.format("\\%s", DBAdapter.separator));
//               String [] tsValue = sValue.split("\\.");

            iValue = (tsValue[0].isEmpty() ? 0 : Integer.valueOf(tsValue[0]) * 100) + (tsValue.length == 2 ?
                  (sValue.contains("-") ? -1 : 1) * (tsValue[1].length() == 1 ? 10 : 1) * Integer.valueOf(tsValue[1]) : 0);
         }

         price.iValue = iValue;
         ((Changeable<String>) fieldMoney.getTag()).value = price.toString();

      });

      rootView.addView(fieldMoney);

      if(!field.isVisible && ((Changeable<?>) fieldMoney.getTag()).isEmpty())
         hideField(fieldMoney, field.sName);

   }

   public void addFieldDate(ViewGroup rootView, Field field)
   {
      Date date;

      FieldDate fieldDate;

      switch(field.iID)
      {
         case DBAdapter.FLD_READ_DATE:
            date = new Date(book.ciReadDate.value);
            fieldDate = new FieldDate(context);
            fieldDate.setTag(book.ciReadDate);
            break;

         case DBAdapter.FLD_DUE_DATE:
            date = new Date(book.ciDueDate.value);
            fieldDate = new FieldDate(context);
            fieldDate.setTag(book.ciDueDate);
            break;

         default:
            return;

      }

      fieldDate.setTitle(field.sName);
      fieldDate.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldDate.setHint(field.sName);
      fieldDate.setDate(date);

      fieldDate.setUpdateListener(new FieldDate.OnUpdateListener()
      {
         @Override
         public void onUpdate(Date date)
         {
         }

         @SuppressWarnings("unchecked")
         @Override
         public void onUpdate(FieldDate oFieldDate)
         {
            ((Changeable<Integer>) oFieldDate.getTag()).value = oFieldDate.getDate()
                                                                          .toInt();
         }
      });
      rootView.addView(fieldDate);

      if(!field.isVisible && ((Changeable<Integer>) fieldDate.getTag()).value == 0)
         hideField(fieldDate, field.sName);

   }

   public void addFieldRating(ViewGroup rootView, Field field)
   {
      final FieldRating fieldRating = new FieldRating(context);

      fieldRating.setTitle(field.sName);
      fieldRating.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));

      final ArrayList<Property> propertyValues = getPropertyValues(field.iID);
      Property property = new Property(field.iID);

      // Looking in book property collection for property of type
      for(int i = 0; property.iID == 0 && i < book.alProperties.size(); i++)
      {
         if(field.iID == book.alProperties.get(i).iFieldTypeID)
            property = book.alProperties.get(i);
      }

      if(property.iID == 0) // The book has not such a property
         book.alProperties.add(property);
      else
      {
         float fRating = Float.parseFloat(property.sValue);
         fieldRating.setRating(fRating);
      }
      fieldRating.setTag(property);

      fieldRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
      {
         @Override
         public void onRatingChanged(RatingBar ratingBar,
                                     float rating,
                                     boolean fromUser)
         {
            String sValue = String.valueOf(rating);
            boolean isFound = false;
            for(Property propertyOfType : propertyValues)
            {
               if(propertyOfType.sValue.equalsIgnoreCase(sValue))
               {
                  isFound = true;
                  ((Property) fieldRating.getTag()).copy(propertyOfType);
                  break;
               }
            }
            if(!isFound)
            {
               ((Property) fieldRating.getTag()).iID = 0;
               ((Property) fieldRating.getTag()).sValue = sValue;
            }
         }
      });

      rootView.addView(fieldRating);

      if(!field.isVisible && property.sValue.trim().isEmpty())
         hideField(fieldRating, field.sName);
   }

   public void addFieldCheckBox(ViewGroup rootView, Field field)
   {
      final FieldCheckBox fieldCheckBox = new FieldCheckBox(context);

      fieldCheckBox.setTitle(field.sName);
      fieldCheckBox.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));

      final ArrayList<Property> propertyValues = getPropertyValues(field.iID);
      Property property = new Property(field.iID);

      // Looking in book property collection for property of type
      for(int i = 0; property.iID == 0 && i < book.alProperties.size(); i++)
      {
         if(field.iID == book.alProperties.get(i).iFieldTypeID)
            property = book.alProperties.get(i);
      }

      if(property.iID == 0) // The book has not such a property
         book.alProperties.add(property);
      else
      {
         boolean bValue = Boolean.parseBoolean(property.sValue);
         fieldCheckBox.setChecked(bValue);
      }
      fieldCheckBox.setTag(property);

      fieldCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
      {

         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            String sValue = String.valueOf(isChecked);
            boolean isFound = false;
            for(Property propertyValue : propertyValues)
            {
               if(propertyValue.sValue.equalsIgnoreCase(sValue))
               {
                  isFound = true;
                  ((Property) fieldCheckBox.getTag()).copy(propertyValue);
                  break;
               }
            }
            if(!isFound)
            {
               ((Property) fieldCheckBox.getTag()).iID = 0;
               ((Property) fieldCheckBox.getTag()).sValue = sValue;
            }
         }
      });

      rootView.addView(fieldCheckBox);

      if(!field.isVisible && property.sValue.trim().isEmpty())
         hideField(fieldCheckBox, field.sName);
   }

   public ArrayList<Property> getPropertyValues(int fieldID)
   {
      return dbAdapter.getPropertyValues(fieldID);
   }

   private ArrayList<Property> getPropertyValues(int fieldID, boolean isOrdered)
   {
      return  dbAdapter.getPropertyValues(fieldID, true);
   }

   private void hideField(View view, String name)
   {

      view.setVisibility(View.GONE);
      for(Listener listener : getListeners())
         listener.onFieldHide(view, name);

//      pmHiddenFields.getMenu()
//                    .add(Menu.NONE, pmHiddenFields.getMenu().size(), 0, sName);
//      hmHiddenFileds.put(pmHiddenFields.getMenu().getItem(pmHiddenFields.getMenu().size() - 1), view);
   }

   private boolean hasNotPropertiesOfType(int iTypeID)
   {
      boolean hasPropertiesOfType = false;
      for(Property property : book.alProperties)
      {
         if(property.iFieldTypeID == iTypeID)
         {
            hasPropertiesOfType = true;
            break;
         }
      }
      return !hasPropertiesOfType;
   }

   public void onPause()
   {}

   public void onResume()
   {}

   public interface Listener
   {
      void onFieldHide(View view, String name);
   }
}
