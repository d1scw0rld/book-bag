package org.d1scw0rld.bookbag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Calendar;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.d1scw0rld.bookbag.dto.BooksAdapter;
import org.d1scw0rld.bookbag.dto.DividerItemDecoration;
import org.d1scw0rld.bookbag.dto.FileUtils;
import org.d1scw0rld.bookbag.fileselector.FileOperation;
import org.d1scw0rld.bookbag.fileselector.FileSelectorDialog;
import org.d1scw0rld.bookbag.fileselector.OnHandleFileListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity
{
   public final static int SHOW_EDIT_BOOK = 101,
                           SHOW_EDIT_BOOK_COPY = 102;
   
   private static final String PREF_ORDER_ID = "order_id",
                               PREF_EXPAND_ALL = "pref_expand_all",
                               PREF_EXPORT_FOLDER = "pref_export_folder";
   
   private int iOrderID = DBAdapter.SRT_TTL,
               iClickedItemNdx = -1;
   
   private boolean bExpandAll = false;
   
   private long sel_id;
   
   private String sExportFolder;
   private TextView tvBooksOrder, 
                    tvBooksCount;
   
   private DBAdapter oDbAdapter = null;
   
   private ArrayList<OrderItem> alOrderItems = new ArrayList<>();
   
   private BooksAdapter oBooksAdapter;
   
   private SharedPreferences oPreferences;

   private View vSelected = null;
   
   final String[] mFileFilter = { "*.*", ".db" };

   private RecyclerView recyclerView;

   private ActionMode mActionMode;

   FragmentManager fm = getSupportFragmentManager();

   File flCurrent;

   FileSelectorDialog oFileSelectorDialog;

   /**
    * Whether or not the activity is in two-pane mode, i.e. running on a tablet
    * device.
    */
   private boolean mTwoPane;

   /**
    * Whether or not the activity is in two-pane mode, i.e. running on a tablet
    * device.
    */
   private boolean bUpdate = true;

   private View.OnClickListener onRecyclerViewClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View v)
      {
         iClickedItemNdx = recyclerView.getChildLayoutPosition(v);
         sel_id = oBooksAdapter.getItemId(iClickedItemNdx);
         
         if(mTwoPane)
         {
            if(mActionMode != null)
               mActionMode.finish();
            
            Bundle arguments = new Bundle();
            arguments.putLong(BookDetailFragment.ARG_ITEM_ID, sel_id);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            v.setSelected(true);
            if(vSelected != null && !vSelected.equals(v))
               vSelected.setSelected(false);
            vSelected = v;
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.book_detail_container, fragment)
                                       .commit();
         } 
         else
         {
            Context context = v.getContext();
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, sel_id);

            startActivityForResult(intent, 0);
         }         
      }
   };

   private View.OnLongClickListener onRecyclerViewLongClickListener = new View.OnLongClickListener()
   {
      @Override
      public boolean onLongClick(View v)
      {
         iClickedItemNdx = recyclerView.getChildLayoutPosition(v);
         sel_id = oBooksAdapter.getItemId(iClickedItemNdx);
         if(mActionMode != null)
         {
            return false;
         }


         // Start the CAB using the ActionMode.Callback defined above
         mActionMode = startSupportActionMode(mActionModeCallback);

         v.setSelected(true);
         
         if(mTwoPane)
         {
            Bundle arguments = new Bundle();
            arguments.putLong(BookDetailFragment.ARG_ITEM_ID, sel_id);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            if(vSelected != null && !vSelected.equals(v))
               vSelected.setSelected(false);
            vSelected = v;
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.book_detail_container, fragment)
                                       .commit();
         }          
         return true;      
      }
   };
   
   private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() 
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
               Intent intent = new Intent(getApplicationContext(), EditBookActivity.class);
               intent.putExtra(EditBookActivity.BOOK_ID, sel_id);
               startActivityForResult(intent, SHOW_EDIT_BOOK);
               
               mode.finish();
               return true;

            case R.id.action_duplicate:
               Intent ntDubplicateBook = new Intent(getApplicationContext(), EditBookActivity.class);
               ntDubplicateBook.putExtra(EditBookActivity.BOOK_ID, sel_id);
               ntDubplicateBook.putExtra(EditBookActivity.IS_COPY, true);
               startActivityForResult(ntDubplicateBook, SHOW_EDIT_BOOK_COPY);
               
               mode.finish();
               return true;
               
            case R.id.action_delete:
               assert oDbAdapter != null;
               oDbAdapter.deleteBook(sel_id);
               oBooksAdapter.removeAt(iClickedItemNdx);
               tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, oBooksAdapter.getAllChildrenCount(), oBooksAdapter.getAllChildrenCount()));
