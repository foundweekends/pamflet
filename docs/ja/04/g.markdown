---
out: Color-Schemes.html
---

配色スキーム
----------

暗めの配色で pamflet を読みたいと思うかもしれない。
そのため Pamflet には初めから <a href="?color_scheme=github">github</a>、
<a href="?color_scheme=monokai">monokai</a>、
<a href="?color_scheme=redmond">redmond</a>
と 3つの配色スキームがついてくる。

### 配色スキームの適用

暗めの配色スキームを適用するには pamflet を
<nobr><code>?color_scheme=monokai</code></nobr> というクエリ文字と共に開く。
これは HTML5 のローカルストレージに `"monokai"` を格納して、pamflet にそのスキームを適用する。

### デフォルトの配色スキーム

デフォルトの配色スキームは redmond だ。これを変更するには `color_scheme` プロパティを定義する:

    color_scheme=github

### カスタム配色スキーム

独自の配色スキームを定義して、例えば zen という名前をつける場合、`color_scheme-zen.css`
というファイルを作って、`body.color_scheme-zen` 以下に css プロパティを定義する:

```css
body.color_scheme-zen {
    color: black;
    background: white;
}
body.color_scheme-zen code.prettyprint span.str {
  color: #dd1144
}
```
