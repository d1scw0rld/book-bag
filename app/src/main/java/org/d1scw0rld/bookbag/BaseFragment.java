package org.d1scw0rld.bookbag;

import androidx.fragment.app.Fragment;
import android.view.View;

public class BaseFragment extends Fragment
{
   protected <T extends View> T findViewById(int id) {
      return getView().findViewById(id);
   }

}
