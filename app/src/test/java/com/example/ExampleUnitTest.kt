package com.example

import com.example.data.tools.ContentKind
import com.example.data.tools.FormatTarget
import com.example.data.tools.MAXToolRegistry
import com.example.data.tools.QuickTransformAction
import com.example.data.tools.SmartContentDetector
import com.example.data.tools.ToolCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAllRequiredToolsAreRegistered() {
        val tools = MAXToolRegistry.BUILT_IN_TOOLS

        // 1. Life Admin (6 tools)
        assertNotNull(tools.find { it.id == "bill_expense_organizer" })
        assertNotNull(tools.find { it.id == "form_filling_assistant" })
        assertNotNull(tools.find { it.id == "appointment_preparation" })
        assertNotNull(tools.find { it.id == "checklist_builder" })
        assertNotNull(tools.find { it.id == "important_date_planner" })
        assertNotNull(tools.find { it.id == "personal_document_checklist" })

        // 2. Decision Tools (5 tools)
        assertNotNull(tools.find { it.id == "decision_matrix" })
        assertNotNull(tools.find { it.id == "compare_two_choices" })
        assertNotNull(tools.find { it.id == "priority_sorter" })
        assertNotNull(tools.find { it.id == "pros_cons_organizer" })
        assertNotNull(tools.find { it.id == "help_me_decide" })

        // 3. Communication (7 tools)
        assertNotNull(tools.find { it.id == "reply_to_message" })
        assertNotNull(tools.find { it.id == "difficult_message_helper" })
        assertNotNull(tools.find { it.id == "apology_writer" })
        assertNotNull(tools.find { it.id == "request_writer" })
        assertNotNull(tools.find { it.id == "complaint_feedback_writer" })
        assertNotNull(tools.find { it.id == "conversation_starter" })
        assertNotNull(tools.find { it.id == "polite_refusal_generator" })

        // 4. Information Extractor (8 tools)
        assertNotNull(tools.find { it.id == "extract_names" })
        assertNotNull(tools.find { it.id == "extract_dates" })
        assertNotNull(tools.find { it.id == "extract_prices" })
        assertNotNull(tools.find { it.id == "extract_addresses" })
        assertNotNull(tools.find { it.id == "extract_contacts" })
        assertNotNull(tools.find { it.id == "extract_action_items" })
        assertNotNull(tools.find { it.id == "extract_requirements" })
        assertNotNull(tools.find { it.id == "extract_important_numbers" })

        // 5. Format Converter (9 target formats)
        assertEquals(9, FormatTarget.values().size)

        // 6. Quick AI Actions (12 transform actions)
        assertEquals(12, QuickTransformAction.values().size)

        // 10. Tool Combinations
        assertTrue(MAXToolRegistry.PREBUILT_COMBINATIONS.isNotEmpty())
    }

    @Test
    fun testUniversalToolSearchNaturalLanguage() {
        val billResults = MAXToolRegistry.searchTools("split lunch bill expense")
        assertTrue(billResults.any { it.id == "bill_expense_organizer" || it.id == "extract_prices" })

        val decideResults = MAXToolRegistry.searchTools("I need to choose between two job offers")
        assertTrue(decideResults.any { it.id == "compare_two_choices" || it.id == "decision_matrix" })

        val emailResults = MAXToolRegistry.searchTools("how to say no politely to invitation")
        assertTrue(emailResults.any { it.id == "polite_refusal_generator" || it.id == "reply_to_message" })
    }

    @Test
    fun testSmartContentDetector() {
        val invoiceText = "Invoice #1024. Total due: $450.50 on 2026-10-01. Subtotal: $400."
        val resultExpense = SmartContentDetector.detect(invoiceText)
        assertEquals(ContentKind.EXPENSE_RECEIPT, resultExpense.kind)
        assertTrue(resultExpense.suggestedToolIds.contains("bill_expense_organizer"))

        val dilemmaText = "Should I accept the startup offer vs stay at the corporate job?"
        val resultDilemma = SmartContentDetector.detect(dilemmaText)
        assertEquals(ContentKind.DILEMMA_DECISION, resultDilemma.kind)
        assertTrue(resultDilemma.suggestedToolIds.contains("compare_two_choices"))

        val emailText = "Hi team, regarding the meeting tomorrow, please review the attached agenda. Best regards, Sarah"
        val resultEmail = SmartContentDetector.detect(emailText)
        assertEquals(ContentKind.MESSAGE_EMAIL, resultEmail.kind)
        assertTrue(resultEmail.suggestedToolIds.contains("reply_to_message"))
    }
}
