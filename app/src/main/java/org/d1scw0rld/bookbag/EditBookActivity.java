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
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.dto.FieldType;
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
         IS_COPY                      = "is_copy";

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

      for(FieldType fieldType : DBAdapter.FIELD_TYPES)
      {
         switch(fieldType.iType)
         {
            case FieldType.TYPE_TEXT:
               addFieldText(llFields, fieldType);
               break;

            case FieldType.TYPE_MULTIFIELD:
               addFieldMultiText(llFields, fieldType);
               break;

            case FieldType.TYPE_TEXT_AUTOCOMPLETE:
               addAutocompleteField(llFields, fieldType);
               break;

            case FieldType.TYPE_SPINNER:
               addFieldSpinner(llFields, fieldType);
               break;

            case FieldType.TYPE_MULTI_SPINNER:
               addFieldMultiSpinner(llFields, fieldType);
               break;

            case FieldType.TYPE_MONEY:
               addFieldMoney(llFields, fieldType);
               break;

            case FieldType.TYPE_DATE:
               addFieldDate(llFields, fieldType);
               break;

            case FieldType.TYPE_RATING:
               addFieldRating(llFields, fieldType);
               break;

            case FieldType.TYPE_CHECK_BOX:
               addFieldCheckBox(llFields, fieldType);
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
      for(int i = book.alFields.size() - 1; i >= 0; i--)
      {
         if(book.alFields.get(i).sValue.trim()
                                       .isEmpty())
            book.alFields.remove(i);
      }

      if(book.iID != 0)
         dbAdapter.updateBook(book);
      else
         dbAdapter.insertBook(book);
   }

   private void addFieldText(LinearLayout rootView, FieldType fieldType)
   {
      switch(fieldType.iID)
      {
         case DBAdapter.FLD_TITLE:
            addFieldText(rootView, fieldType, book.csTitle);
            break;

         case DBAdapter.FLD_DESCRIPTION:
            addFieldText(rootView, fieldType, book.csDescription);
            break;

         case DBAdapter.FLD_VOLUME:
            addFieldText(rootView, fieldType, book.ciVolume);
            break;

         case DBAdapter.FLD_PAGES:
            addFieldText(rootView, fieldType, book.ciPages);
            break;

         case DBAdapter.FLD_EDITION:
            addFieldText(rootView, fieldType, book.ciEdition);
            break;

         case DBAdapter.FLD_ISBN:
            addFieldText(rootView, fieldType, book.csISBN);
            break;

         case DBAdapter.FLD_WEB:
            addFieldText(rootView, fieldType, book.csWeb);
            break;

         default:
      }
   }

   private <T> void addFieldText(ViewGroup rootView, FieldType fieldType, final Changeable<T> cValue)
   {
      final FieldEditTextUpdatableClearable fieldEditTextUpdatableClearable = new FieldEditTextUpdatableClearable(this);

      fieldEditTextUpdatableClearable.setTitle(fieldType.sName);
      fieldEditTextUpdatableClearable.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldEditTextUpdatableClearable.setText(cValue.toString());
      fieldEditTextUpdatableClearable.setHint(fieldType.sName);
      fieldEditTextUpdatableClearable.setInputType(fieldType.iInputType);
      if(fieldType.iID == DBAdapter.FLD_TITLE)
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
      if(!fieldType.isVisible && cValue.isEmpty())
         hideField(fieldEditTextUpdatableClearable, fieldType.sName);
   }

   private void addAutocompleteField(ViewGroup rootView, final FieldType fieldType)
   {
      final FieldAutoCompleteTextView fieldAutoCompleteTextView = new FieldAutoCompleteTextView(this);
      fieldAutoCompleteTextView.setTitle(fieldType.sName);
      fieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldAutoCompleteTextView.setHint(fieldType.sName);
      View view = new View(this);
      view.setNextFocusDownId(fieldAutoCompleteTextView.getId());

      final ArrayList<Field> alFieldsOfType = dbAdapter.getFieldValues(fieldType.iID, true);
      Field field = new Field(fieldType.iID);

      for(int i = 0; field.iID == 0 && i < book.alFields.size(); i++)
      {
         if(fieldType.iID == book.alFields.get(i).iTypeID)
            field = book.alFields.get(i);
      }

      if(field.iID == 0) // The book has not such a field
         book.alFields.add(field);
      else
         fieldAutoCompleteTextView.setText(field.sValue);

      fieldAutoCompleteTextView.setTag(field);

//      FilteredArrayAdapter oArrayAdapter = new FilteredArrayAdapter(this, android.R.layout.select_dialog_item, alFieldsOfType);
      FilteredArrayAdapter oArrayAdapter = new FilteredArrayAdapter(this, R.layout.dropdown, alFieldsOfType);
//      FilteredArrayAdapter oArrayAdapter = new FilteredArrayAdapter(this, android.R.layout.simple_expandable_list_item_2, alFieldsOfType);
      fieldAutoCompleteTextView.setAdapter(oArrayAdapter);
      fieldAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener()
      {
         public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId)
         {
            Field fldSelected = (Field) adapter.getItemAtPosition(position);
            ((Field) fieldAutoCompleteTextView.getTag()).copy(fldSelected);
         }
      });
      fieldAutoCompleteTextView.setUpdateListener(new AutoCompleteTextViewX.OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText et)
         {
            boolean isFound = false;
            for(Field f : alFieldsOfType)
            {
               if(et.getText()
                    .toString()
                    .trim()
                    .equalsIgnoreCase(f.sValue))
               {
                  isFound = true;
                  ((Field) fieldAutoCompleteTextView.getTag()).copy(f);
                  break;
               }
            }
            if(!isFound)
            {
               ((Field) fieldAutoCompleteTextView.getTag()).iID = 0;
               ((Field) fieldAutoCompleteTextView.getTag()).sValue = et.getText()
                                                                       .toString();
            }
         }
      });

      rootView.addView(fieldAutoCompleteTextView);
      if(!fieldType.isVisible && field.sValue.trim()
                                             .isEmpty())
         hideField(fieldAutoCompleteTextView, fieldType.sName);
   }

   private void addFieldSpinner(ViewGroup rootView, FieldType fieldType)
   {
      final FieldSpinner fieldSpinner = new FieldSpinner(this);

      fieldSpinner.setTitle(fieldType.sName);
      fieldSpinner.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      Field field = new Field(fieldType.iID);
      final ArrayList<Field> alFieldsOfType = dbAdapter.getFieldValues(fieldType.iID);

      for(int i = 0; i < book.alFields.size() && (field.iID == 0 || field.iTypeID != fieldType.iID); i++)
      {
         if(book.alFields.get(i).iTypeID == fieldType.iID)
            field = book.alFields.get(i);
      }
      if(field.iID == 0) // The book has not such a field
         book.alFields.add(field);

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
//            ((TextView)view.findViewById(R.id.text1)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.text, null));

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
      arrayAdapter.add(fieldType.sName);
      for(Field fieldOfType : alFieldsOfType)
      {
         arrayAdapter.add(fieldOfType.sValue);
      }

      fieldSpinner.setAdapter(arrayAdapter);
      int iSelected = 0;
      for(int i = 0; i < alFieldsOfType.size(); i++)
      {
         if(alFieldsOfType.get(i)
                         .equals(field))
            iSelected = i + 1;
      }
      fieldSpinner.setSelection(iSelected);
      fieldSpinner.setTag(field);
      fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            if(pos > 0)
               ((Field) fieldSpinner.getTag()).copy(alFieldsOfType.get(pos - 1));
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });

      rootView.addView(fieldSpinner);
      if(!fieldType.isVisible && field.iID == 0)
         hideField(fieldSpinner, fieldType.sName);
   }

   private void addFieldMultiText(ViewGroup rootView, final FieldType fieldType)
   {
      final FieldMultiText fieldMultiText = new FieldMultiText(this);
      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
         fieldMultiText.setId(Utils.generateViewId());
      else
         fieldMultiText.setId(View.generateViewId());
      if(vPrevious != null)
         vPrevious.setNextFocusDownId(R.id.et_author_1);
      String[] tsNames = fieldType.sName.split("\\|");
      fieldMultiText.setTitle(tsNames.length > 1 ? tsNames[1] : fieldType.sName);
      fieldMultiText.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldMultiText.setHint(tsNames[0]);

      // Set adapter
      final ArrayList<Field> alFieldsOfType = dbAdapter.getFieldValues(fieldType.iID, true);
      final ArrayList<Field> alItemsValues = new ArrayList<>(alFieldsOfType);

//      final ArrayItemsAdapter filteredArrayAdapter = new ArrayItemsAdapter(this, android.R.layout.select_dialog_item, alItemsValues);
//      final ArrayItemsAdapter filteredArrayAdapter = new ArrayItemsAdapter(this, R.layout.dropdown, alItemsValues);
      final FilteredArrayAdapter<Field> filteredArrayAdapter = new FilteredArrayAdapter<>(this, R.layout.dropdown, alItemsValues);

      fieldMultiText.setOnAddRemoveListener(new FieldMultiText.OnAddRemoveFieldListener()
      {
         @Override
         public void onFieldRemove(View view)
         {
            book.alFields.remove(view.getTag());
         }

         @Override
         public void onAddNewField(View view)
         {
            Field fldNew = new Field(fieldType.iID);
            book.alFields.add(fldNew);
            view.setTag(fldNew);
         }

         @Override
         public void onFieldUpdated(View view, String value)
         {
            boolean isExists = false;
            for(Field field : alFieldsOfType)
            {
               if(field.sValue.trim()
                              .equalsIgnoreCase(value.trim()))
               {
                  ((Field) view.getTag()).copy(field);
                  isExists = true;
                  break;
               }
            }
            if(!isExists)
            {
               ((Field) view.getTag()).iID = 0;
               ((Field) view.getTag()).sValue = value;
            }
         }

         @Override
         public void onItemSelect(View view, Field selection)
         {
            ((Field) view.getTag()).copy(selection);
         }
      });

      fieldMultiText.setItems(filteredArrayAdapter, book.alFields);

      rootView.addView(fieldMultiText);
      fieldMultiText.clearFocus();

      if(!fieldType.isVisible && hasNotFieldsOfType(fieldType.iID))
         hideField(fieldMultiText, fieldType.sName);
   }

   private void addFieldMultiSpinner(ViewGroup rootView, final FieldType fieldType)
   {
      final FieldMultiSpinner fieldMultiSpinner = new FieldMultiSpinner(this);
      String[] tsNames = fieldType.sName.split("\\|");
      fieldMultiSpinner.setTitle(tsNames.length > 1 ? tsNames[1] : fieldType.sName);
      fieldMultiSpinner.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldMultiSpinner.setHint(tsNames.length > 1 ? tsNames[1] : fieldType.sName);

      final ArrayList<Field> alFieldsOfType = dbAdapter.getFieldValues(fieldType.iID);
      ArrayList<Item> alItems = new ArrayList<>();
      for(Field filedOfType : alFieldsOfType)
      {
         Item item = new Item(filedOfType.sValue);
         item.setSelected(book.alFields.contains(filedOfType));
         alItems.add(item);
      }

      fieldMultiSpinner.setItems(alItems);
      fieldMultiSpinner.setOnUpdateListener(new FieldMultiSpinner.OnUpdateListener()
      {
         @Override
         public void onUpdate(Item item)
         {
            boolean isFound = false;
            for(Field fieldValue : alFieldsOfType)
            {
               if(fieldValue.sValue.equalsIgnoreCase(item.getTitle()))
               {
                  isFound = true;
                  if(item.isSelected())
                     book.alFields.add(fieldValue);
                  else
                     book.alFields.remove(fieldValue);
                  break;
               }
            }
            if(!isFound)
            {
               Field newfieldvalue = new Field(fieldType.iID, item.getTitle());
               alFieldsOfType.add(newfieldvalue);
               book.alFields.add(newfieldvalue);
            }
         }
      });

      rootView.addView(fieldMultiSpinner);

      if(!fieldType.isVisible && hasNotFieldsOfType(fieldType.iID))
         hideField(fieldMultiSpinner, tsNames.length > 1 ? tsNames[1] : fieldType.sName);

   }

   @SuppressWarnings("unchecked")
   private void addFieldMoney(ViewGroup rootView, FieldType fieldType)
   {
      final FieldMoney fieldMoney = new FieldMoney(this);
      fieldMoney.setTitle(fieldType.sName);
      fieldMoney.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldMoney.setHint(fieldType.sName);

      switch(fieldType.iID)
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

      final ArrayList<Field> alCurrencies = dbAdapter.getFieldValues(DBAdapter.FLD_CURRENCY);
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

      if(!fieldType.isVisible && ((Changeable<?>) fieldMoney.getTag()).isEmpty())
         hideField(fieldMoney, fieldType.sName);

   }

   private void addFieldDate(ViewGroup rootView, FieldType fieldType)
   {
      Date date;

      FieldDate fieldDate;

      switch(fieldType.iID)
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

      fieldDate.setTitle(fieldType.sName);
      fieldDate.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      fieldDate.setHint(fieldType.sName);
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

      if(!fieldType.isVisible && ((Changeable<Integer>) fieldDate.getTag()).value == 0)
         hideField(fieldDate, fieldType.sName);

   }

   private void addFieldRating(ViewGroup rootView, FieldType fieldType)
   {
      final FieldRating fieldRating = new FieldRating(this);

      fieldRating.setTitle(fieldType.sName);
      fieldRating.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      final ArrayList<Field> alFieldsOfType = dbAdapter.getFieldValues(fieldType.iID);
      Field field = new Field(fieldType.iID);

      // Looking in book field collection for field of type 
      for(int i = 0; field.iID == 0 && i < book.alFields.size(); i++)
      {
         if(fieldType.iID == book.alFields.get(i).iTypeID)
            field = book.alFields.get(i);
      }

      if(field.iID == 0) // The book has not such a field
         book.alFields.add(field);
      else
      {
         float fRating = Float.parseFloat(field.sValue);
         fieldRating.setRating(fRating);
      }
      fieldRating.setTag(field);

      fieldRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener()
      {
         @Override
         public void onRatingChanged(RatingBar ratingBar,
                                     float rating,
                                     boolean fromUser)
         {
            String sValue = String.valueOf(rating);
            boolean isFound = false;
            for(Field fieldOfType : alFieldsOfType)
            {
               if(fieldOfType.sValue.equalsIgnoreCase(sValue))
               {
                  isFound = true;
                  ((Field) fieldRating.getTag()).copy(fieldOfType);
                  break;
               }
            }
            if(!isFound)
            {
               ((Field) fieldRating.getTag()).iID = 0;
               ((Field) fieldRating.getTag()).sValue = sValue;
            }
         }
      });

      rootView.addView(fieldRating);

      if(!fieldType.isVisible && field.sValue.trim().isEmpty())
         hideField(fieldRating, fieldType.sName);
   }

   private void addFieldCheckBox(LinearLayout rootView, FieldType fieldType)
   {
      final FieldCheckBox fieldCheckBox = new FieldCheckBox(this);

      fieldCheckBox.setTitle(fieldType.sName);
      fieldCheckBox.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      final ArrayList<Field> alFieldsOfType = dbAdapter.getFieldValues(fieldType.iID);
      Field field = new Field(fieldType.iID);

      // Looking in book field collection for field of type 
      for(int i = 0; field.iID == 0 && i < book.alFields.size(); i++)
      {
         if(fieldType.iID == book.alFields.get(i).iTypeID)
            field = book.alFields.get(i);
      }

      if(field.iID == 0) // The book has not such a field
         book.alFields.add(field);
      else
      {
         boolean bValue = Boolean.parseBoolean(field.sValue);
         fieldCheckBox.setChecked(bValue);
      }
      fieldCheckBox.setTag(field);

      fieldCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
      {

         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            String sValue = String.valueOf(isChecked);
            boolean isFound = false;
            for(Field fieldValue : alFieldsOfType)
            {
               if(fieldValue.sValue.equalsIgnoreCase(sValue))
               {
                  isFound = true;
                  ((Field) fieldCheckBox.getTag()).copy(fieldValue);
                  break;
               }
            }
            if(!isFound)
            {
               ((Field) fieldCheckBox.getTag()).iID = 0;
               ((Field) fieldCheckBox.getTag()).sValue = sValue;
            }
         }
      });

      rootView.addView(fieldCheckBox);

      if(!fieldType.isVisible && field.sValue.trim().isEmpty())
         hideField(fieldCheckBox, fieldType.sName);
   }

   private void hideField(View view, String sName)
   {

      view.setVisibility(View.GONE);

      pmHiddenFields.getMenu()
                    .add(Menu.NONE, pmHiddenFields.getMenu().size(),0, sName);
      hmHiddenFileds.put(pmHiddenFields.getMenu().getItem(pmHiddenFields.getMenu().size() - 1), view);
   }

   private boolean hasNotFieldsOfType(int iTypeID)
   {
      boolean hasFieldsOfType = false;
      for(Field field : book.alFields)
      {
         if(field.iTypeID == iTypeID)
         {
            hasFieldsOfType = true;
            break;
         }
      }
      return !hasFieldsOfType;
   }
}
