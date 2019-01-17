package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class EditTextUpdatableClearable extends EditTextUpdatable
{
   private Callback oCallback;

   public EditTextUpdatableClearable(final Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public EditTextUpdatableClearable(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
   }
   
   public EditTextUpdatableClearable(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);

      vInit(context);
   }

   private void vInit(Context context)
   {
      updateDeleteIcon(isFocused());

      addTextChangedListener(new TextWatcher() 
      {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) 
          {
          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) 
          {
              updateDeleteIcon(s.toString(), isFocused());
          }

          @Override
          public void afterTextChanged(Editable s) 
          {
          }
      });
      
//      final OnFocusChangeListener ff =  getOnFocusChangeListener();
      
      setOnFocusChangeListener(new OnFocusChangeListener() 
      {
         @Override
         public void onFocusChange(View v, boolean hasFocus) 
         {
            //ff.onFocusChange(v, hasFocus);
//            onFocusChangeListener.onFocusChange(v, hasFocus);
            updateDeleteIcon(hasFocus);
            
            if(!hasFocus)
            {
               if(getOnUpdateListener() != null)
                  getOnUpdateListener().onUpdate((EditText) v);
            }
//            else
//            {
//               ((EditText)v).setHint("");
//            }
            
         }
      });      

      // NOTE: The most important.
      setOnTouchListener(new OnTouchListener() 
      {
         @Override
         public boolean onTouch(View v, MotionEvent event) 
         {
            final int DRAWABLE_RIGHT = 2;
              
            // Only for warning disposal
            v.performClick();

            if (event.getAction() == MotionEvent.ACTION_UP) 
            {
               final Drawable rightDrawable = getCompoundDrawables()[DRAWABLE_RIGHT];
               if (rightDrawable != null && event.getRawX() >= (getRight() - rightDrawable.getBounds().width())) 
               {
                  if (oCallback != null) oCallback.beforeClear(EditTextUpdatableClearable.this);
                     setText("");
                  requestFocus();
                  if (oCallback != null) oCallback.afterClear(EditTextUpdatableClearable.this);
//                      return true;
               }
            }
            return false;
         }
      });
      
   }
   
   // Only for warning disposal
   @Override
   public boolean performClick()
   {
      return super.performClick();
   }

   public void setCallback(Callback callback) 
   {
      this.oCallback = callback;
   }   
   
   private void updateDeleteIcon(boolean focused) 
   {  
      updateDeleteIcon(null, focused);
   }

   private void updateDeleteIcon(final String text, final boolean focused) 
   {
      final String currentText = (text != null) ? text : getText().toString();
      post(new Runnable() 
      {
          @Override
          public void run() 
          {
              if (TextUtils.isEmpty(currentText) || !focused) 
              {
                  setCompoundDrawables(null, null, null, null);
              } 
              else 
              {
//                  setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_cancel, 0);
                  setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_search_api_holo_light, 0);
              }
          }
      });
   }
   
   public interface Callback 
   {  
      void beforeClear(EditText editText);
   
      void afterClear(EditText editText);
   }
}
