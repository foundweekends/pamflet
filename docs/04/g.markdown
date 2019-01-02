Changing the look
-----------------

You might want to read pamflets using a dark color scheme. That's why Pamflet ships with three color schemes:
<a href="?color_scheme=github">github</a>,
<a href="?color_scheme=monokai">monokai</a>, and
<a href="?color_scheme=redmond">redmond</a>.

### Applying a color scheme

To apply a dark color scheme, open your pamflet with query string
<nobr><code>?color_scheme=monokai</code></nobr>. This will store `"monokai"` to the HTML5 local storage, and apply the scheme to the pamflet.

### Default color scheme

The default scheme is github. To change it to something else, set `color_scheme` property:

    color_scheme=monokai

### Custom color scheme

To create your own custom color scheme, for example named "zen", create `color_scheme-zen.css`
and declare css properties under `body.color_scheme-zen`:

```css
body.color_scheme-zen {
    color: black;
    background: white;
}
body.color_scheme-zen code.prettyprint span.str {
  color: #dd1144
}
```

### Custom header and footer

To change the header to something else, set `layout.header` and `layout.header.height` property:

    layout.header=header.md
    layout.header.height=2em

Now drop a file named `header.md` into `docs/layout/`:

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

This will add "(draft)" at the end of the default header. The footer works the same way. Just use `layout.footer` and `layout.footer.height` instead.
