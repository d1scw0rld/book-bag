package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.FilteredArrayAdapter;
import org.d1scw0rld.bookbag.R;
import org.d1scw0rld.bookbag.dto.Property;
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

public class FieldAutoCompleteTextView extends LinearLayout implements Field
{
   
   private Title                 title;
   private AutoCompleteTextViewX autoCompleteTextViewX;

   public FieldAutoCompleteTextView(Context context)
   {
      super(context);
      
      init(context);
   }

   public FieldAutoCompleteTextView(Context context, AttributeSet attrs)
   {
      super(context, attrs);
      
      init(context);
      
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

      title.setText(titleText);
      title.setColor(titleValueColor);
      title.setTextSize(titleTextSize);
      title.setLineSize(titleLineSize);
      
      autoCompleteTextViewX.setText(text);
      autoCompleteTextViewX.setHint(hint);
   }

   public void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_auto_complete_text_view, this, true);

      title = this.findViewById(R.id.title);
      autoCompleteTextViewX = this.findViewById(R.id.autoCompleteTextView);
      autoCompleteTextViewX.setThreshold(1);
   }

   public void setTitle(String title)
   {
      this.title.setText(title);
   }

   public void setTitle(int resid)
   {
      title.setText(resid);
   }

   @Override
   public String getTitle()
   {
      return title.getTitle();
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
   
   public void setText(String text)
   {
      autoCompleteTextViewX.setText(text);
   }

   public void setText(int resid)
   {
      autoCompleteTextViewX.setText(resid);
   }
   
   public Editable getText()
   {
      return autoCompleteTextViewX.getText();
   }
   
   public void setHint(String hint)
   {
      autoCompleteTextViewX.setHint(hint);
   }

   public void setHint(int resid)
   {
      autoCompleteTextViewX.setHint(resid);
   }
   
   public void setThreshold(int threshold)
   {
      autoCompleteTextViewX.setThreshold(threshold);
   }
   
   public void setAdapter(ArrayAdapter<?> adapter)
   {
      autoCompleteTextViewX.setAdapter(adapter);
   }
   
   public void setUpdateListener(AutoCompleteTextViewX.OnUpdateListener onUpdateListener)
   {
      autoCompleteTextViewX.setOnUpdateListener(onUpdateListener);
   }

   public void setCallback(Callback callback)
   {
      autoCompleteTextViewX.setCallback(callback);
   }   
   
   public void setOnItemSelectedListener(OnItemSelectedListener l)
   {
      autoCompleteTextViewX.setOnItemSelectedListener(l);
   }

   public void setOnItemClickListener(OnItemClickListener l)
   {
      autoCompleteTextViewX.setOnItemClickListener(l);
   }
}
