(ns cyrats.arena-model)

;; Constructors

(defn ->module
  "Creates new module"
  [[hp ap dp]]
  {:pre [(<= 0 hp 4)
         (<= 0 ap 4)
         (<= 0 dp 4)]}
  {:hp hp
   :ap ap
   :dp dp})

(defn ->rat
  "Creates new rat"
  [modules]
  {:pre [(seq modules) ;; list of modules can't be empty
         (<= (count modules) 3)]}
  {:modules modules})

(defn ->model
  "Creates whole model"
  ([rats modules]
   (->model rats modules nil))
  ([rats modules selected-rat]
   {:rats (mapv (comp ->rat (partial mapv ->module)) rats)
    :storage (mapv ->module modules)
    :selected-rat selected-rat}))

;; Helpers

(defn total-stats
  "Calculates total stats from the list of modules"
  [ms]
  (apply merge-with + ms))

(defn free-cells?
  "Returns true if rat have free cells for modules"
  [rat]
  (> 3 (count (:modules rat))))

(defn- work-with-nth
  [n v]
  {:pre [(<= 0 n)
         (< n (count v))]}
  (let [[xs [x & xxs]] (split-at n v)]
    [[xs xxs] x]))

(defn- recompose
  ([[xs xxs] insertion]
   (vec (concat xs insertion xxs))))

;; Model manipulations

(defn select-rat
  [model idx]
  {:pre [(< idx (count (:rats model)))]}
  (assoc model :selected-rat idx))

(defn use-module
  "Inserts module (referenced by index) into the rat's cell"
  [{:keys [rats storage selected-rat] :as model} idx]
  (let [[other-modules module] (work-with-nth idx storage)
        [new-rats new-selection]
        (if (or (nil? selected-rat)
                (not (free-cells? (get rats selected-rat))))
          ;; insertion into nearest free cell
          (let [[full other] (split-with #(= 3 (count (:modules %))) rats)
                [new-rat other'] (if (seq other)
                                   (let [[x & xs] other]
                                     [(update-in x [:modules] conj module) xs])
                                   [(->rat [module]) []])]
            [(vec (concat full [new-rat] other'))
             nil])
          ;; insertion into selected cell
          (let [[other-rats rat] (work-with-nth selected-rat rats)
                new-rat (update-in rat [:modules] conj module)]
            [(recompose other-rats [new-rat])
             (when (free-cells? new-rat)
               selected-rat)]
            ))]
    (assoc model
           :storage (recompose other-modules [])
           :selected-rat new-selection
           :rats new-rats
           )))

(defn drop-module
  "Removes module from the particular rat (and position within it)
  and puts module to storage"
  [{:keys [rats storage] :as model} idx mod-idx]
  (let [[other-rats rat] (work-with-nth idx rats)
        [other-cells cell] (work-with-nth mod-idx (:modules rat))
        maybe-new-rat (let [new-cells (recompose other-cells [])]
                        (if (seq new-cells)
                          [(assoc rat :modules new-cells)]
                          []))]
    (assoc model
           :rats (recompose other-rats maybe-new-rat)
           :storage (conj storage cell)
           :selected-rat (if (seq? maybe-new-rat)
                           idx
                           nil))))
