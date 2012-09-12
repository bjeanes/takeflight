# takeflight

A little web app to show a [flight status board](http://culturedcode.com/status/) for Pivotal Tracker projects (and, eventually, maybe others).

## Developing

You need to have Leiningen 2 for this project

1. Install the dependencies: `lein deps`
1. Run the ring server: `lein ring server-headless`
1. To work on the UI, go to http://localhost:3000/layout.html and edit `resources/views/layout.html`

## TODO

1. Account system so different users can have different project lists
  * Each account provides their API token
  * A project list that the API token can see is listed with checkboxes
  * User opts into projects whose releases should be shown
1. Dashboard should not be per-project.
  * Each login has own dashboard that shows all their projects they asked for
1. Fetch all releases in background on a timer
  * Agents?
  * Views can just pull the latest releases
  * No need for persistence beyond the project IDs and API tokens... I think
1. Now that I'm beyond just flirting with Compojure and Enlive and this is a real project, let's add some tests and clean up the code...

## License

Copyright © 2012 Bo Jeanes

Distributed under the Eclipse Public License, the same as Clojure.
