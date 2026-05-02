package dev.mindroid.host

object NativeBridge {
    init {
        System.loadLibrary("mindroidnative")
    }

    external fun runtimeInfo(): String
}
