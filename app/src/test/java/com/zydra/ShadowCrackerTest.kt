package com.zydra

import com.zydra.cracker.ShadowCracker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowCrackerTest {

    private val cracker = ShadowCracker()

    @Test
    fun testVerifySha512() {
        val salt = "\$6\$saltstring"
        val realHash = org.apache.commons.codec.digest.Sha2Crypt.sha512Crypt("password123".toByteArray(), salt)

        assertTrue("Password harus cocok dengan hash", cracker.verifyPassword("password123", realHash))
        assertFalse("Password salah tidak boleh cocok", cracker.verifyPassword("wrongpass", realHash))
    }

    @Test
    fun testVerifySha256() {
        val salt = "\$5\$randomsalt"
        val realHash = org.apache.commons.codec.digest.Sha2Crypt.sha256Crypt("admin".toByteArray(), salt)

        assertTrue("Admin password harus cocok", cracker.verifyPassword("admin", realHash))
    }

    @Test
    fun testParseShadowLine() {
        val line = "root:\$6\$salt\$hash:19850:0:99999:7:::"
        val entry = cracker.parseShadowLine(line)

        assertTrue(entry != null)
        assertTrue(entry?.user == "root")
        assertTrue(entry?.fullHash == "\$6\$salt\$hash")
    }
}
