(ns jp.c4se.bookup.main
  (:require [jp.c4se.bookup.book :as book]
            [cats.core :as m]
            [cats.monad.either :as either]
            [neko.activity :refer [defactivity set-content-view!]]
            [neko.debug :refer [*a]]
            [neko.notify :refer [toast]]
            [neko.resource :as res]
            [neko.find-view :refer [find-view]]
            [neko.threading :refer [on-ui]])
  (:import com.google.zxing.integration.android.IntentIntegrator))

(res/import-all)

(defn scan-bar-code
  [activity]
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
             book (book/find-by-isbn isbn)]
            book)
    (fn [error] (toast error :long))
    (fn [[:book {title :title}]] (toast title :long)))))
