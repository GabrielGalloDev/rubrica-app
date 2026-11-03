package br.com.fiap.letssign.utils

import br.com.fiap.letssign.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {
    @Test
    fun `name must be a full name`() {
        assertEquals(R.string.error_name_required, Validators.validateName(" "))
        assertEquals(R.string.error_name_full, Validators.validateName("Maria"))
        assertNull(Validators.validateName("Maria Silva"))
    }

    @Test
    fun `email must be present and well formed`() {
        assertEquals(R.string.error_email_required, Validators.validateEmail("   "))
        assertEquals(R.string.error_email_invalid, Validators.validateEmail("not-an-email"))
        assertEquals(R.string.error_email_invalid, Validators.validateEmail("user@domain"))
        assertNull(Validators.validateEmail("user@domain.com"))
    }

    @Test
    fun `phone needs area code`() {
        assertEquals(R.string.error_phone_required, Validators.validatePhone(""))
        assertEquals(R.string.error_phone_invalid, Validators.validatePhone("99999-0000"))
        assertNull(Validators.validatePhone("(11) 99999-0000"))
    }

    @Test
    fun `password has a minimum length`() {
        assertEquals(R.string.error_password_required, Validators.validatePassword(""))
        assertEquals(R.string.error_password_too_short, Validators.validatePassword("12345"))
        assertNull(Validators.validatePassword("123456"))
    }

    @Test
    fun `optional password accepts empty but not short values`() {
        assertNull(Validators.validateOptionalPassword(""))
        assertEquals(R.string.error_password_too_short, Validators.validateOptionalPassword("123"))
    }

    @Test
    fun `confirmation must match the password`() {
        assertEquals(R.string.error_password_mismatch, Validators.validatePasswordConfirmation("secret1", "secret2"))
        assertEquals(R.string.error_password_required, Validators.validatePasswordConfirmation("secret1", ""))
        assertNull(Validators.validatePasswordConfirmation("secret1", "secret1"))
    }
}
