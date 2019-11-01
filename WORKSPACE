workspace(name = "reviewassistant")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "e1360b49f90930e280f6a43380ab533705c85446",
    #local_path = "/home/<user>/projects/bazlets",
)

# Polymer dependencies
load(
    "@com_googlesource_gerrit_bazlets//:gerrit_polymer.bzl",
    "gerrit_polymer",
)

gerrit_polymer()

# Load closure compiler with transitive dependencies
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_repositories")

closure_repositories()

# Load Gerrit npm_binary toolchain
load("@com_googlesource_gerrit_bazlets//tools:js.bzl", "GERRIT", "npm_binary")

npm_binary(
    name = "polymer-bundler",
    repository = GERRIT,
)

npm_binary(
    name = "crisper",
    repository = GERRIT,
)

# Snapshot Plugin API
#load(
#    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
#    "gerrit_api_maven_local",
#)

# Load snapshot Plugin API
#gerrit_api_maven_local()

# Release Plugin API
load(
    "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
    "gerrit_api",
)

# Load release Plugin API
gerrit_api()

# Protobuf rules support
http_archive(
    name = "rules_proto",
    sha256 = "e1360b49f90930e280f6a43380ab533705c85446366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-e1360b49f90930e280f6a43380ab533705c85446",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/e1360b49f90930e280f6a43380ab533705c85446.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/e1360b49f90930e280f6a43380ab533705c85446.tar.gz",
    ],
)
