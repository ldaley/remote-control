# Transports

This section discusses the available transports.

## Local

The [local transport][localtransport-api] is used for executing commands in the same virtual machine. As such, it does not have a lot of practical use.

## HTTP

The HTTP transport module provides a [`Transport`][transport-api] implementation for http, [HttpTransport][httptransport-api].

It also provides a [Jakarta servlet][jakarta-httpservlet-api] and [Javax servlet][javax-httpservlet-api] for receiving commands, as well as a [handler][handler] for use with the [com.sun.net.httpserver](http://download.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html) package.

> The HTTP transport classes do not provide any kind of authentication/authorisation mechanism. The classes do however provide sufficient hooks for subclasses to implement this functionality. If you are going to deploy this in a publically accessible application, you are going to want to add some kind of authentication/authorisation.
