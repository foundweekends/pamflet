Source Code Highlighting
------------------------

Pamflet hooks into the very slick prettify.js source code
highlighter. To activate it, use github's fenced code block syntax to
enclose your source and specify a language name known to prettify.

    ```scala
    // Some comment
    case class Page(name: String) {
      def foo: Int = (1 to 10) /: { _ + _ }
      val bar = "wat"
    }
    ```

appears as

```scala
// Some comment
case class Page(name: String) {
  def foo: Int = (1 to 10) /: { _ + _ }
  val bar = "wat"
}
```

Really clears things up, right?
