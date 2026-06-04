package org.d1scw0rld.bookbag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Date;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.dto.Price;
import org.d1scw0rld.bookbag.dto.Property;

import java.util.ArrayList;

public class BookDetailFieldsFactory
{
   private final static String SEP = ", ";

   private final DBAdapter dbAdapter;
   private final Book           book;
   private final Context        context;
   private final LayoutInflater inflater;

   public BookDetailFieldsFactory(Context context, DBAdapter dbAdapter, Book book)
   {
      this.dbAdapter = dbAdapter;
      this.book = book;
      this.context = context;
      inflater = LayoutInflater.from(context);
   }

   public void addFields(ViewGroup rootView)
   {
      // Show the dummy content as text in a TextView.
      if (book != null)
      {
         LinearLayout llCategories = rootView.findViewById(R.id.ll_categories);
         String sName,
               sValue = "";

         ArrayList<Property> alCurrencies = dbAdapter.getPropertyValues(DBAdapter.FLD_CURRENCY);

         Price oPrice = null;

         for(Field field : DBAdapter.FIELDS)
         {
            sName = field.sName;

            if(field.iID > 99)
            {
               switch (field.iType)
               {
                  case Field.TYPE_TEXT:
                  {
                     switch(field.iID)
                     {
                        case DBAdapter.FLD_TITLE:
                           sValue = book.csTitle.value;
                           break;

                        case DBAdapter.FLD_DESCRIPTION:
                           sValue = book.csDescription.value;
                           break;

                        case DBAdapter.FLD_VOLUME:
                           if(book.ciVolume.value != 0)
                              sValue = book.ciVolume.value.toString();
                           break;

                        case DBAdapter.FLD_PAGES:
                           if(book.ciPages.value != 0)
                              sValue = book.ciPages.value.toString();
                           break;

                        case DBAdapter.FLD_EDITION:
                           if(book.ciEdition.value != 0)
                              sValue = book.ciEdition.value.toString();
                           break;

                        case DBAdapter.FLD_ISBN:
                           sValue = book.csISBN.value;
                           break;

                        case DBAdapter.FLD_WEB:
                           sValue = book.csWeb.value;
                           break;
                     }
                  }
                  break;

                  case Field.TYPE_MONEY:
                  {
                     switch(field.iID)
                     {
                        case DBAdapter.FLD_PRICE:
                           oPrice = new Price(book.csPrice.value);
                           break;

                        case DBAdapter.FLD_VALUE:
                           oPrice = new Price(book.csValue.value);
                           break;

                     }

                     if(oPrice == null || oPrice.iValue == 0)
                        break;

                     Property fldCurrency = null;
                     for(Property oCurrency : alCurrencies)
                        if(oCurrency.iID == oPrice.iCurrencyID)
                        {
                           fldCurrency = oCurrency;
                           break;
                        }

                     sValue = fldCurrency == null ?
                           String.format(context.getResources().getString(R.string.amn_vl), oPrice.iValue / 100, DBAdapter.separator, oPrice.iValue % 100) :
                           String.format(context.getResources().getString(R.string.amn_vl_crn), oPrice.iValue / 100, DBAdapter.separator, oPrice.iValue % 100, fldCurrency.sValue);
                  }
                  break;

                  case Field.TYPE_DATE:
                  {
                     Date date = null;
                     switch(field.iID)
                     {
                        case DBAdapter.FLD_READ_DATE:
                           date = new Date(book.ciReadDate.value);
                           break;

                        case DBAdapter.FLD_DUE_DATE:
                           date = new Date(book.ciDueDate.value);
                           break;

                        default:
                           break;
                     }
                     if(date == null || date.toInt() == 0)
                        break;
                     sValue = date.toString();
                  }
                  break;
               }
            }
            else
            {
               for(Property oProperty : book.alProperties)
               {
                  if(oProperty.iFieldTypeID == field.iID)
                  {
                     switch (field.iType)
                     {
                        case Field.TYPE_MULTIFIELD:
                        case Field.TYPE_MULTI_SPINNER:
                           String[] tsNames = field.sName.split("\\|");
                           if(tsNames.length > 1)
                              sName = tsNames[1];
                           sValue += (!sValue.trim().isEmpty() ? SEP : "") + oProperty.sValue;
                           break;

                        default:
                           sValue = oProperty.sValue;
                           break;
                     }
                  }
               }
            }

            if(!sValue.trim().isEmpty())
            {
               if(field.iType == Field.TYPE_RATING)
                  addRatingField(llCategories, sName, sValue);
               else if(field.iType == Field.TYPE_CHECK_BOX)
                  addCheckBoxField(llCategories, sName, sValue);
               else
                  addField(llCategories, sName, sValue);
               sValue = "";
            }
         }
      }

   }

   private void addField(LinearLayout rootView, String sName, String sValue)
   {
      View vRow = inflater.inflate(R.layout.row_category_new, null);
      ((TextView) vRow.findViewById(R.id.tv_title)).setText(sName);
      ((TextView) vRow.findViewById(R.id.tv_value)).setText(sValue);

      rootView.addView(vRow);
   }

   private void addRatingField(LinearLayout rootView,
                               String sName,
                               String sValue)
   {
      View vRow = inflater.inflate(R.layout.row_category_rating, null);
      ((TextView) vRow.findViewById(R.id.tv_title)).setText(sName);
      ((RatingBar) vRow.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(sValue));

      rootView.addView(vRow);
   }

   private void addCheckBoxField(LinearLayout rootView,
                                 String sName,
                                 String sValue)
   {
      View vRow = inflater.inflate(R.layout.row_category_check_box, null);
      ((TextView) vRow.findViewById(R.id.tv_title)).setText(sName);
      ((CheckBox) vRow.findViewById(R.id.check_box)).setChecked(Boolean.parseBoolean(sValue));

      rootView.addView(vRow);
   }

}
