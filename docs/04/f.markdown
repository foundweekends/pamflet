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
