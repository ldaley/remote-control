# Introduction

The Groovy Remote Control library facilitates executing closures that are defined in one application (client) being executed in another, potentially remote, system (server). This works by transmitting the closure class definition from the client to the server and then executing it. This means that the server has no prior knowledge of the closure. The library provides an API for plugging different transport mechanisms (such as http) to communicate between the client and server.

The name “Remote Control” is used as an analogy with a remote controller device such as a television remote.

## Uses

The original driver for developing this library was to assist in functionally testing [Grails](http://grails.org/ "Grails - The search is over.") applications (the plugin for Grails can be found [here](http://grails.org/plugin/remote-control)). It's use there is to allow the functional tests to define how test data is to be loaded and any other fixtures/mocks setup inside the application under test which may be remote.

However, this could be used as a kind of remote method invocation mechanism, or just for distributed systems generally.

## Example

To demonstrate what it looks like, here is an example using the http transport and the provided embedded http server based on the standard [com.sun.net.httpserver.HttpServer](http://download.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html "HttpServer (Java HTTP Server)")

Server side…

    import com.sun.net.httpserver.HttpServer
    import io.remotecontrol.transport.http.RemoteControlHttpHandler
    import io.remotecontrol.server.Receiver
    
    // the receiver of remote commands
    def receiver = new Receiver()
    
    // our handler (specific to the httpserver package)
    def handler = new RemoteControlHttpHandler(receiver)
    
    def server = HttpServer.create(new InetSocketAddress(8080), 0)
    server.createContext("/groovy-remote-control", handler)
    server.start()
    
Client side…

    import io.remotecontrol.client.RemoteControl
    import io.remotecontrol.transport.http.HttpTransport
    
    // defines how to talk to the server
    def transport = new HttpTransport("http://localhost:8080/groovy-remote-control")
    
    // our agent for remote execution
    def remote = new RemoteControl(transport)
    
    
    // Do some math on the server
    // The return value of the closure is returned to the client
    assert remote.exec { 1 + 1 } == 2
    
    // Multiple closures can be executed in a chain, with the result of the previous passed to the next
    assert remote.exec({ 1 }, { it + 1 }, { it + 1 }) == 3

These are just some examples of what can be achieved, and not a complete listing.

## Installation

> The current version of Groovy Remote Control is **0.8**

To get up and running you need the `remote-core` jar (available from Maven central) and a transport implementation. 

Via `@Grab`…
    
    @Grab("org.codehaus.groovy.module.remote:remote-transport-http:latest.release")

Via Maven…

    <dependency>
      <groupId>org.codehaus.groovy.module.remote</groupId>
      <artifactId>remote-transport-http</artifactId>
      <version>RELEASE</version>
    </dependency>
    
Via Gradle…

    compile "org.codehaus.groovy.remote:remote-transport-http:latest.release"

> Development snapshots are available via the [Codehaus snapshot repository](http://snapshots.repository.codehaus.org/)

### Groovy Version

This library requires version 3.0.11 or greater of Groovy on both the server and client side.

### Java Version

This library requires Java version 17 or greater on both the server and client side.
