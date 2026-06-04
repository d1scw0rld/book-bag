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

public class FieldEditTextUpdatableClearable extends LinearLayout implements Field
{
   private Title     title;
   private EditTextX editTextX;


   public FieldEditTextUpdatableClearable(Context context)
   {
      super(context);
      
      init(context);
   }

   public FieldEditTextUpdatableClearable(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
      
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

      this.title.setText(title);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
      
      editTextX.setText(text);
      if(inputType > 0)
         editTextX.setInputType(inputType);
      editTextX.setContentDescription(contentDescription);
      editTextX.setHint(hint);
   }
   
   void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_edit_text_updatable_clearable, this, true);

      title = findViewById(R.id.title);
      editTextX = findViewById(R.id.editTextX);
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
      editTextX.setText(text);
   }

   public void setText(int resid)
   {
      editTextX.setText(resid);
   }
   
   public Editable getText()
   {
      return editTextX.getText();
   }

   public void setInputType(int type)
   {
      if(type > 0)
         editTextX.setInputType(type);
   }
   
   public void setMultiline()
   {
      editTextX.setSingleLine(false);
      editTextX.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
      
   }

   public void setDigits(String digits)
   {
      editTextX.setInputType(InputType.TYPE_CLASS_PHONE);
      editTextX.setKeyListener(DigitsKeyListener.getInstance(digits));
   }

   public void setContentDescription(String contentDescription)
   {
      editTextX.setContentDescription(contentDescription);
   }
   
   public void setHint(String hint)
   {
      editTextX.setHint(hint);
   }
   
   public void setHint(int resid)
   {
      editTextX.setHint(resid);
   }
   
   public void setError(CharSequence error)
   {
      editTextX.setError(error);
   }
   
   public void setUpdateListener(OnUpdateListener onUpdateListener)
   {
      editTextX.setOnUpdateListener(onUpdateListener);
   }
   
   public void setCallback(Callback callback)
   {
      editTextX.setCallback(callback);
   }
}
