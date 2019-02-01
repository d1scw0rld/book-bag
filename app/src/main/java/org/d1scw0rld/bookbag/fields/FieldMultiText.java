package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.R;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

public class FieldMultiText extends LinearLayout
{
   public interface OnAddRemoveFieldListener
   {

      void onFieldRemove(View view);

      void onAddNewField(View view);
      
      void onFieldUpdated(View view, String value);

      void onItemSelect(View view, Item selection);
   }

   private LayoutInflater oInflater;
   private Title oTitle;
   private LinearLayout llFields;
   private String hint = "";
   private String contentDescription = "";
   private ArrayAdapter<Item> adapter;
   private OnAddRemoveFieldListener onAddRemoveFieldListener;

   public FieldMultiText(Context context)
   {
      super(context);
      
      vInit(context);
   }

   public FieldMultiText(Context context, AttributeSet attrs)
   {
      super(context, attrs);
     
      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldMultiText, 0, 0);
      
      String title = a.getString(R.styleable.FieldMultiText_title);
      int titleValueColor = a.getColor(R.styleable.FieldMultiText_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldMultiText_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldMultiText_titleLineSize, 0);
      contentDescription = a.getString(R.styleable.FieldMultiText_android_contentDescription);
      hint = a.getString(R.styleable.FieldMultiText_android_hint);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(title);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
   }

   void vInit(Context context)
   {
      oInflater = LayoutInflater.from(context);
      oInflater.inflate(R.layout.field_multi_text, this, true);

      
      oTitle = findViewById(R.id.title);
      
      llFields = findViewById(R.id.ll_fields);
      findViewById(R.id.ib_add_field).setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            addNewField();
         }
      });
   }
   
   private View addRow()
   {
      final View vRow = oInflater.inflate(R.layout.row_field, null);
      vRow.findViewById(R.id.ib_remove_field).setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            View vParent = (View) v.getParent();
            removeField(vParent);
         }
      });
      final AutoCompleteTextViewX etValue = vRow.findViewById(R.id.et_value);
      etValue.setHint(hint);
      etValue.setContentDescription(contentDescription);
      etValue.setAdapter(adapter);
      etValue.setThreshold(1);
      
      etValue.setOnItemClickListener(new OnItemClickListener()
      {
         @Override
         public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId)
         {
            Item selection = (Item) adapter.getItemAtPosition(position);
            etValue.setText(selection.getValue());
            etValue.setSelection(selection.getValue().length());
            onAddRemoveFieldListener.onItemSelect(vRow, selection);
         }
      });
      
      etValue.setOnUpdateListener(new OnUpdateListener()
      {
         @Override
         public void onUpdate(EditText et)
         {
            onAddRemoveFieldListener.onFieldUpdated(vRow, et.getText().toString().trim());
         }
      });
      
      llFields.addView(vRow);

      if(llFields.getChildCount() == 1)
         vRow.findViewById(R.id.ib_remove_field).setVisibility(View.INVISIBLE);
      else
         etValue.requestFocus();
      
      return vRow;
   }
   
   private void addNewField()
   {
      onAddRemoveFieldListener.onAddNewField(addRow());
   }

   private void addField(LinearLayout llFields, Item item)
   {
      final View vRow = addRow();
      
      EditText etValue = vRow.findViewById(R.id.et_value);
      if(llFields.getChildCount() == 1)
         etValue.setId(R.id.et_author_1);

      etValue.setText(item.getValue());
      vRow.setTag(item);
   }
   
   private void removeField(View vField)
   {
      onAddRemoveFieldListener.onFieldRemove(vField);
      ViewGroup parent = (ViewGroup) vField.getParent();
      parent.removeView(vField);
   }   
   
   public void setTitle(String title)
   {
      oTitle.setText(title);
   }
   
   public void setTitle(int resid)
   {
      oTitle.setText(resid);
   }
   
   public void setTitleColor(int valueColor)
   {
      oTitle.setColor(valueColor);
   }
   
   public void setTitleTextSize(int textSize)
   {
      oTitle.setTextSize(textSize);
   }
   
   public void setLineSize(int lineSize)
   {
      oTitle.setTextSize(lineSize);
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

   public interface Item 
   {
      String getValue();
   }
   
   public void setItems(ArrayAdapter<Item> adapter, ArrayList<? extends Item> alItems)
   {
      this.adapter = adapter;

      boolean hasFieldsOfType = false;
      for(Item item: alItems)
      {
         int i = adapter.getPosition(item);
         if(i >= 0)
         {
            addField(llFields, item);
            hasFieldsOfType = true;
         }
      }
    
      if(!hasFieldsOfType)
         addNewField();
   }
   
}
