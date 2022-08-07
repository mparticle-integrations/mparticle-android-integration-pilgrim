package com.mparticle.kits

import android.content.Context
import com.mparticle.MParticle
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

//@RunWith(PowerMockRunner.class)
class PilgrimKitTests {
    private val kit: KitIntegration
         get() {
            val kit: KitIntegration = PilgrimKit()
            try {
                kit.configuration =
                    KitConfiguration().parseConfiguration(
                        JSONObject().put(
                            "id",
                            MParticle.ServiceProviders.PILGRIM
                        )
                    )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return kit
        }

    @Test
    fun testGetName() {
        val name = kit.name
        Assert.assertTrue(!name.isNullOrEmpty())
    }

    /**
     * Kit *should* throw an exception when they're initialized with the wrong settings.
     */
    @Test
    fun testOnKitCreate() {
        var e: Exception? = null
        try {
            val kit = kit
            val settings = HashMap<String, String>()
            settings["fake setting"] = "fake"
            kit.onKitCreate(settings, Mockito.mock(Context::class.java))
        } catch (ex: Exception) {
            e = ex
        }
        Assert.assertNotNull(e)
    }

    @Test
    fun testClassName() {
        val factory = KitIntegrationFactory()
        val integrations = factory.knownIntegrations
        val className = kit.javaClass.name
        for (integration in integrations) {
            if (integration.value == className) {
                return
            }
        }
        Assert.fail("$className not found as a known integration.")
    }

    @Test //    @PrepareForTest({PilgrimSdk.class, Result.class})
    @Throws(Exception::class)
    fun testCorrectInitialization() {
//        PowerMockito.mockStatic(Result.class);
//        PowerMockito.doNothing().when(Result.class, "getCurrentLocation");
//        PowerMockito.mockStatic(PilgrimSdk.class);
//        PowerMockito.doNothing().when(PilgrimSdk.class, "with", Mockito.any(PilgrimSdk.Builder.class));
        val kit = kit
        val settings = HashMap<String, String>()
        settings[PilgrimKit.SDK_KEY] = "MyKey"
        settings[PilgrimKit.SDK_SECRET] = "MySuperSecretSecret"
        try {
            val messageList = kit.onKitCreate(
                settings, Mockito.mock(
                    Context::class.java
                )
            )
            // We did pass one
            Assert.assertTrue(
                "No messages were returned when initializing app",
                messageList.size > 0
            )
            var appStateMessageFound = false
            for (i in messageList.indices) {
                val msg = messageList[i]
                if (msg.toJson()
                        .getString("dt") == ReportingMessage.MessageType.APP_STATE_TRANSITION
                ) {
                    appStateMessageFound = true
                }
            }
            Assert.assertTrue("Could not find APP_STATE_STRANSITION message", appStateMessageFound)
        } catch (e: Exception) {
            Assert.fail(e.cause.toString())
        }
    }
}
