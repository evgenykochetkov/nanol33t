(ns nanol33t.app
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce !global
         (r/atom {:tempo               120
                  :instrument-selected :a
                  :instruments         {:a {:selected-pattern 0
                                            :patterns         [{:pattern-length 16
                                                                :current-step   0
                                                                :pattern        [64 nil nil 64 64 nil nil 64 64 nil nil 64 64 nil nil 64]}]}
                                        :b {} :c {} :d {} :f {} :g {} :h {}
                                        }}))

(defn note-to-freq [note]
      (* (js/Math.pow 2 (/ (- note 69) 12) ) 440))


(defn pattern-selector []
      [:div
       (for [x (range 10)]
            [:button {:key x} x])])


(defn step-mouse-move [current-pos start-pos index]
  (let [delta (- start-pos current-pos)
        selected-pattern (get-in @!global [:instruments (:instrument-selected @!global) :selected-pattern])]
    (swap! !global (fn [s] (update-in s [:instruments (:instrument-selected @!global) :patterns selected-pattern :pattern index]
                                      #(max (min (- % delta) 127) 0))))))



(defn step-view [index pattern]
      (let [selected-instrument (:instrument-selected @!global)
            selected-pattern (get-in @!global [:instruments selected-instrument :selected-pattern])
            !pressed? (r/atom false)
            !start-pos (r/atom nil)]
           (fn [index pattern]
               (let [step (get-in @!global [:instruments selected-instrument :patterns selected-pattern :pattern index])]
                    [:button {:class         (if (= (:current-step pattern) index) "current")
                              :style         {:background (str "linear-gradient(180deg, white, white " (dec step) "%, red " step "%)")}
                              :on-mouse-move #(if @!pressed? (step-mouse-move (.-pageY %) @!start-pos index))
                              :on-mouse-down #(do (reset! !pressed? true)
                                                  (reset! !start-pos (.-pageY %)))
                              :on-mouse-up   #(reset! !pressed? false)}
                     (if (nil? step) 0 step)]))))


(defn pattern-view []
  (let [selected-instrument (get-in @!global [:instruments (:instrument-selected @!global)])
        selected-pattern    (get-in selected-instrument [:patterns (:selected-pattern selected-instrument)])]
       [:div.pattern-view
        (for [index (range (:pattern-length selected-pattern))]
             ^{:key index} [step-view index selected-pattern])]))


(defn instrument-selector [index instrument-name]
  [:button {:key index} (name (key instrument-name))])



(defn root-component []
  [:div
   [pattern-selector]
   [pattern-view]
   (map-indexed instrument-selector (:instruments @!global))])


(defn advance-step []
  (swap! !global (fn [s] (update-in s [:instruments :a :patterns 0 :current-step] #(if (= % 15) 0 (inc %)))))
      (let [current-step (get-in @!global [:instruments :a :patterns 0 :current-step])]
           (prn (get-in @!global [:instruments :a :patterns 0 :pattern current-step]))))


(defonce bpm (js/setInterval advance-step 500))

(defn init []
  (r/render-component [root-component]
                            (.getElementById js/document "container")))
