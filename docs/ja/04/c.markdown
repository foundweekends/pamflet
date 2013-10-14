---
out: Template-Properties.html
---

テンプレート・プロパティ
--------------------

現行のバージョン番号などドキュメント中に繰り返されるものがいくつかあると思う。
Pamflet は StringTemplate を使ってテンプレート・プロパティをサポートする。

pamflet ソースディレクトリ内に `template.properties` というファイルを見つけると、
`java.util.Properties` を用いて読み込まれ、全ての Markdown ソースに
StringTemplate が適用される。以下に `template.properties` の具体例をみてみよう:

    version=0.3.4
    vrsn=034
    scala=2.8.1

プロパティ名をドル記号で囲むことでソース内からプロパティを参照する。
例えば、Markdown ソースに書かれた全ての `\$version\$` は HTML 出力では
`0.3.4` へと置換される。

### Properties Front Matter

さらに、各ページはページの先頭に以下のような properties front matter
を書くことで独自のプロパティを追加することができる:

    ---
    version=0.2.5
    dispatch=0.8.5
    ---

front matter に書かれたプロパティは `template.properties` よりも優先される。

###### 注意

.properties ファイルによって StringTemplate が有効になると、
それは単独の `\$` によって混乱状態になる。
そのため、プロパティではないドル記号はバックスラッシュを使ってエスケープする必要がある: `\\\$`
