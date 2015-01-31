---
out: On-the-Command-Line.html
---

コマンドライン
------------

Pamflet は Scala アプリケーション用の汎用インストーラ兼アップデータである [conscript][conscript] によってインストールされる。
conscript のセットアップは簡単なので、まだセットアップしていなければ[今すぐやってきて][conscript]ほしい。

[conscript]: https://github.com/n8han/conscript#readme

conscript がセットアップされて、`~/bin` にパスが通っているとすると、Pamflet は以下のようにインストールできる:

    cs n8han/pamflet

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
