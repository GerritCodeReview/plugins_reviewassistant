load("//tools/bzl:plugin.bzl", "gerrit_plugin")
load("//tools/bzl:genrule2.bzl", "genrule2")
load("//tools/bzl:js.bzl", "polygerrit_plugin")

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
)

genrule2(
    name = "gr-reviewassistant-static",
    srcs = [":gr-reviewassistant"],
    outs = ["gr-reviewassistant-static.jar"],
    cmd = " && ".join([
        "mkdir $$TMP/static",
        "cp -r $(locations :gr-reviewassistant) $$TMP/static",
        "cd $$TMP",
        "zip -Drq $$ROOT/$@ -g .",
    ]),
)

polygerrit_plugin(
    name = "gr-reviewassistant",
    srcs = glob([
        "gr-reviewassistant/*.html",
        "gr-reviewassistant/*.js",
    ]),
    app = "plugin.html",
)

