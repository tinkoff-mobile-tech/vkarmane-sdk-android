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

import android.annotation.SuppressLint
import android.util.Base64
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @author Sergei Solodkov
 */
internal class CipherManager {

    private companion object {
        const val RSA_ALGORITHM = "RSA"
        const val AES_ALGORITHM = "AES/ECB/PKCS5Padding"
        const val KEY_LENGTH = 2048
    }

    @SuppressLint("TrulyRandom")
    fun generateKeys(): Keys {
        val generator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        generator.initialize(KEY_LENGTH)
        val keyPair = generator.genKeyPair()
        return Keys(
            publicKey = keyPair.public.encoded.encode(),
            privateKey = keyPair.private.encoded.encode()
        )
    }

    fun decryptDataRSA(encryptedData: String, privateKey: String): String {
        val spec = PKCS8EncodedKeySpec(privateKey.decode())
        val keySpec = KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(spec)
        val cipher = Cipher.getInstance(RSA_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, keySpec)
        }
        val decryptedData = cipher.doFinal(encryptedData.decode())
        return Base64.encodeToString(decryptedData, Base64.DEFAULT)
    }

    @SuppressLint("GetInstance")
    fun decryptDataAES(encryptedData: String, sessionKey: String): String {
        val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(sessionKey.decode(), AES_ALGORITHM))
        }
        val decryptedData = cipher.doFinal(encryptedData.decode())
        return String(decryptedData)
    }

    fun isDecryptionException(e: Exception): Boolean {
        return e is GeneralSecurityException || e is InvalidKeyException || e is IllegalArgumentException
    }

    private fun String.decode(): ByteArray {
        return Base64.decode(this, Base64.DEFAULT)
    }

    private fun ByteArray.encode(): String {
        return Base64.encodeToString(this, Base64.DEFAULT)
    }

}