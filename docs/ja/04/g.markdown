---
out: Changing-the-look.html
---

装丁
----

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

### カスタムヘッダとフッタ

ヘッダを変えるには `layout.header` と `layout.header.height` プロパティを定義する:

    layout.header=header.md
    layout.header.height=2em

次に、`header.md` という名前のファイルを `docs/layout/` 以下に置く:

    <div class="container">
      <div class="span-16 prepend-1 append-1">
        <div class="span-16 top nav">
          <div class="span-16 title">
            <span>\$contents.title\$</span>\$if(page.title)\$ —
             \$page.title\$ \$endif\$ (draft)
          </div>
        </div>
      </div>
    </div>

これでデフォルトのヘッダに「(draft)」と追加したヘッダができた。フッタも同じように変更できる。`layout.footer` と `layout.footer.height` を指定すればいい。
