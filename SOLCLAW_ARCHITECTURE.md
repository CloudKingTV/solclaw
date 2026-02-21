# SOLCLAW × SOLANA SEEKER — Mobile-Native Architecture v3.0

> **PROJECT BRIEF FOR AI-ASSISTED DEVELOPMENT**
> This document is the complete architectural specification for SolClaw. It should be treated as the single source of truth for all development decisions. When building any component, reference the relevant section here.

---

## 1. EXECUTIVE SUMMARY

SolClaw is OpenClaw rebuilt from the ground up as a **mobile-native Android application** for the Solana Seeker phone. Where OpenClaw gives you a personal AI agent on your desktop that can control your computer, browse the web, manage files, and execute tasks across messaging platforms — SolClaw delivers the exact same experience in your pocket.

The agent lives inside the SolClaw app. You talk to it in a native chat interface. It can open apps on your phone, navigate them, tap buttons, fill forms, browse the web, post on X, execute swaps on Jupiter, monitor whale wallets, scan pump.fun launches — everything OpenClaw does on desktop, SolClaw does on Seeker.

Your agent is an NFT. Mint it with one tap using your Seeker's Seed Vault, and you're live. No CLI. No Node.js installation. No API key configuration. No Docker containers.

**THE ONE-LINER:** SolClaw = OpenClaw that runs on your Solana Seeker phone with zero technical setup. Mint an NFT, get an AI agent that can use your entire phone.

---

## 2. THE PROBLEM SOLCLAW SOLVES

### 2.1 OpenClaw Is Brilliant But Inaccessible

**Problem 1: Setup is Brutal**
To run OpenClaw you need: Node.js (correct version), clone a GitHub repo, configure env vars, obtain API keys from Anthropic/OpenAI, set up Docker, configure messaging bridges (Telegram BotFather, Discord tokens, WhatsApp Business API), edit YAML config files, manage dependencies. One of OpenClaw's own maintainers warned that if you can't run a command line, the project is too dangerous for you. Bounce rate for first-time setup: 99%+.

**Problem 2: Desktop Only**
OpenClaw runs as a local Node.js gateway daemon. Requires persistent process, filesystem access, browser control via CDP, shell execution. None works on mobile. Agent dies when you close your laptop. In a world where most people spend 4+ hours daily on their phone, desktop-only misses the largest market entirely.

### 2.2 The Crypto Problem
No unified AI agent natively understands the Solana ecosystem. Existing tools are fragmented — separate apps for DEX trading, wallet monitoring, NFT tracking, social feeds, governance. An AI agent that orchestrates across all of these, on your phone, owned as a tradeable NFT? Doesn't exist yet.

---

## 3. THE VISION

You open the Solana dApp Store on your Seeker. Download SolClaw. Authenticate with Seed Vault (fingerprint). Tap "Mint Agent." Your agent NFT is created on-chain. The app drops you into a native chat interface. You say: "Monitor any wallet that bought more than $50K of a token in the last hour on Jupiter, alert me immediately, and if it's a token I'm already holding, auto-buy $200 more." The agent sets up monitoring, opens relevant apps when needed, sends push notifications. Install to operational in under 60 seconds.

### 3.1 What the Agent Can Do

**App Control:**
- Open any installed app (Phantom, Jupiter, Backpack, Tensor, X, Discord, Telegram, Chrome, etc.)
- Navigate app UIs: tap buttons, fill forms, scroll, swipe, select dropdowns
- Read screen content: prices, balances, notifications, messages, feed items
- Execute multi-step workflows across multiple apps without user intervention

**DeFi Operations:**
- Execute token swaps via Jupiter (open app, input trade, confirm with Seed Vault)
- Monitor liquidity pools on Raydium/Orca, alert on changes
- Track staking positions on Marinade, Jito, native SOL staking
- Scan pump.fun for new launches matching user-defined criteria
- Monitor whale wallets and replicate trades with configurable parameters

