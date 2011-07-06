Build Plugins and Previews
--------------------------

Projects using Pamflet typically have a `docs/` directory under their
project base for the Pamflet source directory. This allows anyone with
a clone of the project to easily refer to its text-formatted docs
whether or not they have a network connection. Furthermore, any
corrections or contributions to the docs can be made through the
project's normal fork/patch process.

### Build Plugin

Projects built with Scala's [Simple Build Tool][sbt] (sbt) 0.10 may
use the provided Pamflet plugin to streamline their editing
workflow. Those who work on multiple projects will find it easiest to
[configure the plugin globally][plugins], in `~/.sbt/plugins/build.sbt`

[sbt]: https://github.com/harrah/xsbt/#readme
[plugins]: https://github.com/harrah/xsbt/wiki/Plugins

```scala
libraryDependencies ++= Seq(
  "net.databinder" %% "pamflet-plugin" % "$version$"
)
```

#### Preview

With the plugin installed, several tasks specific to Pamflet become
available in the sbt console. The most useful is **`start-pamflet`**,
which spawns a preview server for the `docs/` directory under the
current project. The preview server binds on the loopback interface to
some available port, and requests that a local web browser open the
corresponding URL.

If everything is in place, you will see your pamflet's title
page. Linear navigation and the *Contents* listing will work exactly
as in the published pamflet. But the best thing about preview is that
any changes to the pamflet source are reflected immediately: just hit
reload.

You can stop the preview server with the `pamflet-stop` task, or by
exiting the sbt console.

#### Other Options

If your pamflet source is not under `docs/`, you map override the
`pamfletDocs` setting to point elsewhere.

In addition to `star-pamflet` there is a `write-pamflet` task writes
the finished html and other files needed for statically serving the
site; these are placed according to the `pamfletOutput` setting, which
resolves to `target/docs` by default.
