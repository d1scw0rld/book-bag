package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;
import org.d1scw0rld.bookbag.fields.EditTextX.Callback;
import org.d1scw0rld.bookbag.fields.EditTextX.OnUpdateListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

public class FieldEditTextUpdatableClearable extends LinearLayout
{
   private Title oTitle;
   private EditTextX oEditTextX;
   
   public FieldEditTextUpdatableClearable(Context context)
   {
      super(context);
      
      vInit(context);
   }

   public FieldEditTextUpdatableClearable(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldEditTextUpdatableClearable, 0, 0);
      
      String title = a.getString(R.styleable.FieldEditTextUpdatableClearable_title);
      int titleValueColor = a.getColor(R.styleable.FieldEditTextUpdatableClearable_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldEditTextUpdatableClearable_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldEditTextUpdatableClearable_titleLineSize, 0);
      String text = a.getString(R.styleable.FieldEditTextUpdatableClearable_android_text);
      int inputType = a.getInteger(R.styleable.FieldEditTextUpdatableClearable_android_inputType, 0);
      String contentDescription = a.getString(R.styleable.FieldEditTextUpdatableClearable_android_contentDescription);
      String hint = a.getString(R.styleable.FieldEditTextUpdatableClearable_android_hint);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(title);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      
      oEditTextX.setText(text);
      if(inputType > 0)
         oEditTextX.setInputType(inputType);
      oEditTextX.setContentDescription(contentDescription);
      oEditTextX.setHint(hint);
   }
   
   void vInit(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_edit_text_updatable_clearable, this, true);
      
      oTitle = (Title)this.findViewById(R.id.title);
      oEditTextX = (EditTextX) this.findViewById(R.id.editTextX);
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
      oEditTextX.setText(text);
   }

   public void setText(int resid)
   {
      oEditTextX.setText(resid);
   }
   
   public Editable getText()
   {
      return oEditTextX.getText();
   }

   public void setInputType(int type)
   {
      if(type > 0)
         oEditTextX.setInputType(type);
   }
   
   public void setMultiline()
   {
      oEditTextX.setSingleLine(false);
      oEditTextX.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);   
      
   }

   public void setDigits(String digits)
   {
      oEditTextX.setInputType(InputType.TYPE_CLASS_PHONE);
      oEditTextX.setKeyListener(DigitsKeyListener.getInstance(digits));
   }

   public void setContentDescription(String contentDescription)
   {
      oEditTextX.setContentDescription(contentDescription);
   }
   
   public void setHint(String hint)
   {
      oEditTextX.setHint(hint);
   }
   
   public void setHint(int resid)
   {
      oEditTextX.setHint(resid);
   }
   
   public void setError(CharSequence error)
   {
      oEditTextX.setError(error);
   }
   
   public void setUpdateListener(OnUpdateListener onUpdateListener)
   {
      oEditTextX.setOnUpdateListener(onUpdateListener);
   }
   
   public void setCallback(Callback callback)
   {
      oEditTextX.setCallback(callback);
   }
}
