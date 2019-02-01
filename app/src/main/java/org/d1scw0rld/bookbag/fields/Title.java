package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Title extends LinearLayout
{
   TextView title;
   LinearLayout line;
   
   public Title(Context context)
   {
      this(context, null);
   }

   public Title(Context context, AttributeSet attrs)
   {
      super(context, attrs);
      if(!isInEditMode())
      {
         TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Title, 0, 0);
         String titleText = a.getString(R.styleable.Title_text);
         
         int valueColor = a.getColor(R.styleable.Title_color, getResources().getColor(android.R.color.black));
         int textSize = a.getDimensionPixelOffset(R.styleable.Title_textSize, 0);
         int lineSize = a.getDimensionPixelOffset(R.styleable.Title_lineSize, 0);
         
         a.recycle();

         setOrientation(LinearLayout.VERTICAL);
         setGravity(Gravity.CENTER_VERTICAL);

         LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.title, this, true);

         title = findViewById(R.id.tv_title);
         line = findViewById(R.id.ll_line);
         title.setText(titleText);
         title.setTextColor(valueColor);
         if(textSize > 0)
            title.setTextSize(textSize);

         line.setBackgroundColor(valueColor);
         android.view.ViewGroup.LayoutParams params = line.getLayoutParams();
         // Changes the height and width to the specified *pixels*
         if(lineSize > 0)
         {
            params.height = lineSize;
            params.width = LayoutParams.MATCH_PARENT;
            line.setLayoutParams(params);
         }
      }
   }
   
   public void setText(String text)
   {
      title.setText(text);
   }

   public void setText(int resid)
   {
      title.setText(resid);
   }
   
   public void setTextSize(int textSize)
   {
      if(textSize > 0)
         title.setTextSize(textSize);
   }
   
   public void setColor(int valueColor)
   {
      title.setTextColor(valueColor);
      line.setBackgroundColor(valueColor);
   }
   
   public void setLineSize(int lineSize)
   {
      android.view.ViewGroup.LayoutParams params = line.getLayoutParams();
      if(lineSize > 0)
      {
         params.height = lineSize;
         params.width = LayoutParams.MATCH_PARENT;
         line.setLayoutParams(params);
      }
   }
}
