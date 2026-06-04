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
      
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FieldEditTextUpdatableClearable, 0, 0);
      
      String titleText = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_title);
      int titleValueColor = typedArray.getColor(R.styleable.FieldEditTextUpdatableClearable_titleColor, 0);
      int titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldEditTextUpdatableClearable_titleTextSize, 0);
      int titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldEditTextUpdatableClearable_titleLineSize, 0);
      String text = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_android_text);
      int inputType = typedArray.getInteger(R.styleable.FieldEditTextUpdatableClearable_android_inputType, 0);
      String contentDescription = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_android_contentDescription);
      String hint = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_android_hint);

      typedArray.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(titleText);
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
   
   public void setTitle(int resourceId)
   {
      title.setText(resourceId);
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

   public void setText(int resourceId)
   {
      editTextX.setText(resourceId);
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
   
   public void setHint(int resourceId)
   {
      editTextX.setHint(resourceId);
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