**Social & Research:**
- Open X, browse timeline, post tweets, reply, like, retweet
- Scrape Crypto Twitter for alpha: track specific accounts, monitor token mentions
- Open Discord servers, read channels, post messages, manage community
- Browse the web: research tokens, read articles, check CoinGecko/Birdeye

**System & Utilities:**
- Push notifications with deep-linked context
- Manage phone settings, take screenshots, access camera for QR codes
- Schedule recurring tasks (daily portfolio summary, weekly DeFi harvest)
- Interact with other SolClaw agents (future multi-agent mesh)

---

## 4. CORE ARCHITECTURE

### 4.1 Architecture Overview

Hybrid architecture: native Android app as local orchestrator (replaces OpenClaw's Node.js Gateway), LLM inference in cloud via API calls. Phone control via Android's Accessibility Service API — same mechanism used by DroidRun, password managers, and screen readers.

| Layer | Component | Role |
|-------|-----------|------|
| Presentation | SolClaw App (Kotlin/Jetpack Compose) | Chat UI, agent dashboard, marketplace, settings |
| Orchestration | Agent Runtime Service (Android foreground service) | Message routing, task queue, session management, heartbeat |
| Intelligence | Cloud LLM API (Anthropic Claude / OpenAI) | Reasoning, planning, tool selection, NLU |
| Phone Control | Accessibility Service + Screen Parser | Read UI trees, execute taps/swipes/typing across all apps |
| Browser | WebView or Chrome Custom Tab + JS bridge | Web browsing, page parsing, form filling |
| Blockchain | Solana SDK + MWA + Seed Vault | Transaction signing, NFT minting, on-chain state |
| Storage | Local SQLite + Arweave/IPFS (anchored to NFT) | Agent memory, conversation history, skill configs, state snapshots |
| Distribution | Solana dApp Store | App distribution to 150K+ Seeker devices |

### 4.2 The Agent Loop

1. **INPUT:** User sends message in SolClaw chat (text, voice-to-text, or quick action button)
2. **CONTEXT:** Agent Runtime assembles prompt: system instructions (SOUL.md) + conversation history + current screen state + active skills + memory
3. **REASON:** Cloud LLM processes context, returns structured response with reasoning + planned actions
4. **ACT:** Agent Runtime executes actions: open app, tap element, type text, navigate, read screen, call API
5. **OBSERVE:** Screen state captured again after action (accessibility tree + optional screenshot)
6. **LOOP:** If task incomplete, return to step 2 with updated state. If complete, respond to user.

> **SEMANTIC SNAPSHOTS:** Like OpenClaw, SolClaw parses the Accessibility XML tree into structured semantic snapshots rather than relying on screenshots + OCR. Reduces token costs ~90% vs vision-based approaches, increases accuracy, enables faster action cycles. Screenshots only as fallback when accessibility tree is empty/ambiguous.

---

## 5. PHONE CONTROL LAYER

### 5.1 How It Works

When user enables SolClaw's Accessibility Service (one-time onboarding), the app gains ability to:
- Read complete UI tree of any foreground app as structured XML (element type, text content, coordinates, clickable state, content descriptions)
- Perform actions on any interactive element: tap, long-press, scroll, swipe, type text, clear fields
- Monitor screen changes in real-time via accessibility events
- Launch apps via Android Intent system
- Read notifications as they arrive

This is exactly how DroidRun works (3.8K GitHub stars, 63% success rate on AndroidWorld benchmark, raised €2.1M). Also how 1Password detects login fields, Tasker automates workflows, TalkBack provides screen reading.

### 5.2 Action System (22 actions, modeled after DroidRun)

| Action | Description | Example |
|--------|-------------|---------|
| `tap(element)` | Tap interactive UI element by ID/coordinates | Tap "Swap" button in Jupiter |
| `longPress(element)` | Long-press on element | Long-press to copy wallet address |
| `type(text)` | Input text into focused field | Type "500" into swap input |
| `clearField()` | Clear current text field | Clear old search query |
| `scroll(direction)` | Scroll within container | Scroll token list to find SOL |
| `swipe(direction)` | Directional swipe gesture | Swipe between app screens |
| `back()` | Press Android back button | Navigate back |
| `home()` | Press home button | Return to home screen |
| `launchApp(pkg)` | Open app by package name | Launch Phantom wallet |
| `openUrl(url)` | Open URL in browser | Open birdeye.so/token/... |
| `screenshot()` | Capture current screen | Fallback when a11y tree insufficient |
| `readScreen()` | Parse full accessibility tree | Understand current UI state |
| `waitFor(condition)` | Wait for UI element/state change | Wait for tx confirmation dialog |
| `notification(msg)` | Send push notification | Alert: Whale bought $2M BONK |

### 5.3 Screen State Parser

Every action cycle begins with parsing current screen:
1. **Filter:** Remove non-interactive and invisible elements
2. **Classify:** Tag each element (button, input, text, list, image, toggle, link)
3. **Index:** Assign sequential IDs so LLM can reference by number
4. **Contextualize:** Include parent container info (e.g., "Button #7 'Confirm' inside 'Swap Confirmation' dialog")
5. **Compress:** Strip redundant attributes, merge text nodes, cap ~2K tokens per snapshot

### 5.4 Google Play Bypass — dApp Store Advantage

> **CRITICAL:** As of January 28, 2026, Google Play explicitly prohibits any Accessibility API use that enables an app to "autonomously initiate, plan, and execute actions or decisions." SolClaw CANNOT be on Google Play. The Solana dApp Store has no such restriction. This makes Seeker + dApp Store the ONLY viable distribution path for a fully autonomous mobile AI agent in crypto. This is a moat, not a limitation.

---

## 6. OPENCLAW → SOLCLAW COMPONENT MAPPING

| OpenClaw Component | SolClaw Equivalent | Key Differences |
|---|---|---|
| Node.js Gateway (daemon) | Android Foreground Service | Kotlin-native, battery-optimized |
| CLI (`openclaw …`) | In-app chat + quick actions | No terminal; all via natural language or tap |
| SOUL.md (personality) | Agent Personality (stored on-chain) | Bundled into NFT metadata on Arweave/IPFS |
| AGENTS.md (config) | Agent Config (SQLite + on-chain) | Local for speed, synced to Arweave for portability |
| MEMORY.md (memory) | Memory Store (SQLite + Arweave) | Indexed for mobile search, compressed for on-chain |
| Skills (SKILL.md files) | Mobile Skill Packs (Kotlin modules) | Accessibility-based phone control replaces shell/file/browser |
| Browser Control (CDP) | Accessibility Service + WebView bridge | Controls real apps; can also control Chrome |
| Shell Execution | Android Intent + Accessibility actions | No shell; system APIs and UI automation |
| Channel Adapters | In-app chat IS the channel | Agent can also open WhatsApp/Telegram as needed |
| Heartbeat Daemon | Android WorkManager + AlarmManager | System-scheduled; survives app closure |
| Lane Queue | Coroutine-based Task Queue | Kotlin coroutines; same isolation model |
| WebChat UI | Jetpack Compose Chat UI | Native mobile with haptics, gestures |
| Pi Agent Core | SolClaw Agent Core | Same loop; Kotlin-native with mobile tools |
| Workspace Files | Agent NFT State Bundle | All state as NFT metadata — enables trading |

---

## 7. SEEKER HARDWARE INTEGRATION

### 7.1 Seed Vault
- Hardware-encrypted secure enclave for private key custody. Keys never leave the chip.
- **SolClaw Use:** All transaction signing routes through Seed Vault. Agent CANNOT sign transactions autonomously — user must authenticate via fingerprint/double-tap. This is the critical safety boundary.
- **UX:** Agent prepares tx → presents summary in chat → user confirms with biometric → tx submitted

> **SAFETY PRINCIPLE:** Agent can prepare, recommend, and queue transactions. NEVER sign without Seed Vault authentication. Hardware-enforced even against prompt injection.

### 7.2 Genesis Token
- Soulbound NFT proving genuine Seeker ownership. Cannot be transferred.
- Gates: "Verified Seeker Mint" badge, reduced fees (3% vs 5%), exclusive skill packs, priority features

### 7.3 TEEPIN (Three-Layer Trust)
- TEE + device attestation for proving genuine Seeker hardware
- Verifies agents minted on real devices (not emulators). Prevents bot farms.

### 7.4 SKR Token
- Native ecosystem token for rewards/governance
- Earn through: minting agents, training, marketplace activity

### 7.5 Mobile Wallet Adapter (MWA)
- Solana Mobile's MWA SDK (Kotlin) for all blockchain interactions
- Native, secure communication with Seed Vault without external wallet apps

---

## 8. AGENT NFT ARCHITECTURE

### 8.1 The Agent IS the NFT

NFT = Metaplex Core standard. On-chain identity, ownership record, pointer to complete state bundle on Arweave/IPFS.

**On-Chain (Metaplex Core NFT):**
- Agent name, avatar, creation timestamp
- Owner's wallet address
- Arweave/IPFS URI to full state bundle
- Version counter (incremented on sync)
- Skill manifest hash
- TEEPIN attestation flag
- Marketplace listing status and price

**Off-Chain State Bundle (Arweave/IPFS):**
- `SOUL.md` — Personality: tone, expertise, behavioral rules, communication style
- `MEMORY.md` — Persistent memory: facts, preferences, decisions, context
- `SKILLS.json` — Installed skill packs with configs
- `CONFIG.json` — Agent settings: LLM preference, notifications, watchlists
- `HISTORY` (compressed) — Recent conversation history
- **NO credentials** — API keys, wallet secrets, auth tokens NEVER in state bundle

### 8.2 State Sync
Local state in SQLite for speed. Periodic snapshots to Arweave/IPFS with NFT URI update:
- After significant learning events
- On configurable schedule (default: daily)
- Before marketplace listing (mandatory)
- On user request

### 8.3 Minting Flow
1. User taps "Mint Agent"
2. Chooses template (DeFi Alpha Hunter, Community Manager, Research Analyst, Custom)
3. Names agent, optionally customizes personality
4. Seed Vault authenticates (fingerprint)
5. Metaplex Core NFT created; initial state bundle uploaded to Arweave
6. Agent immediately active. User dropped into chat.

**Total time: under 60 seconds.**

---

## 9. AGENT MARKETPLACE

### 9.1 Listing Flow
1. Seller taps "List on Marketplace"
2. Sanitization Engine strips all credentials, API keys, auth tokens, wallet refs, personal data
3. Sanitized state bundle uploaded to Arweave as "listing snapshot"
4. Seller sets price (SOL) and duration
5. Agent appears with stats: age, skill count, memory depth, training hours, TEEPIN badge

### 9.2 Purchase Flow
1. Buyer browses, views stats and personality preview
2. Taps "Buy" → Seed Vault authenticates payment
3. NFT transfers; state bundle downloaded to buyer's device
4. Credential Setup: buyer connects own API keys, wallet permissions, app authorizations
5. Agent live on buyer's device with seller's training intact

> **BUYING THE BRAIN, NOT THE KEYS:** Sanitization ensures buyers get intelligence without private data. Like hiring a trained employee — they bring expertise, not passwords.

### 9.3 Revenue
| Fee Type | Amount | Recipient |
|----------|--------|-----------|
| Mint Fee | 0.05 SOL | SolClaw Treasury |
| Marketplace Transaction | 5% of sale | SolClaw Treasury |
| Genesis Token Discount | 3% (vs 5%) | Reduced for Seeker-verified |
| Arweave Upload | Variable (<$0.01) | Arweave Network |

---

## 10. MOBILE SKILL SYSTEM

### 10.1 Pre-Built Skill Packs

| Skill Pack | Capabilities | Implementation |
|-----------|-------------|----------------|
| DeFi Monitor | Jupiter swaps, Raydium pools, Orca whirlpools, staking yields | API polling + Accessibility app interaction |
| Whale Tracker | Monitor wallets >$X, replicate trades, Helius webhooks | Helius API + push notifications |
| Token Scanner | Pump.fun launches, Raydium new pools, Tensor NFTs, Magic Eden | API polling + pattern matching + filters |
| Social Agent | Post on X, monitor CT, Discord mgmt, Telegram moderation | Accessibility Service controls apps |
| Portfolio Manager | Real-time valuation, P&L, tax export, rebalancing | Birdeye/Jupiter APIs + local compute |
| Research Assistant | Token analysis, whitepaper summary, team investigation | Browser automation + LLM analysis |
| Governance | Realms proposals, vote reminders, delegation | Realms API + notification scheduling |

### 10.2 Custom Skills
Users create via natural language: "Create a skill that checks if any token drops >15% in an hour, sell half." Agent translates to structured skill definition. Advanced users write JSON directly or import community skills.

---

## 11. LLM INTEGRATION & COST MODEL

### 11.1 Model Routing
- **Primary (Complex):** Claude Sonnet 4.5 / GPT-4o — multi-step planning, app navigation
- **Secondary (Simple):** Claude Haiku 4.5 / GPT-4o Mini — quick lookups, formatting
- **Heartbeat/Monitoring:** Cheapest available
- **Fallback Chain:** Auto-rotate on rate limit/downtime

### 11.2 Credit System
| Tier | Credits | Price | Best For |
|------|---------|-------|----------|
| Starter (with mint) | ~500 interactions | Included in 0.05 SOL mint | Trying out |
| Explorer | ~5K/month | 0.5 SOL/month | Casual monitoring |
| Power User | ~25K/month | 2 SOL/month | Active trading |
| Unlimited | Unlimited | 5 SOL/month | Heavy users |
| BYOK | Unlimited (your bill) | Free platform fee | Developers |

---

## 12. SECURITY MODEL

### 12.1 Seed Vault Boundary
Agent cannot sign transactions without Seed Vault biometric. Hardware-enforced. Even if compromised by prompt injection, malicious skill, or corrupt marketplace bundle — physically cannot move funds.

### 12.2 Permission Tiers
| Tier | Autonomous | Requires Seed Vault |
|------|-----------|-------------------|
| Read-Only | Read screens, monitor prices, browse | N/A |
| Notify | + push notifications, write memory | N/A |
| Interact | + open apps, navigate, fill forms, post social | N/A |
| Transact | + prepare transactions | Every tx, transfer, mint, swap |

Default: "Interact" — everything except money movement without fingerprint.

### 12.3 Prompt Injection Defense
- Screen content sanitized before LLM prompts
- System prompt hardened against instruction-following from screen
- Marketplace bundles scanned for injection before import
- Rate limiting prevents runaway loops
- User can kill agent instantly via notification quick-action

### 12.4 Data Privacy
- All data stored locally by default
- Arweave/IPFS sync optional and encrypted
- Marketplace sanitization strips personal data
- LLM calls via HTTPS; no data stored by providers
- TEEPIN verifies genuine hardware

---

## 13. TECHNICAL STACK

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| Mobile App | Kotlin + Jetpack Compose | Native performance; required for Accessibility Service |
| Agent Runtime | Kotlin Coroutines + Foreground Service | Persistent background execution |
| UI Framework | Material 3 + Custom Design System | Modern native feel |
| Screen Parser | AccessibilityService + UIAutomator APIs | System-level app control without root |
| Browser Control | Android WebView + JS Bridge | In-app web browsing with agent control |
| LLM Integration | OkHttp + Retrofit (Anthropic/OpenAI APIs) | Reliable HTTP with streaming |
| Local Database | Room (SQLite) | Memory, history, skill configs |
| Blockchain | Solana Kotlin SDK + MWA 2.0 + Metaplex Core | NFT minting, marketplace, Seed Vault |
| State Storage | Arweave (via Irys/Bundlr) + IPFS | Permanent decentralized state |
| Push Notifications | Firebase Cloud Messaging | Real-time alerts when backgrounded |
| Background Tasks | Android WorkManager + AlarmManager | Heartbeat, monitoring, scheduled skills |
| Build System | Gradle + GitHub Actions CI/CD | Automated builds, dApp Store publishing |

---

## 14. DISTRIBUTION STRATEGY

### Primary: Solana dApp Store
- 150K+ Seeker owners, zero fees, no Accessibility API restrictions, NFT-based registry, 2-5 day review, Season 2 rewards

### Secondary: Direct APK / Sideloading
- solclaw.xyz APK for non-Seeker Android. Full functionality minus Seed Vault (uses Phantom/MWA instead).

### NOT Available On
- Google Play (Accessibility policy blocks autonomous AI agents)
- Apple App Store (no Accessibility equivalent; no sideloading outside EU)

---

## 15. COMPETITIVE LANDSCAPE

| Product | What It Does | SolClaw Advantage |
|---------|-------------|-------------------|
| OpenClaw | Desktop AI agent | Mobile + NFT ownership + zero setup |
| DroidRun | Android agent framework (dev tool) | Consumer product, no coding |
| Candy AI (Seeker) | AI video generation | General agent, full device control |
| Manus AI | Cloud-based AI agent | On-device, owns data, Solana-native |
| ChatGPT/Claude apps | Chat with limited tools | Controls entire phone |
| Telegram bots | Chat-based crypto bots | Full agent with memory + phone control |

No product combines: mobile AI agent + full phone control + NFT ownership + tradeable marketplace + Solana DeFi skills + Seeker hardware + zero-setup UX.

---

## 16. DEVELOPMENT ROADMAP

### Phase 1: Foundation (Weeks 1–4)
- Native Android app shell (Jetpack Compose chat UI)
- Accessibility Service: screen reading + basic actions
- LLM integration (Claude API): agent loop with screen context
- Seed Vault + MWA for wallet signing
- Basic agent minting (Metaplex Core)
- Local state persistence (Room/SQLite)

### Phase 2: Skills & Intelligence (Weeks 5–8)
- Screen parser optimization (compressed semantic snapshots)
- Multi-app navigation: 10+ target apps
- DeFi skill pack: Jupiter, whale tracking, pump.fun
- Social skill pack: X, Discord, Telegram
- Agent personality system (SOUL.md equivalent)
- Persistent memory + Arweave state sync
- Push notification pipeline

### Phase 3: Marketplace & Polish (Weeks 9–12)
- Marketplace smart contracts
- Sanitization engine
- Buyer credential setup
- Credit/subscription system
- Genesis Token gating + SKR rewards
- TEEPIN attestation
- dApp Store submission

### Phase 4: Launch — Solana Accelerate Miami (May 2026)
- Public launch at conference
- SagaDAO House live demo
- Partnership announcements
- Community skill marketplace opens

### Phase 5: Post-Launch (Months 4–6)
- Multi-agent support
- Agent-to-agent communication
- Voice interaction (ElevenLabs)
- Community skill SDK
- Non-Seeker Android support
- Advanced marketplace: auctions, skill sales, agent breeding

---

## 17. WHY SOLCLAW WINS

1. **Only mobile AI agent with full phone control in crypto.** Google Play ban = this can't exist on mainstream stores. dApp Store is the only path.
2. **Solves OpenClaw's two fatal flaws.** Zero setup + mobile-native = accessible to everyone.
3. **You own your agent.** NFT = your property. Trade it, sell it, hold it. Intelligence has transferable value.
4. **Hardware security solves trust.** Seed Vault = agent physically can't steal funds.
5. **Marketplace flywheel.** Train → list → earn. Buy → get instant value. Every trade = platform revenue.
6. **Seeker alignment = distribution cheat code.** 150K+ crypto-native users starving for quality dApps. AI category nearly empty.

---

*SolClaw — Your AI agent. Your phone. Your NFT.*
*Launching at Solana Accelerate Miami — May 2026*
