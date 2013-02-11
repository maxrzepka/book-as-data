(ns book-as-data.parser)


;; Some Utilities

(defn splitter [^String text]
  (if (string? text)
    (clojure.string/split text #"[,\s]+")
    text))

(defn make-scan
  [regexp & {tokenizer :token :or {tokenizer identity}}]
  (fn scan [^String line]
      (when (.startsWith (.trim line) regexp)
        (tokenizer (.trim (.substring line (.length regexp)))))
      ))

(defn extract-content [node & [tag]]
  (let [content (:content node)]
    (if (and tag (sequential? content))
      (keep #(when (= tag (get % :tag)) (:content %)) content)
      content)))

;; Parsing Part

;;TODO
(defn parser [options-map & rules] )

(defn scan
  "Transform lines into tokens as [rule data].
First rule matching in scanner is applied"
  [scanner lines]
  (keep
   (fn [line]
     (some (fn [[rule sc]] (when-let [data (sc line)] [rule data]))
           scanner))
   lines))


(defn make-stepper
  "Returns function parsing a token given a FSM and a map rule -> action "
  [fsm actions]
  (fn [{rule :rule :as world} [new-rule data]]
    (if-let [states (fsm rule)]
      (if (states new-rule) ;;change rule allowed
        (assoc ((actions new-rule) world data) :rule new-rule)
        (println "change state impossible : " rule " -> " new-rule " [" data"]"))
      (println "No associated rule found for " rule))
    ))

;; Q&A parser

;; Whishfull parser definition
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

(defn qa-format [tree]
  (map
   (fn [item]
     {:question (first (extract-content item :question))
           :answer (extract-content item :answer)
           :tags (extract-content item :tags)})
   (:content tree))
  )


;; Usage

(defn parse
  "Parses lines given a grammar and returns a tree of nodes"
  [{:keys [scanner fsm act format] :as grammar} lines]
  (reduce (make-stepper fsm act)
               {:tag :sheet :rule :sheet
                :context [:content] :content []}
               (scan scanner lines)))

(comment

  (map format-qa
   (reduce (make-stepper qa-fsm qa-act)
           {:tag :sheet :rule :sheet :context [:content] :content []}
           (scan qa-scanner (clojure.string/split-lines (slurp "<Q&A file>")))))
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
           ]
    )
