---
out: Apache-Setup.html
---

Apache の設定
------------

もう知っているかもしれないが、僕たちみたいな知らない人が時間を無駄にしないために
Apache を使って pamflet ディレクトリを公開する方法を書いておく:

```xml
<VirtualHost *:80>
        ServerName pamflet.databinder.net
        DocumentRoot /home/me/app/pamflet
        RewriteEngine On
        RewriteRule ^/\$ /Pamflet.html [L,R=permanent]
</VirtualHost>
```

###### 注意

ルートパスを解決させるためには、ルートをリダイレクトする必要がある。
タイトルページも他のページ同様にアウトプット名が指定されるからだ。
