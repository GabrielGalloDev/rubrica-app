package br.com.fiap.letssign.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PBKDF2 password hash with a random salt (passwords are never stored in plain text). */
object PasswordHasher {
    private const val ITERATIONS = 60_000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun hash(password: String): String {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        return "${encode(salt)}:${encode(derive(password, salt))}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val expected = Base64.decode(parts[1], Base64.NO_WRAP)
        val actual = derive(password, Base64.decode(parts[0], Base64.NO_WRAP))
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(password: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance(ALGORITHM)
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH))
            .encoded

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
