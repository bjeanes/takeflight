# takeflight

A little web app to show a [flight status board](http://culturedcode.com/status/) for Pivotal Tracker projects (and, eventually, maybe others).

## Developing

You need to have Leiningen 2 for this project

1. Install the dependencies: `lein deps`
1. Run the ring server: `lein ring server-headless`
1. To work on the UI, go to http://localhost:3000/layout.html and edit `resources/views/layout.html`

## License

Copyright © 2012 Bo Jeanes

Distributed under the Eclipse Public License, the same as Clojure.
