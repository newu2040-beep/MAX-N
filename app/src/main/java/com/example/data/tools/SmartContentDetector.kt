package com.example.data.tools

enum class ContentKind(val displayName: String, val badge: String) {
    EXPENSE_RECEIPT("Bill / Receipt / Expenses", "Financial"),
    DILEMMA_DECISION("Dilemma / Decision", "Strategy"),
    MESSAGE_EMAIL("Message / Communication", "Inbox"),
    CONTACT_DIRECTORY("Contact / Directory Info", "Entities"),
    JOB_SPEC("Job Posting / RFP", "Career"),
    MEETING_NOTES_TODO("Meeting Notes & Tasks", "Action Items"),
    RAW_TEXT("General Text / Draft", "Content")
}

data class DetectionResult(
    val kind: ContentKind,
    val summary: String,
    val suggestedToolIds: List<String>,
    val suggestedTransforms: List<QuickTransformAction>
)

object SmartContentDetector {

    fun detect(text: String): DetectionResult {
        val lower = text.lowercase().trim()
        val length = text.length

        // 1. Check for Bill / Expense / Receipt
        val hasCurrency = lower.contains("$") || lower.contains("€") || lower.contains("£") || lower.contains("usd") || lower.contains("total:") || lower.contains("subtotal") || lower.contains("invoice") || lower.contains("due date") || lower.contains("amount due")
        if (hasCurrency) {
            return DetectionResult(
                kind = ContentKind.EXPENSE_RECEIPT,
                summary = "Detected invoice, bill, or itemized expense text",
                suggestedToolIds = listOf("bill_expense_organizer", "extract_prices", "extract_dates", "format_converter"),
                suggestedTransforms = listOf(QuickTransformAction.SUMMARIZE, QuickTransformAction.TURN_INTO_BULLETS, QuickTransformAction.EXPLAIN)
            )
        }

        // 2. Check for Decision / Dilemma / Comparison
        val hasDilemma = lower.contains("should i ") || lower.contains(" vs ") || lower.contains("versus") || lower.contains("choice between") || lower.contains("option a") || lower.contains("can't decide") || lower.contains("pros and cons")
        if (hasDilemma) {
            return DetectionResult(
                kind = ContentKind.DILEMMA_DECISION,
                summary = "Detected a decision, dilemma, or comparative choices",
                suggestedToolIds = listOf("compare_two_choices", "decision_matrix", "help_me_decide", "pros_cons_organizer"),
                suggestedTransforms = listOf(QuickTransformAction.EXPLAIN, QuickTransformAction.SUMMARIZE, QuickTransformAction.MAKE_SIMPLER)
            )
        }

        // 3. Check for Message / Email / Incoming Communication
        val hasMessage = lower.contains("hi ") || lower.contains("hello ") || lower.contains("dear ") || lower.contains("re:") || lower.contains("best regards") || lower.contains("thanks,") || lower.contains("let me know") || lower.contains("could you please") || lower.contains("sent from my")
        if (hasMessage) {
            return DetectionResult(
                kind = ContentKind.MESSAGE_EMAIL,
                summary = "Detected an email, direct message, or correspondence",
                suggestedToolIds = listOf("reply_to_message", "polite_refusal_generator", "difficult_message_helper", "extract_action_items"),
                suggestedTransforms = listOf(QuickTransformAction.REWRITE, QuickTransformAction.MAKE_PROFESSIONAL, QuickTransformAction.SHORTEN, QuickTransformAction.SUMMARIZE)
            )
        }

        // 4. Check for Contacts / Directory Info
        val hasContacts = (lower.contains("@") && lower.contains(".")) || lower.contains("phone:") || lower.contains("tel:") || lower.contains("linkedin.com") || lower.contains("mobile:")
        if (hasContacts) {
            return DetectionResult(
                kind = ContentKind.CONTACT_DIRECTORY,
                summary = "Detected contact details, emails, or directory info",
                suggestedToolIds = listOf("extract_contacts", "extract_names", "extract_addresses", "format_converter"),
                suggestedTransforms = listOf(QuickTransformAction.TURN_INTO_BULLETS, QuickTransformAction.EXPLAIN)
            )
        }

        // 5. Check for Job Posting / RFP
        val hasJob = lower.contains("requirements:") || lower.contains("qualifications:") || lower.contains("responsibilities:") || lower.contains("years of experience") || lower.contains("job description")
        if (hasJob) {
            return DetectionResult(
                kind = ContentKind.JOB_SPEC,
                summary = "Detected a job description, criteria, or project spec",
                suggestedToolIds = listOf("extract_requirements", "extract_action_items", "request_writer", "format_converter"),
                suggestedTransforms = listOf(QuickTransformAction.SUMMARIZE, QuickTransformAction.TURN_INTO_BULLETS, QuickTransformAction.EXPLAIN)
            )
        }

        // 6. Check for Meeting Notes / Action Items
        val hasMeeting = lower.contains("meeting") || lower.contains("action item") || lower.contains("attendees") || lower.contains("agenda") || lower.contains("discussion") || lower.contains("next steps")
        if (hasMeeting) {
            return DetectionResult(
                kind = ContentKind.MEETING_NOTES_TODO,
                summary = "Detected meeting notes, transcripts, or project tasks",
                suggestedToolIds = listOf("extract_action_items", "checklist_builder", "extract_dates", "format_converter"),
                suggestedTransforms = listOf(QuickTransformAction.SUMMARIZE, QuickTransformAction.TURN_INTO_BULLETS, QuickTransformAction.TURN_INTO_EMAIL)
            )
        }

        // Default Raw Text
        return DetectionResult(
            kind = ContentKind.RAW_TEXT,
            summary = "General text (${length} characters)",
            suggestedToolIds = listOf("format_converter", "quick_transform", "priority_sorter", "checklist_builder"),
            suggestedTransforms = listOf(QuickTransformAction.FIX, QuickTransformAction.REWRITE, QuickTransformAction.MAKE_PROFESSIONAL, QuickTransformAction.SUMMARIZE, QuickTransformAction.TURN_INTO_BULLETS)
        )
    }
}
