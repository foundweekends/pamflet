On the Command Line
-------------------

Pamflet is installed with [conscript][conscript], a general installer
and updater for Scala applications. Conscript is pretty easy to set
up, so please [do that and come back][conscript] if you haven't yet.

[conscript]: https://github.com/n8han/conscript#readme

Once you have conscript setup, and assuming that `~/bin` is on your
executable search path, you can install Pamflet like so:

    cs n8han/pamflet

That installs Pamflet's `pf` command, which is used to both preview
and publish pamflets. 

    Usage: pf [SRC] [DEST]

### Preview

Pamflet's preview mode allows you to check the layout and text
rendering as much as you like while editing document sources.

When you call `pf` with no arguments, `SRC` is assumed to be "docs"
under the working directory--typically, your project's base
directory. When you call `pf` with one argument, you are specifying
the `SRC` directory. In both these cases where no `DEST` is specified,
Pamflet goes into preview mode.

    2011-07-15 09:29:38.033:INFO::jetty-7.2.2.v20101205
    2011-07-15 09:29:38.066:INFO::Started SocketConnector@127.0.0.1:44449

    Previewing `docs`. Press CTRL+C to stop.

Preview mode starts an [Unfiltered][uf] web server bound to the
loopback interface on some available port. It also requests that a
local web browser open the relevant URL; this occurs in a background
or foreground window depending on your platform and browser.

[uf]: http://unfiltered.databinder.net/

If a usable pamflet is found in the `docs` or other specified
directory, you will see its title page in the browser. Linear
navigation and the *Contents* listing will work exactly as in the
published pamflet. But the best thing about preview is that any
changes to the pamflet source are reflected immediately: just hit
reload.

### Publish

To publish Pamflet sources into web sources, call the `pf` command
with both `SRC` and `DEST` parameters. If `SRC` has Pamflet sources
and `DEST` is some directory that the current user can write to,
you'll find all the output files written to it after the command
completes.
