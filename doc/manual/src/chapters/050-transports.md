# Transports

This section discusses the available transports.

## Local

The [local transport][localtransport-api] is used for executing commands in the same virtual machine. As such, it does not have a lot of practical use.

## HTTP

The HTTP transport module provides a [`Transport`][transport-api] implementation for http, [HttpTransport][httptransport-api].

It also provides a [servlet][httpservlet-api] for receiving commands, and a [handler](groovy-api/remote-transport-http/groovyx/remote/transport/http/RemoteControlHttpHandler.html) for use with the [com.sun.net.httpserver](http://download.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html) package.