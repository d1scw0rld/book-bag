package org.d1scw0rld.bookbag;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.d1scw0rld.bookbag.dto.BooksAdapter;
import org.d1scw0rld.bookbag.dto.FileUtils;
import org.d1scw0rld.bookbag.fileselector.FileOperation;
import org.d1scw0rld.bookbag.fileselector.FileSelectorDialog;
import org.d1scw0rld.bookbag.fileselector.OnHandleFileListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookListFragment extends BaseFragment
{
   public final static String IS_UPDATED = "is_updated";

   private final static String PREF_ORDER_ID = "order_id",
                               PREF_EXPAND_ALL = "pref_expand_all",
                               PREF_EXPORT_FOLDER = "pref_export_folder";

   private final String[] mFileFilter = {"*.*",
                                         ".db"};

   private final ArrayList<OrderItem> alOrderItems = new ArrayList<>();

   private boolean isExpandAll = false,
   /**
    * Whether or not the activity is in two-pane mode, i.e. running on a tablet
    * device.
    */
                   isTwoPane,
                   isUpdated = true;

   private int iOrderID  = DBAdapter.SRT_TTL,
               iClickedItemNdx = -1;

   private long bookID;

   private String sExportFolderAbsPath;

   private TextView tvBooksOrder,
         tvBooksCount;

   private DBAdapter dbAdapter = null;

   private BooksAdapter booksAdapter;

   private SharedPreferences preferences;

   private View selectedBookView = null;

   private RecyclerView recyclerView;

   private ActionMode actionMode;

   private FragmentManager fragmentManager;

   private FileSelectorDialog fileSelectorDialog;

   private final View.OnClickListener onCategoryClickListener = v -> deselectBookView();

   private final View.OnClickListener onBookClickListener = v -> {
         getSelectedBook(v);

         if(actionMode != null)
            actionMode.finish();


         if(isTwoPane)
         {
            selectBookView(v);
            showBookDetails();
         }
         else
            navigateToBookDetails(v);
   };

   private final View.OnLongClickListener onBookLongClickListener = new View.OnLongClickListener()
   {
      @Override
      public boolean onLongClick(View v)
      {
         getSelectedBook(v);

         if(actionMode != null)
            return false;

         // Start the CAB using the ActionMode.Callback defined above
         actionMode = requireActivity().startActionMode(onActionModeCallback);

         if(isTwoPane)
         {
            selectBookView(v);
            showBookDetails();
         }
         return true;
      }
   };
   private final ActionMode.Callback onActionModeCallback = new ActionMode.Callback()
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
         int itemId = item.getItemId();
         if(itemId == R.id.action_edit)
            navigateToEditBook(requireView(), bookID, false);
         else if(itemId == R.id.action_duplicate)
            navigateToEditBook(requireView(), bookID, true);
         else if(itemId == R.id.action_delete)
            deleteBook();
         else
            return false;

         mode.finish(); // Action picked, so close the CAB
         return true;
      }

      // Called when the user exits the action mode
      @Override
      public void onDestroyActionMode(ActionMode mode)
      {
         actionMode = null;
      }
   };

   private void deleteBook()
   {
      dbAdapter.deleteBook(bookID);
      booksAdapter.removeAt(iClickedItemNdx);
      tvBooksCount.setText(getResources().getQuantityString(R.plurals.books,
                                                            booksAdapter.getAllChildrenCount(),
                                                            booksAdapter.getAllChildrenCount()));
      deselectBookAndHideDetails();
   }

   private final OnHandleFileListener onLoadFileListener = new OnHandleFileListener()
   {
      @Override
      public void handleFile(final String filePath)
      {
         dbAdapter.close();
         if(dbAdapter.importDatabase(filePath))
            showToast(R.string.prf_imp_db_scs);
         dbAdapter.open();
         setupRecyclerView(recyclerView, iOrderID);
      }
   };

   private final OnHandleFileListener onSaveFileListener = filePath -> {
      dbAdapter.close();
      if(dbAdapter.exportDatabase(filePath))
      {
         showToast(R.string.prf_xpr_db_scs);
      }
      dbAdapter.open();
   };

   private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (prefs, key) -> {
      if(key.equalsIgnoreCase(PREF_EXPAND_ALL))
      {
         isExpandAll = preferences.getBoolean(PREF_EXPAND_ALL, false);
      }
      if(key.equalsIgnoreCase(PREF_EXPORT_FOLDER))
      {
         sExportFolderAbsPath = getExportFolderAbsPath(preferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name)));
         checkCreateExportFolder(sExportFolderAbsPath);
      }
   };

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.fragment_book_list, container, false);
      setHasOptionsMenu(true);

      fragmentManager = requireActivity().getSupportFragmentManager();

      FileUtils.verifyStoragePermissions(getActivity());

      preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
      preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
      loadPreferences();
      checkCreateExportFolder(sExportFolderAbsPath);

      dbAdapter = new DBAdapter(getContext());

      getOrderItems();

      return view;
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);

      tvBooksOrder = view.findViewById(R.id.tv_books_order);
      tvBooksCount = view.findViewById(R.id.tv_books_count);

      FloatingActionButton fab = view.findViewById(R.id.fab_add_book);
      fab.setOnClickListener(this::navigateToEditBook);

      if((recyclerView = view.findViewById(R.id.book_list)) != null)
      {
         recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
         recyclerView.setItemAnimator(new DefaultItemAnimator());
         recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
      }

      if(view.findViewById(R.id.book_detail_container) != null)
      {
         // The detail container view will be present only in the
         // large-screen layouts (res/values-w900dp).
         // If this view is present, then the
         // activity should be in two-pane mode.
         isTwoPane = true;
      }

