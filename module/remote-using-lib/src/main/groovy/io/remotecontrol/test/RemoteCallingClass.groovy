package io.remotecontrol.test

import io.remotecontrol.groovy.client.RemoteControl

class RemoteCallingClass {
	RemoteControl remote

	RemoteCallingClass(RemoteControl remote) {
		this.remote = remote
	}

	int multiplyBy2OnRemote(int multiplied) {
		remote.exec { multiplied * 2 }
	}
}
