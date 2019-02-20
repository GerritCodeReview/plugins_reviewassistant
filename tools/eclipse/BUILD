load("//tools/bzl:classpath.bzl", "classpath_collector")
load("//tools/bzl:plugin.bzl", "PLUGIN_DEPS")

classpath_collector(
    name = "main_classpath_collect",
    deps = PLUGIN_DEPS + [
        "//:reviewassistant__plugin",
    ],
)
