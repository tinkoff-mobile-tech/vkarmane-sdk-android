/*
 * Copyright © 2019 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.vkarmane.sdk

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import java.lang.Exception
import java.lang.RuntimeException

/**
 *  @author Sergei Solodkov
 */
object VKarmaneSDK {

    private const val SUPPORTED_VERSION_VKARMANE = 149
    private const val PACKAGE_NAME = "me.vkarmane"
    private const val ERROR_CODE_PARAM = "code"
    private const val DATA_PARAM = "data"
    private const val SESSION_KEY_PARAM = "sessionKey"

    private val cipherManager = CipherManager()

    fun isAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            val info = pm.getPackageInfo(PACKAGE_NAME, 0)
            info.versionCode >= SUPPORTED_VERSION_VKARMANE
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getDocumentsLinkBuilder(): GetDocumentsLinkBuilder {
        return GetDocumentsLinkBuilder()
    }

    fun getJsonFromLink(data: Uri, privateKey: String): String {
        data.getQueryParameter(ERROR_CODE_PARAM)?.let { code ->
            throw VkarmaneSdkException(VKarmaneSDKError.values().first { it.code.toString() == code })
        }
        try {
            val encryptedSessionKey = data.getQueryParameter(SESSION_KEY_PARAM) ?: throw IllegalStateException("Session key not found")
            val sessionKey = cipherManager.decryptDataRSA(Uri.decode(encryptedSessionKey), privateKey)
            val encryptedData = data.getQueryParameter(DATA_PARAM) ?: throw IllegalStateException("Document data not found")
            return cipherManager.decryptDataAES(Uri.decode(encryptedData), sessionKey)
        } catch (e: Exception) {
            if (cipherManager.isDecryptionException(e)) {
                throw VkarmaneSdkException(VKarmaneSDKError.DECRYPTION_ERROR)
            }
            throw e
        }
    }

    fun generateKeys(): Keys = cipherManager.generateKeys()

    fun getErrorFromLink(data: Uri): VKarmaneSDKError {
        val errorCode = data.getQueryParameter(ERROR_CODE_PARAM) ?: throw IllegalArgumentException("Error code not found")
        return VKarmaneSDKError.values().first { it.code.toString() == errorCode }
    }

    class GetDocumentsLinkBuilder {

        private companion object {
            const val VKARMANE_SCHEME = "vkarmaneapp"
            const val VKARMANE_AUTHORITY = "x-callback-url"
            const val VKARMANE_VERSION = "v3"
            const val VKARMANE_ACTION = "get_documents"

            const val X_SOURCE_PARAM = "x-source"
            const val X_SUCCESS_PARAM = "x-success"
            const val X_ERROR_PARAM = "x-error"
            const val X_CANCEL_PARAM = "x-cancel"
            const val KINDS_PARAM = "kinds"
            const val MULTICHOICE_PARAM = "isMultichoice"
            const val PUBLIC_KEY_PARAM = "publicKey"
        }

        private var sourceName: String? = null
        private var successLink: String? = null
        private var errorLink: String? = null
        private var cancelLink: String? = null
        private var publicKey: String? = null
        private var kinds: List<DocumentKind>? = null
        private var isMultiChoiceEnabled = false

        fun setXSource(sourceName: String): GetDocumentsLinkBuilder {
            this.sourceName = sourceName
            return this
        }

        fun setXSuccessLink(successLink: String): GetDocumentsLinkBuilder {
            this.successLink = successLink
            return this
        }

        fun setXErrorLink(errorLink: String): GetDocumentsLinkBuilder {
            this.errorLink = errorLink
            return this
        }

        fun setXCancelLink(cancelLink: String): GetDocumentsLinkBuilder {
            this.cancelLink = cancelLink
            return this
        }

        fun setKinds(kinds: List<DocumentKind>): GetDocumentsLinkBuilder {
            this.kinds = kinds
            return this
        }

        fun setMultiChoice(isMultiChoiceEnabled: Boolean): GetDocumentsLinkBuilder {
            this.isMultiChoiceEnabled = isMultiChoiceEnabled
            return this
        }

        fun setPublicKey(publicKey: String): GetDocumentsLinkBuilder {
            this.publicKey = publicKey
            return this
        }

        fun build(): String {
            ensureDataValid()
            return Uri.Builder()
                .scheme(VKARMANE_SCHEME)
                .authority(VKARMANE_AUTHORITY)
                .appendPath(Uri.encode(VKARMANE_VERSION))
                .appendPath(Uri.encode(VKARMANE_ACTION))
                .appendQueryParameter(X_SOURCE_PARAM, Uri.encode(sourceName))
                .appendQueryParameter(X_SUCCESS_PARAM, Uri.encode(successLink))
                .appendQueryParameter(X_ERROR_PARAM, Uri.encode(errorLink))
                .appendQueryParameter(X_CANCEL_PARAM, Uri.encode(cancelLink))
                .appendQueryParameter(MULTICHOICE_PARAM, Uri.encode(isMultiChoiceEnabled.toString()))
                .appendQueryParameter(KINDS_PARAM, Uri.encode(kinds!!.joinToString(separator = ",") { it.name }))
                .appendQueryParameter(PUBLIC_KEY_PARAM, Uri.encode(publicKey))
                .build()
                .toString()
        }

        private fun ensureDataValid() {
            val sb = StringBuilder("You have to provide:")
            var isErrorState = false
            if (sourceName == null) {
                sb.append(" Source Name,")
                isErrorState = true
            }
            if (successLink == null) {
                sb.append(" Success link,")
                isErrorState = true
            }
            if (errorLink == null) {
                sb.append(" Error link,")
                isErrorState = true
            }
            if (cancelLink == null) {
                sb.append(" Cancel link,")
                isErrorState = true
            }
            if (kinds == null) {
                sb.append(" Document kinds,")
                isErrorState = true
            }
            if (publicKey == null) {
                sb.append(" Public key,")
                isErrorState = true
            }
            if (isErrorState) {
                throw IllegalStateException(sb.removeSuffix(",").toString())
            }
        }

    }

}

class VkarmaneSdkException(val vKarmaneSDKError: VKarmaneSDKError): RuntimeException()