package com.paleru.congress.data

import android.content.Context
import android.content.Intent
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.io.File

/**
 * Rules and cryptographic helpers for the offline device PIN.
 *
 * This object has no Android dependencies, so validation, hashing, hex conversion, and
 * comparison can be exercised by ordinary JVM unit tests. A PIN is deliberately accepted only
 * when every character is an ASCII digit; Unicode decimal digits are not normalized here.
 */
object DevicePinSecurity {
    const val MIN_PIN_LENGTH = 4
    const val MAX_PIN_LENGTH = 6
    const val DEFAULT_HASH_ROUNDS = 120_000
    const val SALT_SIZE_BYTES = 32

    private const val MIN_SALT_SIZE_BYTES = 16
    private const val MAX_HASH_ROUNDS = 2_000_000
    private val hashDomain = "PaleruCongressDevicePin:v1".toByteArray(StandardCharsets.UTF_8)

    enum class Validation {
        VALID,
        EMPTY,
        INVALID_LENGTH,
        NON_ASCII_DIGIT
    }

    /** Validates without trimming or converting the input. */
    fun validate(pin: String): Validation = when {
        pin.isEmpty() -> Validation.EMPTY
        pin.any { it !in '0'..'9' } -> Validation.NON_ASCII_DIGIT
        pin.length !in MIN_PIN_LENGTH..MAX_PIN_LENGTH -> Validation.INVALID_LENGTH
        else -> Validation.VALID
    }

    fun isValid(pin: String): Boolean = validate(pin) == Validation.VALID

    fun newSalt(random: SecureRandom = SecureRandom()): ByteArray =
        ByteArray(SALT_SIZE_BYTES).also(random::nextBytes)

    /**
     * Produces an iterated salted SHA-256 hash.
     *
     * Repeating SHA-256 makes an offline guess more expensive than one raw digest while retaining
     * the explicitly required salted SHA-256 format. This remains a convenience-level local gate,
     * not strong member authentication; the UI must display [DevicePinCopy] accordingly.
     */
    fun hash(
        pin: String,
        salt: ByteArray,
        rounds: Int = DEFAULT_HASH_ROUNDS
    ): ByteArray {
        require(isValid(pin)) { "PIN must contain 4-6 ASCII digits" }
        require(salt.size >= MIN_SALT_SIZE_BYTES) { "Salt must contain at least 16 bytes" }
        require(rounds in 1..MAX_HASH_ROUNDS) { "Hash rounds are outside the supported range" }

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(hashDomain)
        digest.update(salt)
        var value = digest.digest(pin.toByteArray(StandardCharsets.UTF_8))

        repeat(rounds - 1) {
            digest.reset()
            digest.update(hashDomain)
            digest.update(salt)
            value = digest.digest(value)
        }
        return value
    }

    fun matches(
        pin: String,
        salt: ByteArray,
        expectedHash: ByteArray,
        rounds: Int = DEFAULT_HASH_ROUNDS
    ): Boolean {
        if (!isValid(pin) || expectedHash.size != 32) return false
        return MessageDigest.isEqual(expectedHash, hash(pin, salt, rounds))
    }

    fun toHex(bytes: ByteArray): String = buildString(bytes.size * 2) {
        bytes.forEach { byte ->
            val value = byte.toInt() and 0xff
            append(HEX_CHARS[value ushr 4])
            append(HEX_CHARS[value and 0x0f])
        }
    }

    fun fromHexOrNull(value: String): ByteArray? {
        if (value.isEmpty() || value.length % 2 != 0) return null
        val result = ByteArray(value.length / 2)
        for (index in result.indices) {
            val high = hexValue(value[index * 2])
            val low = hexValue(value[index * 2 + 1])
            if (high < 0 || low < 0) return null
            result[index] = ((high shl 4) or low).toByte()
        }
        return result
    }

    private fun hexValue(character: Char): Int = when (character) {
        in '0'..'9' -> character - '0'
        in 'a'..'f' -> character - 'a' + 10
        in 'A'..'F' -> character - 'A' + 10
        else -> -1
    }

    private const val HEX_CHARS = "0123456789abcdef"
}

/** The screen the host UI should show for the current process session. */
enum class DevicePinStage {
    CREATE_PIN,
    UNLOCK,
    UNLOCKED,
    RECOVERY_REQUIRED
}

/** Stable result codes let a screen choose its own visual treatment and translation. */
enum class DevicePinResultCode {
    SUCCESS,
    EMPTY_PIN,
    INVALID_LENGTH,
    NON_ASCII_DIGIT,
    CONFIRMATION_MISMATCH,
    INCORRECT_PIN,
    PIN_ALREADY_CONFIGURED,
    PIN_NOT_CONFIGURED,
    RECOVERY_REQUIRED,
    LOCAL_DATA_WIPE_FAILED,
    STORAGE_FAILED
}

