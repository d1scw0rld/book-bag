package org.d1scw0rld.bookbag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.FragmentManager;
//import android.support.v7.view.ActionMode;
import android.view.ActionMode;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.d1scw0rld.bookbag.dto.BooksAdapter;
import org.d1scw0rld.bookbag.dto.FileUtils;
import org.d1scw0rld.bookbag.fileselector.FileOperation;
import org.d1scw0rld.bookbag.fileselector.FileSelectorDialog;
import org.d1scw0rld.bookbag.fileselector.OnHandleFileListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class BookListFragment extends BaseFragment
{
   public final static int SHOW_EDIT_BOOK = 101,
         SHOW_EDIT_BOOK_COPY = 102;

   private static final String PREF_ORDER_ID = "order_id",
         PREF_EXPAND_ALL = "pref_expand_all",
         PREF_EXPORT_FOLDER = "pref_export_folder";

   private int iOrderID = DBAdapter.SRT_TTL,
         iClickedItemNdx = -1;

   private boolean isExpandAll = false;

   private long sel_id;

   private String   sExportFolder;
   private TextView tvBooksOrder,
         tvBooksCount;

   private DBAdapter dbAdapter = null;

   private ArrayList<OrderItem> alOrderItems = new ArrayList<>();

   private BooksAdapter booksAdapter;

   private SharedPreferences preferences;

   private View vSelected = null;

   final String[] mFileFilter = { "*.*", ".db" };

   private RecyclerView recyclerView;

   private ActionMode mActionMode;

   FragmentManager fragmentManager;

   File flCurrent;

   FileSelectorDialog fileSelectorDialog;

   /**
    * Whether or not the activity is in two-pane mode, i.e. running on a tablet
    * device.
    */
   private boolean mTwoPane;

   private boolean bUpdate = true;

   private final View.OnClickListener onRecyclerViewClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View v)
      {
         iClickedItemNdx = recyclerView.getChildLayoutPosition(v);
         sel_id = booksAdapter.getItemId(iClickedItemNdx);

         if(mTwoPane)
         {
            if(mActionMode != null)
               mActionMode.finish();

            Bundle arguments = new Bundle();
            arguments.putLong(BookDetailFragment.BOOK_ID, sel_id);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            v.setSelected(true);
            if(vSelected != null && !vSelected.equals(v))
               vSelected.setSelected(false);
            vSelected = v;
            requireActivity().getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.book_detail_container, fragment)
                                       .commit();
         }
         else
         {
            NavDirections action = BookListFragmentDirections.actionBookListFragmentToBookDetailNewFragment(sel_id);

//            // With default value
//            BookListFragmentDirections.ActionBookListFragmentToBookDetailNewFragment action = BookListFragmentDirections.actionBookListFragmentToBookDetailNewFragment();
//            action.setARGITEMID(sel_id);
            Navigation.findNavController(v).navigate(action);
//            Context context = v.getContext();
//            Intent intent = new Intent(context, BookDetailActivity.class);
//            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, sel_id);
//
//            startActivityForResult(intent, 0);
         }
      }
   };

   private final View.OnLongClickListener onRecyclerViewLongClickListener = new View.OnLongClickListener()
   {
      @Override
      public boolean onLongClick(View v)
      {
         iClickedItemNdx = recyclerView.getChildLayoutPosition(v);
         sel_id = booksAdapter.getItemId(iClickedItemNdx);
         if(mActionMode != null)
         {
            return false;
         }


         // Start the CAB using the ActionMode.Callback defined above
//         mActionMode = ((BookListActivity)getActivity()).startSupportActionMode(mActionModeCallback);
         mActionMode = getActivity().startActionMode(mActionModeCallback);

         v.setSelected(true);

         if(mTwoPane)
         {
            Bundle arguments = new Bundle();
            arguments.putLong(BookDetailFragment.BOOK_ID, sel_id);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            if(vSelected != null && !vSelected.equals(v))
               vSelected.setSelected(false);
            vSelected = v;
            getActivity().getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.book_detail_container, fragment)
                                       .commit();
         }
         return true;
      }
   };

   private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback()
   {
      // Called when the action mode is created; startActionMode() was called
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu)
      {
         // Inflate a menu resource providing context menu items
         MenuInflater inflater = mode.getMenuInflater();
         inflater.inflate(R.menu.menu_context, menu);
         return true;
      }

      // Called each time the action mode is shown. Always called after onCreateActionMode, but
      // may be called multiple times if the mode is invalidated.
      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu)
      {
         return false; // Return false if nothing is done
      }

      // Called when the user selects a contextual menu item
      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item)
      {
         switch (item.getItemId())
         {
            case R.id.action_edit:
               Intent intent = new Intent(getContext(), EditBookActivity.class);
               intent.putExtra(EditBookActivity.BOOK_ID, sel_id);
               startActivityForResult(intent, SHOW_EDIT_BOOK);

               mode.finish();
               return true;

            case R.id.action_duplicate:
               Intent ntDuplicateBook = new Intent(getContext(), EditBookActivity.class);
               ntDuplicateBook.putExtra(EditBookActivity.BOOK_ID, sel_id);
               ntDuplicateBook.putExtra(EditBookActivity.IS_COPY, true);
               startActivityForResult(ntDuplicateBook, SHOW_EDIT_BOOK_COPY);

               mode.finish();
               return true;

            case R.id.action_delete:
               assert dbAdapter != null;
               dbAdapter.deleteBook(sel_id);
               booksAdapter.removeAt(iClickedItemNdx);
               tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, booksAdapter.getAllChildrenCount(), booksAdapter.getAllChildrenCount()));

               mode.finish(); // Action picked, so close the CAB
               return true;

            default:
               return false;
         }
      }

      // Called when the user exits the action mode
      @Override
      public void onDestroyActionMode(ActionMode mode)
      {
         mActionMode = null;
      }
   };

   private final OnHandleFileListener mLoadFileListener = new OnHandleFileListener()
   {
      @Override
      public void handleFile(final String filePath)
      {
         dbAdapter.close();
         if(dbAdapter.importDatabase(filePath))
            Toast.makeText(getContext(), R.string.prf_imp_db_scs, Toast.LENGTH_SHORT).show();
         dbAdapter.open();
         setupRecyclerView(recyclerView, iOrderID);
      }
   };

   private final OnHandleFileListener mSaveFileListener = new OnHandleFileListener()
   {
      @Override
      public void handleFile(final String filePath)
      {
         dbAdapter.close();
         if(dbAdapter.exportDatabase(filePath))
            Toast.makeText(getContext(), R.string.prf_xpr_db_scs, Toast.LENGTH_SHORT).show();
         dbAdapter.open();
      }
   };

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.activity_book_list, container, false);
      preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

      preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener()
      {
         public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
         {
            if(key.equalsIgnoreCase(PREF_EXPAND_ALL))
               isExpandAll = preferences.getBoolean(PREF_EXPAND_ALL, false);
            if(key.equalsIgnoreCase(PREF_EXPORT_FOLDER))
            {
               sExportFolder = preferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name));
//               new File(Environment.getExternalStorageDirectory() + File.separator + sExportFolder + File.separator).mkdirs();
               new File(requireContext().getExternalFilesDir(null) + File.separator + sExportFolder + File.separator).mkdirs();

            }

         }
      });

      fragmentManager = requireActivity().getSupportFragmentManager();

      loadPreferences();

      tvBooksOrder = view.findViewById(R.id.tv_books_order);
      tvBooksCount = view.findViewById(R.id.tv_books_count);

      FileUtils.verifyStoragePermissions(getActivity());

      dbAdapter = new DBAdapter(getContext());

