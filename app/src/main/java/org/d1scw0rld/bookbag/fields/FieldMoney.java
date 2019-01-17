package org.d1scw0rld.bookbag.fields;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d1scw0rld.bookbag.DBAdapter;
import org.d1scw0rld.bookbag.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class FieldMoney extends LinearLayout
{
   private Title oTitle;
   private Spinner oSpinner;
   private EditTextX oEditTextX;
//   private Price oPrice = new Price();
//   private ArrayList<Field> alCurrencies;
//   private OnUpdateListener onUpdateListener;
//   private int iSelected = 0;
   
  
   public FieldMoney(Context context)
   {
      super(context);
      
      vInit(context);
   }

   public FieldMoney(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      vInit(context);
      
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldMoney, 0, 0);
      
      String title = a.getString(R.styleable.FieldMoney_title);
      int titleValueColor = a.getColor(R.styleable.FieldMoney_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldMoney_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldMoney_titleLineSize, 0);
      String contentDescription = a.getString(R.styleable.FieldMoney_android_contentDescription);
      String hint = a.getString(R.styleable.FieldMoney_android_hint);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(title);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);
      
      oSpinner.setContentDescription(contentDescription);
      oEditTextX.setHint(hint);
   }
   
   void vInit(Context context)
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_money, this, true);
      
      oTitle = (Title)this.findViewById(R.id.title);
      oSpinner = (Spinner) this.findViewById(R.id.action_select_type);
      oEditTextX = (EditTextX) this.findViewById(R.id.editTextX);
      oEditTextX.setFilters(new InputFilter[] {new DecimalDigitsInputFilter()});
//      oEditTextX.setOnUpdateListener(new EditTextX.OnUpdateListener()
//      {
//         @Override
//         public void onUpdate(EditText et)
//         {
//            String sValue = et.getText().toString();
//            sValue = sValue.replace(" ", "");
//            sValue = sValue.replace("-,", "-0,");
//            int iValue;
//            if(sValue.isEmpty() || sValue.matches("-|,|-,"))
//               iValue = 0;
//            else
//            {
//               String [] tsValue = sValue.split("\\" + DBAdapter.separator);
////               String [] tsValue = sValue.split("\\.");
//                
//               iValue = (tsValue[0].isEmpty() ? 0 : Integer.valueOf(tsValue[0])*100) + (tsValue.length == 2 ? (sValue.contains("-") ? -1 : 1) * (tsValue[1].length() == 1 ? 10 : 1) * Integer.valueOf(tsValue[1]) : 0);
//            }
//            oPrice.iValue = iValue;
//            onUpdateListener.onUpdate(FieldMoney.this);
//         }
//      });
//
//      setValue(oPrice.iValue);
//      
//      String tCurrencies[] = new String[this.alCurrencies.size()];
//      for(int i = 0; i < alCurrencies.size(); i++)
//      {
//         tCurrencies[i] = alCurrencies.get(i).sValue;
//         if(oPrice != null && oPrice.iCurrencyID == alCurrencies.get(i).iID)
//            iSelected = i;
//      }
//      
//      ArrayAdapter<String> oArrayAdapter = new ArrayAdapter<String> (context, R.layout.spinner_item, tCurrencies);
//      oSpinner.setAdapter(oArrayAdapter);
//      oSpinner.setSelection(iSelected);
//
//      oSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//      {
//         @Override
//         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
//         {
//            oPrice.iCurrencyID = FieldMoney.this.alCurrencies.get(pos).iID;
//            onUpdateListener.onUpdate(FieldMoney.this);
//         }
//
//         @Override
//         public void onNothingSelected(AdapterView<?> parent)
//         {
//            // TODO Auto-generated method stub
//            
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
   
   public void setValue(int iValue)
   {
      oEditTextX.setText(String.format(getResources().getString(R.string.amn_vl), iValue / 100, DBAdapter.separator, iValue % 100));  
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
      oSpinner.setContentDescription(contentDescription);
   }
   
   public void setHint(String hint)
   {
      oEditTextX.setHint(hint);
   }
   
//   public Price getPrice()
//   {
//      return oPrice;
//   }
//
//   public void setPrice(Price oPrice)
//   {
//      this.oPrice = oPrice;
//      setValue(oPrice.iValue);
//   }

   public void setAdapter(ArrayAdapter<?> adapter)
   {
      oSpinner.setAdapter(adapter);
   }
   
   public void setOnItemSelectedListener(OnItemSelectedListener listener)
   {
      oSpinner.setOnItemSelectedListener(listener);
   }

   public void setSelection(int position)
   {
      if(position >= 0)
         oSpinner.setSelection(position);
   }
   
   public interface OnUpdateListener
   {
      public void onUpdate(FieldMoney oFieldMoney);
   }
   
//   public void setUpdateListener(OnUpdateListener onUpdateListener)
//   {
////      oEditTextX.setOnUpdateListener(onUpdateListener);
//      this.onUpdateListener = onUpdateListener;
//      
//   }
   
   private class DecimalDigitsInputFilter implements InputFilter 
   {
      Pattern mPattern;

      public DecimalDigitsInputFilter() 
      {
//          mPattern = Pattern.compile("([-1-9]{1}[0-9]{0,2}([0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?) \u20ac");
//          mPattern = Pattern.compile("((([\\-1-9]?\\d{0,3}))|([\\-1-9]?\\d{0,3}(\\.\\d?)?)|([\\-1-9]?\\d{0,3}(\\.\\d{0,2})?))");
          mPattern = Pattern.compile("0*[1-9]?\\d{0,3}(\\" + DBAdapter.separator + "\\d{0,2})?");
//          mPattern = Pattern.compile("((((\\-\\d)?\\d{0,6}))|([\\-1-9]?\\d{0,4}(\\.\\d?)?)|([\\-1-9]?\\d{0,3}(\\.\\d{0,2})?)) \u20ac");
      }

      @Override
      public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) 
      {
         String formatedSource = source.subSequence(start, end).toString();

         String destPrefix = dest.subSequence(0, dstart).toString();

         String destSuffix = dest.subSequence(dend, dest.length()).toString();

         String result = destPrefix + formatedSource + destSuffix;

         result = result.replace(",", ".");
//         result = result.replace(".", ",");

         Matcher matcher = mPattern.matcher(result);

         if(matcher.matches())
            return null;
         
         return "";
      }
   }

   public void setUpdateListener(EditTextX.OnUpdateListener onUpdateListener)
   {
      oEditTextX.setOnUpdateListener(onUpdateListener);
   }   
}
