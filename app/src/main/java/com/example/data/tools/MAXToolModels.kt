package com.example.data.tools

enum class ToolCategory(val displayName: String, val description: String) {
    LIFE_ADMIN("Life Admin", "Organize expenses, appointments, checklists, and daily administrative tasks"),
    DECISION("Decision Tools", "Evaluate dilemmas, compare choices, weight priorities, and make confident choices"),
    COMMUNICATION("Communication", "Draft polite replies, difficult conversations, requests, apologies, and boundary setting"),
    EXTRACTOR("Information Extractor", "Extract names, dates, prices, addresses, contacts, and action items from messy text"),
    CONVERTER("Format Converter", "Transform content into Tables, Checklists, Timelines, FAQ, JSON, and Markdown"),
    COMBINATIONS("Tool Combinations", "Chain multi-step AI pipelines sequentially (e.g. Extract → Summarize → Rewrite → Email)"),
    CUSTOM("Custom Tools", "User-created specialized AI tools saved to Home")
}

data class MAXTool(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val description: String,
    val inputLabel: String = "Enter text or paste data",
    val inputPlaceholder: String = "Paste or type here...",
    val suggestedSample: String = "",
    val systemInstruction: String,
    val keywords: List<String> = emptyList(),
    val badge: String = "Everyday AI",
    val isCustom: Boolean = false,
    val preferredModel: String? = null
)

enum class QuickTransformAction(val displayName: String, val promptInstruction: String) {
    EXPLAIN("Explain", "Explain this clearly in plain terms with concise analogies if helpful."),
    FIX("Fix", "Fix all grammatical errors, spelling mistakes, awkward phrasing, and punctuation while preserving the original meaning."),
    REWRITE("Rewrite", "Rewrite this to be engaging, crisp, natural, and highly articulate."),
    SHORTEN("Shorten", "Condense this significantly, keeping only the most essential points without filler."),
    EXPAND("Expand", "Expand this with thorough depth, practical examples, context, and nuance."),
    TRANSLATE("Translate", "Translate this text accurately into natural, fluent English (or if already in English, translate to Spanish and French with notes)."),
    SUMMARIZE("Summarize", "Provide a concise executive summary with 3-5 high-impact takeaways."),
    CHANGE_TONE("Change Tone", "Adjust tone to be balanced, warm, empathetic, and engaging."),
    MAKE_PROFESSIONAL("Make Professional", "Refactor this into sophisticated, corporate-ready, executive professional communication."),
    MAKE_SIMPLER("Make Simpler", "Explain and rephrase this in simple, everyday language that a 10-year-old can easily grasp."),
    TURN_INTO_BULLETS("Turn into Bullets", "Restructure all information into clean, scannable, hierarchical bullet points."),
    TURN_INTO_EMAIL("Turn into Email", "Draft a complete, polished email (subject line, professional greeting, body, sign-off) based on this content.")
}

enum class FormatTarget(val displayName: String, val instruction: String) {
    TABLE("Table", "Convert this content into a well-structured Markdown table with appropriate columns and aligned data rows."),
    CHECKLIST("Checklist", "Convert this content into an actionable Markdown checklist using `[ ]` task format, organized logically."),
    TIMELINE("Timeline", "Convert this content into a sequential, chronological timeline with dates/timeframes, phases, and milestones."),
    BULLET_POINTS("Bullet Points", "Convert this content into crisp, hierarchical, scannable bullet points highlighting key insights."),
    STEP_BY_STEP("Step-by-Step", "Convert this content into a numbered step-by-step instructional guide with explicit actions."),
    FAQ("FAQ", "Convert this content into a comprehensive Frequently Asked Questions (FAQ) format with clear Q&A pairs."),
    JSON("Structured JSON", "Extract and organize all data from this content into valid, clean, strictly parseable JSON schema."),
    MARKDOWN("Markdown", "Format this content into clean, elegant GitHub-flavored Markdown with bolding, headers (#, ##), and callouts."),
    PLAIN_TEXT("Plain Text", "Strip away formatting and output clean, well-spaced, direct plain text prose.")
}

data class ToolChainStep(
    val stepNumber: Int,
    val toolId: String,
    val toolName: String,
    val promptDescription: String
)

data class ToolCombination(
    val id: String,
    val title: String,
    val description: String,
    val inputPlaceholder: String,
    val steps: List<ToolChainStep>
)
