(ns book-as-data.cli
  (:require [book-as-data.parser :as p]
            [book-as-data.template :as t]))

(def engines
  {:qa {:format p/qa-format :scanner p/qa-scanner :fsm p/qa-fsm :act p/qa-act
        :template t/qa-page}})

;;TODO add title as option
(defn -main [& [type file :as args]]
  (if-let [{tmpl :template fmt :format :as engine}
           (get engines (keyword type))]
    (let [output (clojure.string/replace file #".\w+$" ".html")
          title (clojure.string/replace file #".\w+$" "")
          lines (clojure.string/split-lines (slurp file))
          items (fmt (p/parse engine lines))]
      (println "Convert " file " of " (count lines)
               " lines into " (count items) "records saved in " output)
      (spit output
            (apply str
                   (tmpl title items))))
    (println "No parser found for " args)))