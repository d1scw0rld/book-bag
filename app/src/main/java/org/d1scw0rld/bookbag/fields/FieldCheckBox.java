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

public class FieldCheckBox extends LinearLayout implements Field
{
   private Title title;
   
   private CheckBox checkBox;
   
   public FieldCheckBox(Context context)
   {
      super(context);
      
      init(context);
   }
   
   public FieldCheckBox(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldCheckBox, 0, 0);
      
      String title = a.getString(R.styleable.FieldCheckBox_title);
      int titleValueColor = a.getColor(R.styleable.FieldCheckBox_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldCheckBox_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldCheckBox_titleLineSize, 0);
      boolean checked = a.getBoolean(R.styleable.FieldCheckBox_android_checked, false);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(title);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
      checkBox.setChecked(checked);
   }
   
   void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_check_box, this, true);

      title = findViewById(R.id.title);
      checkBox = findViewById(R.id.check_box);
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

   public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener)
   {
      checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
   }
   
   public void setChecked(boolean checked)
   {
      checkBox.setChecked(checked);
   }
   
   public boolean isChecked()
   {
      return checkBox.isChecked();
   }
}
