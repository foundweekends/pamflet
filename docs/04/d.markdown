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
