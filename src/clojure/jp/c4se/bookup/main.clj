(ns jp.c4se.bookup.main
  (:require [neko.activity :refer [defactivity set-content-view!]]
            [neko.debug :refer [*a]]
            [neko.notify :refer [toast]]
            [neko.resource :as res]
            [neko.find-view :refer [find-view]]
            [neko.threading :refer [on-ui]])
  (:import (com.google.zxing.integration.android IntentIntegrator IntentResult)))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defn scan-bar-code
  [activity]
  (.initiateScan (IntentIntegrator. activity)))

;; This is how an Activity is defined. We create one and specify its onCreate
;; method. Inside we create a user interface that consists of an edit and a
;; button. We also give set callback to the button.
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
   (let [scan-result (IntentIntegrator/parseActivityResult request-code result-code intent)]
     (if (nil? scan-result)
       nil
       (toast (.getContents scan-result) :long)))))
