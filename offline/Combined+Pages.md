Pamflet
=======

*Pamflet* is a publishing application for short texts, particularly
user documentation of open-source software. It is designed to be easy
to write and read on any platform.

> [Version 0.6.0 Release Notes](http://notes.implicit.ly/post/87661053009/pamflet-0-6-0)

Like a Book
-----------

Pamflet generates a linked sets of pages called "pamflets". Although
hierarchical organization of pages is supported, the primary means of
navigation is linear and authors are encouraged to write pamflets that
progress logically when read front to back.

The table of contents appears at the bottom of every page. The first
page of a pamflet should be brief so that the generated contents
listing will appear above the fold. Think of it as a title page, with
a few sentences of description.

### On the Big Screen

On devices with large screens--typically, laptop and desktop
computers--pamflet texts appear in a fixed-width center column.  The
margins are giant **clickable regions** for page flipping. The left
and right **keyboard arrows** may also be used for the same purpose.

### Going Mobile

Since Pamflets are meant to be carried around, having a good mobile
appearance and behavior is vital. When viewed on a phone, tablet, or
other small-screen device, pamflets take the full screen width.

#### Radio Silent

And because mobile devices aren't always connected, an
[HTML5 cache manifest][manifest] is generated for all pamflets. Add a
bookmark for a pamflet to your home screen so you can read it
anywhere.

[manifest]: http://diveintohtml5.org/offline.html

Writing and Organizing
----------------------

Pamflet's purpose is to make it so easy to write decent project
documentation that everyone will do it. This section tells you
everything there is to know about writing pamflets.

Filenames and Page Names
------------------------

Pamflet reads source files from the source directory and produces
finished web files at the destination. Sources with a `markdown` or
`md` extension correspond to one `html` output file.


Filenames are used only for ordering pages and sections; the display
titles and page URLs are determined instead by the first heading
element in the source of the page.

### Naming Conventions

It is suggested that you name your sources with a simple numeric or
alphabetical prefix to control ordering; after that you can include
some portion of the title, or nothing, and end with the markdown
extension.

For example, the title page of a pamflet could be named `00.markdown`
to ensure that is the first source in the ordered list. The contents
might look like this:

    Pamflet
    =======

    *Pamflet* is a publishing application...

This tells the processor that "Pamflet" is the name of the page. If
this source is indeed the first one in the ordered list, it is
interpreted as the name of the entire pamflet.

#### Output Names

The output `html` filenames are URL-encoded versions of the page
names; the have no relationship to the input filenames. Page names
must therefore be unique across the pamflet.

Shaping the Contents Tree
-------------------------

As mentioned on the previous page, pages are ordered alphabetically
by their source filenames. This ordering also affects the *Contents*
listing that appears below every page.

Pamflet uses HTML ordered lists for *Contents*. In the default
stylesheet, the top-level contents list is made of simple numbers.
There is one quirk of the structure that may be surprising: The first
markdown source found in a directory is not placed in the same list
as the other sources. The title page described in the previous
example would not appear with any number at all. Given the following
directory:

    docs/
      00.markdown
      01.markdown
      02.markdown

The *Contents* will appear in this manner, with the actual names
and links derived from the first header found in each page:

    <00.markdown>
    1. <01.markdown>
    2. <02.markdown>

For this reason, we consider the title page to be the zeroth page and
typically name it as such.

Going Deep
----------

Even the simplest pamflet can often benefit from grouping related
pages into sections--in fact, you're reading a section right now.

Pamflet supports arbitrarily many nested sections, defined by
directories beneath the primary source directory. These directories
participate in the same name-based ordering as their adjacent markdown
sources.

    docs/
      00.markdown
      01/
        00.markdown
        a.markdown
        b.markdown
      02.markdown

Given valid sources containing page headings, the above structure will
produce the following *Contents* listing:

    <00.markdown>
    1. <01/00.markdown>
      a. <01/a.markdown>
      b. <01/b.markdown>
    2, <02.markdown>

Here you can see how the first ordered source in a directory,
`00.markdown`, always acts as a title page whether it is for a the
entire pamflet or just one section. It is numbered and positioned
according on the higher level numbering; at the top level, this is
simply the absense of any number.

You can also see in this example that Pamflet's second level numbering
is lowercase letters. The third is lowercase roman numerals, and and
after that it is back to the browser default. You can override these
for any level using a custom stylesheet.

###### Note

Nesting is reflected only in the *Contents* listing. The pamflet page
namespace is flat, with all output `html` under a single path.

Pamflet in a Project
--------------------

Although it can be applied for any purpose, Pamflet is tailored to
produce open-source project documentation. This section explores the
workflow from drafting to publication.

On the Command Line
-------------------

Pamflet is installed with [conscript][conscript], a general installer
and updater for Scala applications. Conscript is pretty easy to set
up, so please [do that and come back][conscript] if you haven't yet.

[conscript]: https://github.com/foundweekends/conscript#readme

Once you have conscript setup, and assuming that `~/bin` is on your
executable search path, you can install Pamflet like so:

    cs foundweekends/pamflet

That installs Pamflet's `pf` command, which is used to both preview
and publish pamflets. 

    Usage: pf [SRC] [DEST]

### Preview

Pamflet's preview mode allows you to check the layout and text
rendering as much as you like while editing project documentation.

When you call `pf` with no arguments, `SRC` is assumed to be "docs"
under the working directory--typically, your project's base
directory. When you call `pf` with one argument, you are specifying
the `SRC` directory. In both these cases where no `DEST` is specified,
Pamflet goes into preview mode.

    2011-07-15 09:29:38.033:INFO::jetty-7.2.2.v20101205
    2011-07-15 09:29:38.066:INFO::Started SocketConnector@127.0.0.1:44449

    Previewing `docs`. Press CTRL+C to stop.

Preview mode starts an [Unfiltered][uf] web server bound to the
loopback interface on some available port. It also requests that a
local web browser open the relevant URL; this occurs in a background
or foreground window depending on your platform and browser.

[uf]: http://unfiltered.databinder.net/

If a usable pamflet is found in the `docs` or other specified
directory, you will see its title page in the browser. Linear
navigation and the *Contents* listing will work exactly as in the
published pamflet. But the best thing about preview is that any
changes to the pamflet source are reflected immediately: just hit
reload.

### Publish

To publish Pamflet sources into web sources, call the `pf` command
with both `SRC` and `DEST` parameters. If `SRC` has Pamflet sources
and `DEST` is some directory that the current user can write to,
you'll find all the output files written to it after the command
completes.

Git Hooks
---------

Git hooks turn Pamflet from a cute toy into digital printing
press. Set them up on a server under your control (you do have one,
yes?) and have your published documentation updated every time you
push.

Assuming your have a `--bare` git repository on your server, it should
already have a `hooks/` subdirectory. In `hooks/`, create or edit a
file `post-update` that is similar to the following:

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

`DOCB` is a clone of the same repo, but with a working tree. `PUB` is the
directory where the finished `html` and other files will be
placed. `HOME` is your home directory, because git runs the hook in a
weird environment and it's best to be explicit.

Set all that up, and make the hook script executable *or it will not
run and you'll be very confused for a while*. Also, make sure that
`~/bin/pf` runs normally on the server.

When you push to the bare repo from elsewhere, you'll see the normal
git output along with a success or error message from Pamflet. (And
yes, this is some pretty lame scripting. Fork this pamflet, that's
what it's here for.)

Apache Setup
------------

You probably know all this already, but to save the rest of us some
time, Apache can be pointed to your pamflet directory like this:

```xml
<VirtualHost *:80>
        ServerName pamflet.databinder.net
        DocumentRoot /home/me/app/pamflet
        RewriteEngine On
        RewriteRule ^/$ /Pamflet.html [L,R=permanent]
</VirtualHost>
```

###### Note

You do need a root redirect if you want requests to the root path to
resolve. The title page is assigned an output name like any other.

Fancy Techniques
----------------

We've covered how to write and publish pamflets. This is how to write
and publish even fancier pamflets.

Source Code Highlighting
------------------------

Pamflet hooks into the very slick prettify.js source code
highlighter. To activate it, use github's fenced code block syntax to
enclose your source and specify a language name known to prettify.

    ```scala
    // Some comment
    case class Page(name: String) {
      def foo: Int = (1 to 10) /: { _ + _ }
      val bar = "wat"
    }
    ```

appears as

```scala
// Some comment
case class Page(name: String) {
  def foo: Int = (1 to 10) /: { _ + _ }
  val bar = "wat"
}
```

Really clears things up, right?

Custom Stylesheets
------------------

To give your project that special edge, you will surely want to set
all headings to Copperplate MT or Bank Gothic.

Not to worry, Pamflet supports this. All files in the source directory
with the extension `css` will be packaged with the pamflet. These will
be loaded after all other stylesheets, so go crazy.

Template Properties
-------------------

Because you probably have a few current version numbers and things
repeated throughout your documentation, Pamflet supports template
properties through StringTemplate.

If a file `template.properties` is found in the pamflet source
directory, all Markdown sources are passed through StringTemplate with
the properties as read by `java.util.Properties`. This is an example
`template.properties`:

    version=0.3.4
    vrsn=034
    scala=2.8.1

Properties are referenced in sources by bracketing them with the
dollar sign. In this example, occurrences of `$version$` in the
Markdown source would be replaced by `0.3.4` in the HTML output.

### Properties Front Matter

In addition, each page may add its own properties by including
a properties front matter at the beginning of the page as follows:

    ---
    version=0.2.5
    dispatch=0.8.5
    ---

Properties in the front matter takes precedence over the ones in
`template.properties`.

###### Note

Once activated by the presence of the .properties file, StringTemplate
will be confused by any stray `$` in your source. Always escape
dollar signs that are not template properties with a backslash: `\$`

Special Properties
------------------

While [template properties](Template+Properties.html) exist generally
for you to define and use as in your own markdown sources, Pamflet
also checks for a special, optional property that links to a project's
GitHub repository, etc.

### Fork Me on GitHub

If you define a `github` property with the repo owner and name,
Pamflet constructs a link to the repository. For example:

    github=foundweekends/pamflet

This is the property that produces the link to GitHub from Pamflet's
own documentation. Note that there is only one slash.

### I Want Out

If you define an `out` property with a file name,
Pamflet uses it to generate the file for the page.
It is intended to be used in the properties front matter of the page:

    ---
    out: index.html
    ---

### Collapsible table of contents

`toc` property may be used to control how the table of contents
is displayed at the end of each page.
The allowed values are `show`, `hide`, and `collapse`.

### Disqus

Defining `disqus` property with a disqus short name will add a comment section at the end of all your pages.

    disqus=namehere

### Twitter

Defining `twitter` property with any value will pop up a twitter button
when some text is selected on any page. The button will compose a tweet including
the selected text, the property, and the URL.
The value `show` would display the button, but will not be included in the tweet.

    twitter=#pamflet

### Google Analytics

Defining `google-analytics` with a Google Analytics web property ID will 
insert [tracking Javascript][ga] in the head of all your pages. For
example:

    google-analytics=UA-12345-6


[ga]: http://code.google.com/apis/analytics/docs/tracking/asyncTracking.html

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

Globalization
-------------

If you want to translate your pamflet into multiple languages, 
there are special [template properties](Template+Properties.html) that enable globalization.

### Default Language

`language` property is used to specify the language used at the root level.
By default, it is set to `en`, which is ISO 639-1 code for English.

    language=en

### List of Languages

`languages` property is used to specify the list of languages used in your pamflet.
Use ISO 639-1 two-letter language codes or IETF language tags separated by comma:

    languages=en,ja

Then place non-default language contents under the folder named after the language code.

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

This will generate Japanese contents under `ja/` folder. 

### Language Labels

Contents are assumed to be a translation when the relative path is the same. When they are detected,
the language links appears at the bottom of each page to link to all translations.

Pamflet is able to resolve some of the language codes, but you can
provide custom language labels using `lang-` properties if it doesn't or
just to change it:

    lang-pt-BR=Português (Brasil)

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

### Custom header and footer

To change the header to something else, set `layout.header` and `layout.header.height` property:

    layout.header=header.md
    layout.header.height=2em

Now drop a file named `header.md` into `docs/layout/`:

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

This will add "(draft)" at the end of the default header. The footer works the same way. Just use `layout.footer` and `layout.footer.height` instead.

For Your Perusal
----------------

All techniques described in this pamflet are used to produce it. Take
a look through Pamflet's `docs/` directory [on github][gh].

[gh]: https://github.com/foundweekends/pamflet/tree/master/docs

Who's Using Pamflet?
--------------------

* [Dispatch](http://dispatch.databinder.net/)
* [Pamflet](http://pamflet.databinder.net/)
* [Unfiltered](http://unfiltered.databinder.net/)
* [Subset](http://osinka.github.com/subset/Subset.html)
* [Lifty](http://lifty.github.com/Lifty.html)
* [treehugger.scala](http://eed3si9n.com/treehugger/)
* [GeoTrellis](http://azavea.github.com/geotrellis/getting_started/GeoTrellis.html)
* [BND book](http://bnd-book.duck-asteroid.cloudbees.net/BND.html)
* [learning Scalaz](http://eed3si9n.com/learning-scalaz/)
* [tetrix in Scala](http://eed3si9n.com/tetrix-in-scala/)
* [sbt](http://www.scala-sbt.org/0.13/tutorial/index.html)
* [scodec](http://scodec.org/guide/)

Are you using Pamflet? Add your project and send a pull request.