//      NavController navController = NavHostFragment.findNavController(this);
//      final MutableLiveData<Boolean> liveData = Objects.requireNonNull(navController.getCurrentBackStackEntry())
//                                                       .getSavedStateHandle()
//                                                       .getLiveData(IS_UPDATED);
//      liveData.observe(getViewLifecycleOwner(), b -> isUpdated = b);
   }

   @Override
   public void onResume()
   {
      super.onResume();

      dbAdapter.open();
      if(isUpdated)
         setupRecyclerView(recyclerView, iOrderID);
      requireContext().getTheme()
                      .applyStyle(R.style.AppTheme, true);
   }

   @Override
   public void onPause()
   {
      dbAdapter.close();

      super.onPause();
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.menu_book_list, menu);

      final MenuItem searchItem = menu.findItem(R.id.action_search);
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
            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books,
                                                                  booksAdapter.getAllChildrenCount(),
                                                                  booksAdapter.getAllChildrenCount()));
            return true;
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item)
   {
      NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
      return NavigationUI.onNavDestinationSelected(item, navController)
            || optionsItemSelect(item);
   }
   @SuppressWarnings("deprecation")
   private String getExportFolderAbsPath(String sExportFolder)
   {
      return Environment.getExternalStorageDirectory() + File.separator + sExportFolder + File.separator;
   }

   private void checkCreateExportFolder(String sExportFolderAbsPath)
   {
      File file = new File(sExportFolderAbsPath);
      if(!file.isDirectory())
      {
         if(!file.mkdirs())
         {
            throw new RuntimeException("Can't create export folder");
         }
      }
   }

   private void getOrderItems()
   {
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
   }

   private boolean optionsItemSelect(MenuItem item)
   {
      deselectBookAndHideDetails();

      int itemId = item.getItemId();
      if(itemId == R.id.action_imp_db)
      {
         showImportDbDialog();
         return true;
      }
      else if(itemId == R.id.action_exp_db)
      {
         showExportDbDialog();
         return true;
      }
      else if(itemId == R.id.action_exp_all)
      {
         booksAdapter.expandAll();
         return true;
      }
      else if(itemId == R.id.action_clp_all)
      {
         booksAdapter.collapseAll();
         return true;
      }
      else if(itemId == R.id.action_sort)
      {
         View menuItemView = requireActivity().findViewById(item.getItemId()); // SAME ID AS MENU ID
         showOrderPopupMenu(menuItemView);
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   private void getSelectedBook(View v)
   {
      iClickedItemNdx = recyclerView.getChildLayoutPosition(v);
      bookID = booksAdapter.getItemId(iClickedItemNdx);
      v.setSelected(true);
   }

   private void navigateToBookDetails(View v)
   {
      NavDirections action = BookListFragmentDirections.actionBookListFragmentToBookFragment(bookID);
      Navigation.findNavController(v)
                .navigate(action);
   }

   private void navigateToEditBook(View v)
   {
      NavDirections action = BookListFragmentDirections.actionBookListFragmentToEditBookFragment();
      androidx.navigation.NavDestination i = Navigation.findNavController(v).getCurrentDestination();
      Navigation.findNavController(v)
                .navigate(action);
   }

   private void navigateToEditBook(View v, long iBookID, boolean isCopy)
   {
      BookListFragmentDirections.ActionBookListFragmentToEditBookFragment actionBookListFragmentToEditBookFragment = BookListFragmentDirections.actionBookListFragmentToEditBookFragment();
      actionBookListFragmentToEditBookFragment.setBookID(iBookID);
      actionBookListFragmentToEditBookFragment.setIsCopy(isCopy);
      Navigation.findNavController(v)
                .navigate(actionBookListFragmentToEditBookFragment);
   }

   private void selectBookView(View v)
   {
      if(selectedBookView != null && !selectedBookView.equals(v))
         selectedBookView.setSelected(false);
      selectedBookView = v;
   }

   private void deselectBookView()
   {
      if(selectedBookView != null)
         selectedBookView.setSelected(false);
   }

   private void deselectBookAndHideDetails()
   {
      deselectBookView();
      hideBookDetails();
   }

   private void showBookDetails()
   {
      Bundle arguments = new Bundle();
      arguments.putLong(BookDetailFragment.BOOK_ID, bookID);
      BookDetailFragment fragment = new BookDetailFragment();
      fragment.setArguments(arguments);
      requireActivity().getSupportFragmentManager()
                       .beginTransaction()
                       .replace(R.id.book_detail_container, fragment)
                       .commit();
   }

   private void hideBookDetails()
   {
      Fragment fragment = requireActivity().getSupportFragmentManager()
                                           .findFragmentById(R.id.book_detail_container);
      if(fragment != null)
      {
         requireActivity().getSupportFragmentManager()
                          .beginTransaction()
                          .remove(fragment)
                          .commit();
      }
   }

   private void showImportDbDialog()
   {
      File importFolder = new File(sExportFolderAbsPath);

      fileSelectorDialog = FileSelectorDialog.newInstance(importFolder,
                                                          FileOperation.LOAD,
                                                          onLoadFileListener,
                                                          mFileFilter);
      fileSelectorDialog.show(fragmentManager, null);
   }

   private void showExportDbDialog()
   {
      String sFileName = getFileName();
      File exportFile = new File(sExportFolderAbsPath
                                       + sFileName);
      fileSelectorDialog = FileSelectorDialog.newInstance(exportFile,
                                                          FileOperation.SAVE,
                                                          onSaveFileListener,
                                                          mFileFilter);
      fileSelectorDialog.show(fragmentManager, null);
   }

   private String getFileName()
   {
      Calendar calendar = Calendar.getInstance(Locale.getDefault());
      int iExtNdx = DBAdapter.DATABASE_NAME.lastIndexOf(".");
      return String.format(getString(R.string.fmt_fl_nm),
                           DBAdapter.DATABASE_NAME.substring(0, iExtNdx),
                           calendar.get(Calendar.YEAR),
                           calendar.get(Calendar.MONTH) + 1,
                           calendar.get(Calendar.DAY_OF_MONTH),
                           calendar.get(Calendar.HOUR_OF_DAY),
                           calendar.get(Calendar.MINUTE),
                           DBAdapter.DATABASE_NAME.substring(iExtNdx + 1));
   }

   private void showOrderPopupMenu(View view)
   {
      PopupMenu popupMenu = new PopupMenu(requireContext(), view);
      for(OrderItem orderItem : alOrderItems)
      {
         popupMenu.getMenu()
                  .add(1, orderItem.iID, 0, orderItem.sTitle)
                  .setCheckable(true)
                  .setChecked(orderItem.iID == iOrderID);
      }
      popupMenu.getMenu()
               .setGroupCheckable(1, true, true);
      popupMenu.setOnMenuItemClickListener(menuItem -> {
         iOrderID = menuItem.getItemId();
         saveOrderID(iOrderID);
         setupRecyclerView(recyclerView, iOrderID);
         return true;
      });
      popupMenu.show();
   }

   private void setupRecyclerView(@NonNull RecyclerView recyclerView, int iOrderID)
   {
      booksAdapter = new BooksAdapter(getContext(), dbAdapter.getBooks(iOrderID));
      booksAdapter.setBookClickListener(onBookClickListener);
      booksAdapter.setBookLongClickListener(onBookLongClickListener);
      booksAdapter.setHeaderClickListener(onCategoryClickListener);
      if(isExpandAll)
         booksAdapter.expandAll();
      recyclerView.setAdapter(booksAdapter);
      for(OrderItem orderItem : alOrderItems)
      {
         if(orderItem.iID == iOrderID)
         {
            tvBooksOrder.setText(orderItem.sTitle);
            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books,
                                                                  booksAdapter.getAllChildrenCount(),
                                                                  booksAdapter.getAllChildrenCount()));
         }
      }
   }

   private void loadPreferences()
   {
      iOrderID = preferences.getInt(PREF_ORDER_ID, DBAdapter.SRT_TTL);
      isExpandAll = preferences.getBoolean(PREF_EXPAND_ALL, false);
      sExportFolderAbsPath = getExportFolderAbsPath(preferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name)));
   }

   private void saveOrderID(int iOrderID)
   {
      SharedPreferences.Editor editor = preferences.edit();
      editor.putInt(PREF_ORDER_ID, iOrderID);
      editor.apply();
   }
   private static class OrderItem
   {

      int    iID;
      String sTitle;

      OrderItem(int iID, String sTitle)
      {
         this.iID = iID;
         this.sTitle = sTitle;
      }
   }
}
