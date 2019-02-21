# halboy

[![Current Version](https://clojars.org/halboy/latest-version.svg)](https://clojars.org/halboy)

A Clojure library for all things hypermedia.

* Create hypermedia resources
* Marshal to and from JSON, or a map
* Navigate JSON+HAL APIs

## New in version 5

Halboy now filters out keys with nil values:

```clojure
(require '[halboy.resource :as hal])

(-> (hal/new-resource)
        (hal/add-links
            :self "/orders/123" 
            :creator nil)
        (links))
; { :self "/orders/123" }
```

## API

### Resources

With Halboy you can create resources, and pull information from them.

```clojure
(require '[halboy.resource :as hal])

(def my-resource
    (-> (hal/new-resource "/orders/123")
        (hal/add-link :creator "/users/rob")
        (hal/add-resource :discount (-> (hal/new-resource "/discounts/1256")
                                         (hal/add-property :discount-percentage 10)))
        (hal/add-resource :items [(-> (hal/new-resource "/items/534")
                                     (hal/add-property :price 25.48))])
        (hal/add-property :state :dispatching)))

(hal/get-link my-resource :self)
; { :href "/orders/123" }

(hal/get-href my-resource :creator)
; "/users/rob"

(hal/get-property my-resource :state)
; :dispatching

(-> (hal/get-resource my-resource :discount)
    (hal/get-property :discount-percentage))
; 10

(-> (hal/get-resource my-resource :items)
    (first)
    (hal/get-property :price))
; 25.48
```

### Marshalling

You can also marshal your hal resources to and from maps, or JSON.

```clojure
(require '[halboy.resource :as hal])
(require '[halboy.json :as haljson])

(def my-resource
    (-> (hal/new-resource "/orders/123")
        (hal/add-link :creator "/users/rob")
        (hal/add-resource :items (-> (hal/new-resource "/items/534")
                                     (hal/add-property :price 25.48)))
        (hal/add-property :state :dispatching)))

(haljson/resource->map my-resource)
; {:_links    {:self {:href "/orders/123"},
;              :creator {:href "/users/rob"}},
;  :_embedded {:items {:_links {:self {:href "/items/534"}},
;                      :price  25.48}},
;   :state    :dispatching}

(haljson/resource->json my-resource)
; Formatted in these docs only.
;
; {
;   \"_links\": {
;     \"self\": {
;       \"href\": \"/orders/123\"
;     },
;     \"creator\": {
;       \"href\": \"/users/rob\"
;     }
;   },
;   \"_embedded\": {
;     \"items\": {
;       \"_links\": {
;         \"self\": {
;           \"href\": \"/items/534\"
;         }
;       },
;       \"price\": 25.48
;     }
;   },
;   \"state\": \"dispatching\"
; }

(-> (haljson/resource->json my-resource)
    (haljson/json->resource)
    (hal/get-href :self))
; "/orders/123"
```

### Navigation

Provided you're calling a HAL+JSON API, you can discover the API and navigate
through its links. When you've found what you want, you call
`navigator/resource` and you get a plain old HAL resource, which you can inspect
using any of the methods above.

```clojure
(require '[halboy.resource :as hal])
(require '[halboy.navigator :as navigator])

; GET / - 200 OK
; {
;  "_links": {
;    "self": {
;      "href": "/"
;    },
;    "users": {
;      "href": "/users"
;    },
;    "user": {
;      "href": "/users/{id}",
;      "templated": true
;    }
;  }
;}

(def users-result
     (-> (navigator/discover "https://api.example.com/")
         (navigator/get :users))

(navigator/status users-result)
; 200

(navigator/location users-result)
; "https://api.example.com/users"

(-> (navigator/discover "https://api.example.com/")
    (navigator/get :user {:id "rob"})
    (navigator/location))
; "https://api.example.com/users/rob"

(def sue-result
     (-> (navigator/discover "https://api.example.com/")
         (navigator/post :users {:id "sue" :name "Sue" :title "Dev"}))

(navigator/location sue-result)
; "https://api.example.com/users/sue"

(-> (navigator/resource sue-result)
    (hal/get-property :title))
; "Dev"
```

### Customisation

### Custom HTTP clients

Halboy offers an out-of-the-box HTTP client which uses HTTPKit. You can pass
a HTTP client into Halboy using the `:client` key of the settings. It must
adhere to the `halboy.http.protocol.HttpClient` protocol.

#### CachableHttpClient
Halboy also offers an HTTP client with caching support which is an in-memory 
cache. It uses `clojure.core.cache TTLCache` for caching with the default TTL of 
2000 miliseconds which can be overridden to a different type of cache of choice
or a different TTL. 
```clojure
; default cachable client
(navigator/discover "https://api.example.com"
 {:client           (halboy.http.cachable/new-http-client)
  :follow-redirects true
  :http             {:headers {}}})
  
; cachable client with an hour TTL
(require '[clojure.core.cache :as cache])

(def in-memory-cache (atom (cache/ttl-cache-factory {} :ttl 3600000)))
  (navigator/discover "https://api.example.com"
     {:client           (halboy.http.cachable/new-http-client in-memory-cache)
      :follow-redirects true
      :http             {:headers {}}})
```
#### HTTP settings

All settings under the `:http` key are passed into the HTTP client. These are
_deep_ merged into each request, with keys on the request taking priority.

The request will always fill in the keys `:method`, `:url`, `:body`, and
`:query-params`.

Headers specified in HTTP settings will be merged with headers defined by
`set-header`. If they share the same key, the `set-header` call wins.

## Contributing

I'm happy to receive and go through feedback, bug reports, and pull requests.

If you need to contact me, my email is jimmy[at]jimmythompson.co.uk.

### Development 
To run the tests:

```sh
$ lein eftest
```
