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

Given valid sources containing page headers, the above structure will
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
