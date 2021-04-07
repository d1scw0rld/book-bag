package org.d1scw0rld.bookbag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
{
   public SettingsFragment()
   {
   }
//   @Override
//   public View onCreateView(LayoutInflater inflater,
//                            ViewGroup container,
//                            Bundle savedInstanceState)
//   {
////      container.getContext().setTheme(R.style.SettingsTheme);
//      View view = super.onCreateView(inflater, container, savedInstanceState);
////      ((AppCompatActivity)requireActivity()).getActionBar().setTitle(R.string.title_settings);
//      return view;
//   }

   @Override
   public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
   {
      setPreferencesFromResource(R.xml.preference_screen,rootKey);
   }

//   @Override
//   public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
//   {
//      super.onViewCreated(view, savedInstanceState);
//   }
}
