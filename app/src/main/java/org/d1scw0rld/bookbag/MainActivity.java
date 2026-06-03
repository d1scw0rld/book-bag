package org.d1scw0rld.bookbag;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


/**
 * An activity representing a list of Books. this activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of books, which when touched,
 * lead to a {@link BookFragment} representing
 * book details. On tablets, the activity presents the list of books and
 * book details side-by-side using two vertical panes.
 */
public class MainActivity extends AppCompatActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
   }
}