//      new File(Environment.getExternalStorageDirectory() + File.separator + sExportFolder + File.separator).mkdirs();
      new File(requireContext().getExternalFilesDir(null) + File.separator + sExportFolder + File.separator).mkdirs();

//      Toolbar toolbar = view.findViewById(R.id.toolbar);
//      ((BookListActivity)getActivity()).setSupportActionBar(toolbar);
//      toolbar.setTitle(getActivity().getTitle());

      FloatingActionButton fab = view.findViewById(R.id.fab);
      fab.setOnClickListener(view1 -> {
//            Intent intent = new Intent(getContext(), EditBookActivity.class);
//            intent.putExtra(EditBookActivity.BOOK_ID, 0);
//            startActivityForResult(intent, SHOW_EDIT_BOOK);
         NavDirections action = BookListFragmentDirections.actionBookListFragmentToEditBookFragment();
         Navigation.findNavController(view1).navigate(action);
      });

      recyclerView = view.findViewById(R.id.book_list);
      assert recyclerView != null;
//      recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
      recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
      recyclerView.setItemAnimator(new DefaultItemAnimator());
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

      if (view.findViewById(R.id.book_detail_container) != null)
      {
         // The detail container view will be present only in the
         // large-screen layouts (res/values-w900dp).
         // If this view is present, then the
         // activity should be in two-pane mode.
         mTwoPane = true;
      }

      alOrderItems.add(new OrderItem(DBAdapter.SRT_TTL, getString(R.string.srt_title)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_AUT, getString(R.string.srt_author)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_WNT_PBL_TTL, getString(R.string.srt_wanted_pbl_ttl)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_WNT_PBL_AUT, getString(R.string.srt_wanted_pbl_aut)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_RD_AUT, getString(R.string.srt_read_aut)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_RD_TTL, getString(R.string.srt_read_ttl)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_NOT_RD_AUT, getString(R.string.srt_not_read_aut)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_NOT_RD_TTL, getString(R.string.srt_not_read_ttl)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_PBL_AUT, getString(R.string.srt_pbl_aut)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_PBL_TTL, getString(R.string.srt_pbl_ttl)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_LND_TTL, getString(R.string.srt_lnd_ttl)));
      alOrderItems.add(new OrderItem(DBAdapter.SRT_LND_BRW, getString(R.string.srt_lnd_brw)));

      return view;
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
//      super.onViewCreated(view, savedInstanceState);

