package org.d1scw0rld.bookbag;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import org.d1scw0rld.bookbag.dto.FieldType;
import org.d1scw0rld.bookbag.dto.Price;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookDetailFragment extends Fragment
{
   /**
    * The fragment argument representing the item ID that this fragment
    * represents.
    */
   public static final String ARG_ITEM_ID = "item_id";

   /**
    * The dummy content this fragment is presenting.
    */
//   private DummyContent.DummyItem mItem;
   private final static String SEP = ", ";
   
   private Book oBook;
   
   private DBAdapter oDbAdapter = null;
   
   private LayoutInflater oInflater;

   /**
    * Mandatory empty constructor for the fragment manager to instantiate the
    * fragment (e.g. upon screen orientation changes).
    */
   public BookDetailFragment()
   {
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      
      oDbAdapter = new DBAdapter(getActivity());
      oDbAdapter.open();
      
      oInflater = LayoutInflater.from(getActivity());

      if (getArguments().containsKey(ARG_ITEM_ID))
      {
         // Load the dummy content specified by the fragment
         // arguments. In a real-world scenario, use a Loader
         // to load content from a content provider.
//         long id = getArguments().getLong(ARG_ITEM_ID);
         oBook = oDbAdapter.getBook(getArguments().getLong(ARG_ITEM_ID));

         Activity activity = this.getActivity();
         CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
         if (appBarLayout != null)
         {
            appBarLayout.setTitle(oBook.csTitle.value);
         }
      }
   }

   @Override
   public void onPause()
   {
      oDbAdapter.close();
      
      super.onPause();
   }

   @Override
   public void onResume()
   {
      super.onResume();
      
      oDbAdapter.open();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View rootView = inflater.inflate(R.layout.book_detail, container, false);

      // Show the dummy content as text in a TextView.
      if (oBook != null)
      {
//         ((TextView) rootView.findViewById(R.id.book_detail)).setText(mItem.details);
//         ((TextView) rootView.findViewById(R.id.book_detail)).setText(mItem.sDescription);
         
         LinearLayout llCategories = (LinearLayout) rootView.findViewById(R.id.ll_categories);
         String sName = "", 
                sValue = "";
         
         ArrayList<Field> alCurrencies = oDbAdapter.getFieldValues(DBAdapter.FLD_CURRENCY);
         
         Price oPrice = null;
         
         for(FieldType fieldType: DBAdapter.FIELD_TYPES)
         {
//            sName = fieldType.sName + ":";
            sName = fieldType.sName;
            
            if(fieldType.iID > 99)
            {
               switch (fieldType.iType)
               {
                  case FieldType.TYPE_TEXT:
                  {
                     switch(fieldType.iID)
                     {
                        case DBAdapter.FLD_TITLE:
                           sValue = oBook.csTitle.value;
                        break;
                        
                        case DBAdapter.FLD_DESCRIPTION:
                           sValue = oBook.csDescription.value;
                        break;

                        case DBAdapter.FLD_VOLUME:
                           if(oBook.ciVolume.value != 0)
                              sValue = oBook.ciVolume.value.toString();
                        break;

                        case DBAdapter.FLD_PAGES:
                           if(oBook.ciPages.value != 0)
                              sValue = oBook.ciPages.value.toString();
                        break;
                         
                        case DBAdapter.FLD_EDITION:
                           if(oBook.ciEdition.value != 0)
                              sValue = oBook.ciEdition.value.toString();
                        break;

                        case DBAdapter.FLD_ISBN:
                           sValue = oBook.csISBN.value;
                        break;
                         
                        case DBAdapter.FLD_WEB:
                           sValue = oBook.csWeb.value;
                        break;
                     }
                  }
                  break;
                  
                  case FieldType.TYPE_MONEY:
                  {
                     switch(fieldType.iID)
                     {
                        case DBAdapter.FLD_PRICE:
                           oPrice = new Price(oBook.csPrice.value);
                        break;
                        
                        case DBAdapter.FLD_VALUE:
                           oPrice = new Price(oBook.csValue.value);
                        break;                  
                        
                     }
                     
                     if(oPrice == null || oPrice.iValue == 0)
                        break;

                     Field fldCurrency = null;
                     for(Field oCurrency : alCurrencies)
                        if(oCurrency.iID == oPrice.iCurrencyID)
                        {
                           fldCurrency = oCurrency;
                           break;
                        }
                     
                     sValue = fldCurrency == null ? 
                              String.format(getResources().getString(R.string.amn_vl), oPrice.iValue / 100, DBAdapter.separator, oPrice.iValue % 100) :  
                              String.format(getResources().getString(R.string.amn_vl_crn), oPrice.iValue / 100, DBAdapter.separator, oPrice.iValue % 100, fldCurrency.sValue);
                  }
                  break;
                  
                  case FieldType.TYPE_DATE:
                  {
                     Date date = null;
                     switch(fieldType.iID)
                     {
                        case DBAdapter.FLD_READ_DATE:
                           date = new Date(oBook.ciReadDate.value);
                           
//                           sValue = String.valueOf(new Date(oBook.ciReadDate.value).toString());
                        break;
                        
                        case DBAdapter.FLD_DUE_DATE:
                           date = new Date(oBook.ciDueDate.value);
                           
//                           sValue = String.valueOf(new Date(oBook.ciDueDate.value).toString());
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
               for(Field oField: oBook.alFields)
               {
                  if(oField.iTypeID == fieldType.iID)
                  {
                     switch (fieldType.iType)
                     {
//                        case FieldType.TYPE_TEXT_AUTOCOMPLETE:
//                        case FieldType.TYPE_SPINNER:
//                           sValue = oField.sValue;
//                        break;

                        case FieldType.TYPE_MULTIFIELD:
                        case FieldType.TYPE_MULTI_SPINNER:
                           String tsNames[] = fieldType.sName.split("\\|");
                           if(tsNames.length > 1)
                              sName = tsNames[1];
                           sValue += (!sValue.trim().isEmpty() ? SEP : "") + oField.sValue;  
                        break;
                        
                        default:
                           sValue = oField.sValue;
                           break;
                     }
                  }
               }
            }
            
            if(!sValue.trim().isEmpty())
            {
               if(fieldType.iType == FieldType.TYPE_RATING)
                  addRatingField(llCategories, sName, sValue);
               else if(fieldType.iType == FieldType.TYPE_CHECK_BOX)
                  addCheckBoxField(llCategories, sName, sValue);
               else
                  addField(llCategories, sName, sValue);
               sName = "";
               sValue = "";
            }            
         }
/*         
         for(FieldType fieldType: DBAdapter.FIELD_TYPES)
         {
            if(fieldType.iID < 100)
            {
               for(Field oField: oBook.alFields)
               {
                  if(oField.iTypeID == fieldType.iID)
                  {
                     if(!sName.trim().isEmpty())
                        sValue += "\n" + oField.sValue;
                     else
                     {
                        sName = fieldType.sName + ":";
                        sValue = oField.sValue;
                     }
                  }
               }
//               if(!sName.trim().isEmpty())
//               {
//                  addField(llCategories, sName, sValue);
//                  sName = "";
//                  sValue = "";
//               }
            }
            else
            {
               switch(fieldType.iID)
               {
                  case DBAdapter.FLD_DESCRIPTION:
                     if(!oBook.csDescription.value.trim().isEmpty())
                     {
                        sName = "Description:";
                        sValue = oBook.csDescription.value;
                     }
                  break;
                  
                  case DBAdapter.FLD_VOLUME:
                     if(oBook.ciVolume.value != 0)
                     {
                        sName = "Volume:";
                        sValue = String.valueOf(oBook.ciVolume.value);
                     }
                  break;
                  
                  case DBAdapter.FLD_PUBLICATION_DATE:
                     if(oBook.ciPublicationDate.value != 0)
                     {
                        sName = "Publication Date:";
                        sValue = String.valueOf(oBook.ciPublicationDate.value);
                     }
                  break;
                  
                  case DBAdapter.FLD_PAGES:
                     if(oBook.ciPages.value != 0)
                     {
                        sName = "Pages:";
                        sValue = String.valueOf(oBook.ciPages.value);
                     }
                  break;
                  
//                  case DBAdapter.FLD_PRICE:
//                     if(mItem.iPrice != 0)
//                     {
//                        sName = "Price:";
//                        String sPrice = String.format(getResources().getString(R.string.amn_vl), mItem.iPrice / 100, mItem.iPrice % 100);
//                        sValue = String.valueOf(sPrice);
//                     }
//                  break;

                  case DBAdapter.FLD_PRICE:
                     if(!oBook.csPrice.value.trim().isEmpty())
                     {
                        sName = "Price:";
                        Price oPrice = new Price(oBook.csPrice.value);
                        Field fldCurrency = null;
                        for(Field oField : alCurrencies)
                           if(oField.iID == oPrice.iCurrencyID)
                           {
                              fldCurrency = oField;
                              break;
                           }
//                        char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
                        String sPrice = fldCurrency == null ? 
                                          String.format(getResources().getString(R.string.amn_vl), oPrice.iValue / 100, DBAdapter.separator, oPrice.iValue % 100) :  
                                          String.format(getResources().getString(R.string.amn_vl_crn), oPrice.iValue / 100, DBAdapter.separator, oPrice.iValue % 100, fldCurrency.sValue);
                        sValue = String.valueOf(sPrice);
                     }
                  break;
                  
//                  case DBAdapter.FLD_VALUE:
//                     if(mItem.iValue != 0)
//                     {
//                        sName = "Value:";
//                        
//                        String sBookValue = String.format(getResources().getString(R.string.amn_vl), mItem.iValue / 100, mItem.iValue % 100);
//                        sValue = String.valueOf(sBookValue);
//                     }
//                  break;

                  case DBAdapter.FLD_VALUE:
                     if(!oBook.csValue.value.trim().isEmpty())
                     {
                        sName = "Value:";
                        Price oBookValue = new Price(oBook.csValue.value);
                        Field fldCurrency = null;
                        for(Field oField : alCurrencies)
                           if(oField.iID == oBookValue.iCurrencyID)
                           {
                              fldCurrency = oField;
                              break;
                           }
//                        char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
                        String sBookValue = fldCurrency == null ? 
                                 String.format(getResources().getString(R.string.amn_vl), oBookValue.iValue / 100, DBAdapter.separator, oBookValue.iValue % 100) :  
                                 String.format(getResources().getString(R.string.amn_vl_crn), oBookValue.iValue / 100, DBAdapter.separator, oBookValue.iValue % 100, fldCurrency.sValue);

                        sValue = String.valueOf(sBookValue);
                     }
                  break;
                  
                  case DBAdapter.FLD_DUE_DATE:
                     if(oBook.ciDueDate.value != 0)
                     {
                        sName = "Due Date:";
                        sValue = String.valueOf(new Date(oBook.ciDueDate.value).toString());
                     }
                  break;
                  
                  case DBAdapter.FLD_READ_DATE:
                     if(oBook.ciReadDate.value != 0)
                     {
                        sName = "Read Date:";
                        sValue = String.valueOf(new Date(oBook.ciReadDate.value).toString());
                     }
                  break;
                  
                  case DBAdapter.FLD_EDITION:
                     if(oBook.ciEdition.value != 0)
                     {
                        sName = "Edition:";
                        sValue = String.valueOf(oBook.ciEdition.value);
                     }
                  break;
                  
                  case DBAdapter.FLD_ISBN:
                     if(!oBook.csISBN.value.trim().isEmpty())
                     {
                        sName = "ISBN:";
                        sValue = String.valueOf(oBook.csISBN.value);
                     }
                  break;
                  
                  case DBAdapter.FLD_WEB:
                     if(!oBook.csWeb.value.trim().isEmpty())
                     {
                        sName = "Web:";
                        sValue = String.valueOf(oBook.csWeb.value);
                     }
                  break;
               }
            }
            if(!sName.trim().isEmpty())
            {
               addField(llCategories, sName, sValue);
               sName = "";
               sValue = "";
            }
         }
         */
      }

      return rootView;
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);
   }

   private void addField(LinearLayout rootView, String sName, String sValue)
   {
      View vRow = oInflater.inflate(R.layout.row_category_new, null);
      ((TextView) vRow.findViewById(R.id.tv_title)).setText(sName);
      ((TextView) vRow.findViewById(R.id.tv_value)).setText(sValue);
      
      rootView.addView(vRow);  
   }

   private void addRatingField(LinearLayout rootView,
                               String sName,
                               String sValue)
   {
      View vRow = oInflater.inflate(R.layout.row_category_rating, null);
      ((TextView) vRow.findViewById(R.id.tv_title)).setText(sName);
      ((RatingBar) vRow.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(sValue));
      
      rootView.addView(vRow);
   }
   
   private void addCheckBoxField(LinearLayout rootView,
                                 String sName,
                                 String sValue)
   {
      View vRow = oInflater.inflate(R.layout.row_category_check_box, null);
      ((TextView) vRow.findViewById(R.id.tv_title)).setText(sName);
      ((CheckBox) vRow.findViewById(R.id.check_box)).setChecked(Boolean.parseBoolean(sValue));

      rootView.addView(vRow);
   }
   
}
