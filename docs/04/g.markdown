Color Schemes
-------------

You might want to read pamflets using a dark color scheme. That's why Pamflet ships with three color schemes:
<a href="?color_scheme=github">github</a>,
<a href="?color_scheme=monokai">monokai</a>, and
<a href="?color_scheme=redmond">redmond</a>.

### Applying a color scheme

To apply a dark color scheme, open your pamflet with query string
<nobr><code>?color_scheme=monokai</code></nobr>. This will store `"monokai"` to the HTML5 local storage, and apply the scheme to the pamflet.

### Default color scheme

The default scheme is redmond. To change it to something else, set `color_scheme` property:

    color_scheme=github

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
