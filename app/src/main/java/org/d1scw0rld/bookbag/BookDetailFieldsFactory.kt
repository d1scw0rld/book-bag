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
   private final Book book;
   private final Context context;
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
      if (book != null)
      {
         LinearLayout categoriesLayout = rootView.findViewById(R.id.ll_categories);
         String name;
         StringBuilder valueBuilder = new StringBuilder();

         ArrayList<Property> currencies = dbAdapter.getPropertyValues(DBAdapter.FLD_CURRENCY);

         Price price = null;

         for(Field field : DBAdapter.FIELDS)
         {
            name = field.name;
            valueBuilder.setLength(0);

            if(field.id > 99)
            {
               switch (field.type)
               {
                  case Field.TYPE_TEXT:
                  {
                     switch(field.id)
                     {
                        case DBAdapter.FLD_TITLE:
                           valueBuilder.append(book.title.value);
                           break;

                        case DBAdapter.FLD_DESCRIPTION:
                           valueBuilder.append(book.description.value);
                           break;

                        case DBAdapter.FLD_VOLUME:
                           if(book.volume.value != 0)
                              valueBuilder.append(book.volume.value);
                           break;

                        case DBAdapter.FLD_PAGES:
                           if(book.pages.value != 0)
                              valueBuilder.append(book.pages.value);
                           break;

                        case DBAdapter.FLD_EDITION:
                           if(book.edition.value != 0)
                              valueBuilder.append(book.edition.value);
                           break;

                        case DBAdapter.FLD_ISBN:
                           valueBuilder.append(book.isbn.value);
                           break;

                        case DBAdapter.FLD_WEB:
                           valueBuilder.append(book.web.value);
                           break;
                     }
                  }
                  break;

                  case Field.TYPE_MONEY:
                  {
                     switch(field.id)
                     {
                        case DBAdapter.FLD_PRICE:
                           price = new Price(book.price.value);
                           break;

                        case DBAdapter.FLD_VALUE:
                           price = new Price(book.value.value);
                           break;

                     }

                     if(price == null || price.value == 0)
                        break;

                     Property fieldCurrency = null;
                     for(Property currency : currencies)
                        if(currency.id == price.currencyId)
                        {
                           fieldCurrency = currency;
                           break;
                        }

                     String formattedValue = fieldCurrency == null ?
                           String.format(context.getResources().getString(R.string.amn_vl), price.value / 100, DBAdapter.separator, price.value % 100) :
                           String.format(context.getResources().getString(R.string.amn_vl_crn), price.value / 100, DBAdapter.separator, price.value % 100, fieldCurrency.value);
                     valueBuilder.append(formattedValue);
                  }
                  break;

                  case Field.TYPE_DATE:
                  {
                     Date date = null;
                     switch(field.id)
                     {
                        case DBAdapter.FLD_READ_DATE:
                           date = new Date(book.readDate.value);
                           break;

                        case DBAdapter.FLD_DUE_DATE:
                           date = new Date(book.dueDate.value);
                           break;

                        default:
                           break;
                     }
                     if(date == null || date.toInt() == 0)
                        break;
                     valueBuilder.append(date.toString());
                  }
                  break;
               }
            }
            else
            {
               for(Property property : book.properties)
               {
                  if(property.fieldTypeId == field.id)
                  {
                     switch (field.type)
                     {
                        case Field.TYPE_MULTIFIELD:
                        case Field.TYPE_MULTI_SPINNER:
                           String[] splitNames = field.name.split("\\|");
                           if(splitNames.length > 1)
                              name = splitNames[1];
                           if (valueBuilder.length() > 0)
                              valueBuilder.append(SEP);
                           valueBuilder.append(property.value);
                           break;

                        default:
                           valueBuilder.append(property.value);
                           break;
                     }
                  }
               }
            }

            String finalValue = valueBuilder.toString().trim();
            if(!finalValue.isEmpty())
            {
               if(field.type == Field.TYPE_RATING)
                  addRatingField(categoriesLayout, name, finalValue);
               else if(field.type == Field.TYPE_CHECK_BOX)
                  addCheckBoxField(categoriesLayout, name, finalValue);
               else
                  addField(categoriesLayout, name, finalValue);
            }
         }
      }

   }

   private void addField(LinearLayout rootView, String name, String value)
   {
      View rowView = inflater.inflate(R.layout.row_category_new, null);
      ((TextView) rowView.findViewById(R.id.tv_title)).setText(name);
      ((TextView) rowView.findViewById(R.id.tv_value)).setText(value);

      rootView.addView(rowView);
   }

   private void addRatingField(LinearLayout rootView,
                               String name,
                               String value)
   {
      View rowView = inflater.inflate(R.layout.row_category_rating, null);
      ((TextView) rowView.findViewById(R.id.tv_title)).setText(name);
      ((RatingBar) rowView.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(value));

      rootView.addView(rowView);
   }

   private void addCheckBoxField(LinearLayout rootView,
                                 String name,
                                 String value)
   {
      View rowView = inflater.inflate(R.layout.row_category_check_box, null);
      ((TextView) rowView.findViewById(R.id.tv_title)).setText(name);
      ((CheckBox) rowView.findViewById(R.id.check_box)).setChecked(Boolean.parseBoolean(value));

      rootView.addView(rowView);
   }

}
