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
   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);
      setHasOptionsMenu(true);

      Toolbar toolbar = view.findViewById(R.id.toolbar);
      toolbar.setTitle("Settings");

      AppCompatActivity appCompatActivity = requireActivity() instanceof AppCompatActivity ? (AppCompatActivity) requireActivity() : null;

      if(appCompatActivity != null)
      {
         appCompatActivity.setSupportActionBar(toolbar);
         if (appCompatActivity.getSupportActionBar() != null)
         {
            appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            appCompatActivity.getSupportActionBar().setHomeButtonEnabled(true);
         }
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
   }
}
