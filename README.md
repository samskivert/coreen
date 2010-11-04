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
require per-language name resolvers). It also barely accomplishes basic code
display, but new functionality is being added daily.

## Try It

Coreen is an application that runs on your local machine. Download the
installer appropriate to your platform here:

* [Linux](http://github.com/samskivert/coreen/raw/master/client/installers/coreen-install.bin)
* [Mac OS X](http://github.com/samskivert/coreen/raw/master/client/installers/coreen-install.dmg)
* [Windows](http://github.com/samskivert/coreen/raw/master/client/installers/coreen-install.exe)

## Wire it into your development environment

Coreen is most useful when you can use it to look up names while you're editing
source code. Ideally you should be able to press a few keys while the cursor is
sitting on a symbol that you want to know more about. The following are
instructions for making that a reality.

### Emacs

Add the following elisp to your .emacs file:

    (defun coreen-lookup-symbol (class)
      "Looks the symbol under the point up in Coreen."
      (interactive (list (read-from-minibuffer "Class: " (thing-at-point 'symbol))))
      (browse-url (concat "http://localhost:8080/coreen/#LIBRARY~search~" class))
      )
    ;; this maps lookup in Java mode to Ctrl-c Ctrl-j, adjust as desired
    (defun coreen-java-mode-hook ()
      (define-key java-mode-map "\C-c\C-j" 'coreen-lookup-symbol)
      )
    (add-hook 'java-mode-hook 'coreen-java-mode-hook)

### Other editors

Contributions are welcome for instructions on how to easily call out to Coreen
from other editors. Please send them to my email address below.

## Contact

Coreen is a research project being conducted by [Michael
Bayne](mailto:mdb@cs.washington.edu). Feel free to email him to let him know
that this is a crazy idea (crazy good, or crazy bad), or even better, that you
are interested in writing a name resolver for another language.
