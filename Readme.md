dbfixer library
===============

This is a kind of curation utility library to use along with the [appform admin tool](https://github.com/telekosmos/epiquest-admin). This was written mostly in [Groovy](http://groovy.codehaus.org/) to take advantage of its advanced syntax for many issues, as you can check if you click on the upper language statistics bar.

A `build.xml` file is provided, and you will can use it to build the _jar_ file, with or without an IDE (_JetBrains IntelliJ Idea_ is the IDE recommended, _Eclipse_ or any of the other many IDE tools for _Java_ supporting _Groovy_ can be used), but you may have to fix the paths to the ant script can access to the right files.

When the jar file is generated, it has to be dropped into the `WEB-INF/lib` directory of [appform admin project](https://github.com/telekosmos/epiquest-admin) as a library in order the admin tool works.