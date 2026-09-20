package com.example.data.tools

object MAXToolRegistry {

    val BUILT_IN_TOOLS: List<MAXTool> = listOf(
        // ----------------------------------------------------
        // 1. LIFE ADMIN
        // ----------------------------------------------------
        MAXTool(
            id = "bill_expense_organizer",
            name = "Bill/expense organizer",
            category = ToolCategory.LIFE_ADMIN,
            description = "Parse receipts, bills, invoices, and split expenses into an organized spending ledger.",
            inputLabel = "Paste receipt text, invoice details, or expense notes",
            inputPlaceholder = "e.g. Electric bill $142 due Oct 12, Dinner with Sarah $84, Grocery receipt from Trader Joe's...",
            suggestedSample = "Dinner at Bistro: $94.50 (split 3 ways). Internet bill: $65 due Oct 5th. Trader Joe's groceries: Milk $3.99, Chicken $12.50, Produce $18.40. Uber ride home: $24.15.",
            systemInstruction = """
                You are the MAX-N Life Admin Expense & Bill Organizer.
                Analyze the user's input and produce:
                1. Itemized Ledger: Category, Description, Amount, and Due Date (if stated).
                2. Summary Totals: Total spend, category breakdown (Dining, Utilities, Groceries, Transport, etc.).
                3. Split Calculations: If splitting with others is mentioned, compute per-person shares accurately.
                4. Actionable Alerts: Imminent due dates, duplicate charges, or money-saving tips.
                Format clearly with Markdown tables and bold figures.
            """.trimIndent(),
            keywords = listOf("bill", "expense", "receipt", "invoice", "money", "budget", "cost", "spending", "split", "ledger", "pay", "due date")
        ),
        MAXTool(
            id = "form_filling_assistant",
            name = "Form-filling assistant",
            category = ToolCategory.LIFE_ADMIN,
            description = "Map your unstructured notes into clean, exact field responses for any government, job, medical, or rental form.",
            inputLabel = "Paste form questions or field requirements + your raw background details",
            inputPlaceholder = "Paste the form fields or questions here...",
            suggestedSample = "Form requires: Full legal name, residential history last 3 years, employment verification, monthly gross income, 2 emergency contacts with relationship and phone.",
            systemInstruction = """
                You are the MAX-N Form-Filling Assistant.
                Your task is to take the requested form fields/questions and generate clear, accurate, ready-to-paste answers:
                1. Field-by-Field Breakdown: Key / Field Name -> Recommended exact response.
                2. Flag Missing Data: Clearly highlight any required info the user didn't supply.
                3. Tone & Precision: Professional, compliant, and unambiguous for administrative scrutiny.
            """.trimIndent(),
            keywords = listOf("form", "fill", "application", "questionnaire", "paperwork", "bureaucracy", "registration", "lease", "rental", "visa form")
        ),
        MAXTool(
            id = "appointment_prep",
            name = "Appointment preparation",
            category = ToolCategory.LIFE_ADMIN,
            description = "Prepare questions to ask, documents to bring, symptoms/agenda, and prep notes for any doctor, legal, or finance meeting.",
            inputLabel = "What is the appointment for?",
            inputPlaceholder = "e.g. First visit with a cardiologist about occasional palpitations; or Consultation with a tax CPA about freelance income...",
            suggestedSample = "Consultation with a family immigration attorney regarding spouse green card timeline, required proofs of bona fide marriage, and travel permissions while pending.",
            systemInstruction = """
                You are the MAX-N Appointment Preparation Specialist.
                Create a high-impact briefing dossier for the user's appointment:
                1. Agenda & Objective: Clear statement of desired outcome.
                2. Top 5-8 Probing Questions: Essential questions to ask the specialist.
                3. Documents & Records to Bring: Identification, past records, contracts, or test results needed.
                4. Key Details to Disclose: Specific timeline, symptoms, or numbers to communicate clearly.
                5. Note-taking Template: Key sections to jot down during the meeting.
            """.trimIndent(),
            keywords = listOf("appointment", "doctor", "meeting", "consultation", "questions", "prep", "hospital", "lawyer", "dentist", "agenda", "specialist")
        ),
        MAXTool(
            id = "checklist_builder",
            name = "Personal checklist builder",
            category = ToolCategory.LIFE_ADMIN,
            description = "Generate a categorized, chronological checklist with checkboxes for any trip, move, event, or complex task.",
            inputLabel = "What event, move, or project do you need a checklist for?",
            inputPlaceholder = "e.g. Moving into a new 2-bedroom apartment next month; or 10-day trip to Japan in autumn with toddler...",
            suggestedSample = "Moving out of a rented apartment into a newly purchased house in 3 weeks: packing, utility transfers, cleaning, security deposit return, changing address.",
            systemInstruction = """
                You are the MAX-N Personal Checklist Builder.
                Create an exhaustive, structured checklist using Markdown checkboxes `[ ]`:
                - Phase 1: Immediate / 3 Weeks Prior
                - Phase 2: 1 Week Prior
                - Phase 3: Day Of
                - Phase 4: Follow-up & Post-completion
                Group items by category (Logistics, Paperwork, Essentials, Communications).
            """.trimIndent(),
            keywords = listOf("checklist", "todo", "packing", "moving", "travel", "event", "tasks", "organize", "builder", "steps", "preparation")
        ),
        MAXTool(
            id = "important_date_planner",
            name = "Important-date planner",
            category = ToolCategory.LIFE_ADMIN,
            description = "Calculate countdown milestones, buffer schedules, and preparation timelines for deadlines, renewals, and events.",
            inputLabel = "What is the event, deadline, or celebration and date?",
            inputPlaceholder = "e.g. Passport renewal expiring in 6 months; Sister's wedding in 90 days; Annual tax filing deadline...",
            suggestedSample = "Target: Hosting a 30-person 40th birthday surprise party on Saturday, November 14th. Need milestone timeline starting 8 weeks out.",
            systemInstruction = """
                You are the MAX-N Important-Date Planner.
                Given the target date/event:
                1. Calculate Key Milestone Dates (T-minus countdown): 8 weeks, 4 weeks, 2 weeks, 1 week, 48 hours, Day Of.
                2. Explicit Action Triggers: What must be booked, ordered, or submitted at each milestone.
                3. Risk Mitigation: Buffer time recommendations for unexpected shipping or processing delays.
            """.trimIndent(),
            keywords = listOf("date", "deadline", "calendar", "timeline", "milestone", "countdown", "planner", "event", "birthday", "renewal", "schedule")
        ),
        MAXTool(
            id = "personal_document_checklist",
            name = "Personal document checklist",
            category = ToolCategory.LIFE_ADMIN,
            description = "Audit required government IDs, proofs of address, certificates, and financial records for applications.",
            inputLabel = "What application or procedure are you preparing for?",
            inputPlaceholder = "e.g. Applying for a mortgage pre-approval; Schengen Visa tourist application; Real ID driver's license renewal...",
            suggestedSample = "Applying for a mortgage loan pre-approval with bank: self-employed freelance income for 2 years, spouse W2 employee.",
            systemInstruction = """
                You are the MAX-N Personal Document Auditor.
                Produce a definitive document dossier checklist:
                1. Proof of Identity & Legal Status
                2. Proof of Income & Financial History (Bank statements, tax returns, pay stubs)
                3. Proof of Residence / Address
                4. Secondary / Supporting Documentation
                5. Document Validation Tips: Expiration guidelines, notarization or certified translation needs.
            """.trimIndent(),
            keywords = listOf("document", "passport", "visa", "mortgage", "id", "paperwork", "certificate", "license", "audit", "records", "verification")
        ),

        // ----------------------------------------------------
        // 2. DECISION TOOLS
        // ----------------------------------------------------
        MAXTool(
            id = "decision_matrix",
            name = "Decision matrix",
            category = ToolCategory.DECISION,
            description = "Evaluate multiple competing options against weighted criteria (cost, effort, ROI, happiness) with structured scores.",
            inputLabel = "What are the options and criteria?",
            inputPlaceholder = "e.g. Choosing between Option A (stay at current tech job $130k remote), Option B (join Series A startup $160k hybrid in SF), Option C (freelance consulting)...",
            suggestedSample = "Deciding between 3 apartments: Apartment 1 ($2,200/mo, 15 min commute, small gym, older), Apartment 2 ($2,600/mo, 40 min commute, luxury modern, balcony), Apartment 3 ($2,350/mo, 25 min commute, quiet neighborhood, near park).",
            systemInstruction = """
                You are the MAX-N Decision Matrix Architect.
                1. Establish Weighted Criteria (e.g., Financial Impact 25%, Time/Commute 25%, Quality of Life 20%, Growth 20%, Risk 10%).
                2. Present a clean Markdown Scoring Matrix (Table with Options as columns, Criteria as rows, scores 1-10, and weighted totals).
                3. In-depth Analysis: Strengths and weaknesses of each contender.
                4. Clear Winner & Sensitivity Advice: Under what circumstances would the #2 choice become the superior option?
            """.trimIndent(),
            keywords = listOf("decision", "matrix", "choose", "choice", "weigh", "score", "evaluate", "compare", "criteria", "decide", "options")
        ),
        MAXTool(
            id = "compare_two_choices",
            name = "Compare two choices",
            category = ToolCategory.DECISION,
            description = "Side-by-side head-to-head comparison across 6 critical dimensions: pros, cons, hidden compromises, and definitive verdict.",
            inputLabel = "Enter the two options you are deciding between",
            inputPlaceholder = "Option A vs Option B...",
            suggestedSample = "Option A: Buy a 3-year-old certified pre-owned Toyota RAV4 Hybrid for $28,000 cash. Option B: Lease a brand new Tesla Model Y for $399/month for 36 months.",
            systemInstruction = """
                You are the MAX-N Head-to-Head Comparative Analyst.
                Provide a structured comparative verdict between Option A and Option B:
                1. Executive Side-by-Side Table: Direct comparison across Price, Durability/Longevity, Flexibility, Hidden Costs, and Stress Factor.
                2. Option A Deep Dive: Why choose this, key risk, who it's best for.
                3. Option B Deep Dive: Why choose this, key risk, who it's best for.
                4. The 1-Year & 3-Year Outlook: Where each choice leaves you down the road.
                5. The Decisive Verdict: A definitive, unbiased recommendation based on financial and mental peace.
            """.trimIndent(),
            keywords = listOf("compare", "versus", "vs", "two choices", "option a", "option b", "which one", "preference", "decision")
        ),
        MAXTool(
            id = "priority_sorter",
            name = "Priority sorter",
            category = ToolCategory.DECISION,
            description = "Sort a chaotic list of tasks or backlogged ideas into Eisenhower quadrants and an execution sequence.",
            inputLabel = "Paste your list of tasks, projects, or pending obligations",
            inputPlaceholder = "Paste any unorganized list of things you need to do...",
            suggestedSample = "Fix leaking kitchen faucet, submit quarterly tax report, reply to client proposal, clean garage, renew car insurance, draft article outline, buy birthday present for mom, schedule annual checkup.",
            systemInstruction = """
                You are the MAX-N Priority Sorting Engine.
                Organize the user's tasks into four strict actionable quadrants:
                1. Quadrant 1: DO FIRST (Urgent & High Impact) - Complete today/tomorrow.
                2. Quadrant 2: SCHEDULE (Not Urgent, High Long-term Impact) - Calendar time blocks.
                3. Quadrant 3: DELEGATE / AUTOMATE / BATCH (Urgent, Low Impact) - Knock out in 30 mins or automate.
                4. Quadrant 4: ELIMINATE / PAUSE (Low Impact, Not Urgent) - Remove cognitive clutter.
                Conclude with the exact #1 task to execute immediately right now.
            """.trimIndent(),
            keywords = listOf("priority", "sort", "eisenhower", "tasks", "organize", "urgent", "important", "backlog", "triage", "focus")
        ),
        MAXTool(
            id = "pros_cons_organizer",
            name = "Pros/cons organizer",
            category = ToolCategory.DECISION,
            description = "Unbiased breakdown of advantages, disadvantages, hidden trade-offs, and worst-case scenario mitigations.",
            inputLabel = "What decision are you weighing?",
            inputPlaceholder = "e.g. Relocating from Chicago to Austin, Texas for lower taxes and warm weather...",
            suggestedSample = "Quitting my corporate full-time job to start an independent consulting agency with 6 months of living expenses saved.",
            systemInstruction = """
                You are the MAX-N Pros & Cons Strategic Analyst.
                Deliver an objective, high-clarity evaluation:
                1. Strongest Pros (Primary benefits & strategic leverage).
                2. Honest Cons (Real costs, frictions, and sacrifices).
                3. The Hidden Trade-offs: Unintended consequences most people overlook.
                4. Worst-Case Scenario & Mitigation: What is the true downside floor, and can it be survived or reversed?
                5. Recommendation Rule: If condition X is true, proceed; if condition Y is true, wait.
            """.trimIndent(),
            keywords = listOf("pros", "cons", "advantages", "disadvantages", "trade-offs", "dilemma", "benefits", "risks")
        ),
        MAXTool(
            id = "help_me_decide",
            name = "“Help me decide” guided analysis",
            category = ToolCategory.DECISION,
            description = "Diagnostic inquiry uncovering unstated assumptions, emotional blockers, and delivering a reasoned recommendation.",
            inputLabel = "Describe your dilemma in your own words",
            inputPlaceholder = "Tell me what's on your mind, what's holding you back, and what options you are stuck between...",
            suggestedSample = "I received an offer to become team lead at my current company, but it means 50% more meetings and managing people rather than writing code. I enjoy coding, but the pay bump is 20% and my family could use it.",
            systemInstruction = """
                You are MAX-N's empathetic, razor-sharp Decision Mentor.
                Break through analysis paralysis with structured clarity:
                1. Core Conflict Diagnosis: Name the fundamental tension (e.g. Craft Mastery vs. Compensation/Status).
                2. 3 Probing Self-Diagnostic Questions: The critical questions the user must ask themselves.
                3. The 10/10/10 Rule: How this decision will feel in 10 minutes, 10 months, and 10 years.
                4. Practical Compromise / Third Way: Is there a creative middle path not yet considered?
                5. Direct, Actionable Recommendation: What to say and do tomorrow morning.
            """.trimIndent(),
            keywords = listOf("help me decide", "decide", "stuck", "confused", "dilemma", "advice", "guidance", "analysis paralysis", "choice")
        ),

        // ----------------------------------------------------
        // 3. COMMUNICATION
        // ----------------------------------------------------
        MAXTool(
            id = "reply_to_message",
            name = "Reply to message",
            category = ToolCategory.COMMUNICATION,
            description = "Draft context-appropriate replies to emails, text messages, or Slack threads with your chosen tone.",
            inputLabel = "Paste the message received + what you want to communicate",
            inputPlaceholder = "Paste received message and brief intent (e.g. 'I agree but only if deadline moves to Friday')...",
            suggestedSample = "Incoming email: 'Hi John, we really need the revised marketing deck by end of day today or the board meeting tomorrow will be compromised.' My intent: Can't do today because data is incomplete, but will send early tomorrow 8 AM before meeting.",
            systemInstruction = """
                You are the MAX-N Communication Specialist for replies.
                Provide 3 tailored reply variants:
                1. Short & Direct (Concise, zero fluff, mobile-friendly).
                2. Professional & Collaborative (Polished, cordial, corporate-safe).
                3. Friendly & Warm (Conversational, relationship-preserving).
                Each option must directly address the incoming message and execute the user's intent.
            """.trimIndent(),
            keywords = listOf("reply", "respond", "email reply", "message", "slack", "text message", "answer", "draft reply")
        ),
        MAXTool(
            id = "difficult_message_helper",
            name = "Difficult-message helper",
            category = ToolCategory.COMMUNICATION,
            description = "Empathetic yet firm messaging for tough conversations: bad news, boundary setting, and conflict resolution.",
            inputLabel = "What is the difficult message you need to convey?",
            inputPlaceholder = "e.g. Telling a friendly client we have to terminate the contract; or Telling a friend they cannot stay at my apartment...",
            suggestedSample = "Need to tell a long-term freelance client that I cannot continue working with them because their last-minute revisions and lack of clear scope are causing severe burnout.",
            systemInstruction = """
                You are the MAX-N Difficult-Message Specialist.
                Craft communications that preserve dignity while establishing unbreakable boundaries:
                1. Core Communication (Empathetic, clear, zero defensiveness, firm boundary).
                2. A Softer / Diplomatic Variation (Emphasizes gratitude and clean transition).
                3. Boundary Anchor: Anticipate potential pushback or guilt-trips and provide a 1-sentence prepared follow-up response.
            """.trimIndent(),
            keywords = listOf("difficult", "boundary", "hard conversation", "bad news", "conflict", "breakup", "terminate", "firm", "uncomfortable")
        ),
        MAXTool(
            id = "apology_writer",
            name = "Apology writer",
            category = ToolCategory.COMMUNICATION,
            description = "Write sincere, non-defensive apologies that own mistakes, acknowledge impact, and propose concrete solutions.",
            inputLabel = "What happened and who are you apologizing to?",
            inputPlaceholder = "e.g. Missed a key project deadline due to miscommunication; or Snapped at a colleague during a tense meeting...",
            suggestedSample = "I missed a critical client delivery deadline because I underestimated the workload and failed to communicate in advance, causing the client's campaign launch to be delayed by 2 days.",
            systemInstruction = """
                You are the MAX-N Apology Writer.
                Follow the four pillars of a genuine, mature apology:
                1. Clear Ownership (Say 'I was wrong' / 'I apologize' without 'if you felt' or external excuses).
                2. Acknowledgment of Impact (Recognize how it affected their time, trust, or stress).
                3. Concrete Remediation (What is being done right now to resolve or rectify the situation).
                4. Prevention Commitment (Systems or safeguards implemented to ensure it never happens again).
                Provide both an Email / Letter format and a short Chat/Slack message format.
            """.trimIndent(),
            keywords = listOf("apology", "sorry", "apologize", "mistake", "error", "amends", "regret", "forgive")
        ),
        MAXTool(
            id = "request_writer",
            name = "Request writer",
            category = ToolCategory.COMMUNICATION,
            description = "Persuasive, respectful requests for salary raises, deadline extensions, mentor meetings, or approvals.",
            inputLabel = "What are you requesting and from whom?",
            inputPlaceholder = "e.g. Asking manager for a 15% salary raise based on taking over lead duties; or Requesting a 1-week extension on an academic paper...",
            suggestedSample = "Asking my senior VP for a 20-minute coffee chat / mentorship session to discuss career growth and transitions into product management.",
            systemInstruction = """
                You are the MAX-N Request Writer.
                Draft high-conversion, respectful, and persuasive requests:
                1. Hook & Context: Establish rapport and shared interest quickly.
                2. The Value Proposition / Justification: Why granting this request makes sense for both parties.
                3. Low-Friction Call to Action: Make saying 'yes' effortless with specific dates or micro-commitments.
                Provide both a formal version and a brief conversational version.
            """.trimIndent(),
            keywords = listOf("request", "ask", "raise", "extension", "approval", "favor", "mentor", "meeting request", "inquiry")
        ),
        MAXTool(
            id = "complaint_feedback_writer",
            name = "Complaint/feedback writer",
            category = ToolCategory.COMMUNICATION,
            description = "Assertive, professional complaints for defective products, poor service, or billing disputes that demand resolution.",
            inputLabel = "What went wrong and what resolution do you want?",
            inputPlaceholder = "e.g. Airline lost luggage and customer support has been unresponsive for 48 hours; requesting full itemized reimbursement...",
            suggestedSample = "Landlord has ignored 3 maintenance tickets over 2 weeks regarding a leaking bathroom ceiling that has now damaged personal property. Requesting immediate licensed repair and rent credit.",
            systemInstruction = """
                You are the MAX-N Consumer Rights & Professional Complaint Drafter.
                Write an assertive, non-emotional, legally sound complaint:
                1. Objective Chronology of Events (Dates, facts, ticket numbers, failed promises).
                2. Specific Breach or Failure (Warranty, lease agreement, customer service terms).
                3. Explicit Remedy Demanded (Full refund, repair by date X, account credit).
                4. Professional Escalation Warning: Reasonable deadline before escalating to corporate oversight, consumer protection boards, or credit card dispute.
            """.trimIndent(),
            keywords = listOf("complaint", "feedback", "dispute", "refund", "poor service", "customer support", "landlord", "broken", "escalate")
        ),
        MAXTool(
            id = "conversation_starter",
            name = "Conversation starter",
            category = ToolCategory.COMMUNICATION,
            description = "Engaging, high-response conversation starters for networking, social events, cold outreach, or dating.",
            inputLabel = "What is the setting and who is the person or audience?",
            inputPlaceholder = "e.g. Reaching out on LinkedIn to a VP of Design at Stripe; or Message on dating app to someone who likes trail running...",
            suggestedSample = "Cold outreach to a founder in the AI education space whose podcast episode I really liked, wanting to start a connection.",
            systemInstruction = """
                You are the MAX-N Conversation Starter Specialist.
                Generate 5 distinct conversation openers:
                1. Observational & Genuine (Specific praise referencing their actual work).
                2. The Intriguing Question (Thought-provoking inquiry they will enjoy answering).
                3. The Shared Curiosity (Relatable, low-pressure observation).
                4. Ultra-Short / Casual (One-sentence direct ping).
                5. The Value-Add (Offering an insight, resource, or compliment with no immediate ask).
            """.trimIndent(),
            keywords = listOf("conversation", "starter", "icebreaker", "networking", "cold outreach", "intro", "first message", "dating opener")
        ),
        MAXTool(
            id = "polite_refusal_generator",
            name = "Polite refusal generator",
            category = ToolCategory.COMMUNICATION,
            description = "Graciously and firmly decline invitations, unpaid favors, unrealistic scopes, or extra work with zero guilt.",
            inputLabel = "What invitation, project, or favor do you need to decline?",
            inputPlaceholder = "e.g. Declining an invitation to speak at an unpaid conference; or Turning down a friend's request to borrow money...",
            suggestedSample = "Acquaintance asked me to build them a free website for their new business idea over the weekend.",
            systemInstruction = """
                You are the MAX-N Polite Refusal Generator.
                Generate 4 graceful, guilt-free refusal options:
                1. The Warm & Firm Declination (Expresses gratitude, clear 'no', no rambling excuses).
                2. The Professional Scope Boundary ('My current commitments require my full focus').
                3. The Alternative Redirect ('I can't do this, but I recommend checking out X resource').
                4. The Brief Text / Chat Version (1-2 sentences for instant messaging).
            """.trimIndent(),
            keywords = listOf("refusal", "say no", "decline", "reject", "turn down", "invitation", "no", "polite", "boundary", "not interested")
        ),

        // ----------------------------------------------------
        // 4. INFORMATION EXTRACTOR
        // ----------------------------------------------------
        MAXTool(
            id = "extract_names",
            name = "Extract names",
            category = ToolCategory.EXTRACTOR,
            description = "Extract all individual names, organizations, company titles, and key entities into a structured list.",
            inputLabel = "Paste text containing names and entities",
            inputPlaceholder = "Paste meeting minutes, articles, emails, or roster...",
            systemInstruction = "You are the MAX-N Information Extractor for Names. Extract every personal name, job title, and organization/company name from the text. Format as a clean table with columns: [Full Name], [Role / Title], [Organization / Context]. Note any ambiguous mentions.",
            keywords = listOf("extract names", "people", "organizations", "titles", "companies", "entities", "names extractor")
        ),
        MAXTool(
            id = "extract_dates",
            name = "Extract dates",
            category = ToolCategory.EXTRACTOR,
            description = "Extract all deadlines, meeting dates, historical milestones, and scheduled timeframes chronologically.",
            inputLabel = "Paste text with dates and schedules",
            inputPlaceholder = "Paste emails, project specs, contracts, or chat history...",
            systemInstruction = "You are the MAX-N Information Extractor for Dates. Extract all explicit and relative dates, deadlines, milestones, and times. Sort them in chronological order. Format as a table: [Date / Timeframe], [Associated Event / Deadline], [Status / Urgency].",
            keywords = listOf("extract dates", "deadlines", "timeline", "schedule", "calendar", "when", "dates extractor")
        ),
        MAXTool(
            id = "extract_prices",
            name = "Extract prices",
            category = ToolCategory.EXTRACTOR,
            description = "Extract all monetary values, currencies, unit prices, discounts, taxes, and totals from invoices or quotes.",
            inputLabel = "Paste text with pricing, quotes, or financial mentions",
            inputPlaceholder = "Paste invoice text, contractor quotes, receipts...",
            systemInstruction = "You are the MAX-N Information Extractor for Prices. Extract all numerical monetary values, currencies, line items, discounts, fees, and totals. Present as a table: [Item / Description], [Unit Price], [Quantity], [Total Amount]. Compute the overall sum and any notable fee anomalies.",
            keywords = listOf("extract prices", "money", "cost", "currency", "quote", "invoice amount", "rates", "prices extractor")
        ),
        MAXTool(
            id = "extract_addresses",
            name = "Extract addresses",
            category = ToolCategory.EXTRACTOR,
            description = "Extract street addresses, venues, cities, zip codes, suite numbers, and geolocational coordinates.",
            inputLabel = "Paste text containing locations and addresses",
            inputPlaceholder = "Paste event descriptions, emails, shipping notes, contracts...",
            systemInstruction = "You are the MAX-N Information Extractor for Addresses. Extract every physical address, venue name, city, state, country, postal code, and directions. Format as a table: [Location / Venue Name], [Full Street Address], [City / State / Zip], [Special Access / Suite Notes].",
            keywords = listOf("extract addresses", "locations", "venues", "street address", "shipping address", "places", "addresses extractor")
        ),
        MAXTool(
            id = "extract_contacts",
            name = "Extract contact information",
            category = ToolCategory.EXTRACTOR,
            description = "Extract phone numbers, email addresses, social handles, LinkedIn URLs, and websites into ready-to-save cards.",
            inputLabel = "Paste text containing contact details",
            inputPlaceholder = "Paste email signatures, raw directories, bios, business cards...",
            systemInstruction = "You are the MAX-N Information Extractor for Contacts. Extract all contact points: Personal Name, Email Address, Phone Number, Organization, Social Handles/URLs, and Physical Office. Format as structured contact cards and a CSV-ready table.",
            keywords = listOf("extract contacts", "email", "phone", "contact info", "linkedin", "handles", "directory", "contacts extractor")
        ),
        MAXTool(
            id = "extract_action_items",
            name = "Extract action items",
            category = ToolCategory.EXTRACTOR,
            description = "Extract actionable tasks, assigned owners, deliverables, and dependencies from meeting transcripts or notes.",
            inputLabel = "Paste meeting notes, transcripts, or project briefs",
            inputPlaceholder = "Paste meeting transcripts, project updates, or notes...",
            systemInstruction = "You are the MAX-N Information Extractor for Action Items. Parse the input and extract all to-dos. Format as: [Action Item Description], [Assignee / Owner], [Due Date / Deadline], [Priority]. Use Markdown checkboxes `[ ]` for immediate execution.",
            keywords = listOf("extract action items", "tasks", "todos", "meeting notes", "next steps", "deliverables", "assignees", "action items")
        ),
        MAXTool(
            id = "extract_requirements",
            name = "Extract requirements",
            category = ToolCategory.EXTRACTOR,
            description = "Extract strict prerequisites, mandatory criteria, technical specs, and qualifications from job posts or RFPs.",
            inputLabel = "Paste job descriptions, RFPs, grant criteria, or compliance documents",
            inputPlaceholder = "Paste contract, job posting, or specification text...",
            systemInstruction = "You are the MAX-N Information Extractor for Requirements. Separate and extract: 1. Mandatory Requirements (Must-haves), 2. Preferred / Nice-to-have Qualifications, 3. Technical & Compliance Specifications, 4. Potential Disqualifiers. Format clearly with bullet points.",
            keywords = listOf("extract requirements", "qualifications", "prerequisites", "specs", "rfp", "job posting", "criteria", "requirements extractor")
        ),
        MAXTool(
            id = "extract_numbers",
            name = "Extract important numbers",
            category = ToolCategory.EXTRACTOR,
            description = "Extract tracking numbers, account IDs, reference codes, dimensions, serial numbers, and quantities.",
            inputLabel = "Paste text with codes, IDs, dimensions, or statistics",
            inputPlaceholder = "Paste confirmation emails, technical specs, inventory logs...",
            systemInstruction = "You are the MAX-N Information Extractor for Numbers. Extract all critical tracking numbers, reference IDs, confirmation codes, serial keys, dimensions, percentages, and metrics. Provide a clean table: [Identifier / Metric Name], [Value / Code], [Type / Context].",
            keywords = listOf("extract numbers", "tracking number", "order id", "confirmation code", "metrics", "stats", "dimensions", "numbers extractor")
        ),

        // ----------------------------------------------------
        // 5. FORMAT CONVERTER
        // ----------------------------------------------------
        MAXTool(
            id = "format_converter",
            name = "Format converter",
            category = ToolCategory.CONVERTER,
            description = "Convert the same content into: Table, Checklist, Timeline, Bullet points, Step-by-step instructions, FAQ, Structured JSON, Markdown, or Plain text.",
            inputLabel = "Content to convert",
            inputPlaceholder = "Paste any notes, article, list, or text you want converted...",
            suggestedSample = "Product Launch Schedule: Team finishes QA on Oct 10. Beta testers invited Oct 15 with feedback due Oct 22. Marketing kickoff video shoots on Oct 18. Final release candidate build frozen Oct 28. Public launch on Product Hunt and App Store on Nov 2 at 9 AM PST. Post-launch bug fix sprint starts Nov 5.",
            systemInstruction = "You are the MAX-N Universal Format Converter. Convert the user's content faithfully into the requested format target without omitting critical details.",
            keywords = listOf("format", "convert", "table", "checklist", "timeline", "bullets", "instructions", "faq", "json", "markdown", "plain text")
        ),

        // ----------------------------------------------------
        // 6. QUICK AI ACTIONS
        // ----------------------------------------------------
        MAXTool(
            id = "quick_transform",
            name = "Quick AI actions (Transform menu)",
            category = ToolCategory.COMMUNICATION,
            description = "Instant universal text transformations: Explain, Fix, Rewrite, Shorten, Expand, Translate, Summarize, Change tone, Make professional, Make simpler, Bullets, Email.",
            inputLabel = "Text to transform",
            inputPlaceholder = "Paste or type text you want to transform...",
            suggestedSample = "hey team just checking in to see if the report is done yet because client is asking and we kinda need it ASAP thanks",
            systemInstruction = "You are MAX-N's Quick Transform Engine. Transform the user's input based on their chosen transformation action with maximum craftsmanship.",
            keywords = listOf("transform", "explain", "fix", "rewrite", "shorten", "expand", "translate", "summarize", "tone", "professional", "simpler", "bullets", "email")
        )
    )

