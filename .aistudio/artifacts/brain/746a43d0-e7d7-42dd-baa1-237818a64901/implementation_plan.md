# Apex Nexus: Shizuku Bridge, Boot Diagnostics, Movable Widgets & AI Node Routing

An architectural plan to implement the explicit `ShizukuTerminalBridge`, a Room-backed `LifecycleObserver` for boot crash diagnostics, a dedicated `CyberpunkTheme` composable wrapper, interactive movable/resizable persistent widgets for Tactical Comms with session isolation, and an automated AI Squad Quad node connector routing tagged `.txt` and `.zip` packages with step-by-step confirmations.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Decisions from Clarification:**
> 1. **Tactical Comms Widgets**: Movable overlay cards with minimize, drag gesture positioning, and dimensional controls (Desktop vs Mobile User-Agent, fresh session vs retained storage).
> 2. **AI Squad Quad Package Protocol**: Dual-format transmission supporting both tagged `.txt` text bundles and compressed `.zip` multi-file packages with manifest headers.
> 3. **Automated Step Confirmations**: Modal confirmation dialogs ("Are you sure...?" with "Confirm" / "Cancel") for widget spawning, connector node provisioning, and AI package transmissions.

---

### 1. Overview & Core Concept

This update expands Apex Nexus into a flexible multi-widget command workstation and autonomous file-routing nexus:
- **`ShizukuTerminalBridge`**: A standalone Kotlin component that connects to the Shizuku IPC binder, monitors permission status, and dispatches shell commands via Termux broadcast/service intents with non-root security.
- **`BootLifecycleObserver`**: An Android Lifecycle observer tracking cold boot progression, service initialization, and memory statistics into Room SQLite to pinpoint any crash causes.
- **`CyberpunkTheme`**: A distinct theme composable featuring neon green accents (`#39FF14`, `#00FF66`), matrix matrix-emerald, deep charcoal surfaces (`#0D1117`, `#161B22`), and tactical monospace typography.
- **Movable Persistent Widgets**: A dynamic windowing system for "Tactical Comms" allowing operators to spawn independent, concurrent, draggable, and resizable web/app widgets with choice of Desktop/Mobile viewport and fresh session isolation.
- **AI Squad Quad Connector Node**: A local automated node that packages AI messages into tagged `.txt` and `.zip` bundles, routes them to designated recipient AIs, inspects inbound packages, and automates responses.

---

### 2. User Experience & Visual Design

#### Cyberpunk Visual Identity
- **Palette**: Deep Charcoal Background (`#0D1117`), Tactical Slate Surface (`#161B22`), Neon Acid Green (`#39FF14`), Matrix Emerald (`#00E676`), Cyber Cyan (`#00F0FF`), Alert Crimson (`#FF3366`).
- **Typography & Atmosphere**: High-contrast monospaced headers, glowing neon borders (`1.dp` with soft alpha glow), status badges, and compact data readouts.

