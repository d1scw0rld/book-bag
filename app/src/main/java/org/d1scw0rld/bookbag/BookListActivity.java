package org.d1scw0rld.bookbag;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


/**
 * An activity representing a list of Books. this activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of books, which when touched,
 * lead to a {@link BookDetailNewFragment} representing
 * book details. On tablets, the activity presents the list of books and
 * book details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity
{
   public final static int SHOW_EDIT_BOOK_COPY = 102;

   private NavController navController;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
//      setContentView(R.layout.activity_frame_content);
//      setContentView(R.layout.activity_navhost);
      setContentView(R.layout.activity_main);

      navController = Navigation.findNavController(this, R.id.fragment);
      NavigationUI.setupActionBarWithNavController(this, navController);


      if(savedInstanceState == null)
      {
//         BookListFragment fragment = new BookListFragment();
//
//         FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
////         fragmentTransaction.add(R.id.frame_content, fragment).commit();
//         fragmentTransaction.add(R.id.fragment, fragment).commit();

      }
   }

   @Override
   public boolean onSupportNavigateUp()
   {
      return NavigationUI.navigateUp(navController, (DrawerLayout) null);
   }
}
