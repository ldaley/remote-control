# History

This page lists the high level changes between versions of Groovy Remote Control.

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