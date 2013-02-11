# Book As Data

Simple tool to produce any kind of document in a decoupled manner.

Separation of concerns when writing a document means :

   * Write content in the simplest way possible.
   * Choose your output format (HTML, pdf...).
   * Be able to declare own version of the two above steps.

## Formats supported

### Question & Answers

Input format

```
Q How to save collection into a file ?
A (spit "filename" (apply str coll))

Q Get current classpath ?
A  (println (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))
A  (.getURLs (->  (java.lang.Thread/currentThread)  (.getContextClassLoader)))
```

Default output format is HTML.

## Internals

Input file is transformed into a plain clj data-structure.
HTML template are defined with enlive.

## Usage

`lein run "qa" <file in QA format>`  transforms QA file into html file.

## TODO

Here the wish-list :

   * Define your own format.
   * Choose your own template.
   * Build a fully clj blog site generator.
   * Define other format : dictionary , slides ...
## License

Copyright © 2013 Maximilien Rzepka

Distributed under the Eclipse Public License, the same as Clojure.
