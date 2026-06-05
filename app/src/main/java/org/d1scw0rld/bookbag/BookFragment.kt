package org.d1scw0rld.bookbag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;

public class BookFragment extends BaseFragment implements IBackPressListener
{
   private static final String SAVED_BOOK_ID  = "saved_book_id";
   private long bookId = 0;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.fragment_book, container, false);
      setHasOptionsMenu(true);

      Toolbar toolbar = view.findViewById(R.id.detail_toolbar);
      AppCompatActivity activity = (AppCompatActivity) requireActivity();
      if (toolbar != null)
      {
         activity.setSupportActionBar(toolbar);
         activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         activity.getSupportActionBar().setHomeButtonEnabled(true);
      }


      if(savedInstanceState != null)
         bookId = savedInstanceState.getLong(SAVED_BOOK_ID);
      else
         bookId = getBookID();

      return view;
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      view.findViewById(R.id.fab_edit_book).setOnClickListener(v -> navigateToEditBook(v, bookId));

      if (savedInstanceState == null)
         loadFragment(bookId);
   }

   @Override
   public void onSaveInstanceState(@NonNull Bundle outState)
   {
      super.onSaveInstanceState(outState);
      outState.putLong(SAVED_BOOK_ID, bookId);
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.menu_details, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      int itemId = item.getItemId();
      if(itemId == android.R.id.home)
      {
         // This ID represents the Home or Up button. In the case of this
         // activity, the Up button is shown. Use NavUtils to allow users
         // to navigate up one level in the application structure. For
         // more details, see the Navigation pattern on Android Design:
         //
         // http://developer.android.com/design/patterns/navigation.html#up-vs-back
         //
         navigateToBookList(requireView());
         return true;
      }
      else if(itemId == R.id.action_duplicate)
      {
         navigateToEditBook(requireView(), bookId, true);
         return true;
      }
      else if(itemId == R.id.action_delete)
      {
         deleteBook();
         navigateToBookList(requireView());
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onBackPressed()
   {
      navigateToBookList(requireView());
      return true;
   }

   private void loadFragment(long bookId)
   {
      Bundle arguments = new Bundle();
      arguments.putLong(BookDetailFragment.BOOK_ID, bookId);
      BookDetailFragment fragment = new BookDetailFragment();
      fragment.setArguments(arguments);
      getChildFragmentManager().beginTransaction()
                               .replace(R.id.book_detail_container, fragment)
                               .commitAllowingStateLoss();
   }

   private void navigateToEditBook(View view, long bookId)
   {
      BookFragmentDirections.ActionBookFragmentToEditBookFragment action = BookFragmentDirections.actionBookFragmentToEditBookFragment();
      action.setBookID(bookId);
      Navigation.findNavController(view).navigate(action);
   }

   private void navigateToEditBook(View view, long bookId, boolean isCopy)
   {
      BookFragmentDirections.ActionBookFragmentToEditBookFragment actionBookFragmentToEditBookFragment = BookFragmentDirections.actionBookFragmentToEditBookFragment();
      actionBookFragmentToEditBookFragment.setBookID(bookId);
      actionBookFragmentToEditBookFragment.setIsCopy(isCopy);
      Navigation.findNavController(view).navigate(actionBookFragmentToEditBookFragment);
   }

   private void navigateToBookList(View view)
   {
      Navigation.findNavController(view).navigateUp();
   }

   private long getBookID()
   {
      if(getArguments() != null)
         return BookFragmentArgs.fromBundle(getArguments()).getBookID();
      return 0;
   }

   private void deleteBook()
   {
      DBAdapter dbAdapter = new DBAdapter(getContext());
      dbAdapter.open();
      dbAdapter.deleteBook(bookId);
      dbAdapter.close();
   }
}
