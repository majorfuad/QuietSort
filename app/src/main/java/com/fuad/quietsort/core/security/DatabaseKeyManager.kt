package com.fuad.quietsort.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Creates a random SQLCipher passphrase and wraps it with a non-exportable
 * Android Keystore key. The wrapped secret lives under noBackupFilesDir.
 *
 * Keystore invalidation / missing wrapped secret must fail closed: never
 * generate a replacement key for an existing encrypted database.
 */
class DatabaseKeyManager(private val context: Context) {
    private val alias = "quietsort_db_wrap_v1"
    private val secretFile = AtomicFile(context.noBackupFilesDir.resolve("db_passphrase_v1.bin"))

    @Synchronized
    fun getOrCreatePassphrase(databaseExists: Boolean): ByteArray {
        val file = secretFile.baseFile
        if (file.exists()) {
            val wrapped = secretFile.openRead().use { it.readBytes() }
            require(wrapped.size > 12) { "Invalid wrapped database key" }
            val iv = wrapped.copyOfRange(0, 12)
            val ciphertext = wrapped.copyOfRange(12, wrapped.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(128, iv))
            return cipher.doFinal(ciphertext)
        }
        check(!databaseExists) { "Database exists but its encrypted key is missing" }
        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey())
        val wrapped = cipher.iv + cipher.doFinal(passphrase)
        val out = secretFile.startWrite()
        try {
            out.write(wrapped)
            secretFile.finishWrite(out)
        } catch (error: Exception) {
            secretFile.failWrite(out)
            passphrase.fill(0)
            throw error
        }
        return passphrase
    }

    private fun keystoreKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
