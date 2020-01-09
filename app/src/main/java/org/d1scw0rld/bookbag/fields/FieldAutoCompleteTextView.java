package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.FilteredArrayAdapter;
import org.d1scw0rld.bookbag.R;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.fields.AutoCompleteTextViewX.Callback;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

public class FieldAutoCompleteTextView extends LinearLayout
{
   
   private Title oTitle;
   private AutoCompleteTextViewX oAutoCompleteTextViewX;

   public FieldAutoCompleteTextView(Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public FieldAutoCompleteTextView(Context context, Field oField, ArrayList<Field> alFieldValues)
   {
      super(context);
      
      vInit(context, oField, alFieldValues);
   }

   public FieldAutoCompleteTextView(Context context, AttributeSet attrs)
   {
      super(context, attrs);
      
      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldAutoCompleteTextView, 0, 0);

      String titleText = a.getString(R.styleable.FieldAutoCompleteTextView_title);
      int titleValueColor = a.getColor(R.styleable.FieldAutoCompleteTextView_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleLineSize, 0);
      String text = a.getString(R.styleable.FieldAutoCompleteTextView_android_text);
      String hint = a.getString(R.styleable.FieldAutoCompleteTextView_android_hint);
      
      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(titleText);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      
      oAutoCompleteTextViewX.setText(text);
      oAutoCompleteTextViewX.setHint(hint);
   }
   
   public FieldAutoCompleteTextView(Context context, AttributeSet attrs, Field oField, ArrayList<Field> alFieldValues)
   {
      super(context, attrs);
      
      vInit(context, oField, alFieldValues);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldAutoCompleteTextView, 0, 0);

      String titleText = a.getString(R.styleable.FieldAutoCompleteTextView_title);
      int titleValueColor = a.getColor(R.styleable.FieldAutoCompleteTextView_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleLineSize, 0);
      String text = a.getString(R.styleable.FieldAutoCompleteTextView_android_text);
      String hint = a.getString(R.styleable.FieldAutoCompleteTextView_android_hint);
      
      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(titleText);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      
      oAutoCompleteTextViewX.setText(text);
      oAutoCompleteTextViewX.setHint(hint);
   }
   
   public void vInit(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_auto_complete_text_view, this, true);
      
      oTitle = this.findViewById(R.id.title);
      oAutoCompleteTextViewX = this.findViewById(R.id.autoCompleteTextView);
      oAutoCompleteTextViewX.setThreshold(1);
   }
   
   public void vInit(Context context, Field oField, final ArrayList<Field> alFieldValues)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_auto_complete_text_view, this, true);
      
      oTitle = findViewById(R.id.title);
      oAutoCompleteTextViewX = findViewById(R.id.autoCompleteTextView);
      oAutoCompleteTextViewX.setThreshold(1);
      
      if(oField != null && !oField.sValue.isEmpty())
         oAutoCompleteTextViewX.setText(oField.sValue);

//      ArrayItemsAdapter oArrayAdapter = new ArrayItemsAdapter(context, android.R.layout.select_dialog_item, alFieldValues);
      FilteredArrayAdapter<Field> oArrayAdapter = new FilteredArrayAdapter<>(context, android.R.layout.select_dialog_item, alFieldValues);
      oAutoCompleteTextViewX.setAdapter(oArrayAdapter);
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
   
   public void setText(String text)
   {
      oAutoCompleteTextViewX.setText(text);
   }

   public void setText(int resid)
   {
      oAutoCompleteTextViewX.setText(resid);
   }
   
   public Editable getText()
   {
      return oAutoCompleteTextViewX.getText();
   }
   
   public void setHint(String hint)
   {
      oAutoCompleteTextViewX.setHint(hint);
   }

   public void setHint(int resid)
   {
      oAutoCompleteTextViewX.setHint(resid);
   }
   
   public void setThreshold(int threshold)
   {
      oAutoCompleteTextViewX.setThreshold(threshold);
   }
   
   public void setAdapter(ArrayAdapter<?> adapter)
   {
      oAutoCompleteTextViewX.setAdapter(adapter);
   }
   
   public void setUpdateListener(AutoCompleteTextViewX.OnUpdateListener onUpdateListener)
   {
      oAutoCompleteTextViewX.setOnUpdateListener(onUpdateListener);
   }
   
//   public void setUpdateListener(OnUpdateListener onUpdateListener)
//   {
//      this.onUpdateListener = onUpdateListener;
//   }
   
   public void setCallback(Callback callback)
   {
      oAutoCompleteTextViewX.setCallback(callback);
   }   
   
   public void setOnItemSelectedListener(OnItemSelectedListener l)
   {
      oAutoCompleteTextViewX.setOnItemSelectedListener(l);
   }

   public void setOnItemClickListener(OnItemClickListener l)
   {
      oAutoCompleteTextViewX.setOnItemClickListener(l);
   }
 
//   public interface OnUpdateListener
//   {
//      public void onUpdate(FieldAutoCompleteTextView oFieldAutoCompleteTextView, int position);
//      public void onUpdate(EditText et);
//   }
}
