# Coreen

Coreen is a code reading environment. It aims to facilitate the process of
reading and understanding code. It is not an IDE, the primary goal of which is
authoring code, and only secondarily supports code reading and understanding.
That said, Coreen will necessarily overlap with the functionality of various
IDEs.

Coreen's functionality breaks down into three areas:

* search
* navigation
* visualization

Coreen is an extremely early work-in-progress. It presently works only with
Java source code (it is functionally code-agnostic under the hood but will
require per-language name resolvers). It accomplishes basic code display,
navigation and searching, but new functionality is being added frequently.

## Screen shots

Here are some screenshots showing a few different views:

* [Project summary](https://github.com/samskivert/coreen/raw/master/docs/shots/projsum.png)
* [Package summary](https://github.com/samskivert/coreen/raw/master/docs/shots/pkgsum.png)
* [Class summary](https://github.com/samskivert/coreen/raw/master/docs/shots/classsum.png)
* [Searching](https://github.com/samskivert/coreen/raw/master/docs/shots/search.png)

## Try it

Coreen is an application that runs on your local machine. Download the
installer appropriate to your platform here:

* [Linux](http://github.com/samskivert/coreen/raw/master/client/installers/coreen-install.bin)
* [Mac OS X](http://github.com/samskivert/coreen/raw/master/client/installers/coreen-install.dmg)
* [Windows](http://github.com/samskivert/coreen/raw/master/client/installers/coreen-install.exe)

Check out the [Getting Started with
Java](https://github.com/samskivert/coreen/wiki/Getting-started-with-java)
documentation once you have it installed.

## Discuss it

Coreen has a [Google Group](https://groups.google.com/forum/#!forum/coreen)
where you can ask questions, discuss code reading, suggest features, and do
many other communication-related activities.

## Wire it into your development environment

Coreen is most useful when you can use it to look up names while you're editing
source code. Ideally you should be able to press a few keys while the cursor is
sitting on a symbol that you want to know more about. The following are
instructions for making that a reality.

### Emacs

Download the
[coreen.el](https://github.com/samskivert/coreen/raw/master/src/main/elisp/coreen.el)
file (or just check out the whole project and reference it from
`src/main/elisp/coreen.el`), and add the following elisp to your .emacs file:

    (load "path/to/coreen.el")
    ;; these key combinations are, of course, only suggestions
    (defun coreen-java-mode-hook ()
      (define-key java-mode-map "\C-c\C-j" 'coreen-view-symbol)
      (define-key java-mode-map "\M-."     'coreen-open-symbol)
      (define-key java-mode-map "\M-/"     'pop-coreen-mark)
      (define-key java-mode-map "\M-?"     'coreen-view-symbol)
      (define-key java-mode-map "\M-["     'previous-error)
      (define-key java-mode-map "\M-]"     'next-error)
      )
    (add-hook 'java-mode-hook 'coreen-java-mode-hook)

### Vim

Add the following to your .vimrc file:

    " this allows you to enter :c to look up the symbol under the point
    command -nargs=1 -complete=tag Coreen !xdg-open http://localhost:8192/coreen/\#LIBRARY~search~<args>
    nmap <leader>c :Coreen <cword><CR>

On a Mac you'll want to change `xdg-open` to `open` and on Windows you'll want
to change it to `start`. On Linux, `xdg-open` should work as long as you have
Gnome or KDE installed.

### Other editors

Contributions are welcome for instructions on how to easily call out to Coreen
from other editors. Please send them to my email address below.

## Blame

Coreen is a research project being conducted by [Michael
Bayne](mailto:mdb@cs.washington.edu). Feel free to email him to let him know
that this is a crazy idea (crazy good, or crazy bad), or even better, that you
are interested in writing a name resolver for another language.
