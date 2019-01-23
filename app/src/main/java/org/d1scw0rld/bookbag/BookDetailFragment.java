package org.d1scw0rld.bookbag;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
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

      if(getArguments() != null && getArguments().containsKey(ARG_ITEM_ID))
      {
         // Load the dummy content specified by the fragment
         // arguments. In a real-world scenario, use a Loader
         // to load content from a content provider.
         //         long id = getArguments().getLong(ARG_ITEM_ID);
         oBook = oDbAdapter.getBook(getArguments().getLong(ARG_ITEM_ID));

         Activity activity = this.getActivity();
         CollapsingToolbarLayout appBarLayout = null;
         if(activity != null)
         {
            appBarLayout = activity.findViewById(R.id.toolbar_layout);
         }
         if(appBarLayout != null)
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
   public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View rootView = inflater.inflate(R.layout.book_detail, container, false);

      // Show the dummy content as text in a TextView.
      if (oBook != null)
      {
         LinearLayout llCategories = rootView.findViewById(R.id.ll_categories);
         String sName,
                sValue = "";
         
         ArrayList<Field> alCurrencies = oDbAdapter.getFieldValues(DBAdapter.FLD_CURRENCY);
         
         Price oPrice = null;
         
         for(FieldType fieldType: DBAdapter.FIELD_TYPES)
         {
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
                        break;
                        
                        case DBAdapter.FLD_DUE_DATE:
                           date = new Date(oBook.ciDueDate.value);
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
               sValue = "";
            }            
         }
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
