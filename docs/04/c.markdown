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
dollar sign. In this example, occurrences of `\$version\$` in the
Markdown source would be replaced by `0.3.4` in the HTML output.

### Properties Front Matter

In addition, each page may add its own properties by including
a properties front matter at the beginning of the page as follows:

    ---
    version=0.2.5
    dispatch=0.8.5
    ---

Properties in the front patter takes precedence over the ones in
`template.properties`.

###### Note

Once activated by the presence of the .properties file, StringTemplate
will be confused by any stray `\$` in your source. Always escape
dollar signs that are not template properties with a backslash: `\\\$`
