package org.d1scw0rld.bookbag.fields;

import java.util.Calendar;

import org.d1scw0rld.bookbag.R;
import org.d1scw0rld.bookbag.dto.Date;

import android.app.Activity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class FieldDate extends LinearLayout implements OnDateSetListener, Field
{
   private Title  title;
   private Button selectButton;
   private Date date = new Date(0);
   private String hint = "";
   private OnUpdateListener onUpdateListener = null;
   DatePickerDialog datePickerDialog ;

   public FieldDate(Context context)
   {
      super(context);

      init(context);
   }
   
   public FieldDate(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
      
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FieldDate, 0, 0);
      
      String titleText = typedArray.getString(R.styleable.FieldDate_title);
      int titleValueColor = typedArray.getColor(R.styleable.FieldDate_titleColor, 0);
      int titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldDate_titleTextSize, 0);
      int titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldDate_titleLineSize, 0);
      String contentDescription = typedArray.getString(R.styleable.FieldDate_android_contentDescription);
      hint = typedArray.getString(R.styleable.FieldDate_android_hint);

      typedArray.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(titleText);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
      
      selectButton.setContentDescription(contentDescription);
   }
   
   void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_date, this, true);

      final Activity activity = (Activity) context;

      title = findViewById(R.id.title);
      selectButton = findViewById(R.id.action_select_type);
      
      selectButton.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            if(date.toInt() == 0)
            {
               // Use the current date as the default date in the picker
               final Calendar calendar = Calendar.getInstance();
               date = new Date(calendar.get(Calendar.DAY_OF_MONTH),
                               calendar.get(Calendar.MONTH) + 1,
                               calendar.get(Calendar.YEAR));
            }

            datePickerDialog = DatePickerDialog.newInstance(FieldDate.this,
                                                            date.year, 
                                                            date.month - 1, 
                                                            date.day);

            datePickerDialog.setThemeDark(false);

            datePickerDialog.showYearPickerFirst(false);

            datePickerDialog.setAccentColor(ContextCompat.getColor(activity, R.color.primary));
            datePickerDialog.setCancelColor(ContextCompat.getColor(activity, R.color.accent));
            datePickerDialog.setOkColor(ContextCompat.getColor(activity, R.color.accent));

            datePickerDialog.setTitle("Select Date From DatePickerDialog");

            datePickerDialog.show(activity.getFragmentManager(), "DatePickerDialog");
         }
      });
      
   }
   
   private void setButtonText(Button button, Date date)
   {
      if(date.toInt() != 0)
      {
         
         button.setText(date.day + "/" + date.month + "/" + date.year);
         button.setTextColor(Color.BLACK);
      }
      else
      {
         button.setText(hint);
         button.setTextColor(Color.GRAY);
      }
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

   public void setContentDescription(String contentDescription)
   {
      selectButton.setContentDescription(contentDescription);
   }
   
   public void setHint(String hint)
   {
      this.hint = hint;
      setButtonText(selectButton, date);
   }

   public Date getDate()
   {
      return date;
   }

   public void setDate(Date date)
   {
      this.date = date;
      setButtonText(selectButton, date);
   }

   public void setUpdateListener(OnUpdateListener onUpdateListener)
   {
      this.onUpdateListener = onUpdateListener;
   }
   
   public interface OnUpdateListener
   {
      void onUpdate(Date date);
      void onUpdate(FieldDate fieldDate);
   }


   @Override
   public void onDateSet(DatePickerDialog view,
                         int year,
                         int monthOfYear,
                         int dayOfMonth)
   {
      date = new Date(dayOfMonth, monthOfYear + 1, year);
      setButtonText(selectButton, date);
      if(onUpdateListener != null)
      {
         onUpdateListener.onUpdate(FieldDate.this);
      }
   }

}
