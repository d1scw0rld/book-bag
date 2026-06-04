package org.d1scw0rld.bookbag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable;

import java.util.HashMap;

public class EditBookFragment extends Fragment implements IBackPressListener
{
   private Book book;

   private DBAdapter dbAdapter = null;

   private PopupMenu hiddenFieldsPopupMenu = null;

   private FieldEditTextUpdatableClearable bookTitleField = null;

   HashMap<MenuItem, View> hiddenFieldsHashMap = new HashMap<>();
   private FieldsFactory fieldsFactory;
   private ActionBar actionBar;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.fragment_edit_book, container, false);
      setHasOptionsMenu(true);

      requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


      Toolbar toolbar = view.findViewById(R.id.toolbar);
      ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);

      actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
      if(actionBar != null)
      {
         showHomeTitle(false);
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
         actionBar.setCustomView(R.layout.actionbar_custom_view_done);


         ((Toolbar) actionBar.getCustomView()
                             .getParent()).setContentInsetsAbsolute(0, 0);
         actionBar.getCustomView()
                  .findViewById(R.id.actionbar_done)
                  .setOnClickListener(this::onBookSave);

      }

      dbAdapter = new DBAdapter(getContext());
      dbAdapter.open();


      long bookId = getBookID();

      boolean isCopy = getIsCopy();

      if(bookId != 0)
      {
         book = dbAdapter.getBook(bookId);
         if(isCopy)
            book.id = 0;
      }
      else
         book = new Book();

      fieldsFactory = new FieldsFactory(getContext(), book, dbAdapter);

      final Button addFieldButton = view.findViewById(R.id.btn_add_field);
      addFieldButton.setOnClickListener(v -> hiddenFieldsPopupMenu.show());

      hiddenFieldsPopupMenu = new PopupMenu(requireContext(), addFieldButton);
      hiddenFieldsPopupMenu.setOnMenuItemClickListener(menuItem -> {
         View fieldView = hiddenFieldsHashMap.get(menuItem);
         if(fieldView != null)
         {
            fieldView.setVisibility(View.VISIBLE);
            fieldView.requestFocus();
         }
         hiddenFieldsPopupMenu.getMenu()
                              .removeItem(menuItem.getItemId());
         if(hiddenFieldsPopupMenu.getMenu().size() == 0)
            addFieldButton.setEnabled(false);
         return false;
      });

      LinearLayout fieldsRoot = view.findViewById(R.id.ll_fields);

      addFields(fieldsRoot);

      createAddFieldsPopupMenu(fieldsRoot);

      return view;

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

   @Override
   public void onDestroy()
   {
      showHomeTitle(true);

      super.onDestroy();
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.cancel, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(item.getItemId() == R.id.cancel)
      {
         hideKeyboard();
         navigateBack();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onBackPressed()
   {
      navigateBack();
      return true;
   }

   private boolean getIsCopy()
   {
      return EditBookFragmentArgs.fromBundle(requireArguments()).getIsCopy();
   }

   private long getBookID()
   {
      return EditBookFragmentArgs.fromBundle(requireArguments()).getBookID();
   }

   private void createAddFieldsPopupMenu(LinearLayout fieldsRoot)
   {
      for(int i = 0; i < fieldsRoot.getChildCount(); i++)
      {
         if(fieldsRoot.getChildAt(i).getVisibility() == View.GONE)
         {
            hiddenFieldsPopupMenu.getMenu()
                                 .add(Menu.NONE, hiddenFieldsPopupMenu.getMenu().size(), 0, ((org.d1scw0rld.bookbag.fields.Field)fieldsRoot.getChildAt(i)).getTitle());
            hiddenFieldsHashMap.put(hiddenFieldsPopupMenu.getMenu().getItem(hiddenFieldsPopupMenu.getMenu().size() - 1), fieldsRoot.getChildAt(i));

         }
      }
   }

   private void addFields(ViewGroup rootView)
   {
      for(Field field : DBAdapter.FIELDS)
      {
         switch(field.type)
         {
            case Field.TYPE_TEXT:
               fieldsFactory.addFieldText(rootView, field);
               if(field.id == DBAdapter.FLD_TITLE)
                  bookTitleField = (FieldEditTextUpdatableClearable) rootView.getChildAt(rootView.getChildCount()-1);
               break;

            case Field.TYPE_MULTIFIELD:
               fieldsFactory.addFieldMultiText(rootView, field);
               break;

            case Field.TYPE_TEXT_AUTOCOMPLETE:
               fieldsFactory.addAutocompleteField(rootView, field);
               break;

            case Field.TYPE_SPINNER:
               fieldsFactory.addFieldSpinner(rootView, field);
               break;

            case Field.TYPE_MULTI_SPINNER:
               fieldsFactory.addFieldMultiSpinner(rootView, field);
               break;

            case Field.TYPE_MONEY:
               fieldsFactory.addFieldMoney(rootView, field);
               break;

            case Field.TYPE_DATE:
               fieldsFactory.addFieldDate(rootView, field);
               break;

            case Field.TYPE_RATING:
               fieldsFactory.addFieldRating(rootView, field);
               break;

            case Field.TYPE_CHECK_BOX:
               fieldsFactory.addFieldCheckBox(rootView, field);
               break;
         }
      }
   }

   private void onBookSave(View v)
   {
      View currentFocus = requireActivity().getCurrentFocus();
      if(currentFocus != null)
         currentFocus.clearFocus();
      v.requestFocus();

      hideKeyboard();

      if(book.title.value.trim().isEmpty())
         bookTitleField.setError(getResources().getString(R.string.err_emp_ttl));
      else
      {
         bookTitleField.setError(null);
         saveBook();
         if(book.id == 0)
            navigateToBookList();
         else
            navigateBack();
      }
   }

   private void saveBook()
   {
      clearEmptyFields();

      if(book.id != 0)
         dbAdapter.updateBook(book);
      else
         dbAdapter.insertBook(book);
   }

   private void clearEmptyFields()
   {
      for(int i = book.properties.size() - 1; i >= 0; i--)
      {
         if(book.properties.get(i).value.trim().isEmpty())
            book.properties.remove(i);
      }
   }

   private void navigateToBookList()
   {
      NavHostFragment.findNavController(this).navigate(R.id.action_to_book_list);
   }

   private void navigateBack()
   {
      NavHostFragment.findNavController(this).navigateUp();
   }

   private void hideKeyboard()
   {
      View view = requireActivity().getCurrentFocus();
      if(view != null)
      {
         android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }
   }

   private void showHomeTitle(boolean show)
   {
      ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
      if (actionBar != null)
      {
         actionBar.setDisplayShowTitleEnabled(show);
         actionBar.setDisplayShowHomeEnabled(show);
      }
   }
}
