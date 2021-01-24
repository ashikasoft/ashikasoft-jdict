# ashikasoft/jdict

[![Clojars Project](https://img.shields.io/clojars/v/ashikasoft/jdict.svg)](https://clojars.org/ashikasoft/jdict)

Clojure implementation of Ashikasoft Japanese Dictionary, running http://www.ashikasoft.com
Note: this program was ported from an old version of java and may contain non-idiomatic code. Do not use this as a coding style guide.

## Usage

Load the data directory and then look up the word (Roman, Kana or Kanji)

    (def dict (load-data-dir "~/japanese_dict/assets"))
    (lookup dict "tsukue")

## License

Copyright © 2017 Kean Santos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
