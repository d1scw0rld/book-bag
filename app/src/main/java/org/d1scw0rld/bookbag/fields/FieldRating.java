package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.support.v7.widget.AppCompatRatingBar;

public class FieldRating extends LinearLayout
{
   private Title oTitle;

   private boolean isIndicator;
   
   private int iNumStars;
   
   private float fRating,
                 fStepSize;
   
   private String contentDescription = "";
   
   private AppCompatRatingBar oRatingBar; 
   
   public FieldRating(Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public FieldRating(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldRating, 0, 0);
      
      String title = a.getString(R.styleable.FieldRating_title);
      int titleValueColor = a.getColor(R.styleable.FieldRating_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldRating_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldRating_titleLineSize, 0);
      contentDescription = a.getString(R.styleable.FieldRating_android_contentDescription);
      setNumStars(a.getInteger(R.styleable.FieldRating_android_numStars, 5));
      setRating(a.getFloat(R.styleable.FieldRating_android_rating, 0.0f));
      setStepSize(a.getFloat(R.styleable.FieldRating_android_stepSize, 0.5f));
      setIsIndicator(a.getBoolean(R.styleable.FieldRating_android_isIndicator, false));

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(title);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      oRatingBar.setContentDescription(contentDescription);
      oRatingBar.setNumStars(iNumStars);
      oRatingBar.setRating(fRating);
      oRatingBar.setStepSize(fStepSize);
      oRatingBar.setIsIndicator(isIndicator);
   }
   
   void vInit(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_rating, this, true);
      
      oTitle = (Title)this.findViewById(R.id.title);
      oRatingBar = (AppCompatRatingBar) findViewById(R.id.rating_bar);
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

   public void setContentDescription(String contentDescription)
   {
      oRatingBar.setContentDescription(contentDescription);
   }

   public int getNumStars()
   {
      return iNumStars;
   }

   public void setNumStars(int iNumStars)
   {
      this.iNumStars = iNumStars;
   }

   public float getRating()
   {
      return fRating;
   }

   public void setRating(float fRating)
   {
      this.fRating = fRating;
      oRatingBar.setRating(fRating);
   }

   public float getStepSize()
   {
      return fStepSize;
   }

   public void setStepSize(float fStepSize)
   {
      this.fStepSize = fStepSize;
   }

   public boolean isIndicator()
   {
      return isIndicator;
   }

   public void setIsIndicator(boolean isIndicator)
   {
      this.isIndicator = isIndicator;
   }

   public void setOnRatingBarChangeListener(OnRatingBarChangeListener onRatingBarChangeListener)
   {
      oRatingBar.setOnRatingBarChangeListener(onRatingBarChangeListener);
   }
}
