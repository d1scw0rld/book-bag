package org.d1scw0rld.bookbag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;


public class BookDetailNewFragment extends BaseFragment implements IBackPressListener
{
   private int resultCode = Activity.RESULT_CANCELED;
   private long iBookID = 0;

   public static BookDetailNewFragment newInstance(long iBookID)
   {

      Bundle args = new Bundle();
      args.putLong(BookDetailFragment.BOOK_ID, iBookID);
      BookDetailNewFragment fragment = new BookDetailNewFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
   {
//      return super.onCreateView(inflater, container, savedInstanceState);

      View view = inflater.inflate(R.layout.activity_book_detail,container, false);

//      iBookID = getIntent().getLongExtra(BookDetailFragment.ARG_ITEM_ID, 0);
      iBookID = getBookID();

      Toolbar toolbar = view.findViewById(R.id.detail_toolbar);
      ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
//      getActivity().setSupportActionBar(toolbar);

//      NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
//      AppBarConfiguration appBarConfiguration =
//            new AppBarConfiguration.Builder(navController.getGraph()).build();
//      NavigationUI.setupWithNavController(toolbar, NavHostFragment.findNavController(this));


      FloatingActionButton fab = view.findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
//            Intent intent = new Intent(getContext(), EditBookActivity.class);
//            intent.putExtra(EditBookActivity.BOOK_ID, iBookID);
//            startActivityForResult(intent, BookListActivity.SHOW_EDIT_BOOK);

            navigateToEditBook(view);
         }
      });

      // Show the Up button in the action bar.
      ActionBar actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
      if (actionBar != null)
      {
         actionBar.setDisplayHomeAsUpEnabled(true);
      }

      if (savedInstanceState == null)
      {
         loadFragment(iBookID);
      }

      return view;
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      NavController navController = NavHostFragment.findNavController(this);
      // We use a String here, but any type that can be put in a Bundle is supported
      final MutableLiveData<String> liveData = navController.getCurrentBackStackEntry()
                                                            .getSavedStateHandle()
                                                            .getLiveData("key");
      liveData.observe(getViewLifecycleOwner(), new Observer<String>() {
         @Override
         public void onChanged(String s) {
            // Do something with the result.
            Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
         }
      });

   }

   private void navigateToEditBook(View view)
   {
      BookDetailNewFragmentDirections.ActionBookDetailNewFragmentToEditBookFragment action = BookDetailNewFragmentDirections.actionBookDetailNewFragmentToEditBookFragment();
      action.setBookID(iBookID);
//            action.setIsCopy(false);
      Navigation.findNavController(view).navigate(action);
   }

   private long getBookID()
   {
//      iBookID = getIntent().getLongExtra(BookDetailFragment.ARG_ITEM_ID, 0);
      if(getArguments() != null)
//         return getArguments().getLong(BookDetailFragment.ARG_ITEM_ID);
         return BookDetailNewFragmentArgs.fromBundle(getArguments()).getBookID();
      return 0;
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.menu_details, menu);

//      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch(item.getItemId())
      {
         case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
//            NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), BookListActivity.class));
            navigateToBookList();

            return true;

         case R.id.action_duplicate:
//            Intent intent = new Intent(getContext(), EditBookActivity.class);
//            intent.putExtra(EditBookActivity.BOOK_ID, iBookID);
//            intent.putExtra(EditBookActivity.IS_COPY, true);
//            startActivityForResult(intent, BookListActivity.SHOW_EDIT_BOOK_COPY);

            openBookCopy();

            return true;

         case R.id.action_delete:
            deleteBook();

            resultCode = Activity.RESULT_OK;
            close();

            return true;

         default:
            return super.onOptionsItemSelected(item);
      }

   }

   private void openBookCopy()
   {
      BookDetailNewFragmentDirections.ActionBookDetailNewFragmentToEditBookFragment actionBookDetailNewFragmentToEditBookFragment = BookDetailNewFragmentDirections.actionBookDetailNewFragmentToEditBookFragment();
      actionBookDetailNewFragmentToEditBookFragment.setBookID(iBookID);
      actionBookDetailNewFragmentToEditBookFragment.setIsCopy(true);
      Navigation.findNavController(requireView()).navigate(actionBookDetailNewFragmentToEditBookFragment);
   }

   private void navigateToBookList()
   {
//      NavDirections action = BookDetailNewFragmentDirections.actionBookDetailNewFragmentToBookListFragment();
//      Navigation.findNavController(getView()).navigate(action);
      Navigation.findNavController(requireView()).navigateUp();
   }

   private void deleteBook()
   {
      DBAdapter dbAdapter = new DBAdapter(getContext());
      dbAdapter.open();
      dbAdapter.deleteBook(iBookID);
      dbAdapter.close();
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      if(resultCode == Activity.RESULT_OK)
      {
         loadFragment(iBookID);
         this.resultCode = Activity.RESULT_OK;
         if(requestCode == BookListActivity.SHOW_EDIT_BOOK_COPY)
            close();
      }
      else
         this.resultCode = Activity.RESULT_CANCELED;
   }

   private void loadFragment(long iBookID)
   {
      Bundle arguments = new Bundle();
      arguments.putLong(BookDetailFragment.BOOK_ID, iBookID);
      BookDetailFragment fragment = new BookDetailFragment();
      fragment.setArguments(arguments);
      requireActivity().getSupportFragmentManager().beginTransaction()
                                 .replace(R.id.book_detail_container, fragment)
                                 .commitAllowingStateLoss();
   }

   @Override
   public boolean onBackPressed()
   {
      close();
//      super.onBackPressed();
      return true;
   }

   // TODO Fix it
   private void close()
   {
//      setResult(resultCode, new Intent());
//      finish();                  // "Done"
//      NavDirections action = BookDetailNewFragmentDirections.actionBookDetailNewFragmentToBookListFragment();
//      Navigation.findNavController(requireView()).navigate(action);
      Navigation.findNavController(requireView()).navigateUp();

   }
}
