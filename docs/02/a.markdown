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
