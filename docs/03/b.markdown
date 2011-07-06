On the Command Line
-------------------

Pamflet is installed with [conscript][conscript], a general installer
and updater for Scala applications. Conscript is pretty easy to set
up, so please [do that and come back][conscript] if you haven't yet.

[conscript]: https://github.com/n8han/conscript#readme

Once you have conscript setup, and assuming that `~/bin` is on your
executable search path, you can install Pamflet like so:

    cs n8han/pamflet

This installs Pamflet's `pf` command, which takes just two parameters:

    Usage: pf SRC DEST

Unlike the sbt plugin the `pf` command does not make any guesses
about where your docs lie. Give it the full path to your project's
`docs/` directory, or elsewhere.
