package org.d1scw0rld.bookbag;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
{
//   public SettingsFragment()
//   {
//   }
//   @Override
//   public View onCreateView(LayoutInflater inflater,
//                            ViewGroup container,
//                            Bundle savedInstanceState)
//   {
////      container.getContext().setTheme(R.style.SettingsTheme);
//      View view = super.onCreateView(inflater, container, savedInstanceState);
////      ((AppCompatActivity)requireActivity()).getActionBar().setTitle(R.string.title_settings);
//      getContext().getTheme().applyStyle(R.style.SettingsTheme, true);
//      return view;
//   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);
      setHasOptionsMenu(true);

      Toolbar toolbar = view.findViewById(R.id.toolbar);
      toolbar.setTitle("Settings");


      if (toolbar != null)
      {
         ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
         ((AppCompatActivity)requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         ((AppCompatActivity)requireActivity()).getSupportActionBar().setHomeButtonEnabled(true);
      }

   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(item.getItemId() == android.R.id.home)
      {
         Navigation.findNavController(requireView())
                   .navigateUp();
         return true;
      }

      return super.onOptionsItemSelected(item);
   }


   @Override
   public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
   {
      setPreferencesFromResource(R.xml.preference_screen, rootKey);
//      addPreferencesFromResource(R.xml.preference_screen);
   }

//   @Override
//   public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
//   {
//      super.onViewCreated(view, savedInstanceState);
//   }
}
