package org.d1scw0rld.bookbag.fields;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d1scw0rld.bookbag.DBAdapter;
import org.d1scw0rld.bookbag.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class FieldMoney extends LinearLayout implements Field
{
   public static final String FILTER = "0*[1-9]?\\d{0,3}(\\" + DBAdapter.separator + "\\d{0,2})?";

   private Title title;
   private Spinner   spinner;
   private EditTextX editTextX;

   public FieldMoney(Context context)
   {
      super(context);
      
      init(context);
   }

   public FieldMoney(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
      
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FieldMoney, 0, 0);
      
      String titleText = typedArray.getString(R.styleable.FieldMoney_title);
      int titleValueColor = typedArray.getColor(R.styleable.FieldMoney_titleColor, 0);
      int titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMoney_titleTextSize, 0);
      int titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMoney_titleLineSize, 0);
      String contentDescription = typedArray.getString(R.styleable.FieldMoney_android_contentDescription);
      String hint = typedArray.getString(R.styleable.FieldMoney_android_hint);

      typedArray.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(titleText);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
      
      spinner.setContentDescription(contentDescription);
      editTextX.setHint(hint);
   }
   
   void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_money, this, true);

      title = findViewById(R.id.title);
      spinner = findViewById(R.id.action_select_type);
      editTextX = findViewById(R.id.editTextX);
      editTextX.setFilters(new InputFilter[] {new DecimalDigitsInputFilter()});
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

   public void setValue(int value)
   {
      editTextX.setText(String.format(getResources().getString(R.string.amn_vl), value / 100, DBAdapter.separator, value % 100));
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
   
   public void setHint(String hint)
   {
      editTextX.setHint(hint);
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
      public void onUpdate(FieldMoney fieldMoney);
   }
   
   private class DecimalDigitsInputFilter implements InputFilter
   {
      Pattern pattern;

      DecimalDigitsInputFilter()
      {
          pattern = Pattern.compile(FILTER);
      }

      @Override
      public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) 
      {
         String formatedSource = source.subSequence(start, end).toString();

         String destPrefix = dest.subSequence(0, dstart).toString();

         String destSuffix = dest.subSequence(dend, dest.length()).toString();

         String result = destPrefix + formatedSource + destSuffix;

         result = result.replace(",", ".");

         Matcher matcher = pattern.matcher(result);

         if(matcher.matches())
            return null;
         
         return "";
      }
   }

   public void setUpdateListener(EditTextX.OnUpdateListener onUpdateListener)
   {
      editTextX.setOnUpdateListener(onUpdateListener);
   }   
}
