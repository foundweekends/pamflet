---
out: Special-Properties.html
---

特殊プロパティ
------------

一般的に[テンプレート・プロパティ](Template-Properties.html)は独自のプロパティを定義して
markdown ソースで使うためにあるけども、pamflet はいくつかの省略可能な特殊プロパティをチェックして例えばプロジェクトの
GitHub リポジトリへリンクを張ったりする。

### Fork Me on GitHub

`github` プロパティを定義してレポジトリのオーナーと名前を書くことで
Pamflet はレポジトリへのリンクを張る。例えば:　

    github=n8han/pamflet

これは Pamflet そのもののドキュメントから GitHub へのリンクを生成しているプロパティだ。
スラッシュは 1つであることに注意。

### アウトプットの指定

`out` プロパティを定義することで Pamflet が使う生成するのに使うファイル名を指定できる。
これは properties font matter で使うことを意図している:

    ---
    out: index.html
    ---

### 折りたたみ可能な目次

`toc` プロパティを使って各ページに表示される目次を制御できる。
可能な値は `show` (表示)、`hide` (非常時)、そして `collapse` (折りたたみ) だ。

### Disqus

`disqus` プロパティを定義して disqus short name を指定することで全てのページの下にコメント欄を加えることができる。

    disqus=namehere

### Twitter

`twitter` プロパティを定義して任意の値を指定すると、ページ内でテキストが選択されたときにツイッターボタンが表示されるようになる。
ボタンを押すと、選択されたテキスト、プロパティの値、および URL を含んだツイートが作成される。
`show` (表示) という値を指定することで、ボタンは表示されるが、ツイートにはその値を含めないことができる。

    twitter=#pamflet

### Google Analytics

`google-analytics` プロパティを定義して Google Analytics web property ID
を指定することで全ページの head に[追跡 Javascript][ga] を追加する。例えば:


    google-analytics=UA-12345-6

[ga]: http://code.google.com/apis/analytics/docs/tracking/asyncTracking.html
