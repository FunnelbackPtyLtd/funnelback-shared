# funnelback-shared

This project contains the Funnelback shared libraries and supporting components intended for developers of Funnelback plugins.

Documentation for actually developing such plugins will be included with Funnelback itself once support for plugins becomes
publicly available.

The code within this repository is licensed under the Apache 2.0 license and deployed into maven central so that it
can easily be accessed by prospective plugin developers.

## Development

Whenever changes are done and merged into the default branch, GitLab CI/CD job is triggered to deploy a compiled library into 
GitLab package registry (development/snapshot version).

## Release

To release a new version (production/non-snapshot version) of a library to public repository (Maven central)

1. Create a new `release-<version-number>` branch
2. Once CI/CD pipeline finishes, trigger manually `release` job (click play-button icon next to the release stage of the pipeline).

Note, Maven central's syncing process takes a little while, but you should expect to see your new version appear at 
[https://repo1.maven.org/maven2/com/funnelback/](https://repo1.maven.org/maven2/com/funnelback/) within 15 minutes or so at worst.
If not, start by looking at the result of the 'release' job in release's pipeline for what went wrong.

`release` job changes the current version of the library to non-snapshot one and then runs a maven deploy process.
