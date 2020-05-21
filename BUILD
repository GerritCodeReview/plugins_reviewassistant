load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@npm_bazel_rollup//:index.bzl", "rollup_bundle")
load("//tools/bzl:plugin.bzl", "gerrit_plugin")
load("//tools/bzl:genrule2.bzl", "genrule2")
load("//tools/bzl:js.bzl", "polygerrit_plugin")

proto_library(
    name = "reviewassistant_proto",
    srcs = ["src/main/proto/reviewassistant.proto"],
)

java_proto_library(
    name = "reviewassistant_java_proto",
    visibility = ["//visibility:public"],
    deps = [":reviewassistant_proto"],
)

gerrit_plugin(
    name = "reviewassistant",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: reviewassistant",
        "Gerrit-Module: com.github.reviewassistant.reviewassistant.Module",
        "Gerrit-HttpModule: com.github.reviewassistant.reviewassistant.HttpModule",
        "Implementation-Title: Review Assistant",
        "Implementation-URL: https://github.com/reviewassistant/reviewassistant",
    ],
    resource_jars = [":gr-reviewassistant-static"],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "//java/com/google/gerrit/proto",
        ":reviewassistant_java_proto",
    ],
)

genrule2(
    name = "gr-reviewassistant-static",
    srcs = [":gr-reviewassistant"],
    outs = ["gr-reviewassistant-static.jar"],
    cmd = " && ".join([
        "mkdir $$TMP/static",
        "cp $(locations :gr-reviewassistant) $$TMP/static",
        "cd $$TMP",
        "zip -Drq $$ROOT/$@ -g .",
    ]),
)

polygerrit_plugin(
    name = "gr-reviewassistant",
    app = "reviewassistant-bundle.js",
    plugin_name = "reviewassistant",
)

rollup_bundle(
    name = "reviewassistant-bundle",
    srcs = glob(["gr-reviewassistant/*.js"]),
    entry_point = "gr-reviewassistant/plugin.js",
    rollup_bin = "//tools/node_tools:rollup-bin",
    sourcemap = "hidden",
    deps = [
        "@tools_npm//rollup-plugin-node-resolve",
    ],
)