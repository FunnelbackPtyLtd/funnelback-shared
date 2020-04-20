This project contains the Funnelback shared libraries and supporting components intended for developers of Funnelback plugins.

Documentation for actually developing such plugins will be included with Funnelback itself once support for plugins becomes
publicly available.

The code within this repository is licensed under the Apache 2.0 license and deployed into maven central so that it
can easily be accessed by prospective plugin developers.

## Developing the shared components

We have gitlab-ci set up to automatically build new versions of this project and to deploy them into maven central
whenever a non-snapshot version is merged into a release-(version-number) branch.

What that means in practice is that when you are ready to release a new version, you should create
an appropriate release branch (i.e. with the intended version number) and run...

    mvn versions:set -DnewVersion=1.2.3

...to set a non-snapshot version of the intended version in the pom files.

Then you should get that change merged into a release branch with the matching version
(in this example release-1.2.3.x). Assuming everything goes well, gitlab-ci will then
build the change on the release branch.

Once the initial build is finished on the release branch, gitlab-ci's pipeline will show
a play-button icon next to the release stage of the pipeline. If you're sure you're ready
to release the new version to maven central, click the play button and the components here
will be rebuilt, deployed into maven central and released.

Maven central's syncing process takes a little while, but you should expect to see your new
version appear at [https://repo1.maven.org/maven2/com/funnelback/](https://repo1.maven.org/maven2/com/funnelback/)
within 15 minutes or so at worst. If not, start by looking at the result of the 'release' job
in master's pipeline for what went wrong.

After updating the version, you might also want to update the version in master
to reflect the intended 'next' version by running something like...

    mvn versions:set -DnewVersion=1.2.4-SNAPSHOT

...and getting that change merged into master.
