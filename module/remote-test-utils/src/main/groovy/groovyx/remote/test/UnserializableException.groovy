package groovyx.remote.test

class UnserializableException extends Exception {
    UnserializableThing thing = new UnserializableThing()
}

class UnserializableThing {

}

