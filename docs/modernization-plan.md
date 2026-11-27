# Modernization plan

Follow-ups after the upgrade to Java 25, Kotlin 2.4.21 and Gradle 9.9.2.

## Tasks

- [ ] 1. **Fix publishing.** `build.gradle.kts` still deploys to `s01.oss.sonatype.org`, which Sonatype shut down (OSSRH sunset, mid-2025). Migrate to the Central Portal (new credentials, probably the `com.vanniktech.maven.publish` plugin or the Portal endpoint). Touches `publish-lib.yml` (secrets, `publishApple` task).
- [ ] 2. **Verify native targets.** Compile and test them on a macOS runner. Intel Apple targets (`macosX64`, `iosX64`, `tvosX64`, `watchosX64`) and possibly `watchosArm32` are deprecated or demoted in recent Kotlin; drop what no longer builds.
- [x] 3. **Upgrade JUnit.** Move from 5.14.0 to the latest stable (6.1.3; requires Java 17+).
- [x] 4. **Address Gradle deprecations.** The build reports "incompatible with Gradle 10"; find the source with `--warning-mode all`. Evaluate enabling the configuration cache.
- [ ] 5. **Centralize dependency management.** Add `gradle/libs.versions.toml` (versions are currently spread over `build.gradle.kts` and `buildSrc`). Add Renovate or Dependabot.
- [ ] 6. **Review `kotlin("reflect")` in `commonMain`.** Remove it if unused.
- [ ] 7. **Use mise in CI.** Replace `actions/setup-java` in `ci.yml` and `publish-lib.yml` with `jdx/mise-action`, so `mise.toml` is the single source of the Java version.
- [ ] 8. **Optional:** add a `wasmJs` target; convert `settings.gradle` to Kotlin DSL; replace `GlobalScope` in tests (delicate-API warnings).
