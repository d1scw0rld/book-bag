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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

//import static android.app.Activity.RESULT_CANCELED;
//import static android.app.Activity.RESULT_OK;


public class EditBookFragment extends Fragment implements IBackPressListener
{
//   public final static String BOOK_ID = "book_id",
//         IS_COPY = "is_copy";

   private Book book;

   private DBAdapter dbAdapter = null;

   private PopupMenu hiddenFieldsPopupMenu = null;

   private FieldEditTextUpdatableClearable fBookTitle = null;

//   private View vPrevious = null;

   HashMap<MenuItem, View> hiddenFieldsHashMap = new HashMap<>();
   private FieldsFactory fieldsFactory;

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

      View view = inflater.inflate(R.layout.activity_edit_book, container, false);
      requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//      Toolbar toolbar = view.findViewById(R.id.toolbar);
//      ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);


      ActionBar actionBar =  ((AppCompatActivity)requireActivity()).getSupportActionBar();
      if(actionBar != null)
      {
//      actionBar.setDisplayShowHomeEnabled(true);
         actionBar.setDisplayShowHomeEnabled(false);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setDisplayShowCustomEnabled(true);
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//         actionBar.setCustomView(R.layout.actionbar_custom_view_done);
//         View mCustomView = inflater.inflate(R.layout.actionbar_custom_view_done, null);
//         actionBar.setCustomView(mCustomView);
         actionBar.setCustomView(R.layout.actionbar_custom_view_done);


         ((Toolbar) actionBar.getCustomView()
                             .getParent()).setContentInsetsAbsolute(0, 0);
         actionBar.getCustomView()
                  .findViewById(R.id.actionbar_done)
                  .setOnClickListener(new View.OnClickListener()
                  {
                     @Override
                     public void onClick(View v)
                     {
                        onBookSave(v);
                     }
                  });

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
      btnAddField.setOnClickListener(new View.OnClickListener()
      {

         @Override
         public void onClick(View v)
         {
            hiddenFieldsPopupMenu.show();
         }
      });

      hiddenFieldsPopupMenu = new PopupMenu(requireContext(), btnAddField);
      hiddenFieldsPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
      {

         @Override
         public boolean onMenuItemClick(MenuItem menuItem)
         {
            View view = hiddenFieldsHashMap.get(menuItem);
            if(view != null)
            {
               view.setVisibility(View.VISIBLE);
               view.requestFocus();
            }
            hiddenFieldsPopupMenu.getMenu()
                                 .removeItem(menuItem.getItemId());
            if(hiddenFieldsPopupMenu.getMenu().size() == 0)
               btnAddField.setEnabled(false);
            return false;
         }
      });

      LinearLayout fieldsRoot = view.findViewById(R.id.ll_fields);

      addFields(fieldsRoot);

      createAddFieldsPopupMenu(fieldsRoot);

      return view;

   }

   private boolean getIsCopy()
   {
//      if(getArguments() != null)
////         return getArguments().getBoolean(IS_COPY);
//         return EditBookFragmentArgs.fromBundle(getArguments()).getIsCopy();
//      return false;

      return EditBookFragmentArgs.fromBundle(requireArguments()).getIsCopy();

   }

   private long getBookID()
   {
//      if(getArguments() != null)
////         return getArguments().getLong(BOOK_ID);
//         return EditBookFragmentArgs.fromBundle(getArguments()).getBookID();
//      return 0;

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
//      if(Objects.requireNonNull(getActivity())
//                .getCurrentFocus() != null)
//      {
//         getActivity().getCurrentFocus().clearFocus();
//      }

      requireActivity().getCurrentFocus().clearFocus();

      v.requestFocus();
      if(book.csTitle.value.trim()
                           .isEmpty())
      {
         fBookTitle.setError(getResources().getString(R.string.err_emp_ttl));
      }
      else
      {
         fBookTitle.setError(null);

         saveBook();
         returnOK();
      }
   }

   // TODO Fix it.
   private void returnOK()
   {
//      setResult(RESULT_OK, new Intent());
//      finish();                  // "Done"
      NavController navController = NavHostFragment.findNavController(this);
      navController.getPreviousBackStackEntry().getSavedStateHandle().set("key", "result");
      navController.navigateUp();
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
   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.cancel, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(item.getItemId() == R.id.cancel)
      {
         returnCancel();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onBackPressed()
   {
      returnCancel();
      return true;
   }

   // TODO Fix it
   private void returnCancel()
   {
//      setResult(Activity.RESULT_CANCELED, new Intent());
//      finish();
      Navigation.findNavController(getView()).navigateUp();
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
         if(book.alProperties.get(i).sValue.trim()
                                           .isEmpty())
            book.alProperties.remove(i);
      }
   }

   @Override
   public void onDestroy()
   {
      super.onDestroy();

      ActionBar actionBar =  ((AppCompatActivity)requireActivity()).getSupportActionBar();
      if(actionBar != null)
      {
         actionBar.setDisplayShowHomeEnabled(true);
         actionBar.setDisplayShowTitleEnabled(true);
         actionBar.setDisplayShowCustomEnabled(false);
      }
   }
}
