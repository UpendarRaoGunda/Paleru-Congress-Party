package com.paleru.congress.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DevicePinSecurityTest {
    @Test
    fun validationAcceptsOnlyFourToSixAsciiDigits() {
        assertEquals(DevicePinSecurity.Validation.VALID, DevicePinSecurity.validate("0123"))
        assertEquals(DevicePinSecurity.Validation.VALID, DevicePinSecurity.validate("123456"))
        assertEquals(DevicePinSecurity.Validation.EMPTY, DevicePinSecurity.validate(""))
        assertEquals(
            DevicePinSecurity.Validation.INVALID_LENGTH,
            DevicePinSecurity.validate("123")
        )
        assertEquals(
            DevicePinSecurity.Validation.INVALID_LENGTH,
            DevicePinSecurity.validate("1234567")
        )
        assertEquals(
            DevicePinSecurity.Validation.NON_ASCII_DIGIT,
            DevicePinSecurity.validate("\u0C67\u0C68\u0C69\u0C6A")
        )
        assertEquals(
            DevicePinSecurity.Validation.NON_ASCII_DIGIT,
            DevicePinSecurity.validate("12 34")
        )
    }

    @Test
    fun saltedHashIsDeterministicAndConstantTimeMatcherChecksIt() {
        val salt = ByteArray(DevicePinSecurity.SALT_SIZE_BYTES) { it.toByte() }
        val hash = DevicePinSecurity.hash("0426", salt, rounds = 2)

        assertArrayEquals(hash, DevicePinSecurity.hash("0426", salt, rounds = 2))
        assertTrue(DevicePinSecurity.matches("0426", salt, hash, rounds = 2))
        assertFalse(DevicePinSecurity.matches("0427", salt, hash, rounds = 2))
    }

    @Test
    fun differentSaltsProduceDifferentHashes() {
        val firstSalt = ByteArray(DevicePinSecurity.SALT_SIZE_BYTES) { 1 }
        val secondSalt = ByteArray(DevicePinSecurity.SALT_SIZE_BYTES) { 2 }

        val firstHash = DevicePinSecurity.hash("1234", firstSalt, rounds = 2)
        val secondHash = DevicePinSecurity.hash("1234", secondSalt, rounds = 2)

        assertNotEquals(DevicePinSecurity.toHex(firstHash), DevicePinSecurity.toHex(secondHash))
    }

    @Test
    fun hexRoundTripIsStableAndMalformedInputIsRejected() {
        val bytes = byteArrayOf(0, 1, 15, 16, 127, -1)
        val encoded = DevicePinSecurity.toHex(bytes)

        assertEquals("00010f107fff", encoded)
        assertArrayEquals(bytes, DevicePinSecurity.fromHexOrNull(encoded))
        assertEquals(null, DevicePinSecurity.fromHexOrNull("xyz"))
    }
}
