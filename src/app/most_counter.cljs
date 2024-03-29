(ns app.most-counter
  (:require ["@most/core" :as most
             :refer [constant, scan, merge, mergeArray, tap, runEffects]]
            ["@most/scheduler" :refer [newDefaultScheduler]]
            ["@most/dom-event" :refer [click]]
            [clojure.spec.alpha :as s]))

(defn qs [sel el]
  (.querySelector el sel))

(defn make-counter! []
  (let [inc-button (qs "[name=inc]" js/window.document)
        dec-button (qs "[name=dec]" js/window.document)
        value      (qs ".value" js/window.document)
        inc$       (constant 1 (click inc-button))
        dec$       (constant -1 (click dec-button))
        counter$   (scan + 0 (merge inc$ dec$))

        render! (tap (fn [total] (aset value "innerText" (js/String total))) counter$)]

    (runEffects render! (newDefaultScheduler))))

;; new counter 2

(s/def ::actions #{:counter/inc :counter/dec})

(def decide
  {:counter/inc 10
   :counter/dec -1})

(defn run-action [value action]
  {:pre [(s/valid? ::actions action)]}
  (+ value (decide action)))

(defn make-counter-2! []
  (let [inc-button (qs "[name=inc]" js/window.document)
        dec-button (qs "[name=dec]" js/window.document)
        value      (qs ".value" js/window.document)

        inc$ (constant :counter/inc (click inc-button))
        dec$ (constant :counter/dec (click dec-button))

        counter$ (scan
                  (fn [total dom-signal] (run-action total dom-signal))
                  0 (merge inc$ dec$))

        render! (tap #(aset value "innerText" (js/String %)) counter$)]

    (runEffects render! (newDefaultScheduler))))

;; new counter 3

(def decide2
  {:counter/inc inc
   :counter/dec dec})

(defn run-action2 [value action]
  {:pre [(s/valid? ::actions action)]}
  ((decide2 action) value))

(defn dom-elements []
  (map #(qs % js/window.document) ["[name=inc]"
                                   "[name=dec]"
                                   ".value"]))

(defn make-counter-3! []
  (let [[inc-button dec-button value] (dom-elements)

        state    0
        inc$     (->> (click inc-button)
                      (constant :counter/inc))
        dec$     (->> (click dec-button)
                      (constant :counter/dec))
        counter$ (scan
                  #_(fn [total dom-signal] (run-action2 total dom-signal))
                  run-action2
                  state (mergeArray #js [inc$ dec$]))
        render!  (tap #(aset value "innerText" (js/String %)) counter$)]

    (runEffects render! (newDefaultScheduler))))


;; new counter 4


(defn inc$ [action$ store]
  (most/map (fn [val] (inc val)) action$))

(defn dec$ [action$ store]
  (most/map (fn [val] (inc val)) action$))

(def decide3
  {:counter/inc inc$
   :counter/dec dec$})

(defn run-action3 [value action]
  {:pre [(s/valid? ::actions action)]}
  ((decide3 action) value))

(defn dom-elements! []
  (map #(qs % js/window.document) ["[name=inc]"
                                   "[name=dec]"
                                   ".value"]))

(defn make-counter-4! []
  (let [[inc-button dec-button value] (dom-elements!)

        state    0
        inc$     (->> (click inc-button)
                      (constant :counter/inc))
        dec$     (->> (click dec-button)
                      (constant :counter/dec))
        counter$ (scan
                  #_(fn [total dom-signal] (run-action2 total dom-signal))
                  run-action2
                  state (mergeArray #js [inc$ dec$]))
        render!  (tap #(aset value "innerText" (js/String %)) counter$)]

    (runEffects render! (newDefaultScheduler))))

(comment
  "so the thing is! effects and coeffects, vs. flattened streams for side effects.
main thing, changing state based on actions, should be pure functions, but still…
who decides about state change. late-bound messaging tells us, that receiver
decides only in moment of receiving message, but… it decides basing on data got
from message, so how many data can we send?")
