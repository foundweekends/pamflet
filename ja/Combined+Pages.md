
Pamflet
=======

*Pamflet* は短い文書、特にオープンソース・ソフトウェアの
ユーザ・ドキュメントを公開するためのアプリだ。
どのようなプラットフォームからでも簡単に読み書きできる設計になっている。

> [Version 0.6.0 Release Notes](http://notes.implicit.ly/post/87661053009/pamflet-0-6-0)


本のように
---------

Pamflet は「pamflet」と呼ばれるリンクされたページを生成する。
階層化されたページもサポートするけど、主なナビケージョン方法は線形なので、
pamflet をこれから書く人は初めから終わりまで順番に進んでいくものを書くことをお勧めする。

全ページの下に目次が表示される。
生成される目次がスクロールしなくても表示されるように pamflet の最初のページは短くするべきだ。
何行かだけ説明を加えたタイトルページだと考えよう。

### 大きな画面上

大きな画面を持つデバイス (ラップトップやデスクトップなどのコンピュータ)
上では pamflet の文書は固定幅のセンターカラムに表示される。
左右の巨大なマージンはページをめくるための**クリック可能な領域**だ。
左右の**キーボードの矢印キー**も同様に使うことができる。

### モバイル

pamflet は持ち運ぶためにあるので、モバイル上で良い見た目と使い勝手があることは不可欠だ。
ケータイ、タブレット、もしくは小さい画面を持つデバイスで表示すると、pamflet は全画面表示となる。

#### オフライン

モバイル機器はいつもネットにつながっているとは限らないので、全ての pamflet
は [HTML5 cache manifest][manifest] を生成する。
pamflet をホーム画面にブックマークしておけばどこでも読むことができる。

[manifest]: http://diveintohtml5.org/offline.html


文書の整理と書き方
----------------

Pamflet の目的はちゃんとしたプロジェクトドキュメントがあまりにも簡単に書けてしまうため、
皆が書くということだ。このセクションでは pamflet の書き方を説明しよう。


ファイル名とページ名
------------------

Pamflet はソースファイルをソースディレクトリから読みこんで、
指定されたディレクトリに仕上がった web ファイルを生成する。
`markdown` もしくは `md` 拡張子を持つソースは 1つの `html` 出力ファイルに対応する。

ファイル名はページとセクションの順序を決めるためだけに用いられ、
表示されるタイトルとページの URL は、ページのソースの最初の見出し要素によって決まる。

### 命名規約

順序を制御するために、簡単な数字やアルファベットを使ってソースの最初の数文字を命名することを推奨する。
そのあとで必要ならばタイトルを含め、markdown 拡張子を付ける。

例えば、pamflet のソースをソートしたときにタイトルページが最初にくるように `00.markdown`
という名前にすることができる。コンテンツはこんな感じになる:

    Pamflet
    =======

    *Pamflet* は短い文書、特にオープンソース...

プロセッサはこれでページ名が「Pamflet」であると判断する。
このソースが実際にソートされたリストの最初のものならば、この名前は pamflet 全体の名前だと解釈される。

#### アウトプット名

アウトプットされる `html` のファイル名は URL エンコードされたページ名であり、
インプットファイル名と一切関係無い。そのため、ページ名は pamflet 全体で一意のものでなくてはいけない。


目次の形
-------

前のページでみたとおり、ページはソースのファイル名によってアルファベット順にソートされる。
この順序は全てのページの下に表示される**目次**にも影響する。

Pamflet は、目次に HTML の順序付けされたリストを使う。
デフォルトのスタイルシートでは、トップレベルは単純な数字で表示される。
ただし、少しビックリするかもしれない癖が一つあって、それはディレクトリ内の最初の
markdown のソースは他のソースと同列に扱われないということだ。
前の例で見たタイトルページはどの番号も与えられない。以下のディレクトリがあるとする:

    docs/
      00.markdown
      01.markdown
      02.markdown

目次は以下の形で表示さる。ただし、実際の名前とリンクはそれぞれのページの最初見出しから生成される。

    <00.markdown>
    1. <01.markdown>
    2. <02.markdown>

そのため、タイトルページは 0番目のページだと考えられ、典型的にそれを反映した名前がつけられる。


さらに深く
--------

どんなに単純な pamflet でも関連したページをいくつかのセクションに分けると便利だ。
今読んでるこれもセクションの一部だ。

Pamflet は任意の数のセクションをサポートして、一次ソースディレクトリ内にディレクトリを置くことで定義される。これらの子ディレクトリも markdown ソース同様に名前によって順序付けられる。

    docs/
      00.markdown
      01/
        00.markdown
        a.markdown
        b.markdown
      02.markdown

ページの見出しを含む適切なソースの場合、上記の構造は以下の目次を生成する:

    <00.markdown>
    1. <01/00.markdown>
      a. <01/a.markdown>
      b. <01/b.markdown>
    2. <02.markdown>

ディレクトリ内で最初に順序付けされる `00.markdown` が pamflet
全体か一つのセクションかに関わらず常にタイトルページ扱いされるのが分かると思う。
ファイル名で付けられたのと同じ番号で順序付けられ目次も表示されている。
最上位レベルのタイトルは無番号となる。

この例では第2のレベルが小文字のアルファベットを使って番号付けされていることが分かる。
第3のレベルは小文字のローマ数字で、その後はブラウザのデフォルトに戻る。
カスタムのスタイルシートを使うことで任意のレベルの表示を変更することができる。

###### 注意

入れ子の構造は目次の表示にのみ反映される。
pamflet のページ名前空間は全てのアウトプット `html` が単一のパス直下に生成されるフラットなものだ。



プロジェクトの中での Pamflet
-------------------------

どのような用途に使うこともできるけど、Pamflet はオープンソースプロジェクトの
ドキュメンテーションを書くことに特化している。
このセクションでは下書きから公開までの過程を見ていく。



コマンドライン
------------

Pamflet は Scala アプリケーション用の汎用インストーラ兼アップデータである [conscript][conscript] によってインストールされる。
conscript のセットアップは簡単なので、まだセットアップしていなければ[今すぐやってきて][conscript]ほしい。

[conscript]: https://github.com/foundweekends/conscript#readme

conscript がセットアップされて、`~/bin` にパスが通っているとすると、Pamflet は以下のようにインストールできる:

    cs foundweekends/pamflet

これで Pamflet の `pf` コマンドがインストールされた。このコマンドを使って pamflet のプレビューと公開を行う。

    使用例: pf [SRC] [DEST]

### プレビュー

Pamflet のプレビューモードを使ってプロジェクトのドキュメントを書き直しながら
レイアウトやテキストのレンダリングを何回でも確認できる。
`pf` を引数無しで呼び出すと、`SRC` はワーキングディレクトリ内の docs
ディレクトリに指定される。
`pf` を1つの引数だけを渡して呼び出すことで `SRC` ディレクトリを指定できる。
どちらの場合も `DEST` が指定されなければ Pamflet はプレビューモードになる。

    2011-07-15 09:29:38.033:INFO::jetty-7.2.2.v20101205
    2011-07-15 09:29:38.066:INFO::Started SocketConnector@127.0.0.1:44449

    Previewing `docs`. Press CTRL+C to stop.

プレビューモードはループバックインターフェイスの空いているポートを使って
[Unfiltered][uf] web サーバを開始する。
さらにローカルの web ブラウザで使われてる URL を開く。
プラットフォームやブラウザによってウィンドウはバックグラウンドまたは前面で開く。

[uf]: http://unfiltered.databinder.net/

`docs` もしくは指定されたディレクトリに pamflet
が見つかるとブラウザにタイトルページが表示されていることが確認できるはずだ。
線形ナビゲーションや目次も公開される pamflet 同様に動作するはずだ。
プレビューの一番良い所は pamflet のソースへの変更が即座に反映されることだ。
ブラウザを再読み込みするだけでいい。

### 公開

pamflet のソースを web ソースとして公開するには `pf`
コマンドを `SRC` と `DEST` の両方のパラメータを指定して呼び出す。
`SRC` に pamflet のソースを指定して、`DEST` にユーザが書き込み可能な
ディレクトリを指定すれば、出力ファイルは全てそこに書き込まれる。


git フック
---------

git フックを使うことで Pamflet は可愛いオモチャからデジタル印刷機へと変身する。
コントロール権を持っているサーバ (持ってるよね?) にセットアップすることで
push するたびに自動的に公開されるドキュメントが更新されるようになる。

サーバに `--bare` git リポジトリを持っているなら、`hooks/`
サブディレクトリは既にあるはずだ。`hooks/` 内に `post-update`
というファイルを作成するか変更するかして以下のように書く:

```sh
#!/bin/sh
HOME=/home/me
DOCB=$HOME/app/my_doc_build
PUB=$HOME/app/my_pub
cd $DOCB
env -i git pull
cd -
$HOME/bin/pf $DOCB/docs $PUB
```

`DOCB` は同じレポジトリのクローンで、これはワーキングツリーを持つ。
`PUB` は最終的な `html` とその他のファイルが出力される。
git のフックは奇妙な環境で実行されるので、明示的に `HOME` で home ディレクトリも指定する。

全てをセットアップして、さらにこのフックスクリプトを実行可能にする。
**さもなくば何故スクリプトが実行されないのか分からず、混乱状態になる**。
`~/bin/pf` が実行可能かも確認しておこう。

bare リポジトリにどこからか push すると、通常の git の出力と一緒に
Pamflet が成功したかエラーになったかのメッセージも表示されるはずだ。
(このスクリプトは確かにヘナチョコなものだけど、代案があればこの
pamflet を fork してほしい。)


Apache の設定
------------

もう知っているかもしれないが、僕たちみたいな知らない人が時間を無駄にしないために
Apache を使って pamflet ディレクトリを公開する方法を書いておく:

```xml
<VirtualHost *:80>
        ServerName pamflet.databinder.net
        DocumentRoot /home/me/app/pamflet
        RewriteEngine On
        RewriteRule ^/$ /Pamflet.html [L,R=permanent]
</VirtualHost>
```

###### 注意

ルートパスを解決させるためには、ルートをリダイレクトする必要がある。
タイトルページも他のページ同様にアウトプット名が指定されるからだ。


高度なテクニック
--------------

pamflet を書いて公開するまでをみてきた。
さらに凝った pamflet を書いて公開する方法を紹介しよう。


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


カスタムスタイルシート
------------------

他のプロジェクトと差をつけたければ全ての見出しを
Copperplate MT か Bank Gothic に指定したいと思うだろう。

心配しなくても、Pamflet はこれをサポートする。
ソースディレクトリ内の `css` 拡張子を持ったファイルは pamflet に含まれる。
これらは他のスタイルシートの後に読み込まれるため、思う存分改良してほしい。


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
例えば、Markdown ソースに書かれた全ての `$version$` は HTML 出力では
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
それは単独の `$` によって混乱状態になる。
そのため、プロパティではないドル記号はバックスラッシュを使ってエスケープする必要がある: `\$`


特殊プロパティ
------------

一般的に[テンプレート・プロパティ](Template-Properties.html)は独自のプロパティを定義して
markdown ソースで使うためにあるけども、pamflet はいくつかの省略可能な特殊プロパティをチェックして例えばプロジェクトの
GitHub リポジトリへリンクを張ったりする。

### Fork Me on GitHub

`github` プロパティを定義してレポジトリのオーナーと名前を書くことで
Pamflet はレポジトリへのリンクを張る。例えば:　

    github=foundweekends/pamflet

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


グローバリゼーション
-----------------

pamflet を複数の言語に対応させたければ、グローバリゼーション機能を有効にする特殊な[テンプレート・プロパティ](Template-Properties.html)がある。

### デフォルト言語

`language` プロパティはルートレベルでどの言語を使っているかを指定する。
デフォルトでは、英語の ISO 639-1 コードである `en` に指定される。

    language=en

### 言語リスト

`languages` プロパティは pamflet で使われる言語のリストを指定する。
ISO 639-1 の 2文字コードまたは IETF 言語コードをコンマで区切って指定する。

    languages=en,ja

そして、デフォルト言語以外のコンテンツは言語コードと同じ名前のフォルダに入れる。

    docs/
      00.markdown
      01/
        00.markdown
        a.markdown
      ja/
        00.markdown
        01/
          00.markdown
          a.markdown

これで `ja/` フォルダ以下に日本語のコンテンツが生成される。

### 言語ラベル

相対パスが同じコンテンツは翻訳だとみなされ、翻訳が検出されると、
ページの下に言語リンクが表示され他の翻訳へとリンクされる。

Pamflet はいくつかの言語コードを解決できるが、もし検出しなければ `lang-` 
プロパテイを使ってカスタムの言語ラベルを提供することができる:

    lang-pt-BR=Português (Brasil)


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
            <span>$contents.title$</span>$if(page.title)$ —
             $page.title$ $endif$ (draft)
          </div>
        </div>
      </div>
    </div>

これでデフォルトのヘッダに「(draft)」と追加したヘッダができた。フッタも同じように変更できる。`layout.footer` と `layout.footer.height` を指定すればいい。


この pamflet のソース
-------------------

この pamflet で説明されているテクニックを使ってこの pamflet は書かれている。
[GitHub][gh] で Pamflet の `docs/` ディレクトリを開いて眺めてみてほしい。

[gh]: https://github.com/foundweekends/pamflet/tree/master/docs


誰が Pamflet を使っているか?
--------------------------

Pamflet を使っているプロジェクトのリストは[英語版](../Who%E2%80%99s+Using+Pamflet%3F.html)にある。

あなたが Pamflet を使っているなら是非プロジェクトを追加して pull request を送ってほしい。
