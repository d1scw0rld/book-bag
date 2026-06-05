package org.d1scw0rld.bookbag;

import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.Toast;

public class BaseFragment extends Fragment
{
   protected <T extends View> T findViewById(int id) {
      return requireView().findViewById(id);
   }

   protected void showToast(int resourceId)
   {
      Toast.makeText(getContext(), resourceId, Toast.LENGTH_SHORT)
           .show();
   }
}
