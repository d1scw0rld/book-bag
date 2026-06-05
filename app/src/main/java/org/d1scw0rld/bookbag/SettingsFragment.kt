package org.d1scw0rld.bookbag

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = "Settings"

        val appCompatActivity = requireActivity() as? AppCompatActivity
        if (appCompatActivity != null && toolbar != null) {
            appCompatActivity.setSupportActionBar(toolbar)
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            appCompatActivity.supportActionBar?.setHomeButtonEnabled(true)
        }
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Navigation.findNavController(requireView()).navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_screen, rootKey)
    }
}
