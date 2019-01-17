package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.R;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class FieldMultiSpinner extends LinearLayout
{
   private Title              oTitle;
   private Button             btnSpinner;
   private String             hint               = "";
   private String             contentDescription = "";
   private Context            context;
   private ArrayList<Item>    alItems            = new ArrayList<FieldMultiSpinner.Item>(); 
   private OnUpdateListener   onUpdateListener   = null;

   public FieldMultiSpinner(Context context)
   {
      super(context);

      vInit(context);
   }

   public FieldMultiSpinner(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      // vInit(context, alFields, oFieldType);
      vInit(context);

      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FieldMultiSpinner, 0, 0);

      String title = a.getString(R.styleable.FieldMultiSpinner_title);
      int titleValueColor = a.getColor(R.styleable.FieldMultiSpinner_titleColor, 0);
      int titleTextSize = a.getDimensionPixelOffset(R.styleable.FieldMultiSpinner_titleTextSize, 0);
      int titleLineSize = a.getDimensionPixelOffset(R.styleable.FieldMultiSpinner_titleLineSize, 0);
      contentDescription = a.getString(R.styleable.FieldMultiSpinner_android_contentDescription);
      hint = a.getString(R.styleable.FieldMultiSpinner_android_hint);

      a.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      oTitle.setText(title);
      oTitle.setColor(titleValueColor);
      oTitle.setTextSize(titleTextSize);
      oTitle.setLineSize(titleLineSize);

      btnSpinner.setContentDescription(contentDescription);
   }

   private void vInit(Context context)
   {
      this.context = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_multi_spinner, this, true);

      oTitle = (Title) this.findViewById(R.id.title);
      btnSpinner = (Button) this.findViewById(R.id.action_select_type);
      
      setButtonText(btnSpinner, alItems);
      btnSpinner.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
//            displayPopupWindow(v, alValues, alSelectedNdx);
            displayPopupWindow(v, alItems);
         }
      });
   }

   private void setButtonText(Button oButton, ArrayList<Item> alItems)
   {
      String sButtonText = "";
      for(Item item : alItems)
         if(item.isSelected())
            sButtonText += (sButtonText.isEmpty() ? "" : ", ") + item.getTitle();

      if(!sButtonText.isEmpty())
      {
         oButton.setText(sButtonText);
         oButton.setTextColor(Color.BLACK);
      } else
      {
         oButton.setText(hint);
         oButton.setTextColor(Color.GRAY);
      }
   }

   private void displayPopupWindow(final View anchorView, final ArrayList<Item> alItems)
   {
      if(alItems == null)
         return;
      final PopupMenu popupMenu = new PopupMenu(context, anchorView);
      initPopupMenu(popupMenu, alItems);

      popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
      {
         @Override
         public boolean onMenuItemClick(MenuItem menuItem)
         {
            if(menuItem.getOrder() < alItems.size())
            {
               menuItem.setChecked(!menuItem.isChecked());
               Item item = alItems.get(menuItem.getOrder());
               item.setSelected(menuItem.isChecked()); 

               setButtonText((Button) anchorView, alItems);
               onUpdateListener.onUpdate(item);

               popupMenu.show();
            } 
            else
            {
               AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
               builder.setTitle(R.string.add_new);
               final AppCompatEditText etNewValue = new AppCompatEditText(context);
               builder.setView(etNewValue);
               builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
               {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     String sNewValue = etNewValue.getText().toString().trim();
                     Item item = new Item(sNewValue);
                     item.setId(alItems.size());
                     item.setSelected(true);
                     alItems.add(item);
                     setButtonText((Button) anchorView, alItems);
                     onUpdateListener.onUpdate(item);
                     popupMenu.dismiss();
                     initPopupMenu(popupMenu, alItems);
                     
                     InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                     imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                     dialog.cancel();
                     popupMenu.show();
                  }
               });

               builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
               {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                     imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                     dialog.cancel();
                     popupMenu.show();
                  }
               });

               builder.show();

            }
            return true;
         }
      });

      popupMenu.show();
   }

   private void initPopupMenu(PopupMenu popupMenu, final ArrayList<Item> alItems)
   {
      popupMenu.getMenu().clear();
      
      for(int i = 0; i < alItems.size(); i++)
      {
         popupMenu.getMenu().add(Menu.NONE, 0, i, alItems.get(i).getTitle())
         .setCheckable(true)
         .setChecked(alItems.get(i).isSelected());
         
      }
      popupMenu.getMenu().add(Menu.NONE, 0, alItems.size(), "<add>");
   }
   
   public void setTitle(String title)
   {
      oTitle.setText(title);
   }

   public void setTitle(int resid)
   {
      oTitle.setText(resid);
   }

   public void setTitleColor(int valueColor)
   {
      oTitle.setColor(valueColor);
   }

   public void setTitleTextSize(int textSize)
   {
      oTitle.setTextSize(textSize);
   }

   public void setLineSize(int lineSize)
   {
      oTitle.setTextSize(lineSize);
   }

   public void setContentDescription(String contentDescription)
   {
      btnSpinner.setContentDescription(contentDescription);
   }

   public void setHint(String hint)
   {
      this.hint = hint;
      setButtonText(btnSpinner, alItems);
   }

   public void setItems(ArrayList<Item> alItems)
   {
      this.alItems = alItems;
      setButtonText(btnSpinner, alItems);
   }
   
   public void setOnUpdateListener(OnUpdateListener onUpdateListener)
   {
      this.onUpdateListener = onUpdateListener;
   }

   public static class Item 
   {
      private String title = "";
      private boolean selected = false;
      private long id = -1;
      
      public Item()
      {
         
      }
      
      public Item(String title)
      {
         this.title = title;
      }
      
      public Item(long id, String title)
      {
         this.id = id;
      }

      public String getTitle()
      {
         return title;
      }

      public void setTitle(String title)
      {
         this.title = title;
      }

      public boolean isSelected()
      {
         return selected;
      }

      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }

      public long getId()
      {
         return id;
      }

      public void setId(long id)
      {
         this.id = id;
      }
   }
   
   public interface OnUpdateListener
   {
      public void onUpdate(Item item);
   }
}
