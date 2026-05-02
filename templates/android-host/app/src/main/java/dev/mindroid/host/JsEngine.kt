package dev.mindroid.host

/**
 * Contract every JS engine implementation must satisfy.
 * Swap between Hermes, JSC, or QuickJS by providing a different Engine implementation.
 */
interface JsEngine {
    /**
     * Load and execute a JavaScript bundle from [source].
     * Throws on syntax/runtime errors.
     */
    fun executeBundle(source: String)

    /**
     * Call a top-level JS function by [name] with [argsJson].
     * Returns JSON-encoded result or null.
     */
    fun callFunction(name: String, argsJson: String = "[]"): String?

    /**
     * Register a native callable under [name] in the JS global scope.
     * The [handler] is invoked with JSON-encoded arguments and must return JSON.
     */
    fun registerNativeFunction(name: String, handler: (argsJson: String) -> String)

    /**
     * Tear down the engine. Must be called when the host activity is destroyed.
     */
    fun destroy()
}

/**
 * Default Hermes-backed engine, compiled as part of the app via the Hermes C library.
 * In a real build, this delegates to the hermes-engine AAR.
 * This stub allows the project to compile and the interface to be fully defined.
 */
class HermesEngine : JsEngine {
    private val nativeFunctions = mutableMapOf<String, (String) -> String>()
    private var initialized = false

    override fun executeBundle(source: String) {
        initialized = true
        // TODO: delegate to HermesRuntime.executeBundle(source) once the hermes-engine
        // AAR dependency is added to build.gradle.kts.
    }

    override fun callFunction(name: String, argsJson: String): String? {
        if (!initialized) {
            return null
        }
        // TODO: delegate to HermesRuntime.callFunction(name, argsJson)
        return null
    }

    override fun registerNativeFunction(name: String, handler: (argsJson: String) -> String) {
        nativeFunctions[name] = handler
        // TODO: expose via HermesRuntime.registerNativeFunction(name, handler)
    }

    override fun destroy() {
        nativeFunctions.clear()
        initialized = false
        // TODO: HermesRuntime.destroy()
    }
}

/**
 * Factory. Returns a [HermesEngine] by default.
 * Swap to QuickJsEngine or V8Engine at compile time by changing this function.
 */
fun createJsEngine(): JsEngine = HermesEngine()
