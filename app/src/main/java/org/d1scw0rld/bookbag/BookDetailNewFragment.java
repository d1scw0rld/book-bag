package org.d1scw0rld.bookbag;

import android.annotation.SuppressLint;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;


public class BookDetailNewFragment extends BaseFragment implements IBackPressListener
{
   private static final String SAVED_BOOK_ID = "saved_book_id";
   private int resultCode                 = Activity.RESULT_CANCELED;
   private long iBookID = 0;
   private ActionBar actionBar;

   public static BookDetailNewFragment newInstance(long iBookID)
   {

      Bundle args = new Bundle();
      args.putLong(BookDetailFragment.BOOK_ID, iBookID);
      BookDetailNewFragment fragment = new BookDetailNewFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @SuppressLint("RestrictedApi")
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
      if(actionBar != null)
         actionBar.setShowHideAnimationEnabled(false);
   }

   @Override
   public void onStart()
   {
      super.onStart();
      actionBar.hide();
   }

   @Override
   public void onStop()
   {
      super.onStop();
      actionBar.show();
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.activity_book_detail,container, false);
      setHasOptionsMenu(true);

      if(savedInstanceState != null)
         iBookID = savedInstanceState.getLong(SAVED_BOOK_ID);
      else
         iBookID = getBookID();

      Toolbar toolbar = view.findViewById(R.id.detail_toolbar);
      toolbar.inflateMenu(R.menu.menu_details);
      toolbar.setOnMenuItemClickListener(item -> onOptionsItemSelected(item));
////      ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
////      getActivity().setSupportActionBar(toolbar);
//
//      NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
//      AppBarConfiguration appBarConfiguration =
//            new AppBarConfiguration.Builder(navController.getGraph()).build();
//      CollapsibleActionView collapsibleActionView = view.findViewById(R.id.toolbar_layout);
      NavigationUI.setupWithNavController(toolbar, NavHostFragment.findNavController(this));
//      NavigationUI.setupWithNavController(collapsibleActionView, NavHostFragment.findNavController(this));


      FloatingActionButton fab = view.findViewById(R.id.fab_edit_book);
      fab.setOnClickListener(v -> navigateToEditBook(v));

      if (savedInstanceState == null)
         loadFragment(iBookID);

      return view;
   }

   @Override
   public void onSaveInstanceState(@NonNull Bundle outState)
   {
      super.onSaveInstanceState(outState);
      outState.putLong(SAVED_BOOK_ID, iBookID);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {

      NavController navController = NavHostFragment.findNavController(this);
      // We use a String here, but any type that can be put in a Bundle is supported
      final MutableLiveData<String> liveData = navController.getCurrentBackStackEntry()
                                                            .getSavedStateHandle()
                                                            .getLiveData("key");
      liveData.observe(getViewLifecycleOwner(), s -> {
         // Do something with the result.
         Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
      });

   }

   private void navigateToEditBook(View view)
   {
      BookDetailNewFragmentDirections.ActionBookDetailNewFragmentToEditBookFragment action = BookDetailNewFragmentDirections.actionBookDetailNewFragmentToEditBookFragment();
      action.setBookID(iBookID);
      Navigation.findNavController(view).navigate(action);
   }

   private long getBookID()
   {
      if(getArguments() != null)
         return BookDetailNewFragmentArgs.fromBundle(getArguments()).getBookID();
      return 0;
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.menu_details, menu);
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
