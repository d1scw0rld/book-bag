package org.d1scw0rld.bookbag.fields;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class EditTextUpdatable extends EditText
{
   private OnUpdateListener onUpdateListener = null;
   
   private Context oContext;

   private OnEditorActionListener onEditorActionListener = new OnEditorActionListener()
   {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
      {
         if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
         {

            if(onUpdateListener != null)
               onUpdateListener.onUpdate((EditText) v);
            
            InputMethodManager inputManager = (InputMethodManager) oContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.toggleSoftInput(0, 0);
            
            v.clearFocus();
            return true; 
         }
         return false;      
      }
   };
   
   private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener()
   {
      @Override
      public void onFocusChange(View v, boolean hasFocus)
      {
         if(!hasFocus)
         {
            onUpdateListener.onUpdate((EditText) v);
         }
//         else
//         {
//            ((EditText)v).setHint("");
//         }
      }
   };

   public EditTextUpdatable(final Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public EditTextUpdatable(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
   }
   
   public EditTextUpdatable(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);

      vInit(context);
   }

   private void vInit(Context context)
   {
      oContext = context;
      
      setOnEditorActionListener(onEditorActionListener);
      
      setOnFocusChangeListener(onFocusChangeListener);      
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

}
