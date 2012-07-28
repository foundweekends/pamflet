Apache Setup
------------

You probably know all this already, but to save the rest of us some
time, Apache can be pointed to your pamflet directory like this:

```xml
<VirtualHost *:80>
        ServerName pamflet.databinder.net
        DocumentRoot /home/me/app/pamflet
        RewriteEngine On
        RewriteRule ^/\$ /Pamflet.html [L,R=permanent]
</VirtualHost>
```

###### Note

You do need a root redirect if you want requests to the root path to
resolve. The title page is assigned an output name like any other.
