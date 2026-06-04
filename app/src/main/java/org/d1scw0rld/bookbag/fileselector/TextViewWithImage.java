package org.d1scw0rld.bookbag.fileselector;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Class which combines ImageView and TextView in LinearLayout with horizontal
 * orientation
 */
public class TextViewWithImage extends LinearLayout
{

   /**
    * Image - in this project will be used to display icon representing the file
    * type
    */
   private final ImageView imageView;
   /** Text - in this project will be used to display the file name */
   private final TextView textView;

   public TextViewWithImage(Context context)
   {
      super(context);
      setOrientation(HORIZONTAL);
      imageView = new ImageView(context);
      textView = new TextView(context);

      LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
      layoutParams.weight = 1;
      addView(imageView, layoutParams);
      layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
      addView(textView, layoutParams);
   }

   /** Simple wrapper around the TextView.getText() method. */
   public CharSequence getText()
   {
      return textView.getText();
   }

   /**
    * Simple wrapper around ImageView.setImageResource() method, but if resourceId is
    * equal to -1 this method sets Image's visibility as GONE.
    */
   public void setImageResource(int resourceId)
   {
      if(resourceId == -1)
      {
         imageView.setVisibility(View.GONE);
         return;
      }
      imageView.setImageResource(resourceId);
   }

   /** Simple wrapper around TextView.setText() method. */
   public void setText(String text)
   {
      textView.setText(text);
   }

}
