package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class FieldCheckBox extends LinearLayout
{
   private Title oTitle;
   
   private CheckBox oCheckBox;
   
   public FieldCheckBox(Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public FieldCheckBox(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldCheckBox, 0, 0);
      
      String title = a.getString(R.styleable.FieldCheckBox_title);
      int titleValueColor = a.getColor(R.styleable.FieldCheckBox_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldCheckBox_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldCheckBox_titleLineSize, 0);
      boolean checked = a.getBoolean(R.styleable.FieldCheckBox_android_checked, false);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(title);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      oCheckBox.setChecked(checked);
   }
   
   void vInit(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_check_box, this, true);
      
      oTitle = (Title)this.findViewById(R.id.title);
      oCheckBox = (CheckBox) findViewById(R.id.check_box);
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

   public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener)
   {
      oCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
   }
   
   public void setChecked(boolean checked)
   {
      oCheckBox.setChecked(checked);
   }
   
   public boolean isChecked()
   {
      return oCheckBox.isChecked();
   }
}
