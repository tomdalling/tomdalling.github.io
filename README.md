Site generator for http://www.tomdalling.com/

WARNING
-------

USE JAVA 8. DONT TRY TO UPGRDAE. IT WILL HURT U.

Commonly Used Commands
----------------------

    lein midje
    lein midje :autotest
    lein ring server
    lein build-site
    ./publish.sh

TODO
----

 - Put mathjax into the static js, instead of link to CDN
 - Proper titles for category RSS feeds
 - Absolute URLs for RSS feeds (and maybe everywhere)
 - Test that follows all internal links to make sure they're correct
 - Turn on markdown options for common text transformations like "--" and "..."
 - In FizzBuzz article, fix Ruby type docs for `Array(String, int)` to be
   `Array(String, Proc)`
 - Fix gluPerspective (see disqus comments)
 - Fix broken links in opengl article 01 (see disqus comments)

Proofreading
------------

Common typos:

 - therefor
 - its
 - it's
 - homogenous
 - colour (use American spelling)
 - though/thought

Also check for:

 - TODO
 - Places to add pull quotes
 - markdown/quotes inside tags (like <blockquote>s and <figure>s)
 - Broken links
