# Tenzing

### Tenzing is still work in progress and might not work at all. Watch the project to be notified when it's ready.

Tenzing is a template for Clojurescript applications inspired by
[Chestnut][chestnut]. Just like Chestnut it offers the following
features:

1. Easy Clojurescript compilation
1. Browser-REPL
1. Live reloading of your Javascript, CSS, etc.

**There also are some differences:**

1. Tenzing uses [Boot][boot] instead of Leiningen (see below)
1. Tenzing does not provide a backend layer (see below)
1. Tenzing allows you to choose between Om, Reagent and others

### Why Boot?

In contrast to Leiningen Boot offers a clear strategy when it comes to
composing multi-step build processes such as compiling stylesheets and
javascript whenever a relevant file changes.

Many Leinigen plugins come with an `auto` task that allows similar
behavior. If you want to run multiple of those tasks it's usually done
by starting multiple JVM instances which can lead to
[high memory usage](chestnut-mem). Boot allows this sort of behaviour
to reside in one JVM process while making sure that build steps don't
interfere with each other.

You can learn more about Boot in
[a blog post by one of the authors][clojurescript-rebooted], it's
[github project][boot] or [a post I wrote about it][boot-relevant].

### Why #noBackend?

Tenzing is designed with prototyping in mind. Instead of writing your
own backend you're encouraged to use services like [Parse][parse],
[Firebase][firebase], [Usergrid][usergrid] and others.

If you figure out that you need a Clojure based backend down the road
it's simple to either add it yourself or create it as a standalone
service that's being used by your clients.

Please, also consider [offline first][offline-first] as an approach
for building early iterations of your application.

> If you're wondering how files are served during development:
> there is a boot task `serve` that allows you to serve static files.

## Usage

    $ lein new tenzing your-app
    $ cd your-app
    $ boot development

After a moment of waiting you can head to [localhost:3000](http://localhost:3000) to see a small sample app.

### Options

Currently the following options are supported:

- `+om` provides a basic Om application and adds relevant dependencies ✓
- `+reagent` provides a basic Reagent application and adds relevant dependencies ✓
- `+divshot` adds divshot.json for easy deployment to [Divshot](divshot) ✓
- `+garden` sets up [Garden][garden] and integrates into build process ✓
- `+sass` sets up [Sass][sass] and integrates into build process (requires [libsass][libsass])✓

If you want to add an option, pull-requests are welcome. Please make
sure your option doesn't interfere with existing ones. Also use boot
tasks whenever preprocessing of files or similar things are required.

Some ideas for additional options: `+om-tools`, `+freactive`, `+cljs-test`.

I'm also not against adding options that provide a backend layer, I just
preferred to have it optional.

## Deployment

The easiest way to deploy your app is using [Divshot][divshot].

**How to deploy your Tenzing app to Divshot:**

1. `$ divshot login`
1. add [divshot.json][divshot-json] (Only required if your project hasn't been created with the `+divshot` option.)

        {
          "name": "your-app",
          "root": "target",
          "clean_urls": true,
          "error_page": "error.html"
        }

1. `$ divshot push`

Since Tenzing comes without a backend you can also easily deploy
your app to Amazon S3 or even host it in your Dropbox. To do that
just copy the files in `target/` to your desired location.

> PS. A nice tool to easily deploy to S3 from the command line is [stout](https://github.com/EagerIO/Stout).

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
[libsass]: http://libsass.org
[firebase-hosting]: https://www.firebase.com/docs/hosting/
[divshot]: https://divshot.com
[divshot-json]: https://github.com/martinklepsch/tenzing/blob/master/resources/leiningen/new/tenzing/divshot.json
