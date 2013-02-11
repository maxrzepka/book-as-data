(ns book-as-data.template
  (:require [net.cgrand.enlive-html :as h]))

;; QA template

(h/defsnippet qa-dd "qa.html" [:#qa :dl [:dd (h/nth-of-type 1)]]
  [description]
  [h/root]  (h/content description))

(h/defsnippet qa-dt "qa.html" [:#qa :dl :dt]
  [title]
  [h/root] (h/content title))

(h/defsnippet qa-dl "qa.html" [:#qa]
  [{:keys [question answer tags]}]
  [h/root] (h/remove-attr :id)
  [:dl] (h/content (concat [(qa-dt question)]
                           (map qa-dd answer)
                           (map qa-dd tags))))

(h/deftemplate qa-page "qa.html" [title items]
  [:div.page-header :h2] (h/content title)
  [:#content] (h/content (map qa-dl items)))