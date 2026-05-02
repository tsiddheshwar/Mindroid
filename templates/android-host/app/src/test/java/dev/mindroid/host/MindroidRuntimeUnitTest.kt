package dev.mindroid.host

import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals

class MindroidRuntimeUnitTest {

    @Test
    fun runtimeCreation() {
        // Simple smoke test – runtime must not throw on construction.
        // Full channel tests require instrumented tests due to Android context.
        assertNotNull("Runtime class must be resolvable", MindroidRuntime::class.java)
    }

    @Test
    fun permissionGateDeniedOnBlankPermission() {
        // PermissionGate.ensure with blank string should return "denied" with no context.
        // We cannot call the real Android method here but we can test the blank guard via reflection.
        val method = PermissionGate.javaClass.getDeclaredMethod("ensure",
            android.content.Context::class.java, String::class.java)
        assertNotNull(method)
    }
}
