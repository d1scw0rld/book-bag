package org.d1scw0rld.bookbag;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.os.ConfigurationCompat;
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
import android.widget.Filter;
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
                              IS_COPY = "is_copy";

   private Book oBook;
   
   private LinearLayout llFields;
   
   private DBAdapter oDbAdapter = null;
   
   private PopupMenu pmHiddenFields = null;
   
   private Button btnAddField = null;
   
   private FieldEditTextUpdatableClearable fBookTitle = null;
   
   private View vPrevious = null;
   
   HashMap<MenuItem, View> hmHiddenFileds = new HashMap<MenuItem, View>();

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
      actionBar.setDisplayShowHomeEnabled(false);
      actionBar.setDisplayShowTitleEnabled(false);
      actionBar.setDisplayShowCustomEnabled(true);
      actionBar.setCustomView(R.layout.actionbar_custom_view_done);
      
      ((Toolbar)actionBar.getCustomView().getParent()).setContentInsetsAbsolute(0, 0);
      actionBar.getCustomView().findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            getCurrentFocus().clearFocus();
            v.requestFocus();
            if(oBook.csTitle.value.trim().isEmpty())
            {
               fBookTitle.setError(getResources().getString(R.string.err_emp_ttl));
               return;
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

      oDbAdapter = new DBAdapter(this);
      oDbAdapter.open();
      
      Bundle extras = getIntent().getExtras();
      if(extras == null)
         return;

      long iBookID = extras.getLong(BOOK_ID);
      boolean isCopy =  extras.getBoolean(IS_COPY, false);
      
      if(iBookID != 0)
      {
         oBook = oDbAdapter.getBook(iBookID);
         if(isCopy)
            oBook.iID = 0;
      }
      else
         oBook = new Book();
      
      btnAddField = (Button) findViewById(R.id.btn_add_field);
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
//            ((View) menuItem.getActionView().getTag()).setVisibility(View.VISIBLE);
            hmHiddenFileds.get(menuItem).setVisibility(View.VISIBLE);
            hmHiddenFileds.get(menuItem).requestFocus();
            pmHiddenFields.getMenu().removeItem(menuItem.getItemId());

            return false;
         }
      });
      
      llFields = (LinearLayout) findViewById(R.id.ll_fields);
      
      for(FieldType oFieldType: DBAdapter.FIELD_TYPES)
      {
         switch(oFieldType.iType)
         {
            case FieldType.TYPE_TEXT:
               addFieldText(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_MULTIFIELD:
               addFieldMultiText(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_TEXT_AUTOCOMPLETE:
               addAutocompleteField(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_SPINNER:
               addFieldSpinner(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_MULTI_SPINNER:
               addFieldMultiSpinner(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_MONEY:
               addFieldMoney(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_DATE:
               addFieldDate(llFields, oFieldType);
            break;
            
            case FieldType.TYPE_RATING:
               addFieldRating(llFields, oFieldType);
            break;

            case FieldType.TYPE_CHECK_BOX:
               addFieldCheckBox(llFields, oFieldType);
            break;
         }
      }
   }

   @Override
   protected void onPause()
   {
      oDbAdapter.close();

      super.onPause();
   }

   @Override
   protected void onResume()
   {
      super.onResume();

      oDbAdapter.open();

//      oBook = oDbAdapter.getBook(oBook.iID);
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
      switch (item.getItemId())
      {
         case R.id.cancel:
            // "Cancel"
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
//      saveBook();
      setResult(RESULT_CANCELED, new Intent());
      finish();
//      super.onBackPressed();
   }

   private void saveBook()
   {
      // Clear empty fields
      for(int i = oBook.alFields.size()-1; i >= 0; i-- )
         if(oBook.alFields.get(i).sValue.trim().isEmpty())
            oBook.alFields.remove(i);
      
      if(oBook.iID != 0)
         oDbAdapter.updateBook(oBook);
      else
         oDbAdapter.insertBook(oBook);
   }
   
   private <T> void addFieldText(LinearLayout rootView, FieldType oFieldType)
   {
      switch(oFieldType.iID)
      {
         case DBAdapter.FLD_TITLE:
//            oField.setTag(oBook.csTitle);
//            cValue = (Changeable<T>) oBook.csTitle;
            addFieldText(rootView, oFieldType, oBook.csTitle);
         break;
         
         
         case DBAdapter.FLD_DESCRIPTION:
//            oField.setTag(oBook.csDescription);
//            cValue = oBook.csDescription;
            addFieldText(rootView, oFieldType, oBook.csDescription);
         break;

         case DBAdapter.FLD_VOLUME:
//            oField.setTag(oBook.ciVolume);
//            cValue = oBook.ciVolume;
            addFieldText(rootView, oFieldType, oBook.ciVolume);
         break;

         case DBAdapter.FLD_PAGES:
//            oField.setTag(oBook.ciPages);
//            cValue = oBook.ciPages;
            addFieldText(rootView, oFieldType, oBook.ciPages);
         break;
         
         case DBAdapter.FLD_EDITION:
//            oField.setTag(oBook.ciEdition);
//            cValue = oBook.ciEdition;
            addFieldText(rootView, oFieldType, oBook.ciEdition);
         break;

         case DBAdapter.FLD_ISBN:
//            oField.setTag(oBook.csISBN);
//            cValue = oBook.csISBN;
            addFieldText(rootView, oFieldType, oBook.csISBN);
         break;
         
         case DBAdapter.FLD_WEB:
//            oField.setTag(oBook.csWeb);
//            cValue = oBook.csWeb;
            addFieldText(rootView, oFieldType, oBook.csWeb);
         break;
         
         default:
            return;
      }
   }

   private <T> void addFieldText(ViewGroup rootView, FieldType oFieldType,  final Changeable<T> cValue)
   {
      final FieldEditTextUpdatableClearable oField = new FieldEditTextUpdatableClearable(this);
      
      oField.setTitle(oFieldType.sName);
      oField.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      oField.setText(cValue.toString());
      oField.setHint(oFieldType.sName);
      oField.setInputType(oFieldType.iInputType);
      if(oFieldType.iID == DBAdapter.FLD_TITLE)
      {
         fBookTitle = oField;
         vPrevious = oField.findViewById(R.id.editTextX);
      }
//      if(oFieldType.isMultiline)
//         oField.setMultiline();
      oField.setUpdateListener(new EditTextX.OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText et)
         {
            Class<?> c = cValue.getGenericType();
//            T t = null;
            
            Class<?> clazz;
            Object object;
            try
            {
               clazz = Class.forName(c.getName());
               Constructor<?> ctor = clazz.getConstructor(String.class);
               object = ctor.newInstance(et.getText().toString().trim());

            } catch(ClassNotFoundException 
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
      rootView.addView(oField);
      if(!oFieldType.isVisible && cValue.isEmpty())
         hideField(oField, oFieldType.sName);
   }   
   
   private void addAutocompleteField(ViewGroup rootView, final FieldType oFieldType)
   {
      final FieldAutoCompleteTextView oFieldAutoCompleteTextView = new FieldAutoCompleteTextView(this);
      oFieldAutoCompleteTextView.setTitle(oFieldType.sName);
      oFieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      oFieldAutoCompleteTextView.setHint(oFieldType.sName);
      View v = new View(this);
      v.setNextFocusDownId(oFieldAutoCompleteTextView.getId());

      final ArrayList<Field> alFieldValues = oDbAdapter.getFieldValues(oFieldType.iID, true);
      Field oField = new Field(oFieldType.iID);
      
      // Looking in book field collection for field of type 
      for(int i = 0; oField.iID == 0 && i < oBook.alFields.size(); i++)
         if(oFieldType.iID == oBook.alFields.get(i).iTypeID)
            oField = oBook.alFields.get(i);

      if(oField.iID == 0) // The book has not such a field 
         oBook.alFields.add(oField);
      else
//      if(!oField.sValue.isEmpty())
         oFieldAutoCompleteTextView.setText(oField.sValue);
      
      oFieldAutoCompleteTextView.setTag(oField);
      
      ArrayFieldsAdapter oArrayAdapter = new ArrayFieldsAdapter(this, android.R.layout.select_dialog_item, alFieldValues);
      oFieldAutoCompleteTextView.setAdapter(oArrayAdapter);
      oFieldAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener()
      {
         public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId)
         {
            Field fldSelected = (Field)adapter.getItemAtPosition(position);
            ((Field)oFieldAutoCompleteTextView.getTag()).copy(fldSelected);
         }
      });
      oFieldAutoCompleteTextView.setUpdateListener(new AutoCompleteTextViewX.OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText et)
         {
            boolean isFound = false;
            for(Field f : alFieldValues)
            {
//               if(et.getText().toString().trim().equalsIgnoreCase(((Field)oFieldAutoCompleteTextView.getTag()).sValue))
               if(et.getText().toString().trim().equalsIgnoreCase(f.sValue))
               {
                  isFound = true;
                  ((Field)oFieldAutoCompleteTextView.getTag()).copy(f);
                  break;
               }
            }
            if(!isFound)
            {
               ((Field)oFieldAutoCompleteTextView.getTag()).iID = 0;
               ((Field)oFieldAutoCompleteTextView.getTag()).sValue = et.getText().toString();
            }
         }
      });
    
      rootView.addView(oFieldAutoCompleteTextView);
      if(!oFieldType.isVisible && oField.sValue.trim().isEmpty())
         hideField(oFieldAutoCompleteTextView, oFieldType.sName);
   }   
   
   private void addFieldSpinner(ViewGroup rootView, FieldType oFieldType)
   {
      final FieldSpinner oFieldSpinner = new FieldSpinner(this);

      oFieldSpinner.setTitle(oFieldType.sName);
      oFieldSpinner.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      
      Field oField = new Field(oFieldType.iID);
      final ArrayList<Field> alFieldValues = oDbAdapter.getFieldValues(oFieldType.iID);
      
      for(int i = 0; i < oBook.alFields.size() && (oField.iID == 0 || oField.iTypeID != oFieldType.iID); i++)
         if(oBook.alFields.get(i).iTypeID == oFieldType.iID)
            oField = oBook.alFields.get(i);
      if(oField.iID == 0) // The book has not such a field 
         oBook.alFields.add(oField);

      ArrayAdapter<String> oArrayAdapter = new ArrayAdapter<String> (this, R.layout.spinner_item)
      {
         @Override
         public View getView(int position, View convertView, ViewGroup parent) 
         {
            View v = super.getView(position, convertView, parent);
            v.setPadding(0, v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom()); // Removing leading pad
            if(position == 0) 
               ((TextView)v.findViewById(android.R.id.text1)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.text, null));
            
            return v;
         }       

         @Override
         public View getDropDownView(int position, View convertView, ViewGroup parent) 
         {
            View v = null;
            if (position == 0) 
            {
               TextView tv = new TextView(getContext());
               tv.setHeight(0);
               tv.setVisibility(View.GONE);
               v = tv;
            } 
            else 
                 v = super.getDropDownView(position, null, parent);
            
            parent.setVerticalScrollBarEnabled(false);
            return v;
         }         
      };
      oArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
      oArrayAdapter.add(oFieldType.sName);
      for(Field f: alFieldValues)
         oArrayAdapter.add(f.sValue);
      
      oFieldSpinner.setAdapter(oArrayAdapter);
      int iSelected = 0;
      for(int i = 0; i < alFieldValues.size(); i++)
         if(alFieldValues.get(i).equals(oField))
            iSelected = i + 1;
      oFieldSpinner.setSelection(iSelected);
      oFieldSpinner.setTag(oField);
      oFieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            if(pos > 0)
               ((Field) oFieldSpinner.getTag()).copy(alFieldValues.get(pos - 1));
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {}
      });
      
      rootView.addView(oFieldSpinner);
//      if(!oFieldType.isVisible && oField == null)
      if(!oFieldType.isVisible && oField.iID == 0)
         hideField(oFieldSpinner, oFieldType.sName);
   }

   private void addFieldMultiText(ViewGroup rootView, final FieldType oFieldType)
   {
      final FieldMultiText oFieldMultiText = new FieldMultiText(this);
      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
         oFieldMultiText.setId(Utils.generateViewId());
      else
         oFieldMultiText.setId(View.generateViewId());
      if(vPrevious != null)
         vPrevious.setNextFocusDownId(R.id.et_author_1);
      String tsNames[] = oFieldType.sName.split("\\|");
      oFieldMultiText.setTitle(tsNames.length > 1 ? tsNames[1] : oFieldType.sName);
      oFieldMultiText.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
//      oFieldMultiText.setHint(oFieldType.sName);
      oFieldMultiText.setHint(tsNames[0]);

      // Set adapter
      final ArrayList<Field> alFieldsValues = oDbAdapter.getFieldValues(oFieldType.iID, true);
      final ArrayList<FieldMultiText.Item> alItemsValues = new ArrayList<FieldMultiText.Item>();
      for(FieldMultiText.Item field: alFieldsValues)
         alItemsValues.add(field);

      final ArrayItemsAdapter oArrayAdapter = new ArrayItemsAdapter(this, android.R.layout.select_dialog_item, alItemsValues);
      
      oFieldMultiText.setOnAddRemoveListener(new FieldMultiText.OnAddRemoveFieldListener()
      {
         
         @Override
         public void onFieldRemove(View view)
         {
            oBook.alFields.remove((Field) view.getTag());
         }
         
         @Override
         public void onAddNewField(View view)
         {
            Field fldNew = new Field(oFieldType.iID);
            oBook.alFields.add(fldNew);
            view.setTag(fldNew);
         }
         
         @Override
         public void onFieldUpdated(View view, String value)
         {
            boolean isExists = false;
            for(Field field: alFieldsValues)
            {
               if(field.getValue().trim().equalsIgnoreCase(value.trim()))
               {
                  ((Field )view.getTag()).copy(field);
                  isExists = true;
                  break;
               }
            }
            if(!isExists)
            {
               ((Field )view.getTag()).iID = 0;
               ((Field )view.getTag()).sValue = value;
            }
         }

//         @Override
//         public void onItemSelect(ArrayAdapter<?> adapter, View view, int position)
//         {
//            ((Field) view.getTag()).copy(alFieldsValues.get(position));
//         }

         @Override
         public void onItemSelect(View view, FieldMultiText.Item selection)
         {
            if(selection instanceof Field)
               ((Field) view.getTag()).copy((Field)selection);
         }
      });
      
      oFieldMultiText.setItems(oArrayAdapter, oBook.alFields);
      
      rootView.addView(oFieldMultiText);
      
      if(!oFieldType.isVisible && !hasFieldsOfType(oFieldType.iID))
         hideField(oFieldMultiText, oFieldType.sName);
   }
   
   private void addFieldMultiSpinner(ViewGroup rootView, final FieldType oFieldType)
   {
      final FieldMultiSpinner oFieldMultiSpinner = new FieldMultiSpinner(this);
      String tsNames[] = oFieldType.sName.split("\\|");
      oFieldMultiSpinner.setTitle(tsNames.length > 1 ? tsNames[1] : oFieldType.sName);
      oFieldMultiSpinner.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      oFieldMultiSpinner.setHint(tsNames.length > 1 ? tsNames[1] : oFieldType.sName);

      final ArrayList<Field> alFieldValues = oDbAdapter.getFieldValues(oFieldType.iID);
      ArrayList<Item> alItems = new ArrayList<>();
      for(Field oFieldValue : alFieldValues)
      {
         Item item = new Item(oFieldValue.sValue);
         item.setSelected(oBook.alFields.contains(oFieldValue));
         alItems.add(item);
      }
      
      oFieldMultiSpinner.setItems(alItems);
      oFieldMultiSpinner.setOnUpdateListener(new FieldMultiSpinner.OnUpdateListener()
      {
         @Override
         public void onUpdate(Item item)
         {
            boolean isFound = false;
            for(Field oFieldValue : alFieldValues)
            {
               if(oFieldValue.sValue.equalsIgnoreCase(item.getTitle()))
               {
                  isFound = true;
                  if(item.isSelected())
                     oBook.alFields.add(oFieldValue);
                  else
                     oBook.alFields.remove(oFieldValue);
                  break;
               }
            }
            if(!isFound)
            {
               Field oNewFieldValue = new Field(oFieldType.iID, item.getTitle());
               alFieldValues.add(oNewFieldValue);
               oBook.alFields.add(oNewFieldValue);
            }
         }
      });
      
      rootView.addView(oFieldMultiSpinner);
      
      if(!oFieldType.isVisible && !hasFieldsOfType(oFieldType.iID))
         hideField(oFieldMultiSpinner, tsNames.length > 1 ? tsNames[1] : oFieldType.sName);
      
   }
   
   @SuppressWarnings("unchecked")
   private void addFieldMoney(ViewGroup rootView, FieldType oFieldType)
   {
      final FieldMoney oFieldMoney = new FieldMoney(this);
      oFieldMoney.setTitle(oFieldType.sName);
      oFieldMoney.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      oFieldMoney.setHint(oFieldType.sName);
      
      switch(oFieldType.iID)
      {
         case DBAdapter.FLD_PRICE:
            oFieldMoney.setTag(oBook.csPrice);
         break;
         
         case DBAdapter.FLD_VALUE:
            oFieldMoney.setTag(oBook.csValue);
         break;
         
         default:
            return;
      }

      final Price oPrice = new Price(((Changeable<String>) oFieldMoney.getTag()).value);
      if(oPrice.iValue != 0)
         oFieldMoney.setValue(oPrice.iValue);

      final ArrayList<Field> alCurrencies = oDbAdapter.getFieldValues(DBAdapter.FLD_CURRENCY);
      int iSelected = 0;
      ArrayAdapter<String> oArrayAdapter = new ArrayAdapter<String> (this, R.layout.spinner_item);
      for(int i = 0; i < alCurrencies.size(); i++)
      {
//         tCurrencies[i] = alCurrencies.get(i).sValue;
         oArrayAdapter.add(alCurrencies.get(i).sValue);
         if(oPrice != null && oPrice.iCurrencyID == alCurrencies.get(i).iID)
            iSelected = i;
      }
      
      oFieldMoney.setAdapter(oArrayAdapter);
      oFieldMoney.setSelection(iSelected);

      oFieldMoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
         {
            oPrice.iCurrencyID = alCurrencies.get(pos).iID;
            ((Changeable<String>) oFieldMoney.getTag()).value = oPrice.toString();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });
      
      oFieldMoney.setUpdateListener(new EditTextX.OnUpdateListener()
      {
         
         @Override
         public void onUpdate(EditText et)
         {
            String sValue = et.getText().toString();
//            sValue = sValue.replace(" ", "");
//            sValue = sValue.replace("-,", "-0,");
            int iValue;
            if(sValue.isEmpty() || sValue.matches("-|,|-,"))
               iValue = 0;
            else
            {
               String [] tsValue = sValue.split("\\" + DBAdapter.separator);
//               String [] tsValue = sValue.split("\\.");
                
               iValue = (tsValue[0].isEmpty() ? 0 : Integer.valueOf(tsValue[0])*100) + (tsValue.length == 2 ? (sValue.contains("-") ? -1 : 1) * (tsValue[1].length() == 1 ? 10 : 1) * Integer.valueOf(tsValue[1]) : 0);
            }
            
            oPrice.iValue = iValue;
            ((Changeable<String>) oFieldMoney.getTag()).value = oPrice.toString();
            
         }
      });
      
      rootView.addView(oFieldMoney);
      
      if(!oFieldType.isVisible && ((Changeable<?>) oFieldMoney.getTag()).isEmpty())
         hideField(oFieldMoney, oFieldType.sName);
         
   }
   
   private void addFieldDate(ViewGroup rootView, FieldType oFieldType)
   {
      Date date;
      
      FieldDate oFieldDate = null;
      
      switch(oFieldType.iID)
      {
         case DBAdapter.FLD_READ_DATE:
            date = new Date(oBook.ciReadDate.value);
            oFieldDate = new FieldDate(this);
            oFieldDate.setTag(oBook.ciReadDate);
         break;
         
         case DBAdapter.FLD_DUE_DATE:
            date = new Date(oBook.ciDueDate.value);
            oFieldDate = new FieldDate(this);
            oFieldDate.setTag(oBook.ciDueDate);
            
         break;
         
         default:
            return;
            
      }
      
      oFieldDate.setTitle(oFieldType.sName);
      oFieldDate.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
      oFieldDate.setHint(oFieldType.sName);
      oFieldDate.setDate(date);
      
      oFieldDate.setUpdateListener(new FieldDate.OnUpdateListener()
      {
         @Override
         public void onUpdate(Date date)
         {
         }

         @Override
         public void onUpdate(FieldDate oFieldDate)
         {
            ((Changeable<Integer>) oFieldDate.getTag()).value = oFieldDate.getDate().toInt();
         }
      });
      rootView.addView(oFieldDate);
      
      if(!oFieldType.isVisible && ((Changeable<Integer>) oFieldDate.getTag()).value == 0)
         hideField(oFieldDate, oFieldType.sName);
      
   }
   
   private void addFieldRating(ViewGroup rootView, FieldType oFieldType)
   {
      final FieldRating oFieldRating = new FieldRating(this);
      
      oFieldRating.setTitle(oFieldType.sName);
      oFieldRating.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      final ArrayList<Field> alFieldValues = oDbAdapter.getFieldValues(oFieldType.iID);
      Field oField = new Field(oFieldType.iID);
      
      // Looking in book field collection for field of type 
      for(int i = 0; oField.iID == 0 && i < oBook.alFields.size(); i++)
         if(oFieldType.iID == oBook.alFields.get(i).iTypeID)
            oField = oBook.alFields.get(i);

      if(oField.iID == 0) // The book has not such a field 
         oBook.alFields.add(oField);
      else
      {
         Float fValue = Float.parseFloat(oField.sValue);
         oFieldRating.setRating(fValue);
      }
      oFieldRating.setTag(oField);

//      fRating = iRating / 10;
//      oFieldRating.setRating(fRating);
      oFieldRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener()
      {
         @Override
         public void onRatingChanged(RatingBar ratingBar,
                                     float rating,
                                     boolean fromUser)
         {
//            ((Changeable<Integer>) oFieldRating.getTag()).value = (int) rating*10;
            String sValue = String.valueOf(rating);
            boolean isFound = false;
            for(Field oFieldValue : alFieldValues)
            {
               if(oFieldValue.sValue.equalsIgnoreCase(sValue))
               {
                  isFound = true;
                  ((Field)oFieldRating.getTag()).copy(oFieldValue);
                  break;
               }
            }
            if(!isFound)
            {
               ((Field)oFieldRating.getTag()).iID = 0;
               ((Field)oFieldRating.getTag()).sValue = sValue;
            }
         }
      });
      
      rootView.addView(oFieldRating);
      
      if(!oFieldType.isVisible && oField.sValue.trim().isEmpty())
         hideField(oFieldRating, oFieldType.sName);
   }   
   
   private void addFieldCheckBox(LinearLayout rootView, FieldType oFieldType)
   {
      final FieldCheckBox oFieldCheckBox = new FieldCheckBox(this);
      
      oFieldCheckBox.setTitle(oFieldType.sName);
      oFieldCheckBox.setTitleColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));

      final ArrayList<Field> alFieldValues = oDbAdapter.getFieldValues(oFieldType.iID);
      Field oField = new Field(oFieldType.iID);
      
      // Looking in book field collection for field of type 
      for(int i = 0; oField.iID == 0 && i < oBook.alFields.size(); i++)
         if(oFieldType.iID == oBook.alFields.get(i).iTypeID)
            oField = oBook.alFields.get(i);

      if(oField.iID == 0) // The book has not such a field 
         oBook.alFields.add(oField);
      else
      {
         boolean bValue = Boolean.parseBoolean(oField.sValue);
         oFieldCheckBox.setChecked(bValue);;
      }
      oFieldCheckBox.setTag(oField);

      oFieldCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
      {
         
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            String sValue = String.valueOf(isChecked);
            boolean isFound = false;
            for(Field oFieldValue : alFieldValues)
            {
               if(oFieldValue.sValue.equalsIgnoreCase(sValue))
               {
                  isFound = true;
                  ((Field)oFieldCheckBox.getTag()).copy(oFieldValue);
                  break;
               }
            }
            if(!isFound)
            {
               ((Field)oFieldCheckBox.getTag()).iID = 0;
               ((Field)oFieldCheckBox.getTag()).sValue = sValue;
            }
         }
      });
      
      rootView.addView(oFieldCheckBox);
      
      if(!oFieldType.isVisible && oField.sValue.trim().isEmpty())
         hideField(oFieldCheckBox, oFieldType.sName);   
   }

   public class ArrayFieldsAdapter extends ArrayAdapter<Field> 
   {
//      private final String MY_DEBUG_TAG = "ArrayFieldsAdapter";
//      private ArrayList<Field> items;
      private ArrayList<Field> items;
      private ArrayList<Field> suggestions;
//      private int viewResourceId;

      public ArrayFieldsAdapter(Context context, int viewResourceId, ArrayList<Field> items) 
      {
         super(context, viewResourceId, items);
         this.items = items;
//          this.itemsAll = (ArrayList<Field>) items.clone();
         this.suggestions = new ArrayList<Field>();
//          this.viewResourceId = viewResourceId;
      }

      public View getView(int position, View convertView, ViewGroup parent) 
      {
         TextView view = (TextView) super.getView(position, convertView, parent);
         // Replace text with my own
         view.setText(getItem(position).sValue);
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
            String str = ((Field)(resultValue)).sValue; 
            return str;
         }
         
         @Override
         protected FilterResults performFiltering(CharSequence constraint) 
         {
            if(constraint != null) 
            {
               suggestions.clear();
               for (Field oField : items) 
               {
                  if(oField.sValue.toLowerCase(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0)).startsWith(constraint.toString().toLowerCase()))
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

   private void hideField(View view, String sName)
   {
      
      view.setVisibility(View.GONE);
      
      pmHiddenFields.getMenu().add(Menu.NONE, pmHiddenFields.getMenu().size(), 0, sName);
      hmHiddenFileds.put(pmHiddenFields.getMenu().getItem(pmHiddenFields.getMenu().size()-1), view);
   }
   
   private boolean hasFieldsOfType(int iTypeID)
   {
      boolean hasFieldsOfType = false; 
      for(Field field: oBook.alFields)
      {
         if(field.iTypeID == iTypeID)
         {
            hasFieldsOfType = true;
            break;
         }
      }
      return hasFieldsOfType;
   }
}
