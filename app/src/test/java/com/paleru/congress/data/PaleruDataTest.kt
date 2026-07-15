package com.paleru.congress.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaleruDataTest {

    @Test
    fun `directory contains four mandals and 134 gram panchayats`() {
        assertEquals("Unexpected mandal count", 4, PaleruData.mandals.size)
        assertEquals(
            "Unexpected Gram Panchayat count",
            134,
            PaleruData.mandals.sumOf { it.gramPanchayats.size }
        )
        assertEquals(134, PaleruData.gramPanchayats.size)
    }

    @Test
    fun `core public data is complete in Telugu and English`() {
        with(PaleruData.currentLeader) {
            assertBilingual("current leader name", name)
            assertBilingual("current leader role", role)
            assertBilingual("current leader constituency", constituency)
            assertBilingual("current leader portfolio", portfolio)
            assertNotBlank("current leader verification date", verifiedOn)
        }

        PaleruData.mandals.forEach { mandal ->
            assertNotBlank("mandal id", mandal.id)
            assertNotBlank("${mandal.id} Telugu name", mandal.nameTe)
            assertNotBlank("${mandal.id} English name", mandal.nameEn)
            assertNotBlank("${mandal.id} president Telugu name", mandal.congressPresidentTe)
            assertNotBlank("${mandal.id} president English name", mandal.congressPresidentEn)
            assertBilingual("${mandal.id} president evidence", mandal.presidentEvidence)
            assertNotBlank("${mandal.id} president source", mandal.presidentSourceUrl)

            mandal.gramPanchayats.forEach { gramPanchayat ->
                val label = "${mandal.id}/${gramPanchayat.nameEn}"
                assertNotBlank("$label Telugu name", gramPanchayat.nameTe)
                assertNotBlank("$label English name", gramPanchayat.nameEn)
                assertNotBlank("$label Sarpanch name", gramPanchayat.sarpanchOfficialName)
            }
        }

        PaleruData.elections.forEach { election ->
            assertBilingual("${election.year} era", election.era)
            assertBilingual("${election.year} Congress candidate", election.congressCandidate)
            assertBilingual("${election.year} opponent", election.opponent)
            assertBilingual("${election.year} opponent party", election.opponentParty)
        }

        PaleruData.ministers.forEachIndexed { index, minister ->
            val label = "minister $index"
            assertBilingual("$label name", minister.name)
            assertBilingual("$label period", minister.period)
            assertBilingual("$label portfolio", minister.portfolio)
            assertBilingual("$label state", minister.state)
            assertBilingual("$label party", minister.partyAtThatTime)
        }

        PaleruData.sources.forEachIndexed { index, source ->
            assertBilingual("source $index title", source.title)
            assertBilingual("source $index detail", source.detail)
            assertNotBlank("source $index URL", source.url)
        }
    }

    @Test
    fun `display data uses ASCII digits only`() {
        val forbiddenDigits = Regex("[\\u0C66-\\u0C6F\\u0660-\\u0669\\u06F0-\\u06F9]")
        val offendingValues = allDisplayStrings().filter(forbiddenDigits::containsMatchIn)

        assertTrue(
            "Found non-ASCII numeral glyphs in: ${offendingValues.joinToString()}",
            offendingValues.isEmpty()
        )
    }

    private fun assertBilingual(label: String, value: LocalizedText) {
        assertNotBlank("$label (Telugu)", value.te)
        assertNotBlank("$label (English)", value.en)
    }

    private fun assertNotBlank(label: String, value: String) {
        assertFalse("$label must not be blank", value.isBlank())
    }

    private fun allDisplayStrings(): List<String> = buildList {
        with(PaleruData.currentLeader) {
            addAll(name.values())
            addAll(role.values())
            addAll(constituency.values())
            addAll(portfolio.values())
            add(verifiedOn)
        }

        PaleruData.mandals.forEach { mandal ->
            add(mandal.id)
            add(mandal.nameTe)
            add(mandal.nameEn)
            add(mandal.congressPresidentTe)
            add(mandal.congressPresidentEn)
            addAll(mandal.presidentEvidence.values())
            mandal.gramPanchayats.forEach { gramPanchayat ->
                add(gramPanchayat.nameTe)
                add(gramPanchayat.nameEn)
                add(gramPanchayat.sarpanchOfficialName)
                add(gramPanchayat.resultYear.toString())
            }
        }

        PaleruData.elections.forEach { election ->
            add(election.year.toString())
            addAll(election.era.values())
            addAll(election.congressCandidate.values())
            add(election.congressVotes.toString())
            addAll(election.opponent.values())
            addAll(election.opponentParty.values())
            add(election.opponentVotes.toString())
            add(election.margin.toString())
            addAll(election.note.values())
        }

        PaleruData.ministers.forEach { minister ->
            addAll(minister.name.values())
            addAll(minister.period.values())
            addAll(minister.portfolio.values())
            addAll(minister.state.values())
            addAll(minister.partyAtThatTime.values())
            addAll(minister.note.values())
        }

        PaleruData.sources.forEach { source ->
            addAll(source.title.values())
            addAll(source.detail.values())
        }
    }

    private fun LocalizedText.values() = listOf(te, en)
}