data class DevicePinUiState(
    val stage: DevicePinStage,
    val title: LocalizedText,
    val instruction: LocalizedText,
    val identityNotice: LocalizedText = DevicePinCopy.identityNotice,
    val minimumLength: Int = DevicePinSecurity.MIN_PIN_LENGTH,
    val maximumLength: Int = DevicePinSecurity.MAX_PIN_LENGTH,
    val acceptsAsciiDigitsOnly: Boolean = true,
    val canResetWithLocalDataWipe: Boolean = stage != DevicePinStage.CREATE_PIN,
    val isSessionUnlocked: Boolean = stage == DevicePinStage.UNLOCKED
)

data class DevicePinActionResult(
    val code: DevicePinResultCode,
    val nextState: DevicePinUiState
) {
    val succeeded: Boolean get() = code == DevicePinResultCode.SUCCESS
    val message: LocalizedText get() = DevicePinCopy.messageFor(code)
}

/**
 * Required disclosure for the PIN setup and unlock screens.
 *
 * The device PIN protects only files stored by this app on this phone. It must never be presented
 * as proof of Congress membership, elected office, organizational role, or publishing authority.
 */
object DevicePinCopy {
    val identityNotice = LocalizedText(
        te = "ఈ పరికర PIN ఈ ఫోన్‌లోని స్థానిక యాక్సెస్‌ను మాత్రమే రక్షిస్తుంది. " +
            "ఇది కాంగ్రెస్ సభ్యత్వం, ఎన్నికైన పదవి, పార్టీ బాధ్యత లేదా ప్రచురణ అనుమతిని ధృవీకరించదు.",
        en = "This device PIN protects local access on this phone only. It does not verify " +
            "Congress membership, elected office, party role, or permission to publish."
    )

    val resetWarning = LocalizedText(
        te = "PIN రీసెట్ చేస్తే ఈ పరికరంలోని సేవా ముసాయిదాలు, కార్యకలాపాల ముసాయిదాలు, " +
            "వయస్సు సవరణలు మరియు యాప్ సెట్టింగ్‌లు శాశ్వతంగా తొలగించబడతాయి.",
        en = "Resetting the PIN permanently deletes service drafts, activity drafts, age " +
            "corrections, and app settings stored on this device."
    )

    fun messageFor(code: DevicePinResultCode): LocalizedText = when (code) {
        DevicePinResultCode.SUCCESS -> LocalizedText(
            "పూర్తయింది.",
            "Done."
        )

        DevicePinResultCode.EMPTY_PIN -> LocalizedText(
            "PIN నమోదు చేయండి.",
            "Enter the PIN."
        )

        DevicePinResultCode.INVALID_LENGTH -> LocalizedText(
            "PIN తప్పనిసరిగా 4-6 అంకెలు ఉండాలి.",
            "The PIN must contain 4-6 digits."
        )

        DevicePinResultCode.NON_ASCII_DIGIT -> LocalizedText(
            "0-9 అంకెలను మాత్రమే ఉపయోగించండి.",
            "Use only the digits 0-9."
        )

        DevicePinResultCode.CONFIRMATION_MISMATCH -> LocalizedText(
            "రెండు PIN నమోదు విలువలు సరిపోలలేదు.",
            "The two PIN entries do not match."
        )

        DevicePinResultCode.INCORRECT_PIN -> LocalizedText(
            "PIN సరైనది కాదు.",
            "The PIN is incorrect."
        )

        DevicePinResultCode.PIN_ALREADY_CONFIGURED -> LocalizedText(
            "ఈ పరికరానికి PIN ఇప్పటికే సెట్ చేయబడింది.",
            "A PIN is already configured on this device."
        )

        DevicePinResultCode.PIN_NOT_CONFIGURED -> LocalizedText(
            "ముందుగా ఈ పరికరానికి PIN సృష్టించండి.",
            "Create a PIN for this device first."
        )

        DevicePinResultCode.RECOVERY_REQUIRED -> LocalizedText(
            "PIN రికార్డు చదవలేకపోయాం. స్థానిక డేటా రీసెట్ అవసరం.",
            "The PIN record cannot be read. A local-data reset is required."
        )

        DevicePinResultCode.LOCAL_DATA_WIPE_FAILED -> LocalizedText(
            "స్థానిక డేటాను పూర్తిగా తొలగించలేకపోయాం. PIN మార్చబడలేదు.",
            "Local data could not be fully deleted. The PIN was kept."
        )

        DevicePinResultCode.STORAGE_FAILED -> LocalizedText(
            "ఈ పరికరంలో PIN మార్పును భద్రపరచలేకపోయాం.",
            "The PIN change could not be saved on this device."
        )
    }
}

