# takeflight

[![Build Status](https://secure.travis-ci.org/bjeanes/takeflight.png)](http://travis-ci.org/bjeanes/takeflight)

A little web app to show a [flight status board](http://culturedcode.com/status/) for Pivotal Tracker projects (and, eventually, maybe others).

![Screenshot](https://www.evernote.com/shard/s2/sh/042f5248-8c73-4c3a-9615-1f9e58d3b286/201131a856332b6ff4c95b60e821f6df/res/71f2d2bd-94c9-4336-a6d6-53411c31e6ac/Take_Flight%21-20120920-192223.jpg.jpg)

## Developing

You need to have Leiningen 2 for this project

1. Install the dependencies: `lein deps`
1. Run the ring server: `TOKEN="your-pivotal-tracker-api-token-here" lein ring server-headless`
1. To work on the UI, go to http://localhost:3000/layout.html and edit `resources/views/layout.html`

## TODO

1. Account system so different users can have different project lists
  * Each account provides their API token
  * A project list that the API token can see is listed with checkboxes
  * User opts into projects whose releases should be shown
1. Now that I'm beyond just flirting with Compojure and Enlive and this is a real project, let's add some tests and clean up the code...
1. Extract out the pivotal stuff into it's own library

## License

Copyright © 2012 Bo Jeanes

Distributed under the Eclipse Public License, the same as Clojure.
