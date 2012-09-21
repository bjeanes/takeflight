# takeflight

[![Build Status](https://secure.travis-ci.org/bjeanes/takeflight.png)](http://travis-ci.org/bjeanes/takeflight)

A little web app to show a [flight status board](http://culturedcode.com/status/) for Pivotal Tracker projects (and, eventually, maybe others).

![Screenshot](https://img.skitch.com/20120921-becxf8y2btcaakqpg9f3qqpcfs.jpg)

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
