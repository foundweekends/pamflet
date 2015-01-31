Additional resources
--------------------

If you want your pamflet to carry your favorite image or any other
resource, add it to a `files` directory just below `docs`. Then, link
to it as if this directory were beside your pamflet page.

Supposing you drop a file named `a.txt` into `docs/files/` then
`[some file](files/a.txt)` becomes [some file](files/a.txt). Try it!

### Embedding images 

Of course, the trick works great for images using usual markdown:
 
```
![some svg](files/an.svg)
```

Becomes

> ![some svg](files/an.svg)

It's worth a thousand words, give or take.

### favicon

To customize the favicon, place `favicon.ico` in the source directory.
