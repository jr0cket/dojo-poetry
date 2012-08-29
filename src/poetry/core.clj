(ns poetry.core
  (:require [clj-http.client :as http]
            [clojure.string :as str]))

(def haiku-url
  "http://search.twitter.com/search.json?q=%23haiku&result_type=recent&rpp=100")

(defn raw-haikus []
  (->> (http/get haiku-url {:as :json})
       :body
       :results
       (map :text)))

(defn trim-lines [s]
  (->> (str/split-lines s)
       (map str/trim)
       (remove str/blank?)
       (str/join "\n")))

(defn sanitize-haiku [haiku]
  (-> haiku
      (str/replace #"RT" "")
      (str/replace #"#\w+" "")
      (str/replace #"@\w+:?" "")
      (str/replace #"http://[^\s]+" "")
      (str/replace #"/+|•|\||~" "\n")
      (str/replace #"\.\.\." "\n")))

(defn valid-haiku? [haiku]
  (= 3 (count (str/split-lines haiku))))

(defn twitter-haikus []
  (->> (raw-haikus)
       (map sanitize-haiku)
       (map trim-lines)
       (filter valid-haiku?)))

(defn max-line-size [s]
  (->> (str/split-lines s)
       (map count)
       (apply max)))

(def color
  {:red     "\033[31m"
   :green   "\033[32m"
   :yellow  "\033[33m"
   :blue    "\033[34m"
   :magenta "\033[35m"
   :cyan    "\033[36m"
   :default "\033[39m"})

(defn horizontal-line [width]
  (print (color :green))
  (print "+-")
  (dotimes [_ width] (print "-"))
  (println "-+")
  (print (color :default)))

(defn pad [s width]
  (apply str s (repeat (- width (count s)) " ")))

(defn print-haiku [haiku]
  (let [width (max-line-size haiku)]
    (horizontal-line width)
    (doseq [line (str/split-lines haiku)]
      (println (str (color :green) "|" (color :default))
               (pad line width)
               (str (color :green) "|" (color :default))))
    (horizontal-line width)))

(defn rand-haiku []
  (rand-nth (twitter-haikus)))

(defn -main []
  (print-haiku (rand-haiku)))
