package org.d1scw0rld.bookbag.fields;

import org.d1scw0rld.bookbag.R;

import android.annotation.SuppressLint;
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

import androidx.appcompat.widget.AppCompatEditText;

public class EditTextX extends AppCompatEditText
{
   private Context context;
   
   protected OnUpdateListener onUpdateListener = null;

   private Callback callback = null;
   
   private final TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener()
   {
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
      {
         if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
         {

            if(onUpdateListener != null)
               onUpdateListener.onUpdate((EditText) textView);
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
               InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
               inputManager.toggleSoftInput(0, 0);
            }
            
            textView.clearFocus();
            return false; // New
         }
         return false;      
      }
   };
   
   private final View.OnFocusChangeListener onFocusChangeListener = (view, hasFocus) -> {
      updateDeleteIcon(hasFocus);

      if(!hasFocus && onUpdateListener != null)
      {
         onUpdateListener.onUpdate((EditText) view);
      }
   };
   
   private final TextWatcher textWatcher = new TextWatcher()
   {
       @Override
       public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) 
       {
       }

       @Override
       public void onTextChanged(CharSequence charSequence, int start, int before, int count) 
       {
           updateDeleteIcon(charSequence.toString(), isFocused());
       }

       @Override
       public void afterTextChanged(Editable editable) 
       {
       }
   };
   
   @SuppressLint("ClickableViewAccessibility")
   private final OnTouchListener onTouchListener = (view, event) -> {
       final int DRAWABLE_RIGHT = 2;

       if (event.getAction() == MotionEvent.ACTION_UP)
       {
           final Drawable rightDrawable = getCompoundDrawables()[DRAWABLE_RIGHT];
           if (rightDrawable != null && event.getRawX() >= (getRight() - rightDrawable.getBounds().width()))
           {
               if (callback != null) callback.beforeClear(EditTextX.this);
               setText("");
               requestFocus();
               if(onUpdateListener != null)
                  onUpdateListener.onUpdate((EditText) view);
               if (callback != null) callback.afterClear(EditTextX.this);
               return false; // New
           }
       }
       return false;
   };
   
   public EditTextX(final Context context)
   {
      super(context);
      
      init(context);
   }
   
   public EditTextX(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);
   }
   
   public EditTextX(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);

      init(context);
   }

   private void init(Context context)
   {
      this.context = context;
      
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

   public interface OnUpdateListener
   {
      void onUpdate(EditText editText);
   }

   public void setCallback(Callback callback) 
   {
      this.callback = callback;
   }   
   
   private void updateDeleteIcon(boolean focused) 
   {  
      updateDeleteIcon(null, focused);
   }

   private void updateDeleteIcon(final String text, final boolean focused) 
   {
      final String currentText = (text != null) ? text : getText().toString();
      post(() -> {
         if (TextUtils.isEmpty(currentText) || !focused)
         {
            setCompoundDrawables(null, null, null, null);
         }
         else
         {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_search_api_holo_light, 0);
         }
      });
   }
   
   public interface Callback 
   {  
      void beforeClear(EditText editText);
   
      void afterClear(EditText editText);
   }
}
