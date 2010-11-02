Please see [http://groovy.codehaus.org/modules/remote](http://groovy.codehaus.org/modules/remote "Groovy Remote Control") for information.

## Build Instructions

This project uses [Gradle](http://www.gradle.org/ "Home - Gradle") to build. You can use the gradle wrapper in the project to build.

    ./gradlew test

### Git Submodules

This project will not build without initialising submodules. After you have cloned and checked out a branch, you must run

    git submodule init

And…

    git submodule update