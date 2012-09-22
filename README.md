# takeflight

[![Build Status](https://secure.travis-ci.org/bjeanes/takeflight.png)](http://travis-ci.org/bjeanes/takeflight)

A little web app to show a [flight status board](http://culturedcode.com/status/) for Pivotal Tracker projects (and, eventually, maybe others).

![Screenshot](https://img.skitch.com/20120921-becxf8y2btcaakqpg9f3qqpcfs.jpg)

Currently, simply providing your Pivotal Tracker API token with the `TOKEN` environment variable will provide
a list of releases across all projects that the token has access to. Projects are fetched every hour in the
background and releases are refreshed every 5 minutes. Only releases with deadlines are shown on the dashboard.

## Running/Developing

NOTE: You need to have [Leiningen 2](https://github.com/technomancy/leiningen/wiki/Upgrading) for this project.

1. Install the dependencies: `lein deps`
1. Run the ring server: `TOKEN="your-pivotal-tracker-api-token-here" lein ring server-headless`
1. To work on the UI, go to http://localhost:3000/layout.html and edit `resources/views/layout.html`

## Deploying

### JAR

Some variant of the following:

1. `lein uberjar`
1. `scp target/*standalone*.jar your-server:takeflight.jar`
1. On server: `TOKEN=... java -jar ~/takeflight.jar`

Building a WAR is also possible if you want to deploy to a container of some kind. Just run `lein ring uberwar`

Non-`uber` versions are available, but requires that you make sure the dependencies are available on the
`CLASSPATH` at runtime.

### Heroku

#### Initial Setup

Do this once...

1. `bundle install`
1. `heroku create --stack cedar`
1. `heroku config:add TOKEN="your-pivotal-tracker-api-token-here"`
1. Until Heroku uses `leiningen` 2.0.0 or greater, we need to use a custom build pack:
  1. `heroku labs:enable user_env_compile`
  1. `heroku config:add BUILDPACK_URL="http://github.com/timewarrior/heroku-buildpack-clojure.git#lein-2" # Use lein-2 branch`

#### Deploying

After the initial setup, do the following for every deploy...

1. `git push heroku master`

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
