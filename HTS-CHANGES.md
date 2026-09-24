# HTS-ASPM changes to Tai-e

This is HTS-ASPM's fork of [pascal-lab/Tai-e](https://github.com/pascal-lab/Tai-e),
used by the HTSOne mobile deep scan (Android taint analysis). It stays as close to
upstream as possible: `master` merges upstream, plus the patches below. Tai-e is
licensed under the LGPL-3.0 (`COPYING.LESSER`, `COPYING`), and so is every change here.

Releases are tags `v<upstream version>-hts.<n>`. The `HTS release` workflow
(`.github/workflows/hts-release.yml`) builds the fat jar from the tag and publishes
it with its SHA-256.

## Patches

| Commit | Change | Why |
|---|---|---|
| `f5f181da` | Soot frontend: map an annotation's `void.class` literal (class_info `V`) to `void` | Building the world aborted with "Invalid bytecode type descriptor: V" on real Android apps (Element 40106624). The Java frontend already handles this case. |

## Syncing with upstream

```sh
git fetch upstream
git merge upstream/master
```
