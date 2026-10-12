package com.zydra

import com.zydra.data.cracker.ShadowSingleTester
import com.zydra.domain.engine.BruteForceGenerator
import com.zydra.domain.model.CharsetCatalog
import com.zydra.domain.model.CharsetKey
import com.zydra.domain.model.CrackTarget
import com.zydra.domain.usecase.DetectTargetUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowLineParserTest {

    @Test
    fun testParseValidShadowLine() {
        val entry = ShadowSingleTester.parseLine("root:\$6\$salt\$hash:19850:0:99999:7:::")
        assertNotNull(entry)
        assertEquals("root", entry?.user)
        assertEquals("\$6\$salt\$hash", entry?.fullHash)
    }

    @Test
    fun testLockedAccountsAreSkipped() {
        assertNull(ShadowSingleTester.parseLine("daemon:*:19850:0:99999:7:::"))
        assertNull(ShadowSingleTester.parseLine("bin:!:19850:0:99999:7:::"))
    }

    @Test
    fun testMalformedLineReturnsNull() {
        assertNull(ShadowSingleTester.parseLine("no-colon-here"))
        assertNull(ShadowSingleTester.parseLine("user::"))
    }

    @Test
    fun testVerifySha512Crypt() {
        val salt = "\$6\$saltstring"
        val realHash = org.apache.commons.codec.digest.Sha2Crypt.sha512Crypt(
            "password123".toByteArray(),
            salt
        )
        val computed = org.apache.commons.codec.digest.Crypt.crypt("password123", realHash)
        assertEquals(realHash, computed)
        val wrong = org.apache.commons.codec.digest.Crypt.crypt("wrongpass", realHash)
        assertFalse(wrong == realHash)
    }
}

class DetectTargetTest {

    private val useCase = DetectTargetUseCase()

    @Test
    fun testDetectsAllFormats() {
        assertTrue(useCase.invoke("backup.zip") is CrackTarget.Zip)
        assertTrue(useCase.invoke("doc.PDF") is CrackTarget.Pdf)
        assertTrue(useCase.invoke("archive.rar") is CrackTarget.Rar)
        assertTrue(useCase.invoke("etc_shadow") is CrackTarget.Shadow)
        assertTrue(useCase.invoke("notes.txt") is CrackTarget.Unsupported)
    }
}

class CharsetCatalogTest {

    @Test
    fun testCharsForSingleKeys() {
        assertEquals(26, CharsetCatalog.charsFor(setOf(CharsetKey.LOWERCASE)).length)
        assertEquals(10, CharsetCatalog.charsFor(setOf(CharsetKey.DIGITS)).length)
        assertTrue(CharsetCatalog.charsFor(setOf(CharsetKey.SPACE)).contains(" "))
    }

    @Test
    fun testTotalCombinations() {
        val alphabet26 = "abcdefghijklmnopqrstuvwxyz"
        assertEquals(26L + 26L * 26L, CharsetCatalog.totalCombinations(alphabet26, 1, 2))
        assertEquals(10L, CharsetCatalog.totalCombinations("0123456789", 1, 1))
    }
}

class BruteForceGeneratorTest {

    @Test
    fun testGeneratesInOrder() {
        val generated = BruteForceGenerator("ab", 1, 2).generate().toList()
        assertEquals(listOf("a", "b", "aa", "ab", "ba", "bb"), generated)
    }

    @Test
    fun testSingleLength() {
        val generated = BruteForceGenerator("01", 2, 2).generate().toList()
        assertEquals(4, generated.size)
        assertTrue(generated.contains("10"))
    }
}
