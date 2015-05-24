(ns examples-transducer.core)

(defn -main
  "I don't do a whole lot."
  [& args]

  ;; TODO:
  ;;  transduce 설명.

  (= (reduce + (filter odd? (map #(+ 1 %) (range 0 10))))
     ;;;;;;;;;;;;;;;;;;;;;;; vs ;;;;;;;;;;;;;;;;;;;;;;;;;
     (do
       (def xform
         (comp (map #(+ 1 %))
               (filter odd?)))
       (transduce xform + (range 0 10))))

  ;; comp 함수는 주어진 함수들을 순서대로 호출하는 함수를 반환한다.
  ;; 즉, "(comp f1 f2)"는 "(fn [& args] (f1 (apply f2 args)))"가 된다.
  ;; 그리고 "(map f)"는 아래와 같은 step 함수를 반환한다.

  #_(fn [f]
      (fn [rf]
        (fn
          ([] (rf))
          ([result] (rf result))
          ([result input]
           (rf result (f input)))
          ([result input & inputs]
           (rf result (apply f input inputs))))))

  ;; step 함수는 함수 파이프라인을 구성하기 위한 함수로
  ;; 실제 데이터를 처리하는 함수 "f"와 그 결과를 정리하기위한 함수 "rf"을 받는다.
  ;; 예를 들어, 아래와 같이 "#(+ 1 %)"를 "f"로 "conj"를 "rf"로 하는 함수를 만들 수 있다.

  #_(reduce ((map #(+ 1 %)) conj) [] (range 0 10))

  ;; 위 코드에서 "((map #(+ 1 %)) conj)"는 "(fn [result input] (conj result (+ 1 input)))"와 동일하다.
  ;; 즉, step 함수는 "f"에 입력값을 줘서 나온 그 결과를 "rf"로 전달한다.
  ;; 다시 말해, "rf"로 "f"의 결과가 전달된다.

  (= (reduce ((map #(+ 1 %)) conj) [] (range 0 10))
     ;;;;;;;;;;;;;;;;;;;; vs ;;;;;;;;;;;;;;;;;;;;;;
     (reduce (fn [result input]
               (conj result (+ 1 input)))
             [] (range 0 10)))

  ;; 이제, 아래 코드와 같이 처음 transduce를 사용한 코드와 비슷하게 filter를 함수 파이프라인에 추가하였다.
  ;; 즉, step 함수의 "rf"가 "((filter odd?) conj)"가 되었고, 이전 "rf"는 새로운 step 함수의 "rf"가 되었다.
  ;; 다시 말해, step 함수의 "rf"가 또 다른 step 함수가 되었다.
  ;; 이는 transduce가 함수 파이프라인을 만드는 방식이다.

  #_(reduce ((map #(+ 1 %)) ((filter odd?) conj)) [] (range 0 10))

  ;; 아래와 같이 filter의 step 함수는 map의 그것과는 생김새가 좀 다르다.
  ;; step 함수의 "f"의 결과가 참/거짓에 따라 "rf" 함수를 호출하거나 하지 않는다.

  #_(fn [pred] ; f = pred
      (fn [rf]
        (fn
          ([] (rf))
          ([result] (rf result))
          ([result input]
           (if (pred input) ; f = pred
             (rf result input)
             result)))))

  ;; 위 코드의 "((map #(+ 1 %)) ((filter odd?) conj))"를 풀어보면
  ;; "(fn [result input] (if (odd? (+ 1 input)) (conj result (+ 1 input)) result))"가 된다.

  (= (reduce ((map #(+ 1 %)) ((filter odd?) conj)) [] (range 0 10))
     ;;;;;;;;;;;;;;;;;;;;;;;;;;;; vs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     (reduce (fn [result input]
               (let [input (+ 1 input)]
                 (if (odd? input)
                   (conj result input)
                   result)))
             [] (range 0 10)))

  ;; 이 코드를 위 transduce를 사용한 코드와 비교하면 아래와 같다.
  ;; transduce는 step 함수들을 이용해 함수들의 입력과 출력을 연결해주는 방식이다.

  (= (reduce + (filter odd? (map #(+ 1 %) (range 0 10))))
     ;;;;;;;;;;;;;;;;;;;;;;;;;;;; vs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     (do
       (def xform
         (comp (map #(+ 1 %))
               (filter odd?)))
       (transduce xform + (range 0 10)))
     ;;;;;;;;;;;;;;;;;;;;;;;;;;;; vs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     (reduce ((map #(+ 1 %)) ((filter odd?) +)) 0 (range 0 10))
     ;;;;;;;;;;;;;;;;;;;;;;;;;;;; vs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     (reduce (fn [result input]
               (let [input (+ 1 input)]
                 (if (odd? input)
                   (+ result input)
                   result)))
             0 (range 0 10)))

  ;; 그렇다면, transduce는 어떤 이점이 있는 것인가?
  ;; 아래 코드와 비교했을 때 transduce는 함수마다 리스트를 생성하지 않는다.

  #_(reduce + (filter odd? (map #(+ 1 %) (range 0 10))))
  (map #(+ 1 %) (range 0 10))
  ;;=> (1 2 3 4 5 6 7 8 9 10)
  (filter odd? '(1 2 3 4 5 6 7 8 9 10))
  ;;=> (1 3 5 7 9)
  (reduce + '(1 3 5 7 9))
  ;;=> 25

  ;; 다음으로 아래 코드와 비교했을 때는 transduce를 사용한 코드가 비교적 보기 좋다.

  #_(reduce (fn [result input]
              (let [input (+ 1 input)]
                (if (odd? input)
                  (+ result input)
                  result)))
            0 (range 0 10))
  )
