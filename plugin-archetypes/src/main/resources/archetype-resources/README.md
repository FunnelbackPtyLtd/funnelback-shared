 To build your plugin run:

```
mvn clean install
```

That will build, run the tests and assemble the plugin into:

```
target/version
```

That can be installed into Funnelback by dropping it under:
```
/opt/funnelback/share/plugins/<plugin-name>/
```

e.g.

```
cp -a target/version /opt/funnelback/share/plugins/<plugin-name>/
```
