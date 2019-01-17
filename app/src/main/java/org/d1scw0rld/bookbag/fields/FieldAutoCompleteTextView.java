package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.ArrayFieldsAdapter;
import org.d1scw0rld.bookbag.R;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.fields.AutoCompleteTextViewX.Callback;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

public class FieldAutoCompleteTextView extends LinearLayout
{
   
   private Title oTitle;
   private AutoCompleteTextViewX oAutoCompleteTextViewX;
//   private OnUpdateListener onUpdateListener;

   public FieldAutoCompleteTextView(Context context)
   {
      super(context);
      
      vInit(context);
   }
   
   public FieldAutoCompleteTextView(Context context, Field oField, ArrayList<Field> alFieldValues)
   {
      super(context);
      
      vInit(context, oField, alFieldValues);
   }

   public FieldAutoCompleteTextView(Context context, AttributeSet attrs)
   {
      super(context, attrs);
      
      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldAutoCompleteTextView, 0, 0);

      String titleText = a.getString(R.styleable.FieldAutoCompleteTextView_title);
      int titleValueColor = a.getColor(R.styleable.FieldAutoCompleteTextView_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleLineSize, 0);
      String text = a.getString(R.styleable.FieldAutoCompleteTextView_android_text);
      String hint = a.getString(R.styleable.FieldAutoCompleteTextView_android_hint);
      
      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(titleText);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      
      oAutoCompleteTextViewX.setText(text);
      oAutoCompleteTextViewX.setHint(hint);
   }
   
   public FieldAutoCompleteTextView(Context context, AttributeSet attrs, Field oField, ArrayList<Field> alFieldValues)
   {
      super(context, attrs);
      
      vInit(context, oField, alFieldValues);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldAutoCompleteTextView, 0, 0);

      String titleText = a.getString(R.styleable.FieldAutoCompleteTextView_title);
      int titleValueColor = a.getColor(R.styleable.FieldAutoCompleteTextView_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleLineSize, 0);
      String text = a.getString(R.styleable.FieldAutoCompleteTextView_android_text);
      String hint = a.getString(R.styleable.FieldAutoCompleteTextView_android_hint);
      
      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(titleText);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      
      oAutoCompleteTextViewX.setText(text);
      oAutoCompleteTextViewX.setHint(hint);
   }
   
   public void vInit(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_auto_complete_text_view, this, true);
      
      oTitle = (Title)this.findViewById(R.id.title);
      oAutoCompleteTextViewX = (AutoCompleteTextViewX) this.findViewById(R.id.autoCompleteTextView);
      oAutoCompleteTextViewX.setThreshold(1);
      
//      oAutoCompleteTextViewX.setOnUpdateListener(new AutoCompleteTextViewX.OnUpdateListener()
//      {
//         @Override
//         public void onUpdate(EditText et)
//         {
////            ((Field)oFieldAutoCompleteTextView.getTag()).sValue = et.getText().toString();
//            onUpdateListener.onUpdate(et);
//         }
//      });
   }   
   
   public void vInit(Context context, Field oField, final ArrayList<Field> alFieldValues)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_auto_complete_text_view, this, true);
      
      oTitle = (Title)this.findViewById(R.id.title);
      oAutoCompleteTextViewX = (AutoCompleteTextViewX) this.findViewById(R.id.autoCompleteTextView);
      oAutoCompleteTextViewX.setThreshold(1);
      
      if(oField != null && !oField.sValue.isEmpty())
         oAutoCompleteTextViewX.setText(oField.sValue);
//      oFieldAutoCompleteTextView.setTag(f);

//      int iSelected;
//      final String tFieldValues[] = new String[alFieldValues.size()];
//      for(int i = 0; i < alFieldValues.size(); i++)
//      {
//         tFieldValues[i] = alFieldValues.get(i).sValue;
////         if(oField != null && oField.iID == alFieldValues.get(i).iID)
////            iSelected = i;
//      }

//    ArrayAdapter<String> oArrayAdapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item, tFieldValues);
      ArrayFieldsAdapter oArrayAdapter = new ArrayFieldsAdapter(context, android.R.layout.select_dialog_item, alFieldValues);
      oAutoCompleteTextViewX.setAdapter(oArrayAdapter);
//      oAutoCompleteTextViewX.setOnItemClickListener(new OnItemClickListener()
//      {
//         @Override
//         public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId)
//         {
////            Field fldSelected = (Field)adapter.getItemAtPosition(position);
////            ((Field)oFieldAutoCompleteTextView.getTag()).copy(fldSelected);
//            onUpdateListener.onUpdate(FieldAutoCompleteTextView.this, position);
////            String selection = (String) adapter.getItemAtPosition(position);
//////            int pos = -1;
////            for (int i = 0, pos = -1; i < tAuthors.length && pos == -1; i++) 
////            {
////               if (tFieldValues[i].equals(selection)) 
////               {
////                  pos = i;
////                  ((Field)oFieldAutoCompleteTextView.getTag()).copy(alFields.get(pos));
////               }
////            }
//         }
//      });
      
//      oAutoCompleteTextViewX.setOnUpdateListener(new AutoCompleteTextViewX.OnUpdateListener()
//      {
//         @Override
//         public void onUpdate(EditText et)
//         {
////            ((Field)oFieldAutoCompleteTextView.getTag()).sValue = et.getText().toString();
//            onUpdateListener.onUpdate(et);
//         }
//      });
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
   
   public void setText(String text)
   {
      oAutoCompleteTextViewX.setText(text);
   }

   public void setText(int resid)
   {
      oAutoCompleteTextViewX.setText(resid);;
   }
   
   public Editable getText()
   {
      return oAutoCompleteTextViewX.getText();
   }
   
   public void setHint(String hint)
   {
      oAutoCompleteTextViewX.setHint(hint);
   }

   public void setHint(int resid)
   {
      oAutoCompleteTextViewX.setHint(resid);;
   }
   
   public void setThreshold(int threshold)
   {
      oAutoCompleteTextViewX.setThreshold(threshold);
   }
   
   public void setAdapter(ArrayAdapter<?> adapter)
   {
      oAutoCompleteTextViewX.setAdapter(adapter);
   }
   
   public void setUpdateListener(AutoCompleteTextViewX.OnUpdateListener onUpdateListener)
   {
      oAutoCompleteTextViewX.setOnUpdateListener(onUpdateListener);
   }
   
//   public void setUpdateListener(OnUpdateListener onUpdateListener)
//   {
//      this.onUpdateListener = onUpdateListener;
//   }
   
   public void setCallback(Callback callback)
   {
      oAutoCompleteTextViewX.setCallback(callback);
   }   
   
   public void setOnItemSelectedListener(OnItemSelectedListener l)
   {
      oAutoCompleteTextViewX.setOnItemSelectedListener(l);
   }

   public void setOnItemClickListener(OnItemClickListener l)
   {
      oAutoCompleteTextViewX.setOnItemClickListener(l);
   }
 
//   public interface OnUpdateListener
//   {
//      public void onUpdate(FieldAutoCompleteTextView oFieldAutoCompleteTextView, int position);
//      public void onUpdate(EditText et);
//   }
}
