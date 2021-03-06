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
is displayed.
The allowed values are `left`, `bottom`, `hide`, and `collapse`.

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

### Page path

The property `page.localPath` is automatically set to the local path to the page's markdown source,
which allows, for instance, the construction of a direct-to-GitHub page edit URL in the header or footer, e.g:

    <div class="container">
        <div class="row">
          <div class="span-16 prepend-1 append-1">
            <div class="span-16 nav">
              <p style="padding-top: 0.5em">
                <a href="https://github.com/foundweekends/pamflet/edit/master/docs/\$page.localPath\$">
                [Edit on GitHub]</a>
              </p>
            </div>
          </div>
      </div>
    </div>

Scroll to the bottom of this page to see how this works.
