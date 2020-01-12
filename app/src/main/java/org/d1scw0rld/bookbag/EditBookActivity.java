package org.d1scw0rld.bookbag;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.Toolbar;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Changeable;
import org.d1scw0rld.bookbag.dto.Date;
import org.d1scw0rld.bookbag.dto.Property;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.dto.Price;
import org.d1scw0rld.bookbag.dto.Utils;
import org.d1scw0rld.bookbag.fields.AutoCompleteTextViewX;
import org.d1scw0rld.bookbag.fields.EditTextX;
import org.d1scw0rld.bookbag.fields.FieldAutoCompleteTextView;
import org.d1scw0rld.bookbag.fields.FieldCheckBox;
import org.d1scw0rld.bookbag.fields.FieldDate;
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable;
import org.d1scw0rld.bookbag.fields.FieldMoney;
import org.d1scw0rld.bookbag.fields.FieldMultiSpinner;
import org.d1scw0rld.bookbag.fields.FieldMultiText;
import org.d1scw0rld.bookbag.fields.FieldRating;
import org.d1scw0rld.bookbag.fields.FieldSpinner;
import org.d1scw0rld.bookbag.fields.FieldMultiSpinner.Item;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class EditBookActivity extends AppCompatActivity
{
   public final static String BOOK_ID = "book_id",
                              IS_COPY = "is_copy";

   private Book book;

   private DBAdapter dbAdapter = null;

   private PopupMenu pmHiddenFields = null;

   private FieldEditTextUpdatableClearable fBookTitle = null;

   private View vPrevious = null;

   HashMap<MenuItem, View> hmHiddenFileds = new HashMap<>();

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_edit_book);
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

      // BEGIN_INCLUDE (inflate_set_custom_view)
      // Inflate a "Done" custom action bar view to serve as the "Up" affordance.
      // Show the custom action bar view and hide the normal Home icon and title.

      final ActionBar actionBar = getSupportActionBar();
      assert actionBar != null;

      actionBar.setDisplayShowHomeEnabled(false);
      actionBar.setDisplayShowTitleEnabled(false);
      actionBar.setDisplayShowCustomEnabled(true);
      actionBar.setCustomView(R.layout.actionbar_custom_view_done);

      ((Toolbar) actionBar.getCustomView()
                          .getParent()).setContentInsetsAbsolute(0, 0);
      actionBar.getCustomView()
               .findViewById(R.id.actionbar_done)
               .setOnClickListener(new View.OnClickListener()
               {
                  @Override
                  public void onClick(View v)
                  {
                     assert getCurrentFocus() != null;
                     getCurrentFocus().clearFocus();
                     v.requestFocus();
                     if(book.csTitle.value.trim()
                                          .isEmpty())
                     {
                        fBookTitle.setError(getResources().getString(R.string.err_emp_ttl));
                     }
                     else
                     {
                        fBookTitle.setError(null);

                        saveBook();
                        setResult(RESULT_OK, new Intent());
                        finish();                  // "Done"
                     }
                  }
               });
      // END_INCLUDE (inflate_set_custom_view)

      dbAdapter = new DBAdapter(this);
      dbAdapter.open();

      Bundle extras = getIntent().getExtras();
      if(extras == null)
         return;

      long iBookID = extras.getLong(BOOK_ID);
      boolean isCopy = extras.getBoolean(IS_COPY, false);

      if(iBookID != 0)
      {
         book = dbAdapter.getBook(iBookID);
         if(isCopy)
            book.iID = 0;
      }
      else
         book = new Book();

      Button btnAddField = findViewById(R.id.btn_add_field);
      btnAddField.setOnClickListener(new View.OnClickListener()
      {

         @Override
         public void onClick(View v)
         {
            pmHiddenFields.show();
         }
      });

      pmHiddenFields = new PopupMenu(this, btnAddField);
      pmHiddenFields.setOnMenuItemClickListener(new OnMenuItemClickListener()
      {

         @Override
         public boolean onMenuItemClick(MenuItem menuItem)
         {
            View view = hmHiddenFileds.get(menuItem);
            assert view != null;
            view.setVisibility(View.VISIBLE);
            view.requestFocus();
            pmHiddenFields.getMenu()
                          .removeItem(menuItem.getItemId());

            return false;
         }
      });

      LinearLayout llFields = findViewById(R.id.ll_fields);

      for(Field field : DBAdapter.FIELDS)
      {
         switch(field.iType)
         {
            case Field.TYPE_TEXT:
               addFieldText(llFields, field);
               break;

            case Field.TYPE_MULTIFIELD:
               addFieldMultiText(llFields, field);
               break;

            case Field.TYPE_TEXT_AUTOCOMPLETE:
               addAutocompleteField(llFields, field);
               break;

            case Field.TYPE_SPINNER:
               addFieldSpinner(llFields, field);
               break;

            case Field.TYPE_MULTI_SPINNER:
               addFieldMultiSpinner(llFields, field);
               break;

            case Field.TYPE_MONEY:
               addFieldMoney(llFields, field);
               break;

            case Field.TYPE_DATE:
               addFieldDate(llFields, field);
               break;

            case Field.TYPE_RATING:
               addFieldRating(llFields, field);
               break;

            case Field.TYPE_CHECK_BOX:
               addFieldCheckBox(llFields, field);
               break;
         }
      }
   }

   @Override
   protected void onPause()
   {
      dbAdapter.close();

      super.onPause();
   }

   @Override
   protected void onResume()
   {
      super.onResume();

      dbAdapter.open();
   }

   // BEGIN_INCLUDE (handle_cancel)
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.cancel, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(item.getItemId() == R.id.cancel)
      {
         setResult(RESULT_CANCELED, new Intent());
         finish();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
   // END_INCLUDE (handle_cancel)

   @Override
   public void onBackPressed()
   {
      setResult(RESULT_CANCELED, new Intent());
      finish();
   }

   private void saveBook()
   {
      // Clear empty fields
      for(int i = book.alProperties.size() - 1; i >= 0; i--)
      {
         if(book.alProperties.get(i).sValue.trim()
                                           .isEmpty())
            book.alProperties.remove(i);
      }

      if(book.iID != 0)
         dbAdapter.updateBook(book);
      else
         dbAdapter.insertBook(book);
   }

   private void addFieldText(LinearLayout rootView, Field field)
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
      final FieldEditTextUpdatableClearable fieldEditTextUpdatableClearable = new FieldEditTextUpdatableClearable(this);

      fieldEditTextUpdatableClearable.setTitle(field.sName);
      fieldEditTextUpdatableClearable.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
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
      final FieldAutoCompleteTextView fieldAutoCompleteTextView = new FieldAutoCompleteTextView(this);
      fieldAutoCompleteTextView.setTitle(field.sName);
      fieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldAutoCompleteTextView.setHint(field.sName);
      View view = new View(this);
      view.setNextFocusDownId(fieldAutoCompleteTextView.getId());

      final ArrayList<Property> propertyValues = dbAdapter.getPropertyValues(field.iID, true);
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

//      FilteredArrayAdapter filteredArrayAdapter = new FilteredArrayAdapter(this, android.R.layout.select_dialog_item, propertyValues);
      FilteredArrayAdapter<Property> filteredArrayAdapter = new FilteredArrayAdapter<>(this, R.layout.dropdown, propertyValues);
//      FilteredArrayAdapter filteredArrayAdapter = new FilteredArrayAdapter(this, android.R.layout.simple_expandable_list_item_2, propertyValues);
      fieldAutoCompleteTextView.setAdapter(filteredArrayAdapter);
      fieldAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener()
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
            for(Property p : propertyValues)
            {
               if(et.getText()
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

   private void addFieldSpinner(ViewGroup rootView, Field field)
   {
      final FieldSpinner fieldSpinner = new FieldSpinner(this);

      fieldSpinner.setTitle(field.sName);
      fieldSpinner.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      Property property = new Property(field.iID);
      final ArrayList<Property> propertyValues = dbAdapter.getPropertyValues(field.iID);

      for(int i = 0; i < book.alProperties.size() && (property.iID == 0 || property.iFieldTypeID != field.iID); i++)
      {
         if(book.alProperties.get(i).iFieldTypeID == field.iID)
            property = book.alProperties.get(i);
      }
      if(property.iID == 0) // The book has not such a property
         book.alProperties.add(property);

      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item)
//      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this, R.layout.dropdown)
      {
         @NonNull
         @Override
         public View getView(int position, View convertView, @NonNull ViewGroup parent)
         {
            View view = super.getView(position, convertView, parent);
            view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()); // Removing leading pad
            if(position == 0)
               ((TextView) view.findViewById(android.R.id.text1)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.text, null));
            return view;
         }

         @Override
         public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
         {
            View view;
            if(position == 0)
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
//      arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
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

   private void addFieldMultiText(ViewGroup rootView, final Field field)
   {
      final FieldMultiText fieldMultiText = new FieldMultiText(this);
      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
         fieldMultiText.setId(Utils.generateViewId());
      else
         fieldMultiText.setId(View.generateViewId());
      if(vPrevious != null)
         vPrevious.setNextFocusDownId(R.id.et_author_1);
      String[] tsNames = field.sName.split("\\|");
      fieldMultiText.setTitle(tsNames.length > 1 ? tsNames[1] : field.sName);
      fieldMultiText.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldMultiText.setHint(tsNames[0]);

      final ArrayList<Property> propertyValues = dbAdapter.getPropertyValues(field.iID, true);
      final ArrayList<Property> alItemsValues = new ArrayList<>(propertyValues);

//      final ArrayItemsAdapter filteredArrayAdapter = new ArrayItemsAdapter(this, R.layout.dropdown, alItemsValues);
      final FilteredArrayAdapter<Property> filteredArrayAdapter = new FilteredArrayAdapter<>(this, R.layout.dropdown, alItemsValues);

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

   private void addFieldMultiSpinner(ViewGroup rootView, final Field field)
   {
      final FieldMultiSpinner fieldMultiSpinner = new FieldMultiSpinner(this);
      String[] tsNames = field.sName.split("\\|");
      fieldMultiSpinner.setTitle(tsNames.length > 1 ? tsNames[1] : field.sName);
      fieldMultiSpinner.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldMultiSpinner.setHint(tsNames.length > 1 ? tsNames[1] : field.sName);

      final ArrayList<Property> propertyValues = dbAdapter.getPropertyValues(field.iID);
      ArrayList<Item> alItems = new ArrayList<>();
      for(Property property : propertyValues)
      {
         Item item = new Item(property.sValue);
         item.setSelected(book.alProperties.contains(property));
         alItems.add(item);
      }

      fieldMultiSpinner.setItems(alItems);
      fieldMultiSpinner.setOnUpdateListener(new FieldMultiSpinner.OnUpdateListener()
      {
         @Override
         public void onUpdate(Item item)
         {
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
         }
      });

      rootView.addView(fieldMultiSpinner);

      if(!field.isVisible && hasNotPropertiesOfType(field.iID))
         hideField(fieldMultiSpinner, tsNames.length > 1 ? tsNames[1] : field.sName);

   }

   @SuppressWarnings("unchecked")
   private void addFieldMoney(ViewGroup rootView, Field field)
   {
      final FieldMoney fieldMoney = new FieldMoney(this);
      fieldMoney.setTitle(field.sName);
      fieldMoney.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
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

      final ArrayList<Property> alCurrencies = dbAdapter.getPropertyValues(DBAdapter.FLD_CURRENCY);
      int iSelected = 0;
      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item);
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

      fieldMoney.setUpdateListener(new EditTextX.OnUpdateListener()
      {

         @Override
         public void onUpdate(EditText et)
         {
            String sValue = et.getText()
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

         }
      });

      rootView.addView(fieldMoney);

      if(!field.isVisible && ((Changeable<?>) fieldMoney.getTag()).isEmpty())
         hideField(fieldMoney, field.sName);

   }

   private void addFieldDate(ViewGroup rootView, Field field)
   {
      Date date;

      FieldDate fieldDate;

      switch(field.iID)
      {
         case DBAdapter.FLD_READ_DATE:
            date = new Date(book.ciReadDate.value);
            fieldDate = new FieldDate(this);
            fieldDate.setTag(book.ciReadDate);
         break;

         case DBAdapter.FLD_DUE_DATE:
            date = new Date(book.ciDueDate.value);
            fieldDate = new FieldDate(this);
            fieldDate.setTag(book.ciDueDate);
         break;

         default:
            return;

      }

      fieldDate.setTitle(field.sName);
      fieldDate.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
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

   private void addFieldRating(ViewGroup rootView, Field field)
   {
      final FieldRating fieldRating = new FieldRating(this);

      fieldRating.setTitle(field.sName);
      fieldRating.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      final ArrayList<Property> propertyValues = dbAdapter.getPropertyValues(field.iID);
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

      fieldRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener()
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

   private void addFieldCheckBox(LinearLayout rootView, Field field)
   {
      final FieldCheckBox fieldCheckBox = new FieldCheckBox(this);

      fieldCheckBox.setTitle(field.sName);
      fieldCheckBox.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      final ArrayList<Property> propertyValues = dbAdapter.getPropertyValues(field.iID);
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

      fieldCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
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

   private void hideField(View view, String sName)
   {

      view.setVisibility(View.GONE);

      pmHiddenFields.getMenu()
                    .add(Menu.NONE, pmHiddenFields.getMenu().size(),0, sName);
      hmHiddenFileds.put(pmHiddenFields.getMenu().getItem(pmHiddenFields.getMenu().size() - 1), view);
   }

   private boolean hasNotPropertiesOfType(int iTypeID)
   {
      boolean hasProperitesOfType = false;
      for(Property property : book.alProperties)
      {
         if(property.iFieldTypeID == iTypeID)
         {
            hasProperitesOfType = true;
            break;
         }
      }
      return !hasProperitesOfType;
   }
}
