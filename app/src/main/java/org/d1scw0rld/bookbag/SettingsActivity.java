package org.d1scw0rld.bookbag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivity extends AppCompatActivity
{
   @Override
   protected void onCreate(final Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
   }

   public static class MyPreferenceFragment extends PreferenceFragmentCompat
   {
      
      
      @Override
      public View onCreateView(LayoutInflater inflater,
                               ViewGroup container,
                               Bundle savedInstanceState)
      {
         // TODO Auto-generated method stub
         View view = super.onCreateView(inflater, container, savedInstanceState);
//         view.setBackgroundColor(ContextCompat.getColor(getContext(), R.drawable.background_selector_accent));
         return view;
      }

      @Override
      public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
      {
//         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preference_screen);
//         getActivity().getListView().setSelector(R.drawable.background_selector_accent);
      }

      @Override
      public void onViewCreated(View view, Bundle savedInstanceState)
      {
         super.onViewCreated(view, savedInstanceState);
//         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//         getListView().setSelector(R.drawable.background_selector_accent);
//         }
      }
      
      
   }
}
