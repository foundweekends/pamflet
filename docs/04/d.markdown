Special Properties
------------------

While [template properties](Template+Properties.html) exist generally
for you to define and use as in your own markdown sources, Pamflet
also checks for a special, optional property that links to a project's
GitHub repository, etc.

### Fork Me on GitHub

If you define a `github` property with the repo owner and name,
Pamflet constructs a link to the repository. For example:

    github=n8han/pamflet

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