//               tvBooksCount.setText(String.valueOf(oBooksAdapter.getAllChildrenCount())
//                                    + " "
//                                    + getString(R.string.lbl_books));

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
   
   private OnHandleFileListener mLoadFileListener = new OnHandleFileListener()
   {
      @Override
      public void handleFile(final String filePath)
      {
         oDbAdapter.close();
         if(oDbAdapter.importDatabase(filePath))
            Toast.makeText(getApplicationContext(), R.string.prf_imp_db_scs, Toast.LENGTH_SHORT).show();
         oDbAdapter.open();
         setupRecyclerView(recyclerView, iOrderID);
      }
   };

   private OnHandleFileListener mSaveFileListener = new OnHandleFileListener()
   {
      @Override
      public void handleFile(final String filePath)
      {
         oDbAdapter.close();
         if(oDbAdapter.exportDatabase(filePath))
            Toast.makeText(getApplicationContext(), R.string.prf_xpr_db_scs, Toast.LENGTH_SHORT).show();
         oDbAdapter.open();         
      }
   };      
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_book_list);
  
      oPreferences = PreferenceManager.getDefaultSharedPreferences(this);

      oPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() 
      {
         public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
         {
            if(key.equalsIgnoreCase(PREF_EXPAND_ALL))
               bExpandAll = oPreferences.getBoolean(PREF_EXPAND_ALL, false);
            if(key.equalsIgnoreCase(PREF_EXPORT_FOLDER))
            {
               sExportFolder = oPreferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name));
               new File(Environment.getExternalStorageDirectory() + File.separator + sExportFolder + File.separator).mkdirs();
            }
            
         }
      });

      loadPreferences();
      
      tvBooksOrder = (TextView) findViewById(R.id.tv_books_order);
      tvBooksCount = (TextView) findViewById(R.id.tv_books_count);

      FileUtils.verifyStoragePermissions(this);
      
      oDbAdapter = new DBAdapter(this);

      new File(Environment.getExternalStorageDirectory() + File.separator + sExportFolder + File.separator).mkdirs();
      
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      toolbar.setTitle(getTitle());

      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            Intent intent = new Intent(getApplicationContext(), EditBookActivity.class);
            intent.putExtra(EditBookActivity.BOOK_ID, 0);
            startActivityForResult(intent, SHOW_EDIT_BOOK);
         }
      });
      
      recyclerView = (RecyclerView) findViewById(R.id.book_list);
      assert recyclerView != null;
      recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
      recyclerView.setItemAnimator(new DefaultItemAnimator());
      recyclerView.setLayoutManager(new LinearLayoutManager(this));

      if (findViewById(R.id.book_detail_container) != null)
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
   }

   @Override
   protected void onResume()
   {
      super.onResume();

      oDbAdapter.open();
      if(bUpdate)
      {
          setupRecyclerView(recyclerView, iOrderID);
      }
   }

   @Override
   protected void onPause()
   {
      oDbAdapter.close();
   
      super.onPause();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      
      final MenuItem searchItem = menu.findItem(R.id.action_search);
//      final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
      final SearchView searchView = (SearchView) searchItem.getActionView();
      
      searchView.setOnQueryTextListener(new OnQueryTextListener()
      {
         
         @Override
         public boolean onQueryTextSubmit(String arg0)
         {
            return false;
         }
         
         @Override
         public boolean onQueryTextChange(String arg0)
         {
            oBooksAdapter.expandAll();
            oBooksAdapter.filter(arg0);
            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, oBooksAdapter.getAllChildrenCount(), oBooksAdapter.getAllChildrenCount()));
//            tvBooksCount.setText(String.valueOf(oBooksAdapter.getAllChildrenCount()) + " " + getString(R.string.lbl_books));
            return true;
         }
      });
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch(item.getItemId())
      {
         case R.id.action_settings:
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;

         case R.id.action_imp_db:
            flCurrent = new File(Environment.getExternalStorageDirectory()
                                      + File.separator
                                      + sExportFolder);
            oFileSelectorDialog = FileSelectorDialog.newInstance(flCurrent,
                                                                 FileOperation.LOAD,
                                                                 mLoadFileListener,
                                                                 mFileFilter);
            oFileSelectorDialog.show(fm, "fragment_alert");

            return true;
            
         case R.id.action_exp_db:
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            int iExtNdx = DBAdapter.DATABASE_NAME.lastIndexOf(".");
            String sFileName = String.format(getString(R.string.fmt_fl_nm), 
                                             DBAdapter.DATABASE_NAME.substring(0, iExtNdx),
                                             calendar.get(Calendar.YEAR),
                                             calendar.get(Calendar.MONTH) + 1,
                                             calendar.get(Calendar.DAY_OF_MONTH),
                                             calendar.get(Calendar.HOUR_OF_DAY),
                                             calendar.get(Calendar.MINUTE),
                                             DBAdapter.DATABASE_NAME.substring(iExtNdx+1));
            File flCurrent = new File(Environment.getExternalStorageDirectory() 
                                      + File.separator 
                                      + sExportFolder 
                                      + File.separator 
                                      + sFileName);
            
            oFileSelectorDialog = FileSelectorDialog.newInstance(flCurrent,
                                                                 FileOperation.SAVE,
                                                                 mSaveFileListener,
                                                                 mFileFilter);
            oFileSelectorDialog.show(fm, "fragment_alert");
            return true;            
            
         case R.id.action_exp_all:
            oBooksAdapter.expandAll();
            return true;
         
         case R.id.action_clp_all:
            oBooksAdapter.collapseAll();
            return true;
            
         case R.id.action_sort:
            View menuItemView = findViewById(R.id.action_sort); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, menuItemView);
            for(OrderItem oItem: alOrderItems)
               popupMenu.getMenu().add(1, oItem.iID, 0, oItem.sTitle).setCheckable(true).setChecked(oItem.iID == iOrderID);
            popupMenu.getMenu().setGroupCheckable(1, true, true);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
               @Override
               public boolean onMenuItemClick(MenuItem menuItem)
               {
                  iOrderID = menuItem.getItemId();
                  saveOrderID(iOrderID);
                  setupRecyclerView(recyclerView, iOrderID);
                  return true;
               }
            });
            popupMenu.show();
            return true;
            
         default:
            return true;
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);
      
      if(resultCode == RESULT_OK)
         bUpdate = true;
      else
         bUpdate = false;
   }

   private void setupRecyclerView(@NonNull RecyclerView recyclerView, int iOrderID)
   {
      oBooksAdapter = new BooksAdapter(this, oDbAdapter.getBooks(iOrderID));
      oBooksAdapter.setClickListener(onRecyclerViewClickListener);
      oBooksAdapter.setLongClickListener(onRecyclerViewLongClickListener);
      if(bExpandAll)
         oBooksAdapter.expandAll();
      recyclerView.setAdapter(oBooksAdapter);
      for(OrderItem oOrderItem : alOrderItems)
         if(oOrderItem.iID == iOrderID)
         {
            tvBooksOrder.setText(oOrderItem.sTitle);
            tvBooksCount.setText(getResources().getQuantityString(R.plurals.books, oBooksAdapter.getAllChildrenCount(), oBooksAdapter.getAllChildrenCount()));
//            tvBooksCount.setText(String.valueOf(oBooksAdapter.getAllChildrenCount())
//                                 + " " 
//                                 + getString(R.string.lbl_books));
         }
   }
   
   private void loadPreferences()
   {
      iOrderID = oPreferences.getInt(PREF_ORDER_ID, DBAdapter.SRT_TTL);
      bExpandAll = oPreferences.getBoolean(PREF_EXPAND_ALL, false);
      sExportFolder = oPreferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name));
   }
   
   private void saveOrderID(int iOrderID)
   {
      Editor editor = oPreferences.edit();
      
      editor.putInt(PREF_ORDER_ID, iOrderID);
      
      editor.commit();      
   }

   private class OrderItem 
   {
      public int iID;
      public String sTitle;
      
      public OrderItem(int iID, String sTitle)
      {
         this.iID = iID;
         this.sTitle = sTitle;
      }
   }
}
