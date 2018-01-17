(ns jp.c4se.bookup.book.openbd
  (:require [cats.core :as m]
            [cats.monad.either :as either]
            [clojure.data.json :as json]
            [clojure.java.io :refer [reader]]
            [jp.c4se.bookup.book :as book])
  (:import java.lang.IllegalStateException
           (java.net HttpURLConnection UnknownServiceException URL)
           (java.io BufferedInputStream IOException)
           java.text.SimpleDateFormat))

(defn- parse-pubdate [pubdate] (.parse (SimpleDateFormat. "yyyyMMdd HHmmssZ") (str pubdate " 000000+0900")))

(defn- find-raw-by-isbn
  [isbn]
  (let [^HttpURLConnection conn
        (.openConnection (URL. (format "https://api.openbd.jp/v1/get?isbn=%d" (read-string isbn))))]
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

(defn- build-by-raw
  [json isbn]
  (let [summary (((json/read-str json) 0) "summary")]
    (book/->Book isbn
                 (summary "author")
                 (parse-pubdate (summary "pubdate"))
                 (summary "publisher")
                 (summary "title"))))

;(require '[jp.c4se.bookup.book.openbd :as book])
;(book.openbd/find-by-isbn "9784480097545")
(defn find-by-isbn
  [isbn]
  (m/alet [body (find-raw-by-isbn isbn)]
          (build-by-raw body isbn)))
