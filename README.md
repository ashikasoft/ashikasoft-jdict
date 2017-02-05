# ashikasoft/jdict

Clojure implementation of Ashikasoft Japanese Dictionary

## Usage

Load the data directory and then look up the word (Roman, Kana or Kanji)

  (def dict (load-data-dir "~/japanese_dict/assets"))
  (lookup dict "tsukue")

## License

Copyright © 2017 Kean Santos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
