package com.mparticle.kits

import android.content.Context
import com.foursquare.pilgrim.LogLevel
import com.foursquare.pilgrim.PilgrimSdk
import com.foursquare.pilgrim.PilgrimUserInfo
import com.mparticle.MParticle
import com.mparticle.consent.ConsentState
import com.mparticle.identity.MParticleUser
import com.mparticle.kits.KitIntegration.IdentityListener
import java.util.*

/**
 * This is the PilgrimSdk mParticle kit , used to extend the functionality of mParticle SDK and allow control of
 * the PilgimSdk; Mapping analogous public mParticle APIs into PilgrimSdk's API.
 *
 *
 * In addition to this file, you also will need to edit:
 * - ./build.gradle (as explained above)
 * - ./README.md
 */
class PilgrimKit : KitIntegration(), KitIntegration.UserAttributeListener, IdentityListener {
    override fun onKitCreate(
        settings: Map<String, String>,
        context: Context
    ): List<ReportingMessage> {
        val key = settings[SDK_KEY]
        require(!KitUtils.isEmpty(key)) { "PilgrimSdk key is empty." }
        val secret = settings[SDK_SECRET]
        require(!KitUtils.isEmpty(secret)) { "PilgrimSdk secret is empty." }

        val builder: PilgrimSdk.Builder = PilgrimSdk.Builder(context)
        if (key != null && secret != null) {
            builder
                .consumer(key, secret)
                .logLevel(LogLevel.DEBUG)
        }
        if (KitUtils.parseBooleanSetting(settings, SDK_ENABLE_PERSISTENT_LOGS, false)) {
            builder.enableDebugLogs()
        }

        // Configure with our starter
        PilgrimSdk.with(builder)
        val messageList = LinkedList<ReportingMessage>()
        // Can we add messages to track if initialized/started successfully in attributes
        messageList.add(
            ReportingMessage(
                this,
                ReportingMessage.MessageType.APP_STATE_TRANSITION,
                System.currentTimeMillis(),
                null
            )
        )
        return messageList
    }

    override fun getName(): String = KIT_NAME

    override fun setOptOut(optedOut: Boolean): List<ReportingMessage> {
        if (optedOut) {
            val messageList = LinkedList<ReportingMessage>()
            PilgrimSdk.stop(context)
            messageList.add(
                ReportingMessage(
                    this,
                    ReportingMessage.MessageType.OPT_OUT,
                    System.currentTimeMillis(),
                    null
                )
            )
            return messageList
        }
        return emptyList()
    }// can be null if it hasn't been set previously

    /**
     * Returns ether last set user information, or a new
     * PilgrimUserInfo instance.
     */
    private val userInfo: PilgrimUserInfo
        get() {
            var info: PilgrimUserInfo? = PilgrimSdk.get().userInfo
            // can be null if it hasn't been set previously
            if (info == null) {
                info = PilgrimUserInfo()
            }
            return info
        }

    /* Section: User attribute set */
    override fun onSetAllUserAttributes(
        attributes: Map<String, String>,
        attributeLists: Map<String, List<String>>,
        filteredMParticleUser: FilteredMParticleUser
    ) {
        // NOTE: attributeList not supported
        // Is this ID correct?
        updateUser(filteredMParticleUser)
        val info = userInfo
        for ((key, value) in attributes) {
            info[key] = value
        }
        PilgrimSdk.get().setUserInfo(info)
    }

    override fun onIncrementUserAttribute(
        s: String,
        i: Number,
        s1: String,
        filteredMParticleUser: FilteredMParticleUser
    ) {
        // Ignored,  Not supported atm
    }

    override fun onRemoveUserAttribute(s: String, filteredMParticleUser: FilteredMParticleUser) {
        val info = userInfo
        info.remove(s)
    }

    override fun onSetUserAttribute(
        s: String,
        o: Any,
        filteredMParticleUser: FilteredMParticleUser
    ) {
        val info = userInfo
        info[s] = o.toString()
        // update
        PilgrimSdk.get().setUserInfo(info)
    }

    override fun onSetUserTag(s: String, filteredMParticleUser: FilteredMParticleUser) {
        // Ignored, not supported atm.
    }

    override fun supportsAttributeLists(): Boolean {
        // We don't support attribute lists
        // the PilgrimSdk currently only
        // allows single k => value attributes.
        return false
    }

    override fun onSetUserAttributeList(
        s: String,
        list: List<String>,
        filteredMParticleUser: FilteredMParticleUser
    ) {
        // Ignored, not supported
    }

    override fun onConsentStateUpdated(
        consentState: ConsentState,
        consentState1: ConsentState,
        filteredMParticleUser: FilteredMParticleUser
    ) {
        // Ignore?
    }

    override fun onIdentifyCompleted(
        mParticleUser: MParticleUser,
        filteredIdentityApiRequest: FilteredIdentityApiRequest
    ) {
        updateUser(mParticleUser)
    }

    override fun onLoginCompleted(
        mParticleUser: MParticleUser,
        filteredIdentityApiRequest: FilteredIdentityApiRequest
    ) {
        updateUser(mParticleUser)
    }

    override fun onModifyCompleted(
        mParticleUser: MParticleUser,
        filteredIdentityApiRequest: FilteredIdentityApiRequest
    ) {
        updateUser(mParticleUser)
    }

    private fun updateUser(mParticleUser: MParticleUser) {
        val info = userInfo
        val mParticleUserId = mParticleUser.id.toString()
        // only update if it's not null
        val customerId = mParticleUser.userIdentities[MParticle.IdentityType.CustomerId]
        if (customerId != null) {
            // only update if it's not null
            info.setUserId(customerId)
        }
        info[MPARTILE_USER_ID] = mParticleUserId
        PilgrimSdk.get().setUserInfo(info)
    }

    override fun onUserIdentified(mParticleUser: MParticleUser) {
        // ignored, not used
    }

    override fun onLogoutCompleted(
        mParticleUser: MParticleUser,
        filteredIdentityApiRequest: FilteredIdentityApiRequest
    ) {
        // ignored, not used
    }

    companion object {
        /** Name of the kit*/
        private const val KIT_NAME = "PilgrimKit"

        /** Key used to get the Pilgrim Sdk api key from mParticle's settings response*/
        const val SDK_KEY = "pilgrim_sdk_key"

        /** Key used to get the Pilgrim Sdk secret from mParticle's settings response*/
        const val SDK_SECRET = "pilgrim_sdk_secret"

        /** Key used to get the Pilgrim Sdk enable  configuration enabling flag from mParticle's settings response*/
        private const val SDK_ENABLE_PERSISTENT_LOGS = "pilgrim_persistent_logs"
        private const val MPARTILE_USER_ID = "mParticleUserId"
    }
}