/**
 * Offline device-access repository with process-session unlock semantics.
 *
 * Create or unlock work should be invoked off the main thread because the iterated hash is
 * intentionally CPU intensive. The returned state/result objects are directly consumable by a
 * Compose or View-based screen. An unlocked session ends when [lockSession] is called or the app
 * process is destroyed.
 */
class DevicePinAccess(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PIN_PREFERENCES, Context.MODE_PRIVATE)

    fun uiState(): DevicePinUiState = when (credentialState()) {
        CredentialState.MISSING -> stateFor(DevicePinStage.CREATE_PIN)
        CredentialState.CORRUPT -> stateFor(DevicePinStage.RECOVERY_REQUIRED)
        CredentialState.READY -> if (DevicePinSession.unlocked) {
            stateFor(DevicePinStage.UNLOCKED)
        } else {
            stateFor(DevicePinStage.UNLOCK)
        }
    }

    /** Creates the first PIN and unlocks the current process session. */
    @Synchronized
    fun createPin(pin: String, confirmation: String): DevicePinActionResult {
        when (credentialState()) {
            CredentialState.READY -> return result(DevicePinResultCode.PIN_ALREADY_CONFIGURED)
            CredentialState.CORRUPT -> return result(DevicePinResultCode.RECOVERY_REQUIRED)
            CredentialState.MISSING -> Unit
        }

        validationResult(pin)?.let { return result(it) }
        if (pin != confirmation) return result(DevicePinResultCode.CONFIRMATION_MISMATCH)

        val salt = DevicePinSecurity.newSalt()
        val hash = DevicePinSecurity.hash(pin, salt)
        val saved = preferences.edit()
            .putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
            .putInt(KEY_HASH_ROUNDS, DevicePinSecurity.DEFAULT_HASH_ROUNDS)
            .putString(KEY_SALT_HEX, DevicePinSecurity.toHex(salt))
            .putString(KEY_HASH_HEX, DevicePinSecurity.toHex(hash))
            .commit()
        if (!saved) return result(DevicePinResultCode.STORAGE_FAILED)

        DevicePinSession.unlocked = true
        return result(DevicePinResultCode.SUCCESS)
    }

    /** Checks the PIN and, on success, unlocks this app-process session. */
    @Synchronized
    fun unlock(pin: String): DevicePinActionResult {
        when (credentialState()) {
            CredentialState.MISSING -> return result(DevicePinResultCode.PIN_NOT_CONFIGURED)
            CredentialState.CORRUPT -> return result(DevicePinResultCode.RECOVERY_REQUIRED)
            CredentialState.READY -> Unit
        }
        validationResult(pin)?.let { return result(it) }

        val credential = readCredential() ?: return result(DevicePinResultCode.RECOVERY_REQUIRED)
        if (!DevicePinSecurity.matches(pin, credential.salt, credential.hash, credential.rounds)) {
            return result(DevicePinResultCode.INCORRECT_PIN)
        }

        DevicePinSession.unlocked = true
        return result(DevicePinResultCode.SUCCESS)
    }

    /** Relocks immediately without changing the stored PIN or deleting local content. */
    fun lockSession(): DevicePinUiState {
        DevicePinSession.unlocked = false
        return uiState()
    }

    /**
     * Removes the PIN only as part of an explicit wipe of all currently known local app data.
     *
     * The host screen should show [DevicePinCopy.resetWarning] and require an explicit destructive
     * confirmation before calling this method. If clearing private content fails, the PIN is kept.
     */
    @Synchronized
    fun resetPinAndWipeAllLocalData(): DevicePinActionResult {
        DevicePinSession.unlocked = false

        val localDataCleared = LOCAL_DATA_PREFERENCES.all { preferenceName ->
            appContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
        val mediaCleared = File(appContext.filesDir, ACTIVITY_MEDIA_DIRECTORY)
            .let { directory -> !directory.exists() || directory.deleteRecursively() }
        val grantsCleared = appContext.contentResolver.persistedUriPermissions.all { permission ->
            runCatching {
                appContext.contentResolver.releasePersistableUriPermission(
                    permission.uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.isSuccess
        }
        if (!localDataCleared || !mediaCleared || !grantsCleared) {
            return result(DevicePinResultCode.LOCAL_DATA_WIPE_FAILED)
        }

        if (!preferences.edit().clear().commit()) {
            return result(DevicePinResultCode.STORAGE_FAILED)
        }
        return result(DevicePinResultCode.SUCCESS)
    }

    private fun credentialState(): CredentialState {
        val credentialKeyCount = CREDENTIAL_KEYS.count(preferences::contains)
        if (credentialKeyCount == 0) return CredentialState.MISSING
        if (credentialKeyCount != CREDENTIAL_KEYS.size) return CredentialState.CORRUPT
        return if (readCredential() == null) CredentialState.CORRUPT else CredentialState.READY
    }

    private fun readCredential(): StoredCredential? {
        if (preferences.getInt(KEY_SCHEMA_VERSION, -1) != SCHEMA_VERSION) return null
        val rounds = preferences.getInt(KEY_HASH_ROUNDS, -1)
        if (rounds !in 1..MAX_ACCEPTED_HASH_ROUNDS) return null
        val salt = DevicePinSecurity.fromHexOrNull(
            preferences.getString(KEY_SALT_HEX, null) ?: return null
        ) ?: return null
        val hash = DevicePinSecurity.fromHexOrNull(
            preferences.getString(KEY_HASH_HEX, null) ?: return null
        ) ?: return null
        if (salt.size != DevicePinSecurity.SALT_SIZE_BYTES || hash.size != 32) return null
        return StoredCredential(salt, hash, rounds)
    }

    private fun validationResult(pin: String): DevicePinResultCode? =
        when (DevicePinSecurity.validate(pin)) {
            DevicePinSecurity.Validation.VALID -> null
            DevicePinSecurity.Validation.EMPTY -> DevicePinResultCode.EMPTY_PIN
            DevicePinSecurity.Validation.INVALID_LENGTH -> DevicePinResultCode.INVALID_LENGTH
            DevicePinSecurity.Validation.NON_ASCII_DIGIT -> DevicePinResultCode.NON_ASCII_DIGIT
        }

    private fun result(code: DevicePinResultCode) = DevicePinActionResult(code, uiState())

    private fun stateFor(stage: DevicePinStage): DevicePinUiState = when (stage) {
        DevicePinStage.CREATE_PIN -> DevicePinUiState(
            stage = stage,
            title = LocalizedText("పరికర PIN సృష్టించండి", "Create device PIN"),
            instruction = LocalizedText(
                "4-6 అంకెల PIN నమోదు చేసి మరొకసారి నిర్ధారించండి.",
                "Enter a 4-6 digit PIN, then enter it again to confirm."
            )
        )

        DevicePinStage.UNLOCK -> DevicePinUiState(
            stage = stage,
            title = LocalizedText("యాప్‌ను అన్‌లాక్ చేయండి", "Unlock the app"),
            instruction = LocalizedText(
                "ఈ పరికరానికి సెట్ చేసిన 4-6 అంకెల PIN నమోదు చేయండి.",
                "Enter the 4-6 digit PIN set for this device."
            )
        )

        DevicePinStage.UNLOCKED -> DevicePinUiState(
            stage = stage,
            title = LocalizedText("పరికరం అన్‌లాక్ అయింది", "Device unlocked"),
            instruction = LocalizedText(
                "ఈ యాప్ సెషన్‌లో స్థానిక కంటెంట్ అందుబాటులో ఉంది.",
                "Local content is available for this app session."
            )
        )

        DevicePinStage.RECOVERY_REQUIRED -> DevicePinUiState(
            stage = stage,
            title = LocalizedText("స్థానిక యాక్సెస్‌ను రీసెట్ చేయండి", "Reset local access"),
            instruction = LocalizedText(
                "PIN రికార్డు చదవలేకపోయాం. కొనసాగడానికి స్థానిక డేటాను తొలగించి కొత్త PIN సృష్టించండి.",
                "The PIN record cannot be read. Wipe local data and create a new PIN to continue."
            )
        )
    }

    private enum class CredentialState { MISSING, READY, CORRUPT }

    private data class StoredCredential(
        val salt: ByteArray,
        val hash: ByteArray,
        val rounds: Int
    )

    private companion object {
        const val PIN_PREFERENCES = "paleru_device_access_v1"
        const val KEY_SCHEMA_VERSION = "schema_version"
        const val KEY_HASH_ROUNDS = "hash_rounds"
        const val KEY_SALT_HEX = "salt_hex"
        const val KEY_HASH_HEX = "hash_hex"
        const val SCHEMA_VERSION = 1
        const val MAX_ACCEPTED_HASH_ROUNDS = 2_000_000
        const val ACTIVITY_MEDIA_DIRECTORY = "activity_media"

        val CREDENTIAL_KEYS = setOf(
            KEY_SCHEMA_VERSION,
            KEY_HASH_ROUNDS,
            KEY_SALT_HEX,
            KEY_HASH_HEX
        )

        val LOCAL_DATA_PREFERENCES = listOf(
            "paleru_local_drafts_v1",
            "paleru_feed_interactions_v1",
            "paleru_preferences"
        )
    }
}

/** Process-memory only: process death always restores the locked state. */
private object DevicePinSession {
    @Volatile
    var unlocked: Boolean = false
}
