package me.vkarmane.sdksample

import android.content.Context
import ru.tinkoff.vkarmane.sdk.Keys
import ru.tinkoff.vkarmane.sdk.VKarmaneSDK

/**
 * @author Sergei Solodkov
 */
class KeysManager(
    context: Context
) {

    private companion object {
        const val PRIVATE_KEY = "PRIVATE_KEY"
    }

    private val prefs = context.getSharedPreferences("KEYS", Context.MODE_PRIVATE)

    fun getPublicKey(): String {
        val keys = generateKeys()
        return keys.publicKey
    }

    private fun generateKeys(): Keys {
        prefs.edit().clear().apply()
        val keys = VKarmaneSDK.generateKeys()
        prefs.edit().putString(PRIVATE_KEY, keys.privateKey).apply()
        return keys
    }

    fun getPrivateKey(): String = prefs.getString(PRIVATE_KEY, "")!!

}