//      Toolbar toolbar = view.findViewById(R.id.toolbar);
//      ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
//      toolbar.setTitle(requireActivity().getTitle());


      NavController navController = Navigation.findNavController(view);
      AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
      Toolbar toolbar = view.findViewById(R.id.toolbar);
      NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
      toolbar.setTitle(requireActivity().getTitle());
//      setHasOptionsMenu(true);
      toolbar.inflateMenu(R.menu.menu_main);
      final MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
      final SearchView searchView = (SearchView) searchItem.getActionView();

      searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
      {

         @Override
         public boolean onQueryTextSubmit(String arg0)
         {
            return false;
         }

         @Override
         public boolean onQueryTextChange(String arg0)
         {
            booksAdapter.expandAll();
            booksAdapter.filter(arg0);
            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, booksAdapter.getAllChildrenCount(), booksAdapter.getAllChildrenCount()));
            return true;
         }
      });
      toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setHasOptionsMenu(true);
   }

   @Override
   public void onResume()
   {
      super.onResume();

      dbAdapter.open();
      if(bUpdate)
      {
         setupRecyclerView(recyclerView, iOrderID);
      }
   }

   @Override
   public void onPause()
   {
      dbAdapter.close();

      super.onPause();
   }

//   @Override
//   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
//   {
//      inflater.inflate(R.menu.menu_main, menu);
//
//      final MenuItem searchItem = menu.findItem(R.id.action_search);
//      final SearchView searchView = (SearchView) searchItem.getActionView();
//
//      searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
//      {
//
//         @Override
//         public boolean onQueryTextSubmit(String arg0)
//         {
//            return false;
//         }
//
//         @Override
//         public boolean onQueryTextChange(String arg0)
//         {
//            booksAdapter.expandAll();
//            booksAdapter.filter(arg0);
//            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, booksAdapter.getAllChildrenCount(), booksAdapter.getAllChildrenCount()));
//            return true;
//         }
//      });
//   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
//      NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
//      return NavigationUI.onNavDestinationSelected(item, navController)
//            || super.onOptionsItemSelected(item);

      switch(item.getItemId())
      {
         case R.id.settings_fragment:
            if(isAdded())
            {
               Navigation.findNavController(getView()).navigate(BookListFragmentDirections.actionBookListFragmentToSettingsFragment());
            }
            break;
      }

      return super.onOptionsItemSelected(item);


