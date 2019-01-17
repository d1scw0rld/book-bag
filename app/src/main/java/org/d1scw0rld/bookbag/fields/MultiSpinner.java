package org.d1scw0rld.bookbag.fields;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.d1scw0rld.bookbag.R;

import java.util.List;

/**
 * Created by Iasen on 20.7.2016 г..
 */
public class MultiSpinner extends Spinner implements DialogInterface.OnCancelListener
{
   private List<String> listitems;
   private boolean[] checked;
   private CustomAdapter customAdapter;
   public MultiSpinner(Context context)
   {
      super(context);
   }

   public MultiSpinner(Context context, AttributeSet attrs)
   {
      super(context, attrs);
   }

   public MultiSpinner(Context context, AttributeSet attrs, int defStyleAttr)
   {
      super(context, attrs, defStyleAttr);
   }

   @Override
   public void onCancel(DialogInterface dialog)
   {
      setValues();
   }

   void setValues()
   {
      StringBuffer spinnerBuffer = new StringBuffer();
      boolean someUnselected = false;
      for (int i = 0; i < listitems.size(); i++)
      {
         if (checked[i] == true)
         {
            spinnerBuffer.append(listitems.get(i));
            spinnerBuffer.append(", ");
         }
         else
         {
            someUnselected = true;
         }
      }
      String spinnerText;
      if (someUnselected)
      {
         spinnerText = spinnerBuffer.toString();
         if (spinnerText.length() > 2)
            spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
      }
      else
      {
         spinnerText = "select";
      }

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                                                              android.R.layout.simple_spinner_item,
                                                              new String[] { spinnerText });
      setAdapter(adapter);
   }

   @Override
   public boolean performClick()
   {
      super.performClick();
      
      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      ListView lvItems = new ListView(getContext());
      customAdapter = new CustomAdapter(getContext(), 0, 0, listitems);
      lvItems.setAdapter(customAdapter);
      lvItems.setOnItemClickListener(new OnItemClickListener()
      {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
            if(position == listitems.size() - 1)
            {
               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
               builder.setTitle(R.string.add_new);
               final EditText etNewValue = new EditText(getContext());
               builder.setView(etNewValue);
               builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
               {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     String sNewValue = etNewValue.getText().toString();
                     listitems.add(listitems.size()-1, sNewValue);
                     boolean checkedOld[] = new boolean[checked.length];
                     java.lang.System.arraycopy(checked,0, checkedOld, 0, checked.length);
                     checked = new boolean[checked.length+1];
                     java.lang.System.arraycopy(checkedOld, 0, checked, 0, checkedOld.length);
                     customAdapter.notifyDataSetChanged();
                     InputMethodManager imm = (InputMethodManager)  getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                     imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                     dialog.cancel();
                  }
               });

               builder.setNegativeButton(android.R.string.cancel, new DialogInterface
                     .OnClickListener() {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     InputMethodManager imm = (InputMethodManager)  getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                     imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                     dialog.cancel();

                  }
               });

               builder.show();
            }

         }
      });
      builder.setView(lvItems);
      builder.setNegativeButton("done",
                                new DialogInterface.OnClickListener()
                                {

                                   @Override
                                   public void onClick(DialogInterface dialog, int which)
                                   {
                                      dialog.cancel();
                                   }
                                });
      builder.setOnCancelListener(this);
      builder.show();

      return true;
   }

   public void setItems(List<String> items,
                        String allText,
                        multispinnerListener listener)
   {
      this.listitems = items;
      this.listitems.add("<add>");

      checked = new boolean[items.size()];
      for (int i = 0; i < checked.length; i++)
         checked[i] =false;


      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                                                         android.R.layout.simple_spinner_item,
                                                         new String[] { allText });
      setAdapter(adapter);
   }

   public interface multispinnerListener
   {
      public void onItemschecked(boolean[] checked);
   }

   private class CustomAdapter extends BaseAdapter
   {
//      private Context context;
      private List<String> lsFields;
      LayoutInflater inflater;

      public CustomAdapter(Context context, int resource, int textViewResourceId, List<String> objects)
      {
//         this.context = context;
         this.lsFields = objects;
         inflater = ((Activity) context).getLayoutInflater();
      }

      @Override
      public View getDropDownView(int position, View convertView, ViewGroup parent)
      {
         // TODO Auto-generated method stub
         return getCustomView(position, convertView, parent);
      }

      @Override
      public int getCount()
      {
         return lsFields.size();
      }

      @Override
      public Object getItem(int position)
      {
         return lsFields.get(position);
      }

      @Override
      public long getItemId(int position)
      {
         return position;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         // TODO Auto-generated method stub
         return getCustomView(position, convertView, parent);
      }

      public View getCustomView(final int position, View convertView, ViewGroup parent)
      {
         // TODO Auto-generated method stub

         View row;
         final ViewHolder holder;
         if(convertView == null)
         {
            row = inflater.inflate(R.layout.row_spinner, parent, false);
            holder = new ViewHolder();
            holder.tvName = (TextView) row.findViewById(R.id.tv_name);
            holder.cbSelected = (CheckBox) row.findViewById(R.id.cb_selected);
            holder.cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
               {
                  checked[position] = !checked[position];
                  setValues();
               }
            });

            row.setTag(holder);
         }
         else
         {
            row = (View) convertView;
            holder = (ViewHolder) row.getTag();
         }

         holder.tvName.setText(lsFields.get(position));
         if(position == lsFields.size()-1)
            holder.cbSelected.setVisibility(INVISIBLE);
         else
            holder.cbSelected.setVisibility(VISIBLE);

         return row;
      }

      class ViewHolder
      {
         TextView tvName;
         CheckBox cbSelected;
      }
   }
}
