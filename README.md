## Documentation

- [Introduction](./doc/manual/src/chapters/010-intro.md)
- [Client Side](./doc/manual/src/chapters/020-client.md)
- [Server Side](./doc/manual/src/chapters/030-server.md)
- [Command Context](./doc/manual/src/chapters/040-contexts.md)
- [Transports](./doc/manual/src/chapters/050-transports.md)
- [History](./doc/manual/src/chapters/060-history.md)

## Build Instructions

This project uses [Gradle](http://www.gradle.org/ "Home - Gradle") to build. You can use the gradle wrapper in the project to build.

    ./gradlew test

For a complete build, test run and generation of the documentation:

    ./gradlew clean build includeManual
