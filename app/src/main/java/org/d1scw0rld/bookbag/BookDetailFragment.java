package org.d1scw0rld.bookbag;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.d1scw0rld.bookbag.dto.Book;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailNewFragment}
 * on handsets.
 */
public class BookDetailFragment extends Fragment
{
   /**
    * The fragment argument representing the book ID that this fragment
    * represents.
    */
   public static final String BOOK_ID = "book_id";

   private Book book;
   
   private DBAdapter dbAdapter = null;

   private BookDetailFieldsFactory bookDetailFieldsFactory;

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

      dbAdapter = new DBAdapter(getActivity());
      dbAdapter.open();

//      LayoutInflater inflater = LayoutInflater.from(getActivity());

      if(getArguments() != null && getArguments().containsKey(BOOK_ID))
      {
         // Load the dummy content specified by the fragment
         // arguments. In a real-world scenario, use a Loader
         // to load content from a content provider.
         //         long id = getArguments().getLong(ARG_ITEM_ID);
         book = dbAdapter.getBook(getArguments().getLong(BOOK_ID));

         Activity activity = this.getActivity();
         CollapsingToolbarLayout appBarLayout = null;
         if(activity != null)
         {
            appBarLayout = activity.findViewById(R.id.toolbar_layout);
         }
         if(appBarLayout != null)
         {
            appBarLayout.setTitle(book.csTitle.value);
         }

         bookDetailFieldsFactory = new BookDetailFieldsFactory(getContext(), dbAdapter, book);
      }
   }

   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.book_detail, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);

      if (book != null)
      {
         LinearLayout llCategories = view.findViewById(R.id.ll_categories);

         bookDetailFieldsFactory.addFields(llCategories);
      }
   }

   @Override
   public void onPause()
   {
      dbAdapter.close();

      super.onPause();
   }

   @Override
   public void onResume()
   {
      super.onResume();

      dbAdapter.open();
   }

}
