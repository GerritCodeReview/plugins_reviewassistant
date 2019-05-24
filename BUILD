load("//tools/bzl:plugin.bzl", "gerrit_plugin")

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
    resources = glob(["src/main/resources/**/*"]),
    deps = ["//java/com/google/gerrit/server/cache/serialize"],
)
