package groovyx.remote.test

import groovyx.remote.client.RemoteControl

class RemoteCallingClass {
	RemoteControl remote

	RemoteCallingClass(RemoteControl remote) {
		this.remote = remote
	}

	int multiplyBy2OnRemote(int multiplied) {
		remote.exec { multiplied * 2 }
	}
}
