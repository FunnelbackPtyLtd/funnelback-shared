# Plugin: ${artifactId}

Description of what your plugin does.

## Usage

Add the following to `collection.cfg` to enable the plugin.

```ini
plugin.${artifactId}.enabled=true
plugin.${artifactId}.version=${version}
```

The following `collection.cfg` settings can be used to configure the plugin:

* `plugin.${artifactId}.foo`: What it does.
* `plugin.${artifactId}.other-config-setting`: what it does.

## Examples

An example to...

```ini
plugin.${artifactId}.foo=bar
plugin.${artifactId}.other-config-setting=something else.
```

which will result in....

## Upgrade Notes

Provide guidance on how to upgrade between versions, especially if some breakable changes have been introduced.
Recommended structure:

### Upgrading to version x.y.z
Instructions how to upgrade from previous to next version of the plugin

## Changelog

Provide log what has changed in each version of the plugin.
Recommended structure (see https://keepachangelog.com/en/1.0.0/):

### [x.y.z]
#### Added
- new configuration key

#### Changed
- Changed behaviour of x to y

#### Fixed
- Some bug
