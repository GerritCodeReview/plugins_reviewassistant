Build
=====

This plugin can be built with Bazel or Maven.

Bazel
----

Clone (or link) this plugin to the `plugins` directory of Gerrit's source tree.

Put the external dependency Bazel build file into the Gerrit /plugins directory,
replacing the existing empty one.

```
  cd gerrit/plugins
  rm external_plugin_deps.bzl
  ln -s reviewassistant/external_plugin_deps.bzl .
```

Then issue

```
  bazel build plugins/reviewassistant
```

in the root of Gerrit's source tree to build

The output is created in

```
  bazel-genfiles/plugins/reviewassistant/reviewassistant.jar
```

This project can be imported into the Eclipse IDE.
Add the plugin name to the `CUSTOM_PLUGINS` set in
Gerrit core in `tools/bzl/plugins.bzl`, and execute:

```
  ./tools/eclipse/project.py
```

Maven
-----

Note that the Maven build is provided for compatibility reasons, but
it is considered to be deprecated and will be removed in a future
version of this plugin.

To build with Maven, run

```
  mvn clean package
```

When building with Maven, the Gerrit Plugin API must be available.

How to build the Gerrit Plugin API is described in the [Gerrit
documentation](../../../Documentation/dev-bazel.html#_extension_and_plugin_api_jar_files).
