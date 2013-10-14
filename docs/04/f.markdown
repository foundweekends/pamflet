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
Use ISO 639-1 two-letter language codes separated by comma:

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

This will generate Japanese contents under `ja/` folder. Contents are assumed to be
a translation when the relative path is the same. When they are detected,
the language bar appears at the top of the page to link to all translations.
