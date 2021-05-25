# Plugin: ${artifactId}

## Purpose

Provide a concise description of the purpose of the plugin.

e.g. This plugin 

## Usage

### Enabling the plugin

Enable the **${artifactId}** plugin on your **<results page|data source>** from the **Extensions** screen in the administration dashboard or add the following <results page|data source> configuration to enable the plugin.

```ini
plugin.${artifactId}.enabled=true
plugin.${artifactId}.version=${version}
```

FOR DATA SOURCE PLUGINS (update if reqd):
NOTE: The plugin will take effect after a full update of the data source.

FOR RESULTS PAGE PLUGINS (update if reqd):
NOTE: The plugin will take effect as soon as it is enabled.

### Plugin configuration options

The following options can be set in the <results page|data source> configuration to configure the plugin:

* `plugin.${artifactId}.foo`: What it does. Default is <the default value>
* `plugin.${artifactId}.other-config-setting`: what it does. Default is <the default value>

## Details

OPTIONAL: Provide additional details about the plugin in appropriate. Include details on data model output, FreeMarker usage etc. 

## Examples

### <EXAMPLE TITLE>

An example to...

```ini
plugin.${artifactId}.foo=bar
plugin.${artifactId}.other-config-setting=something else.
```

which will result in....

## Upgrade notes

Provide guidance on how to upgrade between versions, especially if some breakable changes have been introduced.
Recommended structure:

### Upgrading to version x.y.z
Instructions how to upgrade from previous to next version of the plugin

## Change log

Provide log what has changed in each version of the plugin.
Recommended structure (see https://keepachangelog.com/en/1.0.0/):

### [x.y.z]
#### Added
- new configuration key

#### Changed
- Changed behaviour of x to y

#### Fixed
- Some bug
