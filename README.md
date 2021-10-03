# AppCommonGradle

AppCommonGradle is a Gradle plugin that automates packaging of an [AppCommon](https://github.com/Yeregorix/AppCommon)
based application.

## Configuration

In the `app` section you must define the main class. You can also override the name, the title and the version. A
corresponding `application.json` file will be generated and included in the final jar.

## Dependency management

Two built-in dependency configurations are included:

- `appcommon` must include the AppCommon dependency that will be shaded in the final jar.
- `export` includes any potential dependency that will be downloaded at runtime by the application.

## Example

```groovy
plugins {
    id 'java-library'
    id 'net.smoofyuniverse.appcommon-gradle' version '1.0.1'
}

dependencies {
    appcommon 'net.smoofyuniverse:appcommon:1.3.1'
    export 'org.spongepowered:noise:2.0.0-SNAPSHOT'
}

app {
    application = "net.smoofyuniverse.chaos.Chaos"
}
```

