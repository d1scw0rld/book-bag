package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.FilteredArrayAdapter;
import org.d1scw0rld.bookbag.R;
import org.d1scw0rld.bookbag.dto.Property;
import org.d1scw0rld.bookbag.fields.AutoCompleteTextViewX.OnUpdateListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;

public class FieldMultiText extends LinearLayout implements Field
{
   public interface OnAddRemoveFieldListener
   {

      void onFieldRemove(View view);

      void onAddNewField(View view);
      
      void onFieldUpdated(View view, String value);

      void onItemSelect(View view, Property selection);
   }

    private LayoutInflater inflater;
    private Title title;
    private LinearLayout fieldsLayout;
    private String hint = "";
    private String contentDescription = "";
   private FilteredArrayAdapter<Property> adapter;
    private OnAddRemoveFieldListener onAddRemoveFieldListener;

   public FieldMultiText(Context context)
   {
      super(context);
      
      init(context);
   }

   public FieldMultiText(Context context, AttributeSet attrs)
   {
      super(context, attrs);
     
      init(context);
      
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FieldMultiText, 0, 0);
      
      String titleText = typedArray.getString(R.styleable.FieldMultiText_title);
      int titleValueColor = typedArray.getColor(R.styleable.FieldMultiText_titleColor, 0);
      int titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiText_titleTextSize, 0);
      int titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiText_titleLineSize, 0);
      contentDescription = typedArray.getString(R.styleable.FieldMultiText_android_contentDescription);
      hint = typedArray.getString(R.styleable.FieldMultiText_android_hint);

      typedArray.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(titleText);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
   }

   void init(Context context)
   {
      inflater = LayoutInflater.from(context);
      inflater.inflate(R.layout.field_multi_text, this, true);


      title = findViewById(R.id.title);
      
      fieldsLayout = findViewById(R.id.ll_fields);
      findViewById(R.id.ib_add_field).setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            addNewField();
         }
      });
   }
   
   private View addRow()
   {
      final View rowView = inflater.inflate(R.layout.row_field, null);
      rowView.findViewById(R.id.ib_remove_field).setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            View parentView = (View) view.getParent();
            removeField(parentView);
         }
      });
      final AutoCompleteTextViewX valueAutoCompleteTextView = rowView.findViewById(R.id.et_value);
      valueAutoCompleteTextView.setHint(hint);
      valueAutoCompleteTextView.setContentDescription(contentDescription);
      valueAutoCompleteTextView.setAdapter(adapter);
      valueAutoCompleteTextView.setThreshold(1);
      
      valueAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener()
      {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId)
         {
            Property selection = (Property) adapterView.getItemAtPosition(position);
            valueAutoCompleteTextView.setText(selection.value);
            valueAutoCompleteTextView.setSelection(selection.value.length());
            onAddRemoveFieldListener.onItemSelect(rowView, selection);
         }
      });
      
      valueAutoCompleteTextView.setOnUpdateListener(new OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText editText)
         {
            onAddRemoveFieldListener.onFieldUpdated(rowView, editText.getText().toString().trim());
         }
      });
      
      fieldsLayout.addView(rowView);

      if(fieldsLayout.getChildCount() == 1)
         rowView.findViewById(R.id.ib_remove_field).setVisibility(View.INVISIBLE);
      else
         valueAutoCompleteTextView.requestFocus();
      
      return rowView;
   }
   
   private void addNewField()
   {
      onAddRemoveFieldListener.onAddNewField(addRow());
   }

   private void addField(LinearLayout fieldsLayout, Property property)
   {
      final View rowView = addRow();
      
      EditText valueEditText = rowView.findViewById(R.id.et_value);
      if(fieldsLayout.getChildCount() == 1)
         valueEditText.setId(R.id.et_author_1);

      valueEditText.setText(property.value);
      rowView.setTag(property);
   }
   
   private void removeField(View fieldView)
   {
      onAddRemoveFieldListener.onFieldRemove(fieldView);
      ViewGroup parent = (ViewGroup) fieldView.getParent();
      parent.removeView(fieldView);
   }   
   
   public void setTitle(String title)
   {
      this.title.setText(title);
   }
   
   @Override
   public String getTitle()
   {
      return title.getTitle();
   }
   
   public void setTitle(int resourceId)
   {
      title.setText(resourceId);
   }
   
   public void setTitleColor(int valueColor)
   {
      title.setColor(valueColor);
   }
   
   public void setTitleTextSize(int textSize)
   {
      title.setTextSize(textSize);
   }
   
   public void setLineSize(int lineSize)
   {
      title.setTextSize(lineSize);
   }

   public void setContentDescription(String contentDescription)
   {
      this.contentDescription = contentDescription;
   }

   public void setHint(String hint)
   {
      this.hint = hint;
   }
   
   public void setOnAddRemoveListener(OnAddRemoveFieldListener onAddRemoveFieldListener)
   {
      this.onAddRemoveFieldListener = onAddRemoveFieldListener;
   }   

   public void setItems(FilteredArrayAdapter<Property> adapter, ArrayList<Property> items)
   {
      this.adapter = adapter;

      boolean hasFieldsOfType = false;
      for(Property item: items)
      {
         int i = adapter.getPosition(item);
         if(i >= 0)
         {
            addField(fieldsLayout, item);
            hasFieldsOfType = true;
         }
      }
    
      if(!hasFieldsOfType)
         addNewField();
   }
   
}
