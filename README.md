# tenzing

Tenzing is a template for Clojurescript applications inspired by
[Chestnut][chestnut].  Just like Chestnut it offers the following
features:

1. Easy Clojurescript compilation
1. Browser-REPL
1. Live reloading of your Javascript, CSS, etc.

**There also are some differences:**

1. Tenzing uses [Boot][boot] instead of Leiningen (see below)
1. Tenzing does not provide a backend layer (see below)
1. Tenzing allows you to choose between Om, Reagent and others

### Why Boot

In contrast to Leiningen Boot offers a clear strategy when it comes to
composing multi-step build processes such as compiling stylesheets and
javascript whenever a relevant file changes.

Many Leinigen plugins come with an `auto` task that allows similar
behaviour but ultimately starts multiple JVM processes which consume
[significant memory](chestnut-mem).

Boot allows this sort of behaviour to reside in one JVM process while
making sure that build steps don't interfere with each other.

You can learn more about Boot in
[a blog post by one of the authors][clojurescript-rebooted], it's
[github project][boot] or [a post I wrote about it][boot-relevant].

### Why #noBackend?

Tenzing is designed with prototyping in mind. Instead of writing your
own backend you're encouraged to use services like [Parse][parse],
[Firebase][firebase], [Usergrid][usergrid] and others.

If you figure out that you need a Clojure based server down the road
it's simple either add it yourself or create it as a standalone
service that's being used by your clients.

Please, also consider [offline first][offline-first] as an approach
for building early iterations of your application.

> If you're wondering how files are served during development:
> there is a boot task `serve` that allows you to serve static files.

## Usage

```
lein new tenzing your-app
```

### Options — WIP: none implemented yet

Currently the following options are supported:

- `+om`: provides a basic Om application and adds relevant dependencies.
- `+reagent`: provides a basic Reagent application and adds relevant dependencies.
- `+garden`: setup [Garden][garden] and integrate into build process
- `+sass`: setup [Sass][sass] and integrate into build process

If you want to add an option, pull-requests are welcome. Please make sure
that all combinations still work with your new option. Also use boot tasks
whenever preprocessing of files or similar things are required.

## Deployment

Since Tenzing comes without a backend deploying is as easy as putting the files
in Boot's `:target-dir` into an S3 bucket, your Dropbox or any other location
that's accessible through your web browser.

For more sophisticated solutions checkout [Divshot][divshot]
or things like [Firebase Hosting][firebase-hosting].

**How to deploy your Tenzing app to Divshot:**

## License

Copyright © 2014 Martin Klepsch

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


[chestnut]: https://github.com/plexus/chestnut
[boot]: https://github.com/boot-clj/boot
[chestnut-mem]: https://github.com/plexus/chestnut/issues/49
[clojurescript-rebooted]: http://adzerk.com/blog/2014/11/clojurescript-builds-rebooted/
[boot-relevant]: http://www.martinklepsch.org/posts/why-boot-is-relevant-for-the-clojure-ecosystem.html
[offline-first]: http://alistapart.com/article/offline-first
[parse]: https://parse.com
[firebase]: https://www.firebase.com
[usergrid]: http://usergrid.incubator.apache.org
[garden]: https://github.com/noprompt/garden
[sass]: http://sass-lang.com
[firebase-hosting]: https://www.firebase.com/docs/hosting/
[divshot]: https://divshot.com
