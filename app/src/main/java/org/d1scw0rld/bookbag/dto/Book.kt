package org.d1scw0rld.bookbag.dto;

import java.util.ArrayList;

/**
 * Created by Iasen on 12.7.2016 г..
 */
public class Book
{
   public long id = 0;
   
   public Changeable<String> title,
                             description,
                             price,
                             value,
                             isbn,
                             web;

   public Changeable<Integer> volume,
                              pages,
                              publicationDate,
                              edition,
                              readDate,
                              dueDate,
                              rating;

   public ArrayList<Property> properties = new ArrayList<>();

   public Book()
   {
      title = new Changeable<>("");
      description  = new Changeable<>("");
      price = new Changeable<>("");
      value = new Changeable<>("");
      isbn = new Changeable<>("");
      web = new Changeable<>("");

      volume = new Changeable<>(0);
      pages = new Changeable<>(0);
      publicationDate = new Changeable<>(0);
      edition = new Changeable<>(0);
      readDate = new Changeable<>(0);
      dueDate = new Changeable<>(0);
      rating = new Changeable<>(0);
   }

   public Book(int id,
               String title,
               String description,
               int volume,
               int publicationDate,
               int pages,
               String price,
               String value,
               int dueDate,
               int readDate,
               int edition,
               String isbn,
               String web)
   {
      this.id = id;
      this.title = new Changeable<>(title);
      this.description = new Changeable<>(description);
      this.volume = new Changeable<>(volume);
      this.publicationDate = new Changeable<>(publicationDate);
      this.pages = new Changeable<>(pages);
      this.price = new Changeable<>(price);
      this.value = new Changeable<>(value);
      this.readDate = new Changeable<>(readDate);
      this.dueDate = new Changeable<>(dueDate);
      this.edition = new Changeable<>(edition);
      this.isbn = new Changeable<>(isbn);
      this.web = new Changeable<>(web);
   }
}
