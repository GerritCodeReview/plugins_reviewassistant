load("//tools/bzl:plugin.bzl", "PLUGIN_DEPS")
load("//tools/bzl:classpath.bzl", "classpath_collector")

classpath_collector(
    name = "main_classpath_collect",
    deps = PLUGIN_DEPS + [
        "//:reviewassistant__plugin",
    ],
)
