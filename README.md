# funnelback-publicui

> Funnelback PublicUI is the Java web application responsible for receiving search requests, processing them, forking padre-sw to perform the underlying index searching and ranking, and then returning the results to the user in html (templated with Freemarker), JSON, XML etc.

## Install

To download the dependencies and build the `target/` directory.

```bash
mvn clean install -DskipTests -B
```

## Test

To run _all_ the tests

```bash
mvn test
```
