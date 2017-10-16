(ns jp.c4se.bookup.book
  (:require [cats.core :as m]
            [cats.monad.either :as either]
            [clojure.data.json :as json]
            [clojure.java.io :refer [reader]])
  (:import java.lang.IllegalStateException
           (java.net HttpURLConnection UnknownServiceException URL)
           (java.io BufferedInputStream IOException)
           java.text.SimpleDateFormat))

(defn get-openbd
  [isbn]
  (let [conn (.openConnection (URL. (format "https://api.openbd.jp/v1/get?isbn=%d" (read-string isbn))))]
    (try
      (.setRequestProperty conn "user-agent" "jp.c4se.bookup v0.1.0")
      (with-open [*in* (BufferedInputStream. (.getInputStream conn))]
        (let [resp (slurp *in*)]
          (if (== 200 (.getResponseCode conn))
            (either/right resp)
            (either/left resp))))
      (catch IllegalStateException error (either/left error))
      (catch IOException error (either/left error))
      (catch UnknownServiceException error (either/left error))
      (finally (.disconnect conn)))))

;(require '(java.time ZonedDateTime ZoneId)) ; added in API level 26
;(require 'java.time.format.DateTimeFormatter) ; added in API level 26
;(defn parse-pubdate
;  [pubdate]
;  (let [zone-id (ZoneId/of "Asia/Tokyo")
;        formatter (.withZone (DateTimeFormatter/ofPattern "yyyyMMdd HHmmss") zone-id)]
;    (ZonedDateTime/parse (str pubdate " 000000") formatter)))
(defn parse-pubdate
  [pubdate]
  (.parse (SimpleDateFormat. "yyyyMMdd HHmmssZ") (str pubdate " 000000+0900")))

(defn new-from-openbd-response
  [json isbn]
  (let [summary (((json/read-str json) 0) "summary")]
    [:book {:isbn isbn
            :pubdate (parse-pubdate (summary "pubdate"))
            :title (summary "title")
            :author (summary "author")}]))

;(require '[jp.c4se.bookup.book :as book])
;(book/find-by-isbn "9784480097545")
(defn find-by-isbn
  [isbn]
  (m/alet [body @(future (get-openbd isbn))]
          (new-from-openbd-response body isbn)))
