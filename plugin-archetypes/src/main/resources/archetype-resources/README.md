 To build your plugin run:

```
mvn clean install
```

That will build, run the tests and assemble the plugin into:

```
target/version
```

## Installing

### Installing to a locally installed Funnelback
If you have a local Funnelback and SEARCH_HOME is defined you can install
the plugin into funnelback with:

```
mvn clean install -Pinstall-local
```

### Manually installing

That can be installed into Funnelback
```
/opt/funnelback/share/plugins/<plugin-name>/
```

e.g.

```
cp -a target/version /opt/funnelback/share/plugins/<plugin-name>/
```
