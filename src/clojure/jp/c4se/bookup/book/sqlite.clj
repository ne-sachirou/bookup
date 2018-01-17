(ns jp.c4se.bookup.book.sqlite
  (:require [cats.core :as m]
            [cats.monad.either :as either]
            [jp.c4se.bookup.book :as book]
            jp.c4se.bookup.SQLiteOpenHelper)
  (:import android.content.ContentValues
           java.lang.String
           java.text.SimpleDateFormat
           java.util.Date
           jp.c4se.bookup.SQLiteOpenHelper))

(defn- format-date [date] (.format (SimpleDateFormat. "yyyy-MM-ddT00:00:00+09:00") date))

(defn- parse-date [date] (.parse (SimpleDateFormat. "yyyy-MM-ddT00:00:00+09:00") date))

(defn find-by-isbn
  [isbn context]
  (let [cursor (-> context
                   SQLiteOpenHelper.
                   .getReadableDatabase
                   (.query "books"
                           ["author" "pubdate" "publisher" "title"]
                           "isbn = ?"
                           [isbn]
                           nil
                           nil
                           nil
                           "1"))]
    (if (.moveToFirst cursor)
      (let [author (.getString cursor (.getColumnIndex cursor "author"))
            pubdate (parse-date (.getString cursor (.getColumnIndex cursor "pubdate")))
            publisher (.getString cursor (.getColumnIndex cursor "publisher"))
            title (.getString cursor (.getColumnIndex cursor "title"))]
        (either/right (book/->Book isbn author pubdate publisher title)))
      (either/left nil))))

(defn insert
  [book context]
  (let [values (doto (ContentValues.)
                 (.put "isbn" ^String (:isbn book))
                 (.put "author" ^String (:author book))
                 (.put "pubdate" ^String (format-date (:pubdate book)))
                 (.put "publisher" ^String (:publisher book))
                 (.put "title" ^String (:title book)))]
    (-> context
        SQLiteOpenHelper.
        .getWritableDatabase
        (.insert nil values))
    (either/right book)))
