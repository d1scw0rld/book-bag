package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class FieldSpinner extends LinearLayout implements Field
{
   private Title   title;
   private Spinner spinner;

   public FieldSpinner(Context context)
   {
      super(context);
      
      init(context);
   }

   public FieldSpinner(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldSpinner, 0, 0);
      
      String title = a.getString(R.styleable.FieldSpinner_title);
      int titleValueColor = a.getColor(R.styleable.FieldSpinner_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldSpinner_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldSpinner_titleLineSize, 0);
      String contentDescription = a.getString(R.styleable.FieldSpinner_android_contentDescription);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(title);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
      
      spinner.setContentDescription(contentDescription);
   }
   
   void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_spinner, this, true);

      title = findViewById(R.id.title);
      spinner = findViewById(R.id.action_select_type);
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

   public void setContentDescription(String contentDescription)
   {
      spinner.setContentDescription(contentDescription);
   }
   
   public void setAdapter(ArrayAdapter<?> adapter)
   {
      spinner.setAdapter(adapter);
   }
   
   public void setOnItemSelectedListener(OnItemSelectedListener listener)
   {
      spinner.setOnItemSelectedListener(listener);
   }
   
   public void setSelection(int position)
   {
      if(position >= 0)
         spinner.setSelection(position);
   }
   
   public interface OnUpdateListener
   {
      public void onUpdate(FieldSpinner oFieldSpinner, int pos);
   }
}