//      switch(item.getItemId())
//      {
//         case R.id.settings_fragment:
////            Intent intent = new Intent(getContext(), SettingsActivity.class);
////            startActivity(intent);
//            return true;
//
//         case R.id.action_imp_db:
//            flCurrent = new File(Environment.getExternalStorageDirectory()
//                                       + File.separator
//                                       + sExportFolder);
//            fileSelectorDialog = FileSelectorDialog.newInstance(flCurrent,
//                                                                FileOperation.LOAD,
//                                                                mLoadFileListener,
//                                                                mFileFilter);
//            fileSelectorDialog.show(fragmentManager, "fragment_alert");
//
//            return true;
//
//         case R.id.action_exp_db:
//            Calendar calendar = Calendar.getInstance(Locale.getDefault());
//            int iExtNdx = DBAdapter.DATABASE_NAME.lastIndexOf(".");
//            String sFileName = String.format(getString(R.string.fmt_fl_nm),
//                                             DBAdapter.DATABASE_NAME.substring(0, iExtNdx),
//                                             calendar.get(Calendar.YEAR),
//                                             calendar.get(Calendar.MONTH) + 1,
//                                             calendar.get(Calendar.DAY_OF_MONTH),
//                                             calendar.get(Calendar.HOUR_OF_DAY),
//                                             calendar.get(Calendar.MINUTE),
//                                             DBAdapter.DATABASE_NAME.substring(iExtNdx+1));
//            File flCurrent = new File(Environment.getExternalStorageDirectory()
//                                            + File.separator
//                                            + sExportFolder
//                                            + File.separator
//                                            + sFileName);
//
//            fileSelectorDialog = FileSelectorDialog.newInstance(flCurrent,
//                                                                FileOperation.SAVE,
//                                                                mSaveFileListener,
//                                                                mFileFilter);
//            fileSelectorDialog.show(fragmentManager, "fragment_alert");
//            return true;
//
//         case R.id.action_exp_all:
//            booksAdapter.expandAll();
//            return true;
//
//         case R.id.action_clp_all:
//            booksAdapter.collapseAll();
//            return true;
//
//         case R.id.action_sort:
//            View menuItemView = findViewById(R.id.action_sort); // SAME ID AS MENU ID
//            PopupMenu popupMenu = new PopupMenu(getContext(), menuItemView);
//            for(OrderItem oItem: alOrderItems)
//               popupMenu.getMenu().add(1, oItem.iID, 0, oItem.sTitle).setCheckable(true).setChecked(oItem.iID == iOrderID);
//            popupMenu.getMenu().setGroupCheckable(1, true, true);
//            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
//            {
//               @Override
//               public boolean onMenuItemClick(MenuItem menuItem)
//               {
//                  iOrderID = menuItem.getItemId();
//                  saveOrderID(iOrderID);
//                  setupRecyclerView(recyclerView, iOrderID);
//                  return true;
//               }
//            });
//            popupMenu.show();
//            return true;
//
//         default:
//            return true;
//      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);

      bUpdate = resultCode == RESULT_OK;
   }

   private void setupRecyclerView(@NonNull RecyclerView recyclerView, int iOrderID)
   {
      booksAdapter = new BooksAdapter(getContext(), dbAdapter.getBooks(iOrderID));
      booksAdapter.setClickListener(onRecyclerViewClickListener);
      booksAdapter.setLongClickListener(onRecyclerViewLongClickListener);
      if(isExpandAll)
         booksAdapter.expandAll();
      recyclerView.setAdapter(booksAdapter);
      for(OrderItem oOrderItem : alOrderItems)
         if(oOrderItem.iID == iOrderID)
         {
            tvBooksOrder.setText(oOrderItem.sTitle);
            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, booksAdapter.getAllChildrenCount(), booksAdapter.getAllChildrenCount()));
         }
   }

   private void loadPreferences()
   {
      iOrderID = preferences.getInt(PREF_ORDER_ID, DBAdapter.SRT_TTL);
      isExpandAll = preferences.getBoolean(PREF_EXPAND_ALL, false);
      sExportFolder = preferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name));
   }

   private void saveOrderID(int iOrderID)
   {
      SharedPreferences.Editor editor = preferences.edit();

      editor.putInt(PREF_ORDER_ID, iOrderID);

      editor.apply();
   }

   private static class OrderItem
   {
      int iID;
      String sTitle;

      OrderItem(int iID, String sTitle)
      {
         this.iID = iID;
         this.sTitle = sTitle;
      }
   }

}
