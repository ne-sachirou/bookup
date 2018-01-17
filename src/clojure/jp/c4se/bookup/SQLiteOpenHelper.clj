(ns jp.c4se.bookup.SQLiteOpenHelper
  (:gen-class
   :extends android.database.sqlite.SQLiteOpenHelper
   :init init
   :constructors {[android.content.Context]
                  [android.content.Context String android.database.Cursor int]})
  (:import android.database.sqlite.SQLiteDatabase))

(def db-version 1)

(defn -init [context] [[context "bookup.db" nil db-version] []])

(defn -onCreate
  [^SQLiteDatabase db]
  (.execSQL db "CREATE TABLE books (
  isbn TEXT PRIMARY KEY
, pubdate TEXT NOT NULL
, publisher TEXT NOT NULL
, title TEXT NOT NULL
, author TEXT NOT NULL
)"))

(defn -onUpgrade [^SQLiteDatabase db old-version new-version] nil)