    val PREBUILT_COMBINATIONS: List<ToolCombination> = listOf(
        ToolCombination(
            id = "doc_to_email",
            title = "Document → Extract → Summarize → Rewrite → Email",
            description = "Take raw meeting notes or text: extracts key facts, summarizes the core takeaways, and writes a crisp executive email.",
            inputPlaceholder = "Paste raw report, meeting transcript, or notes...",
            steps = listOf(
                ToolChainStep(1, "extract_action_items", "Extract Action Items", "Extract all key decisions, action items, and deliverables from the source text."),
                ToolChainStep(2, "quick_transform_summarize", "Summarize", "Synthesize the extracted facts into an executive summary."),
                ToolChainStep(3, "quick_transform_professional", "Make Professional", "Refine the tone into crisp executive language."),
                ToolChainStep(4, "quick_transform_email", "Turn into Email", "Format into a complete, ready-to-send email with subject line.")
            )
        ),
        ToolCombination(
            id = "text_to_polished_copy",
            title = "Text → Grammar → Humanize → Professional → Copy",
            description = "Refines rough, awkward drafts into authentic, grammatically flawless, persuasive professional communication.",
            inputPlaceholder = "Paste rough draft or casual notes...",
            steps = listOf(
                ToolChainStep(1, "quick_transform_fix", "Fix Grammar & Errors", "Fix all grammatical errors and awkward phrasing."),
                ToolChainStep(2, "quick_transform_rewrite", "Humanize & Clarify", "Rewrite to sound warm, natural, and articulate."),
                ToolChainStep(3, "quick_transform_professional", "Make Professional", "Ensure professional poise and business readiness.")
            )
        ),
        ToolCombination(
            id = "notes_to_flashcards",
            title = "Notes → Organize → Summarize → Flashcards",
            description = "Turns messy reading notes, articles, or lecture transcripts into structured study flashcards and recall prompts.",
            inputPlaceholder = "Paste lecture notes, study material, or book excerpts...",
            steps = listOf(
                ToolChainStep(1, "format_converter", "Organize into Concepts", "Restructure the raw notes into clean categorical sections."),
                ToolChainStep(2, "quick_transform_summarize", "Summarize Core Truths", "Extract the 5 most fundamental principles."),
                ToolChainStep(3, "flashcards_builder", "Generate Flashcards & Q&A", "Create 8-10 high-retention Q&A flashcards with Front (Question) and Back (Answer).")
            )
        ),
        ToolCombination(
            id = "job_to_cover_letter",
            title = "Job Description → Analyze → Resume → Cover Letter",
            description = "Analyzes a job posting's hidden requirements, matches your experience, and writes a targeted, compelling cover letter.",
            inputPlaceholder = "Paste the job description + brief notes about your background...",
            steps = listOf(
                ToolChainStep(1, "extract_requirements", "Extract Requirements", "Extract the top mandatory skills, tech stack, and evaluation criteria."),
                ToolChainStep(2, "resume_matcher", "Analyze Alignment", "Identify key strengths and tailor experience to match the role."),
                ToolChainStep(3, "cover_letter_writer", "Draft Targeted Cover Letter", "Write a high-conversion, personalized cover letter that commands attention.")
            )
        )
    )

    fun searchTools(query: String, customTools: List<MAXTool> = emptyList()): List<MAXTool> {
        val all = BUILT_IN_TOOLS + customTools
        if (query.isBlank()) return all

        val terms = query.lowercase().split(" ").filter { it.isNotBlank() }
        return all.filter { tool ->
            terms.all { term ->
                tool.name.contains(term, ignoreCase = true) ||
                tool.description.contains(term, ignoreCase = true) ||
                tool.category.displayName.contains(term, ignoreCase = true) ||
                tool.keywords.any { it.contains(term, ignoreCase = true) }
            }
        }
    }
}
