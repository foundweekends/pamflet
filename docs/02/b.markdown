Shaping the Contents Tree
-------------------------

As mentioned on the previous page, source filenames determine page
ordering of a pamflet. This ordering also affects the *Contents*
listing that appears below every page.

Pamflet uses HTML ordered lists for *Contents*. In the default
stylesheet, the top-level contents list is made of simple numbers.
There is one quirk of the structure that may be surprising: The first
markdown source found in a directory is not places in the same list
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
