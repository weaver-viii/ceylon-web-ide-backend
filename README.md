Ceylon Web IDE
==============

This is a web-based IDE for writing and testing [Ceylon][]
modules, itself written in pure Ceylon, with the following
features:

- code editor based on [Code Mirror][]
- support for editing Ceylon, JavaScript, and MarkDown
- syntax highlighting
- live error reporting
- autocompletion
- online documentation "hover"
- run and stop
- multi-file support ("Advanced" mode)
- support for importing modules from [Ceylon Herd][]
- persistence to [Gist][] and sharing 
- light and dark editor themes

[Ceylon]: http://ceylon-lang.org
[Ceylon Herd]: http://modules.ceylon-lang.org
[Code Mirror]: http://codemirror.net
[Gist]: http://gist.github.com

To compile the project, first install the Ceylon [compiler][], 
then go to the `ceylon-web-ide-backend` directory, and type:

    ceylon compile
    ceylon war --name=ceylon-ide.war com.redhat.ceylon.ide.web -R web-content

This will create a file named `ceylon-ide.war` in the current
directly.

To deploy the project, first install [WildFly 10][], or any
other Java EE server, then copy `ceylon-ide.war` to the 
deployment directory, for example:

    cp ceylon-ide.war ~/wildfly-10.0.0.CR1/standalone/deployments

Finally, go to:

<http://localhost:8080/ceylon-ide>

[compiler]: http://ceylon-lang.org/download
[WildFly 10]: http://wildfly.org/downloads

## License

The content of this repository is released under the ASL v2.0
as provided in the LICENSE file that accompanied this code.

By submitting a "pull request" or otherwise contributing to 
this repository, you agree to license your contribution under 
the license mentioned above.

