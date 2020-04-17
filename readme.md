This project contains the Funnelback shared libraries and supporting components intended for developers of Funnelback plugins.

Documentation for actually developing such plugins will be included with Funnelback itself once support for plugins becomes
publicly available.

The code within this repository is licensed under the Apache 2.0 license and deployed into maven central so that it
can easily be accessed by prospective plugin developers.

## Developing the shared components

We have gitlab-ci set up to automatically build new versions of this project and to deploy them into maven central
whenever a non-snapshot version is merged into the master branch.

What that means in practice is that when you start developing changes you should run...

    mvn versions:set -DnewVersion=1.2.3-SNAPSHOT

...replacing in the appropriate newVersion value, which would generally be `(current-version + 1) + "-SNAPSHOT"`.
That will change all the pom.xml files to have the appropriate new versions and you can start developing.

Once we're ready to actually release a version, run it again without the -SNAPSHOT...

    mvn versions:set -DnewVersion=1.2.3

...and then get that change merged into the master branch. Assuming everything goes well,
gitlab-ci will build the change and deploy it out to maven central. The syncing process takes
a little while, but you should expect to see your new version appear at 
[https://repo1.maven.org/maven2/com/funnelback/](https://repo1.maven.org/maven2/com/funnelback/)
within 15 minutes or so at worst. If not, you probably ought to check out master's pipeline
to see if something went wrong.