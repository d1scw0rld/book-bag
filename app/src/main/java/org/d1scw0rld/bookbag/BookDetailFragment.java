package org.d1scw0rld.bookbag;

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
 * A fragment representing a single book detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link BookFragment}
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
   private LinearLayout llCategories;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      dbAdapter = new DBAdapter(getActivity());
      dbAdapter.open();

      if(getArguments() != null && getArguments().containsKey(BOOK_ID))
      {
         book = dbAdapter.getBook(getArguments().getLong(BOOK_ID));

         bookDetailFieldsFactory = new BookDetailFieldsFactory(getContext(), dbAdapter, book);
      }
   }

   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.fragment_book_detail, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);

      CollapsingToolbarLayout appBarLayout = requireActivity().findViewById(R.id.toolbar_layout);

      if(appBarLayout != null)
         appBarLayout.setTitle(book.csTitle.value);

      llCategories = view.findViewById(R.id.ll_categories);
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

      if (book != null)
         bookDetailFieldsFactory.addFields(llCategories);
   }
}
