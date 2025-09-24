package org.d1scw0rld.bookbag;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class EditBookFragment extends Fragment implements IBackPressListener
{
   private Book book;

   private DBAdapter dbAdapter = null;

   private PopupMenu hiddenFieldsPopupMenu = null;

   private FieldEditTextUpdatableClearable fBookTitle = null;

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


      long iBookID = getBookID();

      boolean isCopy = getIsCopy();

      if(iBookID != 0)
      {
         book = dbAdapter.getBook(iBookID);
         if(isCopy)
            book.iID = 0;
      }
      else
         book = new Book();

      fieldsFactory = new FieldsFactory(getContext(), book, dbAdapter);

      final Button btnAddField = view.findViewById(R.id.btn_add_field);
      btnAddField.setOnClickListener(v -> hiddenFieldsPopupMenu.show());

      hiddenFieldsPopupMenu = new PopupMenu(requireContext(), btnAddField);
      hiddenFieldsPopupMenu.setOnMenuItemClickListener(menuItem -> {
         View view1 = hiddenFieldsHashMap.get(menuItem);
         if(view1 != null)
         {
            view1.setVisibility(View.VISIBLE);
            view1.requestFocus();
         }
         hiddenFieldsPopupMenu.getMenu()
                              .removeItem(menuItem.getItemId());
         if(hiddenFieldsPopupMenu.getMenu().size() == 0)
            btnAddField.setEnabled(false);
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
         switch(field.iType)
         {
            case Field.TYPE_TEXT:
               fieldsFactory.addFieldText(rootView, field);
               if(field.iID == DBAdapter.FLD_TITLE)
                  fBookTitle = (FieldEditTextUpdatableClearable) rootView.getChildAt(rootView.getChildCount()-1);
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

      if(book.csTitle.value.trim().isEmpty())
         fBookTitle.setError(getResources().getString(R.string.err_emp_ttl));
      else
      {
         fBookTitle.setError(null);
         saveBook();
         if(book.iID == 0)
            navigateToBookList();
         else
            navigateBack();
      }
   }

   private void saveBook()
   {
      clearEmptyFields();

      if(book.iID != 0)
         dbAdapter.updateBook(book);
      else
         dbAdapter.insertBook(book);
   }

   private void clearEmptyFields()
   {
      for(int i = book.alProperties.size() - 1; i >= 0; i--)
      {
         if(book.alProperties.get(i).sValue.trim().isEmpty())
            book.alProperties.remove(i);
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
      if (view != null) {
         InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }
   }

   private void showHomeTitle(boolean isShown)
   {
      actionBar.setDisplayShowHomeEnabled(isShown);
      actionBar.setDisplayShowTitleEnabled(isShown);
      actionBar.setDisplayShowCustomEnabled(!isShown);
   }
}
