package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class EditTextX extends android.support.v7.widget.AppCompatEditText
{
   private Context oContext;
   
   protected OnUpdateListener onUpdateListener = null;

   private Callback oCallback = null;
   
   private OnEditorActionListener onEditorActionListener = new OnEditorActionListener()
   {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
      {
         if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
         {

            if(onUpdateListener != null)
               onUpdateListener.onUpdate((EditText) v);
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
               InputMethodManager inputManager = (InputMethodManager) oContext.getSystemService(Context.INPUT_METHOD_SERVICE);
               inputManager.toggleSoftInput(0, 0);
            }
            
            v.clearFocus();
            return false; // New
         }
         return false;      
      }
   };
   
   private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener()
   {
      @Override
      public void onFocusChange(View v, boolean hasFocus)
      {
         updateDeleteIcon(hasFocus);
         
         if(!hasFocus && onUpdateListener != null)
         {
            onUpdateListener.onUpdate((EditText) v);
         }
//         else
//         {
//            ((EditText)v).setHint("");
//         }
      }
   };   
   
   private TextWatcher textWatcher = new TextWatcher() 
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
   };
   
   private OnTouchListener onTouchListener =  new OnTouchListener() 
   {
       @Override
       public boolean onTouch(View v, MotionEvent event) 
       {
           final int DRAWABLE_RIGHT = 2;

           if (event.getAction() == MotionEvent.ACTION_UP) 
           {
               final Drawable rightDrawable = getCompoundDrawables()[DRAWABLE_RIGHT];
               if (rightDrawable != null && event.getRawX() >= (getRight() - rightDrawable.getBounds().width())) 
               {
                   if (oCallback != null) oCallback.beforeClear(EditTextX.this);
                   setText("");
                   requestFocus();
                   if(onUpdateListener != null)
                      onUpdateListener.onUpdate((EditText) v);                   
                   if (oCallback != null) oCallback.afterClear(EditTextX.this);
                   return false; // New
               }
           }
           return false;
       }
   };
   
   public EditTextX(final Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public EditTextX(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
   }
   
   public EditTextX(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);

      vInit(context);
   }

   private void vInit(Context context)
   {
      oContext = context;
      
      updateDeleteIcon(isFocused());
      
      setOnEditorActionListener(onEditorActionListener);
      
      setOnFocusChangeListener(onFocusChangeListener);      
      
      addTextChangedListener(textWatcher);

      // NOTE: The most important.
      setOnTouchListener(onTouchListener);      
   }

   @Override
   public boolean onKeyPreIme(int keyCode, KeyEvent event)
   {
      if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) 
      {
         clearFocus();
         return false;
      }         
      return false;
   }

   public void setOnUpdateListener(OnUpdateListener onUpdateListener)
   {
      this.onUpdateListener = onUpdateListener;
   }

   public OnUpdateListener getOnUpdateListener()
   {
      return onUpdateListener;
   }

   public interface OnUpdateListener
   {
      void onUpdate(EditText et);
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
//                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_cancel, 0);
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
