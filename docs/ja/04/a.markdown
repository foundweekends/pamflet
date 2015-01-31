---
out: Source-Code-Hilighting.html
---

ソースコードのハイライト
---------------------

Pamflet はとても器用な prettify.js ソースコードハイライトを利用する。
これを使うには github の fenced code block 構文を使ってソースを囲んで
prettify が使用可能な言語を指定する。

    ```scala
    // Some comment
    case class Page(name: String) {
      def foo: Int = (1 to 10) /: { _ + _ }
      val bar = "wat"
    }
    ```

これは

```scala
// Some comment
case class Page(name: String) {
  def foo: Int = (1 to 10) /: { _ + _ }
  val bar = "wat"
}
```

と表示される。読みやすくなったよね?
