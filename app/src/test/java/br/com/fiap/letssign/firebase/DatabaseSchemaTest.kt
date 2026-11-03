package br.com.fiap.letssign.firebase

import br.com.fiap.letssign.repository.RemotePaths
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseSchemaTest {
    @Test
    fun `file keys never contain dots`() {
        assertEquals("d1/original_pdf", DatabaseSchema.fileKey(RemotePaths.original("d1")))
        assertEquals("d1/signatures/s1_png", DatabaseSchema.fileKey(RemotePaths.signature("d1", "s1")))
        assertEquals("d1/final_pdf", DatabaseSchema.fileKey(RemotePaths.final("d1")))
    }
}
