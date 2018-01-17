(ns jp.c4se.bookup.main
  (:require [cats.core :as m]
            [cats.monad.either :as either]
            [jp.c4se.bookup.book.openbd :as book.openbd]
            [jp.c4se.bookup.book.sqlite :as book.sqlite]
            [neko.activity :refer [defactivity set-content-view!]]
            [neko.debug :refer [*a]]
            [neko.notify :refer [toast]]
            [neko.resource :as res]
            [neko.find-view :refer [find-view]]
            [neko.threading :refer [on-ui]])
  (:import android.app.Activity
           com.google.zxing.integration.android.IntentIntegrator))

(res/import-all)

(defn scan-bar-code
  [^Activity activity]
  (.initiateScan (IntentIntegrator. activity)))

(defn parse-bar-code-scan-result
  [request-code result-code intent]
  (let [scan-result (IntentIntegrator/parseActivityResult request-code result-code intent)]
    (if (nil? scan-result)
      (either/left "No bar-code")
      (either/right (.getContents scan-result)))))

(defactivity jp.c4se.bookup.BarCodeReaderActivity
  :key :main

  (onCreate
   [this bundle]
   (.superOnCreate this bundle)
   (neko.debug/keep-screen-on this)
   (on-ui
    (set-content-view!
     (*a)
     [:linear-layout {:orientation :vertical
                      :layout-width :fill
                      :layout-height :wrap}
      [:button {:text R$string/scan_bar_code
                :on-click (fn [_] (scan-bar-code (*a)))}]])))

  (onActivityResult
   [this request-code result-code intent]
   (.superOnActivityResult this request-code result-code intent)
   (either/branch
    (m/alet [isbn (parse-bar-code-scan-result request-code result-code intent)
             book @(future (book.openbd/find-by-isbn isbn))
             (book.sqlite/find-by-isbn (:isbn book))
             ]
            book)
    (fn [error] (toast error :long))
    (fn [book] (toast (:title book) :long)))))
