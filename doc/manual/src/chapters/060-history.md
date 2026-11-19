# History

This page lists the high level changes between versions of Groovy Remote Control.

## 0.8

### Breaking Changes

* Upgrade to Groovy 3.0.11
  + Replaced calls to obsolete Groovy methods (e.g. DefaultGroovyMethods.getBytes())
* Upgrade to Java 17
  + Removed specific usage of URLClassLoader.  Starting with Java 9, the default system classloader is no longer a URLClassLoader.  Library now utilizes core classloader functionality to find and load inner closures.
  + Disabled by default in Java 17, reflection must be enabled on the remote server.  This can be done by specifying the option *--add-opens=java.base/java.lang=ALL-UNNAMED*.  See [Migrating From JDK8 to Later JDK Releases](https://docs.oracle.com/en/java/javase/16/migrate/migrating-jdk-8-later-jdk-releases.html#GUID-7744EF96-5899-4FB2-B34E-86D49B2E89B6) for more information.

### Enhancements

* Provided both Javax and Jakarta implementations of the abstract class RemoteControlServlet.  Subclass the appropriate implementation as needed for your application.

## 0.6

### Breaking Changes

* Upgrade to Groovy 2

## 0.5

### New features

* New `usedClosures` option of `Remote.exec()` which enables passing additional closures to the server side

### Fixes

* Rewrite exceptions into Java to avoid problem with Java 7
* Closures from classes using `RemoteControl` added to classpath from jar files are properly retrieved and sent over the wire

## 0.4

Unreleased.

## 0.3

### Fixes 

* Correctly propagate exceptions with causes

## 0.2

### Breaking Changes

* Added specific serialVersionUID values to all classes that go across the wire

### Fixes 

* Support classpath entries containing spaces
* Don't error when classpath contains a non existent file
* Don't error when classpath contains non file entries

## 0.1

**Initial Public Release**
