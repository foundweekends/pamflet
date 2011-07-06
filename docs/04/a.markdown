Source Code Highlighting
------------------------

Pamflet hooks into the very slick prettify.js source code
highlighter. To activate it, use github's fenced code block syntax to
enclose your source and specify a language name known to prettify.

    ```scala
    (1 to 10) /: { _ + _ }
    ```

appears as

```scala
(1 to 10) /: { _ + _ }
```

Really clears things up, right?
