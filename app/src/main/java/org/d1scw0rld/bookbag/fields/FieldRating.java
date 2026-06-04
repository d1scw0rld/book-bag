package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RatingBar.OnRatingBarChangeListener;

import androidx.appcompat.widget.AppCompatRatingBar;

public class FieldRating extends LinearLayout implements Field
{
   private Title title;

   private boolean isIndicator;
   
   private int numStars;
   
   private float rating,
                 stepSize;

   private AppCompatRatingBar ratingBar;
   
   public FieldRating(Context context)
   {
      super(context);
      
      init(context);
   }
   
   public FieldRating(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
      
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FieldRating, 0, 0);
      
      String titleText = typedArray.getString(R.styleable.FieldRating_title);
      int titleValueColor = typedArray.getColor(R.styleable.FieldRating_titleColor, 0);
      int titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldRating_titleTextSize, 0);
      int titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldRating_titleLineSize, 0);
      String contentDescription = typedArray.getString(R.styleable.FieldRating_android_contentDescription);
      setNumStars(typedArray.getInteger(R.styleable.FieldRating_android_numStars, 5));
      setRating(typedArray.getFloat(R.styleable.FieldRating_android_rating, 0.0f));
      setStepSize(typedArray.getFloat(R.styleable.FieldRating_android_stepSize, 0.5f));
      setIsIndicator(typedArray.getBoolean(R.styleable.FieldRating_android_isIndicator, false));

      typedArray.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(titleText);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);
      ratingBar.setContentDescription(contentDescription);
      ratingBar.setNumStars(numStars);
      ratingBar.setRating(rating);
      ratingBar.setStepSize(stepSize);
      ratingBar.setIsIndicator(isIndicator);
   }
   
   void init(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_rating, this, true);

      title = findViewById(R.id.title);
      ratingBar = findViewById(R.id.rating_bar);
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
      ratingBar.setContentDescription(contentDescription);
   }

   public int getNumStars()
   {
      return numStars;
   }

   public void setNumStars(int numStars)
   {
      this.numStars = numStars;
   }

   public float getRating()
   {
      return rating;
   }

   public void setRating(float rating)
   {
      this.rating = rating;
      ratingBar.setRating(rating);
   }

   public float getStepSize()
   {
      return stepSize;
   }

   public void setStepSize(float stepSize)
   {
      this.stepSize = stepSize;
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
      ratingBar.setOnRatingBarChangeListener(onRatingBarChangeListener);
   }
}
