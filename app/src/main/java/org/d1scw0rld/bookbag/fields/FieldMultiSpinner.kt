package org.d1scw0rld.bookbag.fields;

import java.util.ArrayList;

import org.d1scw0rld.bookbag.R;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class FieldMultiSpinner extends LinearLayout implements Field
{
   private Title title;
   private Button selectButton;
   private String hint = "";
   private Context context;
   private ArrayList<Item> items = new ArrayList<>();
   private OnUpdateListener onUpdateListener = null;

   public FieldMultiSpinner(Context context)
   {
      super(context);

      init(context);
   }

   public FieldMultiSpinner(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init(context);

      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FieldMultiSpinner, 0, 0);

      String titleText = typedArray.getString(R.styleable.FieldMultiSpinner_title);
      int titleValueColor = typedArray.getColor(R.styleable.FieldMultiSpinner_titleColor, 0);
      int titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiSpinner_titleTextSize, 0);
      int titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiSpinner_titleLineSize, 0);
      String contentDescription = typedArray.getString(R.styleable.FieldMultiSpinner_android_contentDescription);
      hint = typedArray.getString(R.styleable.FieldMultiSpinner_android_hint);

      typedArray.recycle();

      setOrientation(LinearLayout.VERTICAL);
      setGravity(Gravity.CENTER_VERTICAL);

      this.title.setText(titleText);
      this.title.setColor(titleValueColor);
      this.title.setTextSize(titleTextSize);
      this.title.setLineSize(titleLineSize);

      selectButton.setContentDescription(contentDescription);
   }

   private void init(Context context)
   {
      this.context = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.field_multi_spinner, this, true);

      title = findViewById(R.id.title);
      selectButton = findViewById(R.id.action_select_type);
      
      setButtonText(selectButton, items);
      selectButton.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            displayPopupWindow(v, items);
         }
      });
   }

   private void setButtonText(Button button, ArrayList<Item> itemsList)
   {
      StringBuilder buttonTextBuilder = new StringBuilder();
      for(Item item : itemsList)
         if(item.isSelected())
            buttonTextBuilder.append((buttonTextBuilder.length() == 0) ? "" : ", ")
                       .append(item.getTitle());

      if(buttonTextBuilder.length() > 0)
      {
         button.setText(buttonTextBuilder.toString());
         button.setTextColor(Color.BLACK);
      }
      else
      {
         button.setText(hint);
         button.setTextColor(Color.GRAY);
      }
   }

   private void displayPopupWindow(final View anchorView, final ArrayList<Item> itemsList)
   {
      if(itemsList == null)
         return;
      final PopupMenu popupMenu = new PopupMenu(context, anchorView);
      initPopupMenu(popupMenu, itemsList);

      popupMenu.setOnMenuItemClickListener(menuItem -> {
         if(menuItem.getOrder() < itemsList.size())
         {
            menuItem.setChecked(!menuItem.isChecked());
            Item item = itemsList.get(menuItem.getOrder());
            item.setSelected(menuItem.isChecked());

            setButtonText((Button) anchorView, itemsList);
            onUpdateListener.onUpdate(item);

            popupMenu.show();
         }
         else
         {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(R.string.add_new);
            final AppCompatEditText newValueEditText = new AppCompatEditText(context);
            builder.setView(newValueEditText);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
               public void onClick(DialogInterface dialog, int id)
               {
                  String newValue = newValueEditText.getText().toString().trim();
                  Item item = new Item(newValue);
                  item.setId(itemsList.size());
                  item.setSelected(true);
                  itemsList.add(item);
                  setButtonText((Button) anchorView, itemsList);
                  onUpdateListener.onUpdate(item);
                  popupMenu.dismiss();
                  initPopupMenu(popupMenu, itemsList);

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
      });

      popupMenu.show();
   }

   private void initPopupMenu(PopupMenu popupMenu, final ArrayList<Item> itemsList)
   {
      popupMenu.getMenu().clear();
      
      for(int i = 0; i < itemsList.size(); i++)
      {
         popupMenu.getMenu().add(Menu.NONE, 0, i, itemsList.get(i).getTitle())
         .setCheckable(true)
         .setChecked(itemsList.get(i).isSelected());
         
      }
      popupMenu.getMenu().add(Menu.NONE, 0, itemsList.size(), "<add>");
   }
   
   public void setTitle(String title)
   {
      this.title.setText(title);
   }

   public void setTitle(int resourceId)
   {
      title.setText(resourceId);
   }

   @Override
   public String getTitle()
   {
      return title.getTitle();
   }

   public void setTitleColor(int valueColor)
   {
      title.setColor(valueColor);
   }

   public void setTitleTextSize(int textSize)
   {
      title.setTextSize(textSize);
   }

   public void setLineSize(int lineSize)
   {
      title.setTextSize(lineSize);
   }

   public void setContentDescription(String contentDescription)
   {
      selectButton.setContentDescription(contentDescription);
   }

   public void setHint(String hint)
   {
      this.hint = hint;
      setButtonText(selectButton, items);
   }

   public void setItems(ArrayList<Item> items)
   {
      this.items = items;
      setButtonText(selectButton, items);
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
      void onUpdate(Item item);
   }
}
