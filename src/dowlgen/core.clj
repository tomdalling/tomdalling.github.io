(ns dowlgen.core
  (:require [me.raynes.cegdown :as markdown]
            [net.cgrand.enlive-html :as enlive]
            [net.cgrand.reload]
            [stasis.core :as stasis]
            [optimus.export]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus-sass.core]
            [clojure.data.json :as json]
            [clj-time.core :as clj-time]
            [clj-time.format :as time-format]))

(net.cgrand.reload/auto-reload *ns*)

(def input-dir "resources")
(def output-dir "dist")
(def site-url "http://www.tomdalling.com")
(def markdown-date-formatter (time-format/formatter "yyyy-MM-dd"))
(def human-date-formatter (time-format/formatter "dd MMM, yyyy"))

(defn map-map [f coll]
  (into {} (map #(apply f %) coll)))

(defn read-split-frontmatter [s]
  (try 
    (let [reader (java.io.PushbackReader. (java.io.StringReader. s))
          frontmatter (clojure.edn/read reader)]
      (if (map? frontmatter)
        [frontmatter (slurp reader)]
        [nil s]))
    (catch Exception e [nil s])))

(defn remove-extension [path]
  (let [dot-idx (.lastIndexOf path ".")]
    (if (= dot-idx -1)
      path
      (.substring path 0 dot-idx))))

(defn change-extension [path extension]
  (str (remove-extension path) "." extension))

(defn disqus-js [article]
  (when (:disqus-id article)
    (str "var disqus_shortname = 'tomdalling';\n"
         "var disqus_identifier = " (json/write-str (:disqus-id article)) ";\n"
         "var disqus_title = " (json/write-str (:title article)) ";\n"
         "var disqus_url = " (json/write-str (str site-url (:uri article))) ";\n")))

(defn build-article [path file-content]
  (let [[frontmatter md] (read-split-frontmatter file-content)]
    (as-> {} article
      (merge article frontmatter)
      (assoc article :uri (str (remove-extension path) "/"))
      (assoc article :content (markdown/to-html md [:autolinks :fenced-code-blocks :strikethrough]))
      (update-in article [:date] #(time-format/parse markdown-date-formatter %)))))

(defn get-articles []
  (map #(apply build-article %)
       (stasis/slurp-directory input-dir #"^/blog/.*\.markdown$")))

(defn recent-articles [n]
  (take n
    (sort-by :date (get-articles))))

(enlive/deftemplate article-template "templates/article.html" [article]
  [:title] (enlive/content (:title article))
  [:h1] (enlive/content (:title article))
  [:.post-date] (enlive/content (time-format/unparse human-date-formatter (:date article)))
  [:.post-content] (enlive/html-content (:content article))
  [:#disqus_script] (enlive/content (disqus-js article))
  [:ul.recent-posts :li] (enlive/clone-for [article (recent-articles 5)]
                                           [:a] (enlive/do-> (enlive/set-attr :href (:uri article))
                                                             (enlive/content (:title article)))))

(defn get-assets []
  (concat (assets/load-assets "." ["/style.scss"])))

(defn get-pages []
  (into {}
    (for [article (get-articles)]
      [(str (:uri article) "index.html")
       (apply str (article-template article))])))

(def app
  (optimus/wrap (stasis/serve-pages get-pages)
                get-assets
                optimizations/all
                serve-live-assets))

(defn export []
  (let [assets (optimizations/all (get-assets) {})]
    (stasis/empty-directory! output-dir)
    (optimus.export/save-assets assets output-dir)
    (stasis/export-pages (get-pages) output-dir {:optimus-assets assets})))

