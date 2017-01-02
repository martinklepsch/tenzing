# Tenzing, the awesome Clojurescript application template

[rationale](#rationale) | [usage](#usage) | [deployment](#deployment) | [getting help](#getting-help)


Tenzing is a Clojurescript template offering the following features:

1.  Incremental Clojurescript compilation
2.  Live reloading of your Javascript, CSS, etc.
3.  Browser-REPL

**In contrast to some of the options out there it is opinionated in the following ways:**

1.  Tenzing uses [Boot](https://github.com/boot-clj/boot) instead of Leiningen (see below)
2.  Tenzing does not provide a backend layer (see below)
3.  Tenzing allows you to choose between Om, Reagent and others

# Rationale

## Why Boot?

In contrast to Leiningen Boot offers a clear strategy when it comes to
composing multi-step build processes such as compiling stylesheets and
Javascript whenever a relevant file changes.

Many Leinigen plugins come with an \`auto\` task that allows similar
behavior. If you want to run multiple of those tasks it's usually done
by starting multiple JVM instances which can lead to
[high memory usage](https://github.com/plexus/chestnut/issues/49). Boot
allows this sort of behaviour to reside in one JVM process while
making sure that build steps don't interfere with each other.

You can learn more about Boot in
[a blog post by one of the authors](http://adzerk.com/blog/2014/11/clojurescript-builds-rebooted/),
its [github project](https://github.com/boot-clj/boot) or
[a blog post I wrote about it](http://www.martinklepsch.org/posts/why-boot-is-relevant-for-the-clojure-ecosystem.html).
[Mimmo Costanza's modern-cljs tutorial](https://github.com/magomimmo/modern-cljs) also uses Boot throughout - 
[Tutorial 2](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-02.md)
walks through the setup of a typical Boot-based development environment.

## Why #noBackend?

Tenzing is designed with prototyping in mind. Instead of writing your
own backend you're encouraged to use services like
[Firebase](https://www.firebase.com),
[Usergrid](http://usergrid.incubator.apache.org) and others.

If you figure out that you need a Clojure based backend down the road
it's simple to either add it yourself or create it as a standalone
service that's being used by your clients.

Please, also consider
[offline first](http://alistapart.com/article/offline-first) as an
approach for building early iterations of your application.

> If you're wondering how files are served during development: there
> is a boot task \`serve\` that allows you to serve static files.

# Usage

## Create a Project

To create a new project, [install boot](https://github.com/boot-clj/boot#install) and run:

```shell
$ boot -d seancorfield/boot-new new -t tenzing -n your-app
```

Template options are specified using the `-a` switch. For example:

```shell
$ boot -d seancorfield/boot-new new -t tenzing -n your-app -a +reagent -a +test
```

Alternatively, if you have leiningen installed, you can run

```shell
$ lein new tenzing your-app
```

or to specify options:

```shell
$ lein new tenzing your-app +reagent +test
```

There are a bunch of options that determine what your newly created
project will contain:

-   `+om` provides a basic [Om](https://github.com/omcljs/om)
    application and adds relevant dependencies
-   `+reagent` provides a basic
    [Reagent](https://github.com/reagent-project/reagent) application
    and adds relevant dependencies
-   `+rum` provides a basic
    [Rum](https://github.com/tonsky/rum) application
    and adds relevant dependencies
-   `+garden` sets up [Garden](https://github.com/noprompt/garden) and
    integrates into the build process
-   `+sass` sets up [Sass](http://sass-lang.com) and integrates into
    the build process (requires [libsass](http://libsass.org))
-   `+less` sets up [Less](http://lesscss.org/) and integrates into
    the build process.
-   `+test` adds a
    [cljs test-runner](https://github.com/crisptrutski/boot-cljs-test)
    and adds a `test` task.
-   `+devtools` adds a
    [cljs-devtools](https://github.com/binaryage/cljs-devtools) through
    [boot-cljs-devtools](https://github.com/boot-clj/boot-cljs-devtools)
-   `+dirac` adds a
    [dirac](https://github.com/binaryage/dirac) through
    [boot-cljs-devtools](https://github.com/boot-clj/boot-cljs-devtools).

If you want to add an option,
[pull-requests](https://github.com/martinklepsch/tenzing) are welcome.

## Running it

After you [installed Boot](https://github.com/boot-clj/boot#install)
you can run your Clojurescript application in "development mode" by
executing the following:

    $ boot dev

After a moment of waiting you can head to
[localhost:3000](http://localhost:3000) to see a small sample app. If
you now go and edit one of the Clojurescript source files or a SASS
file (if you've used the `+sass` option) this change will be picked up
by Boot and the respective source file will get compiled. When a
compiled file changes through that mechanism it will get pushed to the
browser.

If you used the `+test` option, then you'll be able to run unit
tests via `boot test`. Use `boot auto-test` to have tests
automatically rerun on file changes.

### Connecting to the browser REPL

After you started your application with `boot dev` there will be a
line printed like the following:

    nREPL server started on port 63518 on host 0.0.0.0

This means there now is an nREPL server that you can connect to. You
can do this with your favorite editor or just by running `boot repl
--client` in the same directory.

Once you are connected you can get into a Clojurescript REPL by
running `(start-repl)`. At this point I usually reload my browser one
last time to make sure the REPL connection is properly setup.

Now you can run things like `(.log js/console "test")`, which should
print "test" in the console of your browser.

### How it works

If you look at the `build` and `run` tasks in the `build.boot` file of
your newly created project you will see something like the following:

```clojure
(deftask build [] (comp (speak) (cljs) (sass :output-dir "css")))

(deftask run [] (comp (serve) (watch) (cljs-repl) (reload)
    (build)))
```

Basically this composes all kinds of build steps into a unified `run`
task that will start our application. From top to bottom:

The `build` task consists of three other tasks:
-   `speak` gives us audible notifications about our build process
-   `cljs` will compile Clojurescript source files to Javascript
-   `sass` will compile Sass source files to CSS

Now if we just run `boot build` instead of the aforementioned `boot
dev` we will compile our Clojurescript and Sass exactly once and then
the program will terminate.

This is where the `run` task comes in:
-   `serve` starts a webserver that will serve our compiled JS, CSS
    and anything else that is in `resources/`
-   `watch` will watch our filesystem for changes and trigger new
    builds when they occur
-   `cljs-repl` sets up various things so we can connect to our
    application through a browser REPL
-   `reload` will watch the compiled files for changes and push them
    to the browser
-   `build` does the things already described above

**Please note that all tasks, except the one we defined ourselves have
  extensive documentation that you can view by running `boot
  <taskname> -h` (e.g. `boot cljs-repl -h`).**

### Writing build artifacts to disk

By default, none of the tasks in projects generated by tenzing output any files. 

For example, when running the `dev` task, your project's source and resources are 
compiled to a temporary boot fileset and served from there. When boot quits, the 
fileset is no longer available.


This is actually boot's default and, at first, might sound like a strange choice for a build tool!
However, these managed filesets are at the core of boot's philosophy and provide it with 
many advantages over declarative, stateful build tools. See the [boot homepage](http://boot-clj.com) or the 
[`filesets` wiki entry] (https://github.com/boot-clj/boot/wiki/Filesets) for more info on these concepts.

So how do you output your built project to disk so that you can deploy it for example? 
Simple! Boot has a [built-in `target` task]
(https://github.com/boot-clj/boot/blob/master/doc/boot.task.built-in.md#target) that you can can compose with other tasks to 
output their results to a given directory (by convention the directory is named `target`).

To build a tenzing project with the production settings and output the
results to disk you would run the following:

    $ boot production build target

If you look in your project directory now, you will see a `target` directory containing the
output of all of the tasks in this chain. By default, this directory is cleaned every time
you run `boot`, unless you pass the `no-clean` option to the `target` task.

Should you need to filter, copy, move or rename the output files, or change the directory 
structure, the [`sift` task](https://github.com/boot-clj/boot/blob/master/doc/boot.task.built-in.md#sift) will help you do this. If you have complex post-processing needs 
or want to save typing at the command line, it's a simple matter to define your own [tasks](https://github.com/boot-clj/boot/wiki/Tasks).

## Deployment

Since Tenzing comes without a backend you can easily deploy your app
to Amazon S3 or even host it in your Dropbox. To do that just copy the
files in `target/` to your desired location.

<img src="https://cloud.githubusercontent.com/assets/97496/11431670/0ef1bb58-949d-11e5-83f7-d07cf1dd89c7.png" alt="confetti logo" align="left" />

> PS. I'm also working on a tool called
> [Confetti](https://github.com/confetti-clj/confetti) 🎉 that helps
> you setting up static sites on AWS infrastructure. It's a bit more
> aimed at people that deploy and manage many static sites but you
> should probably check it out either way :)

## Getting Help

If you run into any problems feel free to open an issue or ping me (`martinklepsch`) in the [ClojureScript room on Riot.im](https://riot.im/app/#/room/#clojurescript:matrix.org) (no signup required).

## Credits

The initial release of Tenzing was prompted by the urge to have
something like the awesome
[Chestnut template](https://github.com/plexus/chestnut) but built on
top of Boot. Many props to [Arne Brasseur](https://lambdaisland.com/)
for making getting started with ClojureScript a lot easier at the
time!

# License

Copyright © 2014 Martin Klepsch

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
