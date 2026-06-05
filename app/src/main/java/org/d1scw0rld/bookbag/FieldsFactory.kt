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

   private View previousView = null;

   public FieldsFactory(Context context, Book book, DBAdapter dbAdapter)
   {
      this.context = context;
      this.book = book;
      this.dbAdapter = dbAdapter;
   }

   public void addFieldText(ViewGroup rootView, Field field)
   {
      switch(field.id)
      {
         case DBAdapter.FLD_TITLE:
            addFieldText(rootView, field, book.title);
            break;

         case DBAdapter.FLD_DESCRIPTION:
            addFieldText(rootView, field, book.description);
            break;

         case DBAdapter.FLD_VOLUME:
            addFieldText(rootView, field, book.volume);
            break;

         case DBAdapter.FLD_PAGES:
            addFieldText(rootView, field, book.pages);
            break;

         case DBAdapter.FLD_EDITION:
            addFieldText(rootView, field, book.edition);
            break;

         case DBAdapter.FLD_ISBN:
            addFieldText(rootView, field, book.isbn);
            break;

         case DBAdapter.FLD_WEB:
            addFieldText(rootView, field, book.web);
            break;

         default:
      }
   }

   public  <T> void addFieldText(ViewGroup rootView, Field field, final Changeable<T> changeableValue)
   {
      final FieldEditTextUpdatableClearable fieldEditTextUpdatableClearable = new FieldEditTextUpdatableClearable(context);

      fieldEditTextUpdatableClearable.setTitle(field.name);
      fieldEditTextUpdatableClearable.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldEditTextUpdatableClearable.setText(changeableValue.toString());
      fieldEditTextUpdatableClearable.setHint(field.name);
      fieldEditTextUpdatableClearable.setInputType(field.inputType);
      if(field.id == DBAdapter.FLD_TITLE)
      {
         previousView = fieldEditTextUpdatableClearable.findViewById(R.id.editTextX);
      }
      fieldEditTextUpdatableClearable.setUpdateListener(editText -> {
         Class<?> clazz = changeableValue.getGenericType();
         Object object;
         try
         {
            Constructor<?> constructor = clazz.getConstructor(String.class);
            object = constructor.newInstance(editText.getText()
                                        .toString()
                                        .trim());

         }
         catch(NoSuchMethodException
               | SecurityException
               | InstantiationException
               | IllegalAccessException
               | IllegalArgumentException
               | InvocationTargetException e)
         {
            e.printStackTrace();
            return;
         }

         changeableValue.value = (T) object;
      });
      rootView.addView(fieldEditTextUpdatableClearable);
      if(!field.isVisible && changeableValue.isEmpty())
         hideField(fieldEditTextUpdatableClearable, field.name);
   }

   public void addAutocompleteField(ViewGroup rootView, final Field field)
   {
      final FieldAutoCompleteTextView fieldAutoCompleteTextView = new FieldAutoCompleteTextView(context);
      fieldAutoCompleteTextView.setTitle(field.name);
      fieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldAutoCompleteTextView.setHint(field.name);
      View focusDummyView = new View(context);
      focusDummyView.setNextFocusDownId(fieldAutoCompleteTextView.getId());

      final ArrayList<Property> propertyValues = getPropertyValues(field.id, true);
      Property property = new Property(field.id);

      for(int i = 0; property.id == 0 && i < book.properties.size(); i++)
      {
         if(field.id == book.properties.get(i).fieldTypeId)
            property = book.properties.get(i);
      }

      if(property.id == 0) // The book has no such property
         book.properties.add(property);
      else
         fieldAutoCompleteTextView.setText(property.value);

      fieldAutoCompleteTextView.setTag(property);

      FilteredArrayAdapter<Property> filteredArrayAdapter = new FilteredArrayAdapter<>(context, R.layout.dropdown, propertyValues);
      fieldAutoCompleteTextView.setAdapter(filteredArrayAdapter);
      fieldAutoCompleteTextView.setOnItemClickListener((adapter, view1, position, rowId) -> {
         Property selectedField = (Property) adapter.getItemAtPosition(position);
         ((Property) fieldAutoCompleteTextView.getTag()).updateFrom(selectedField);
      });
      fieldAutoCompleteTextView.setUpdateListener(editText -> {
         boolean isFound = false;
         for(Property p : propertyValues)
         {
            if(editText.getText()
                 .toString()
                 .trim()
                 .equalsIgnoreCase(p.value))
            {
               isFound = true;
               ((Property) fieldAutoCompleteTextView.getTag()).updateFrom(p);
               break;
            }
         }
         if(!isFound)
         {
            ((Property) fieldAutoCompleteTextView.getTag()).id = 0;
            ((Property) fieldAutoCompleteTextView.getTag()).value = editText.getText()
                                                                       .toString();
         }
      });

      rootView.addView(fieldAutoCompleteTextView);
      if(!field.isVisible && property.value.trim()
                                            .isEmpty())
         hideField(fieldAutoCompleteTextView, field.name);
   }

   public void addFieldSpinner(ViewGroup rootView, Field field)
   {
      final FieldSpinner fieldSpinner = new FieldSpinner(context);

      fieldSpinner.setTitle(field.name);
      fieldSpinner.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));

      Property property = new Property(field.id);
      final ArrayList<Property> propertyValues = getPropertyValues(field.id);

      for(int i = 0; i < book.properties.size() && (property.id == 0 || property.fieldTypeId != field.id); i++)
      {
         if(book.properties.get(i).fieldTypeId == field.id)
            property = book.properties.get(i);
      }
      if(property.id == 0) // The book has no such property
         book.properties.add(property);

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
      arrayAdapter.add(field.name);
      for(Property propertyOfType : propertyValues)
      {
         arrayAdapter.add(propertyOfType.value);
      }

      fieldSpinner.setAdapter(arrayAdapter);
      int selectedPosition = 0;
      for(int i = 0; i < propertyValues.size(); i++)
      {
         if(propertyValues.get(i)
                          .equals(property))
            selectedPosition = i + 1;
      }
      fieldSpinner.setSelection(selectedPosition);
      fieldSpinner.setTag(property);
      fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            if(pos > 0)
               ((Property) fieldSpinner.getTag()).updateFrom(propertyValues.get(pos - 1));
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });

      rootView.addView(fieldSpinner);
      if(!field.isVisible && property.id == 0)
         hideField(fieldSpinner, field.name);
   }

   public void addFieldMultiText(ViewGroup rootView, final Field field)
   {
      final FieldMultiText fieldMultiText = new FieldMultiText(context);
      fieldMultiText.setId(View.generateViewId());
      if(previousView != null)
         previousView.setNextFocusDownId(R.id.et_author_1);
      String[] splitNames = field.name.split("\\|");
      fieldMultiText.setTitle(splitNames.length > 1 ? splitNames[1] : field.name);
      fieldMultiText.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldMultiText.setHint(splitNames[0]);

      final ArrayList<Property> propertyValues = getPropertyValues(field.id, true);
      final ArrayList<Property> itemValues = new ArrayList<>(propertyValues);

      final FilteredArrayAdapter<Property> filteredArrayAdapter = new FilteredArrayAdapter<>(context, R.layout.dropdown, itemValues);

      fieldMultiText.setOnAddRemoveListener(new FieldMultiText.OnAddRemoveFieldListener()
      {
         @Override
         public void onFieldRemove(View view)
         {
            book.properties.remove((Property) view.getTag());
         }

         @Override
         public void onAddNewField(View view)
         {
            Property fieldNew = new Property(field.id);
            book.properties.add(fieldNew);
            view.setTag(fieldNew);
         }

         @Override
         public void onFieldUpdated(View view, String value)
         {
            boolean isExists = false;
            for(Property property : propertyValues)
            {
               if(property.value.trim()
                                 .equalsIgnoreCase(value.trim()))
               {
                  ((Property) view.getTag()).updateFrom(property);
                  isExists = true;
                  break;
               }
            }
            if(!isExists)
            {
               ((Property) view.getTag()).id = 0;
               ((Property) view.getTag()).value = value;
            }
         }

         @Override
         public void onItemSelect(View view, Property selection)
         {
            ((Property) view.getTag()).updateFrom(selection);
         }
      });

      fieldMultiText.setItems(filteredArrayAdapter, book.properties);

      rootView.addView(fieldMultiText);
      fieldMultiText.clearFocus();

      if(!field.isVisible && hasNotPropertiesOfType(field.id))
         hideField(fieldMultiText, field.name);
   }

   public void addFieldMultiSpinner(ViewGroup rootView, final Field field)
   {
      final FieldMultiSpinner fieldMultiSpinner = new FieldMultiSpinner(context);
      String[] splitNames = field.name.split("\\|");
      fieldMultiSpinner.setTitle(splitNames.length > 1 ? splitNames[1] : field.name);
      fieldMultiSpinner.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldMultiSpinner.setHint(splitNames.length > 1 ? splitNames[1] : field.name);

      final ArrayList<Property> propertyValues = getPropertyValues(field.id);
      ArrayList<FieldMultiSpinner.Item> spinnerItems = new ArrayList<>();
      for(Property property : propertyValues)
      {
         FieldMultiSpinner.Item item = new FieldMultiSpinner.Item(property.value);
         item.setSelected(book.properties.contains(property));
         spinnerItems.add(item);
      }

      fieldMultiSpinner.setItems(spinnerItems);
      fieldMultiSpinner.setOnUpdateListener(item -> {
         boolean isFound = false;
         for(Property propertyValue : propertyValues)
         {
            if(propertyValue.value.equalsIgnoreCase(item.getTitle()))
            {
               isFound = true;
               if(item.isSelected())
                  book.properties.add(propertyValue);
               else
                  book.properties.remove(propertyValue);
               break;
            }
         }
         if(!isFound)
         {
            Property newProperty = new Property(field.id, item.getTitle());
            propertyValues.add(newProperty);
            book.properties.add(newProperty);
         }
      });

      rootView.addView(fieldMultiSpinner);

      if(!field.isVisible && hasNotPropertiesOfType(field.id))
         hideField(fieldMultiSpinner, splitNames.length > 1 ? splitNames[1] : field.name);

   }

   @SuppressWarnings("unchecked")
   public void addFieldMoney(ViewGroup rootView, Field field)
   {
      final FieldMoney fieldMoney = new FieldMoney(context);
      fieldMoney.setTitle(field.name);
      fieldMoney.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldMoney.setHint(field.name);

      switch(field.id)
      {
         case DBAdapter.FLD_PRICE:
            fieldMoney.setTag(book.price);
            break;

         case DBAdapter.FLD_VALUE:
            fieldMoney.setTag(book.value);
            break;

         default:
            return;
      }

      final Price price = new Price(((Changeable<String>) fieldMoney.getTag()).value);
      if(price.value != 0)
         fieldMoney.setValue(price.value);

      final ArrayList<Property> currencies = getPropertyValues(DBAdapter.FLD_CURRENCY);
      int selectedPosition = 0;
      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.spinner_item);
      for(int i = 0; i < currencies.size(); i++)
      {
         arrayAdapter.add(currencies.get(i).value);
         if(price.currencyId == currencies.get(i).id)
            selectedPosition = i;
      }

      fieldMoney.setAdapter(arrayAdapter);
      fieldMoney.setSelection(selectedPosition);

      fieldMoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            price.currencyId = currencies.get(pos).id;
            ((Changeable<String>) fieldMoney.getTag()).value = price.toString();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });

      fieldMoney.setUpdateListener(editText -> {
         String valueStr = editText.getText()
                           .toString();
         int intValue;
         if(valueStr.isEmpty() || valueStr.matches("-|,|-,"))
            intValue = 0;
         else
         {
            String[] valueParts = valueStr.split(String.format("\\%s", DBAdapter.separator));

            intValue = (valueParts[0].isEmpty() ? 0 : Integer.valueOf(valueParts[0]) * 100) + (valueParts.length == 2 ?
                  (valueStr.contains("-") ? -1 : 1) * (valueParts[1].length() == 1 ? 10 : 1) * Integer.valueOf(valueParts[1]) : 0);
         }

         price.value = intValue;
         ((Changeable<String>) fieldMoney.getTag()).value = price.toString();

      });

      rootView.addView(fieldMoney);

      if(!field.isVisible && ((Changeable<?>) fieldMoney.getTag()).isEmpty())
         hideField(fieldMoney, field.name);

   }

   public void addFieldDate(ViewGroup rootView, Field field)
   {
      Date date;

      FieldDate fieldDate;

      switch(field.id)
      {
         case DBAdapter.FLD_READ_DATE:
            date = new Date(book.readDate.value);
            fieldDate = new FieldDate(context);
            fieldDate.setTag(book.readDate);
            break;

         case DBAdapter.FLD_DUE_DATE:
            date = new Date(book.dueDate.value);
            fieldDate = new FieldDate(context);
            fieldDate.setTag(book.dueDate);
            break;

         default:
            return;

      }

      fieldDate.setTitle(field.name);
      fieldDate.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
      fieldDate.setHint(field.name);
      fieldDate.setDate(date);

      fieldDate.setUpdateListener(new FieldDate.OnUpdateListener()
      {
         @Override
         public void onUpdate(Date date)
         {
         }

         @SuppressWarnings("unchecked")
         @Override
         public void onUpdate(FieldDate fieldDateObj)
         {
            ((Changeable<Integer>) fieldDateObj.getTag()).value = fieldDateObj.getDate()
                                                                          .toInt();
         }
      });
      rootView.addView(fieldDate);

      if(!field.isVisible && ((Changeable<Integer>) fieldDate.getTag()).value == 0)
         hideField(fieldDate, field.name);

   }

   public void addFieldRating(ViewGroup rootView, Field field)
   {
      final FieldRating fieldRating = new FieldRating(context);

      fieldRating.setTitle(field.name);
      fieldRating.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));

      final ArrayList<Property> propertyValues = getPropertyValues(field.id);
      Property property = new Property(field.id);

      // Looking in book property collection for property of type
      for(int i = 0; property.id == 0 && i < book.properties.size(); i++)
      {
         if(field.id == book.properties.get(i).fieldTypeId)
            property = book.properties.get(i);
      }

      if(property.id == 0) // The book has no such property
         book.properties.add(property);
      else
      {
         float ratingValue = Float.parseFloat(property.value);
         fieldRating.setRating(ratingValue);
      }
      fieldRating.setTag(property);

      fieldRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
      {
         @Override
         public void onRatingChanged(RatingBar ratingBar,
                                     float rating,
                                     boolean fromUser)
         {
            String ratingStr = String.valueOf(rating);
            boolean isFound = false;
            for(Property propertyOfType : propertyValues)
            {
               if(propertyOfType.value.equalsIgnoreCase(ratingStr))
               {
                  isFound = true;
                  ((Property) fieldRating.getTag()).updateFrom(propertyOfType);
                  break;
               }
            }
            if(!isFound)
            {
               ((Property) fieldRating.getTag()).id = 0;
               ((Property) fieldRating.getTag()).value = ratingStr;
            }
         }
      });

      rootView.addView(fieldRating);

      if(!field.isVisible && property.value.trim().isEmpty())
         hideField(fieldRating, field.name);
   }

   public void addFieldCheckBox(ViewGroup rootView, Field field)
   {
      final FieldCheckBox fieldCheckBox = new FieldCheckBox(context);

      fieldCheckBox.setTitle(field.name);
      fieldCheckBox.setTitleColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));

      final ArrayList<Property> propertyValues = getPropertyValues(field.id);
      Property property = new Property(field.id);

      // Looking in book property collection for property of type
      for(int i = 0; property.id == 0 && i < book.properties.size(); i++)
      {
         if(field.id == book.properties.get(i).fieldTypeId)
            property = book.properties.get(i);
      }

      if(property.id == 0) // The book has no such property
         book.properties.add(property);
      else
      {
         boolean checkedValue = Boolean.parseBoolean(property.value);
         fieldCheckBox.setChecked(checkedValue);
      }
      fieldCheckBox.setTag(property);

      fieldCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
      {

         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            String checkedStr = String.valueOf(isChecked);
            boolean isFound = false;
            for(Property propertyValue : propertyValues)
            {
               if(propertyValue.value.equalsIgnoreCase(checkedStr))
               {
                  isFound = true;
                  ((Property) fieldCheckBox.getTag()).updateFrom(propertyValue);
                  break;
               }
            }
            if(!isFound)
            {
               ((Property) fieldCheckBox.getTag()).id = 0;
               ((Property) fieldCheckBox.getTag()).value = checkedStr;
            }
         }
      });

      rootView.addView(fieldCheckBox);

      if(!field.isVisible && property.value.trim().isEmpty())
         hideField(fieldCheckBox, field.name);
   }

   public ArrayList<Property> getPropertyValues(int fieldId)
   {
      return dbAdapter.getPropertyValues(fieldId);
   }

   private ArrayList<Property> getPropertyValues(int fieldId, boolean isOrdered)
   {
      return  dbAdapter.getPropertyValues(fieldId, true);
   }

   private void hideField(View view, String name)
   {
      view.setVisibility(View.GONE);
      for(Listener listener : getListeners())
         listener.onFieldHide(view, name);
   }

   private boolean hasNotPropertiesOfType(int typeId)
   {
      boolean hasPropertiesOfType = false;
      for(Property property : book.properties)
      {
         if(property.fieldTypeId == typeId)
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
