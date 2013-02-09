(ns book-as-data.parser)

(defn splitter [^String text]
  (clojure.string/split text #"[,\s]+"))

(defn make-scan
  [regexp & {tokenizer :token :or {tokenizer identity}}]
  (fn scan [^String line]
      (when (.startsWith (.trim line) regexp)
        (tokenizer (.trim (.substring line (.length regexp)))))
      ))

;; Parsing Part
(defn parser [options-map & rules] )

(defn scan
  "scans lines with rules in map scanner take first rule returning something"
  [scanner lines]
  (map
   (fn [line]
     (some (fn [[rule sc]] (when-let [t (sc line)] [rule t]))
           scanner))
   lines))


(defn make-stepper [fsm actions]
  (fn [{rule :rule :as world} [new-rule data]]
    (if-let [states (fsm rule)]
      (if (states new-rule) ;;change rule allowed
        (assoc ((actions new-rule) world data) :rule new-rule)
        (println "change state impossible : " rule " -> " new-rule))
      (println "No associated rule found for " rule))
    ))

;; Q&A parser

(def qa (parser {:main :sheet}
                :sheet [:item+] :item [:question :answer* :tags]
                :question "A" :answer "Q" :tags "T"
                :empty ""))

(def qa-fsm {:sheet #{:question :empty}
             :question #{:answer :tags :empty :question}
             :answer #{:tags :question :empty :answer}
             :tags #{:answer :question :empty}
             :empty #{:question :empty}
             })

(def qa-act {:sheet (fn [world text]
                      {:context [:sheet] :tag :sheet :content []})
             :question (fn [world text]
                         (let [item {:tag :item
                                     :content [{:tag :question :content text}]}
                               position (-> world :content count)]
                           (-> world
                               (update-in [:content] (fnil conj []) item)
                               (assoc :context [:content position :content]))))
             :answer (fn [{context :context :as world} text]
                       (-> world
                           (update-in context conj {:tag :answer :content text})
                           (assoc :rule :answer)))
             :tags (fn [{context :context :as world} text]
                       (update-in world context conj
                                  {:tag :tags
                                   :content (splitter text)}))
             :empty (fn [world text]
                      (assoc world :rule :sheet :context [:content]))
             })

(def qa-scanner {:answer (make-scan "A")
               :question (make-scan "Q")
               :tags (make-scan "T" :token splitter)
               :empty (fn [^String line] (when-not (seq (.trim line)) ""))})

;; Usage

(comment

  (reduce (make-stepper qa-fsm qa-act)
           {:tag :sheet :rule :sheet :context [:content] :content []}
           (scan qa-scanner (clojure.string/split-lines (slurp "<Q&A file>"))))
  )

#_(def x1 [
           ""
           "Q How to activate IRC ?"
           "A"
           ""
           "Q How to record and play macro ?"
           "A start C - x (  end C - x) play C - x e replay 27 times C - u 27 C - x e"
           "A C - n go to the next line"
           ""
           "Q How to refresh a buffer ?"
           "A M-x revert-buffer"
           ""
           "Q How to activate config changes in org-file"
           "A C-c C-c when cursor in changed line"
           ""
           "Q Useful shorcuts in org-mode"
           "A add tag : C-c C-q  or C-c C-c if cursor on headline"
           "A toggle TODO flags : C-c C-t"
           "A view only specific items  (sparse tree) : C-c /  then choose m t r"
           "A Visibility cycling : C-u <TAB>  global collpase/expand"
           "A Exporting : C-c C-e and then choose the format : h for HTML ,..."
           ]
    )