#### Key User Flows & Screen Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                      APEX NEXUS // CYBERPUNK HUD                       │
│  [BOOT: STABLE] [SHIZUKU: LINKED] [NODE: :8765] [THEME: ACID_GREEN]    │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  ┌─ TACTICAL COMMS: MOVABLE PERSISTENT WIDGETS ─────────────────────┐  │
│  │ [SPAWN NEW WIDGET] -> Dialog: "Are you sure you want to spawn?"  │  │
│  │ ┌─ Movable Widget #1: Discord Web (Desktop Mode) ──────────────┐ │  │
│  │ │ [DRAG BAR] Discord Web [360x320] [ISOLATED]   [_] [□] [X]     │ │  │
│  │ │ • Web contents running in dedicated fresh session             │ │  │
│  │ └──────────────────────────────────────────────────────────────┘ │  │
│  │ ┌─ Movable Widget #2: Termux Web Console (Mobile Mode) ────────┐ │  │
│  │ │ [DRAG BAR] Termux Console [320x240] [RETAINED] [_] [□] [X]    │ │  │
│  │ │ • Web/shell terminal with persistent storage                  │ │  │
│  │ └──────────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                        │
│  ┌─ AI SQUAD QUAD: CONNECTOR & FILE PACKAGE ROUTING NODE ───────────┐  │
│  │ [NEW CONNECTOR] -> Automates local node setup with confirmation  │  │
│  │ Active Node: node_alpha_99 (:8765) | Tag: @copilot <-> @grok     │  │
│  │ [TRANSMIT PACKAGE]                                               │  │
│  │   • Package: payload_20261024.zip (.txt / .zip with tags)        │  │
│  │   • Recipient: @copilot                                          │  │
│  │   • Status: ROUTED -> RECEIVED -> AUTO-REPLIED                   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                        │
│  ┌─ BOOT DIAGNOSTICS & SHIZUKU TERMINAL DRAWER ─────────────────────┐  │
│  │ Lifecycle Events: ON_CREATE (0ms) -> ON_START (42ms) -> ON_RESUME│  │
│  │ $ shizuku termux "uptime" -> Status: DISPATCHED_OK               │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale & Alternatives Considered |
|---|---|---|
| **Shizuku Terminal Bridge** | Dedicated `ShizukuTerminalBridge` with binder listener and Termux broadcast intent | Clean architectural separation. Directly fulfills prompt requirement while keeping non-root security boundaries intact. |
| **Boot Crash Tracking** | `BootLifecycleObserver` writing to Room SQLite `boot_diagnostic_logs` | Persists exact millisecond timestamps of every lifecycle transition and memory reading so crashes can be immediately diagnosed even after reboot. |
| **Widget Architecture** | Movable Compose `Box` overlay with `pointerInput` drag offset, resizing, and sandboxed `WebView` | Avoids complex multi-display Android flags for in-app widgets. Allows real-time dragging, minimizing, and independent cookie jar management. |
| **Package Format** | Dual format: tagged `.txt` (lightweight plain text) & `.zip` (multi-file with JSON manifest) | Balances rapid text exchanges with complex multi-file AI coordination payloads. |
| **Step Confirmations** | Reusable `TacticalConfirmDialog` prompting "Are you sure...?" with Confirm/Cancel | Ensures operator oversight before spawning resource-heavy widgets, creating network nodes, or transmitting packages. |

---

### 4. Technical Architecture & Component Structure

```
com.example
├── bridge/
│   ├── ShizukuTerminalBridge.kt         [New standalone Shizuku binder & Termux intent bridge]
│   └── AiPackageRouter.kt               [Packaging & unpacking of tagged .txt and .zip payloads]
├── data/
│   ├── database/
│   │   ├── BootDiagnosticEntity.kt      [New: Room entity for boot lifecycle event logging]
│   │   ├── ConnectorNodeEntity.kt       [New: Room entity for local AI routing nodes]
│   │   └── ApexDao.kt                   [Updated DAO with queries for diagnostics & nodes]
├── diagnostic/
│   └── BootLifecycleObserver.kt         [New: DefaultLifecycleObserver recording startup states]
├── ui/
│   ├── theme/
│   │   └── CyberpunkTheme.kt            [New: Composable theme wrapper with tactical neon green palette]
│   └── screens/
│       ├── TacticalCommsWidgetConsole.kt[New: Movable, resizable, isolated web/app widget deck]
│       └── SquadQuadConnectorScreen.kt  [New: Local node setup & tagged .txt/.zip routing console]
└── MainActivity.kt                      [Wires LifecycleObserver, CyberpunkTheme, and new consoles]
```

#### Step-by-Step Implementation Sequence:
1. **Module 1 - Shizuku & Termux Bridge**: Create `ShizukuTerminalBridge.kt` implementing `OnBinderReceivedListener`, `checkSelfPermission`, and `executeTermuxCommandViaIntent`.
2. **Module 2 - Boot Lifecycle Observer & Logging**: Create `BootDiagnosticEntity`, update `ApexDatabase`, and implement `BootLifecycleObserver` registering Activity & Application lifecycle events.
3. **Module 3 - Cyberpunk Theme Composable**: Create `CyberpunkTheme.kt` with neon green palette (`#39FF14`, `#0D1117`), high-density monospace typography, and tactical glow card borders.
4. **Module 4 - Tactical Comms Movable Widgets**: Build `TacticalCommsWidgetConsole` with interactive spawn modal, Desktop/Mobile view, dimensions, session isolation, drag gesture positioning, and step confirmation.
5. **Module 5 - AI Squad Quad File Package Routing**: Build `AiPackageRouter` and `SquadQuadConnectorScreen` for local node creation, tagged `.txt`/`.zip` generation and extraction, automated dispatching, and confirmation modals.
6. **Module 6 - Main Activity Integration & Verification**: Wire all modules into `MainActivity.kt`, verify compilation with `compile_applet`, and run unit/Robolectric tests.
