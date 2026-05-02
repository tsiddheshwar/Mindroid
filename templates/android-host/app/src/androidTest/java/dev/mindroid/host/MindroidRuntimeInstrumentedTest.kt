package dev.mindroid.host

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MindroidRuntimeInstrumentedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun appContextPackageName() {
        assertEquals("dev.mindroid.host", context.packageName)
    }

    @Test
    fun deviceChannelReturnsInfo() {
        val channel = DeviceChannels()
        val result = channel.invoke("getInfo", null) as Map<*, *>
        assertNotNull(result["model"])
        assertNotNull(result["apiLevel"])
    }

    @Test
    fun fileChannelWriteAndReadRoundtrip() {
        val channel = FileChannels(context)
        val writeResult = channel.invoke("writeText", mapOf("path" to "test_rt.txt", "text" to "hello")) as Map<*, *>
        assertEquals(true, writeResult["ok"])

        val readResult = channel.invoke("readText", mapOf("path" to "test_rt.txt")) as Map<*, *>
        assertEquals("hello", readResult["text"])
    }

    @Test
    fun networkChannelReturnsState() {
        val channel = NetworkChannels(context)
        val result = channel.invoke("getState", null) as Map<*, *>
        assertNotNull(result["connected"])
        assertNotNull(result["transport"])
    }

    @Test
    fun cameraChannelReturnsAvailability() {
        val channel = CameraChannels(context)
        val result = channel.invoke("isAvailable", null) as Map<*, *>
        assertNotNull(result["available"])
    }
}
