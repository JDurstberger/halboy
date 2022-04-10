(ns halboy.resource-test
  (:require
   [clojure.test :refer :all]
   [halboy.resource :refer :all]))

(deftest halboy-resource
  (testing "should be able to add new-resource with self-link"
    (is (= {:href "/orders"}
          (-> (new-resource "/orders")
            (get-link :self))))

    (is (= {:href "/orders"}
          (-> (new-resource {:href "/orders"})
            (get-link :self)))))

  (testing "should be able to add and retrieve links from the resource"
    (is (= {:href "/orders"}
          (-> (new-resource)
            (add-link :self {:href "/orders"})
            (get-link :self))))

    (is (= "/orders"
          (-> (new-resource)
            (add-link :self {:href "/orders"})
            (get-href :self)))))

  (testing "should not add falsy links"
    (is (false?
          (contains?
            (-> (new-resource)
              (add-link :self nil)
              (links))
            :self))))

  (testing "should be able to add multiple links and they should stack"
    (is (= [{:href "/admins/2" :title "Fred"}
            {:href "/admins/5" :title "Kate"}]
          (-> (new-resource)
            (add-links
              :ea:admin {:href "/admins/2" :title "Fred"}
              :ea:admin {:href "/admins/5" :title "Kate"})
            (get-link :ea:admin))))

    (is (= ["/admins/2"
            "/admins/5"]
          (-> (new-resource)
            (add-links
              :ea:admin {:href "/admins/2" :title "Fred"}
              :ea:admin {:href "/admins/5" :title "Kate"})
            (get-href :ea:admin))))

    (is (= {:href "/admins/2" :title "Fred"}
          (-> (new-resource)
            (add-links
              :ea:admin nil
              :ea:admin {:href "/admins/2" :title "Fred"})
            (get-link :ea:admin)))))

  (testing "should be able to mix truthy and falsy links"
    (is (= nil
          (-> (new-resource)
            (add-links
              :self {:href "/orders"}
              :ea:admin nil)
            (get-link :ea:admin)))))

  (testing "should return nil when getting a link which does not exist"
    (is (nil? (-> (new-resource)
                (get-link :random)))))

  (testing "should be able to get all the links"
    (is (= {:self {:href "/orders"}}
          (-> (new-resource)
            (add-link :self {:href "/orders"})
            (links)))))

  (testing "should be able to add hrefs and it turns them into links"
    (is (= {:href "/orders"}
          (-> (new-resource)
            (add-link :self "/orders")
            (get-link :self))))

    (is (= {:href "/orders"}
          (-> (new-resource)
            (add-href :self "/orders")
            (get-link :self)))))

  (testing "should be able to add multiple hrefs and they should stack"
    (is (= [{:href "/admins/2"}
            {:href "/admins/5"}]
          (-> (new-resource)
            (add-links
              :ea:admin "/admins/2"
              :ea:admin "/admins/5")
            (get-link :ea:admin))))

    (is (= [{:href "/admins/2"}
            {:href "/admins/5"}]
          (-> (new-resource)
            (add-hrefs
              :ea:admin "/admins/2"
              :ea:admin "/admins/5")
            (get-link :ea:admin))))

    (is (= ["/admins/2"
            "/admins/5"]
          (-> (new-resource)
            (add-links
              :ea:admin "/admins/2"
              :ea:admin "/admins/5")
            (get-href :ea:admin)))))

  (testing "should return nil when getting a href which does not exist"
    (is (nil? (-> (new-resource)
                (get-href :random)))))

  (testing "add-resource adds an embedded resource"
    (is (= (-> (new-resource)
             (add-links {:self        {:href "/orders/123"}
                         :ea:basket   {:href "/baskets/98712"}
                         :ea:customer {:href "/customers/7809"}})
             (add-properties {:total    30.0
                              :currency "USD"
                              :status   "shipped"}))
          (-> (new-resource)
            (add-resource
              :ea:order
              (-> (new-resource)
                (add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"})))
            (get-resource :ea:order)))))

  (testing "should not be able to add nil resources"
    (is (false?
          (contains?
            (-> (new-resource)
              (add-resource :ea:order nil)
              (resources))
            :ea:order))))

  (testing "should be able to mix nil and not nil resources"
    (is (= (-> (new-resource)
             (add-links {:self        {:href "/orders/123"}
                         :ea:basket   {:href "/baskets/98712"}
                         :ea:customer {:href "/customers/7809"}})
             (add-properties {:total    30.0
                              :currency "USD"
                              :status   "shipped"}))
          (-> (new-resource)
            (add-resources
              :ea:order
              (-> (new-resource)
                (add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
              :ea:order nil)
            (get-resource :ea:order)))))

  (testing "should be able to add a list of resources"
    (is (= [(-> (new-resource)
              (add-links {:self        {:href "/orders/123"}
                          :ea:basket   {:href "/baskets/98712"}
                          :ea:customer {:href "/customers/7809"}})
              (add-properties {:total    30.0
                               :currency "USD"
                               :status   "shipped"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/124"}
                          :ea:basket   {:href "/baskets/97213"}
                          :ea:customer {:href "/customers/12369"}})
              (add-properties {:total    20.0
                               :currency "USD"
                               :status   "processing"}))]
          (-> (new-resource)
            (add-resource
              :ea:order
              (list (-> (new-resource)
                      (add-links {:self        {:href "/orders/123"}
                                  :ea:basket   {:href "/baskets/98712"}
                                  :ea:customer {:href "/customers/7809"}})
                      (add-properties {:total    30.0
                                       :currency "USD"
                                       :status   "shipped"}))
                (-> (new-resource)
                  (add-links {:self        {:href "/orders/124"}
                              :ea:basket   {:href "/baskets/97213"}
                              :ea:customer {:href "/customers/12369"}})
                  (add-properties {:total    20.0
                                   :currency "USD"
                                   :status   "processing"}))))
            (get-resource :ea:order)))))

  (testing "should be able to add a vector of resources"
    (is (= [(-> (new-resource)
              (add-links {:self        {:href "/orders/123"}
                          :ea:basket   {:href "/baskets/98712"}
                          :ea:customer {:href "/customers/7809"}})
              (add-properties {:total    30.0
                               :currency "USD"
                               :status   "shipped"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/124"}
                          :ea:basket   {:href "/baskets/97213"}
                          :ea:customer {:href "/customers/12369"}})
              (add-properties {:total    20.0
                               :currency "USD"
                               :status   "processing"}))]
          (-> (new-resource)
            (add-resource
              :ea:order
              [(-> (new-resource)
                 (add-links {:self        {:href "/orders/123"}
                             :ea:basket   {:href "/baskets/98712"}
                             :ea:customer {:href "/customers/7809"}})
                 (add-properties {:total    30.0
                                  :currency "USD"
                                  :status   "shipped"}))
               (-> (new-resource)
                 (add-links {:self        {:href "/orders/124"}
                             :ea:basket   {:href "/baskets/97213"}
                             :ea:customer {:href "/customers/12369"}})
                 (add-properties {:total    20.0
                                  :currency "USD"
                                  :status   "processing"}))])
            (get-resource :ea:order)))))

  (testing "should append resources to existing lists of resources"
    (is (= [(-> (new-resource)
              (add-links {:self        {:href "/orders/123"}
                          :ea:basket   {:href "/baskets/98712"}
                          :ea:customer {:href "/customers/7809"}})
              (add-properties {:total    30.0
                               :currency "USD"
                               :status   "shipped"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/124"}
                          :ea:basket   {:href "/baskets/97213"}
                          :ea:customer {:href "/customers/12369"}})
              (add-properties {:total    20.0
                               :currency "USD"
                               :status   "processing"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/125"}
                          :ea:basket   {:href "/baskets/98716"}
                          :ea:customer {:href "/customers/2416"}})
              (add-properties {:total    18.0
                               :currency "GBP"
                               :status   "shipped"}))]
          (-> (new-resource)
            (add-resource
              :ea:order
              [(-> (new-resource)
                 (add-links {:self        {:href "/orders/123"}
                             :ea:basket   {:href "/baskets/98712"}
                             :ea:customer {:href "/customers/7809"}})
                 (add-properties {:total    30.0
                                  :currency "USD"
                                  :status   "shipped"}))
               (-> (new-resource)
                 (add-links {:self        {:href "/orders/124"}
                             :ea:basket   {:href "/baskets/97213"}
                             :ea:customer {:href "/customers/12369"}})
                 (add-properties {:total    20.0
                                  :currency "USD"
                                  :status   "processing"}))])
            (add-resource
              :ea:order
              (-> (new-resource)
                (add-links {:self        {:href "/orders/125"}
                            :ea:basket   {:href "/baskets/98716"}
                            :ea:customer {:href "/customers/2416"}})
                (add-properties {:total    18.0
                                 :currency "GBP"
                                 :status   "shipped"})))
            (get-resource :ea:order)))))

  (testing "should merge two lists of resources added separately"
    (is (= [(-> (new-resource)
              (add-links {:self        {:href "/orders/123"}
                          :ea:basket   {:href "/baskets/98712"}
                          :ea:customer {:href "/customers/7809"}})
              (add-properties {:total    30.0
                               :currency "USD"
                               :status   "shipped"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/124"}
                          :ea:basket   {:href "/baskets/97213"}
                          :ea:customer {:href "/customers/12369"}})
              (add-properties {:total    20.0
                               :currency "USD"
                               :status   "processing"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/125"}
                          :ea:basket   {:href "/baskets/98716"}
                          :ea:customer {:href "/customers/2416"}})
              (add-properties {:total    18.0
                               :currency "GBP"
                               :status   "shipped"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/127"}
                          :ea:basket   {:href "/baskets/98723"}
                          :ea:customer {:href "/customers/2161"}})
              (add-properties {:total    28.0
                               :currency "USD"
                               :status   "shipped"}))]
          (-> (new-resource)
            (add-resource
              :ea:order
              [(-> (new-resource)
                 (add-links {:self        {:href "/orders/123"}
                             :ea:basket   {:href "/baskets/98712"}
                             :ea:customer {:href "/customers/7809"}})
                 (add-properties {:total    30.0
                                  :currency "USD"
                                  :status   "shipped"}))
               (-> (new-resource)
                 (add-links {:self        {:href "/orders/124"}
                             :ea:basket   {:href "/baskets/97213"}
                             :ea:customer {:href "/customers/12369"}})
                 (add-properties {:total    20.0
                                  :currency "USD"
                                  :status   "processing"}))])
            (add-resource
              :ea:order
              (list (-> (new-resource)
                      (add-links {:self        {:href "/orders/125"}
                                  :ea:basket   {:href "/baskets/98716"}
                                  :ea:customer {:href "/customers/2416"}})
                      (add-properties {:total    18.0
                                       :currency "GBP"
                                       :status   "shipped"}))
                (-> (new-resource)
                  (add-links {:self        {:href "/orders/127"}
                              :ea:basket   {:href "/baskets/98723"}
                              :ea:customer {:href "/customers/2161"}})
                  (add-properties {:total    28.0
                                   :currency "USD"
                                   :status   "shipped"}))))
            (get-resource :ea:order)))))

  (testing "should be able to add multiple resources and they should stack"
    (is (= [(-> (new-resource)
              (add-links {:self        {:href "/orders/123"}
                          :ea:basket   {:href "/baskets/98712"}
                          :ea:customer {:href "/customers/7809"}})
              (add-properties {:total    30.0
                               :currency "USD"
                               :status   "shipped"}))
            (-> (new-resource)
              (add-links {:self        {:href "/orders/124"}
                          :ea:basket   {:href "/baskets/97213"}
                          :ea:customer {:href "/customers/12369"}})
              (add-properties {:total    20.0
                               :currency "USD"
                               :status   "processing"}))]
          (-> (new-resource)
            (add-resources
              :ea:order
              (-> (new-resource)
                (add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
              :ea:order
              (-> (new-resource)
                (add-links {:self        {:href "/orders/124"}
                            :ea:basket   {:href "/baskets/97213"}
                            :ea:customer {:href "/customers/12369"}})
                (add-properties {:total    20.0
                                 :currency "USD"
                                 :status   "processing"})))
            (get-resource :ea:order)))))

  (testing "should be able to get all the embedded resources"
    (is (= {:ea:order (-> (new-resource)
                        (add-links {:self        {:href "/orders/123"}
                                    :ea:basket   {:href "/baskets/98712"}
                                    :ea:customer {:href "/customers/7809"}})
                        (add-properties {:total    30.0
                                         :currency "USD"
                                         :status   "shipped"}))}
          (-> (new-resource)
            (add-resources
              {:ea:order (-> (new-resource)
                           (add-links {:self        {:href "/orders/123"}
                                       :ea:basket   {:href "/baskets/98712"}
                                       :ea:customer {:href "/customers/7809"}})
                           (add-properties {:total    30.0
                                            :currency "USD"
                                            :status   "shipped"}))})
            (resources)))))

  (testing "should be able to add and retrieve properties from the resource"
    (is (= 14
          (-> (new-resource)
            (add-property :currently-processing 14)
            (get-property :currently-processing))))

    (is (false?
          (-> (new-resource)
            (add-property :active false)
            (get-property :active)))))

  (testing "should not add nil properties to the resource"
    (is (false?
          (contains?
            (-> (new-resource)
              (add-property :active nil)
              (properties))
            :active))))

  (testing "should be able to add multiple properties"
    (let [resource (-> (new-resource)
                     (add-properties
                       :currently-processing 14
                       :shipped-today 20))]
      (is (= 14 (get-property resource :currently-processing)))
      (is (= 20 (get-property resource :shipped-today)))))

  (testing "should be able to add a map of properties"
    (let [resource (-> (new-resource)
                     (add-properties
                       {:currently-processing 14
                        :shipped-today        20}))]
      (is (= 14 (get-property resource :currently-processing)))
      (is (= 20 (get-property resource :shipped-today)))))

  (testing "should be able to retrieve all the properties"
    (let [resource (-> (new-resource)
                     (add-properties
                       {:currently-processing 14
                        :shipped-today        20}))]
      (is (= {:currently-processing 14
              :shipped-today        20}
            (properties resource)))))

  (testing "should be able to navigate deep within properties"
    (is (= 20
          (-> (new-resource)
            (add-property :currently-processing {:uk 20 :de 12})
            (get-in-properties [:currently-processing :uk]))))))
