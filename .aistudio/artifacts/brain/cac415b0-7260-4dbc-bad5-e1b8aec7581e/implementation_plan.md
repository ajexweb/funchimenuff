# BoxHunter: 10-Box Multiplayer Hide & Seek Coin Game

A high-stakes, real-time multiplayer 10-box hide-and-seek game where two players wager equal coins into a prize pool. One player conceals their avatar in one of ten mystery vault boxes, and the challenger gets three suspenseful attempts to find them. The victor takes the entire coin bounty.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Authentication & Firebase Integration**: In the AI Studio Android environment, Firebase Firestore backend is provisioned with Google Sign-In via Jetpack Credential Manager. We implement one-tap Google Sign-In with instant player nickname and avatar setup. This replaces insecure manual passwords, guarantees zero fake bots, enables cross-device cloud sync, and powers real-time match rooms.

- **Theme & Atmosphere**: Neon Cyber Vault aesthetic with holographic 3D-styled mystery boxes, pulsing cyber-rings, atmospheric dark backgrounds, and vibrant gold coin particle effects.
- **Matchmaking & Rooms**: Dual mode — 6-character private Room Codes to invite friends directly, plus a Quick Play lobby with standard wagers (10, 25, 50, 100 coins).
- **Match Rules & Escrow**:
  - Both players wager equal coins (deducted into match escrow upon starting).
  - New players start with **50 Coins** on initial signup.
  - Hider has 20 seconds to secretly select 1 of 10 boxes.
  - Seeker has 3 chances to find the hider. If seeker discovers the box within 3 tries, seeker wins the pot; if all 3 guesses fail, hider wins the pot.
  - Anti-cheat protocol: The hider's selection is secured and only unlocked/validated on guess match or round conclusion.
- **Power-Ups & Shop**:
  - *Box Radar* (highlights 2 guaranteed empty boxes).
  - *4th Guess Pass* (extra emergency guess for the seeker).
  - *Cosmetic Box Skins* (Cyber Neon, Golden Vault, Obsidian Dragon, Holo Prism).
- **Player Progression & Daily Rewards**:
  - Level system starts at Level 0 with dynamic XP earned per match, win streaks, and coin thresholds.
  - Daily login streak wheel giving bonus coins (10 to 100 coins).
  - Global High-Coin & Win-Streak Leaderboard refreshed in real time.
  - In-game real-time chat with quick tactical emotes and text messaging in rooms and live matches.

---

## 1. Overview & Core Concept

### What It Does
**BoxHunter** connects two players across devices for head-to-head tactical hide-and-seek:
1. **Lobby & Wager**: A player hosts a room, picks a coin wager (e.g. 25 coins), and shares the room code or waits in matchmaking. The guest joins and matches the wager.
2. **Concealment Phase**: The Hider selects 1 out of 10 glowing cyber boxes within 20 seconds.
3. **Investigation Phase**: The Seeker inspects the 10 boxes and taps up to 3 boxes. Each selection triggers an opening animation with sound/haptic cues (Empty with smoke or Found with celebratory burst).
4. **Payout & Leveling**: Winner takes the combined coin pool (e.g. 50 coins). Both players gain XP to increase their player level, update global rankings, and unlock new box themes.

### Target Audience & Persona
Casual competitive gamers, friends looking for quick 1-2 minute social matches, and players who enjoy mind games, deduction, and leaderboard competition.

### Key Value
Pure human-vs-human competitive thrill with real stakes, instant real-time room sync, rich tactile graphics, and no fake or simulated opponents.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Welcome & Authentication**: Smooth Google Sign-In -> Welcome bonus modal (+50 Coins awarded) -> Interactive 30-second visual tutorial explaining the 10-box 3-chance mechanics.
2. **Main Hub (Home Screen)**:
   - Header with player avatar, level badge, XP progress bar, and animated coin wallet.
   - Quick Action Cards: "Create Room", "Join with Code", "Quick Match", and "Claim Daily Reward".
   - Bottom Navigation: `Lobby`, `Leaderboard`, `Shop`, and `Profile`.
3. **Room & Matchmaking Screen**:
   - Host chooses coin wager (10, 25, 50, 100).
   - Generates 6-character room code with 1-tap "Share Invite" copy button.
   - Opponent joins -> Ready status toggles -> 3-second match start countdown.
   - Integrated room chat bar for banter and tactical emojis.
4. **Live Match Arena**:
   - Split-screen player cards with live status and coin pot display.
   - Top status banner: "Hiding Phase" (countdown) or "Seeking Phase (Attempts Left: 3/3)".
   - Central 2x5 Grid of 10 isometric, animated Cyber Boxes with neon highlights.
   - Real-time opponent action indicator: "Friend is choosing a box...", "Friend opened Box #4 — Empty!".
5. **Victory & Defeat Screen**:
   - Golden explosion particles for the winner; dynamic coin counter tallying the pot into the player's balance.
   - XP level-up gauge progression.
   - "Rematch" and "Return to Lobby" buttons.

### Visual Identity & Theme
- **Aesthetic Direction**: High-energy Cyberpunk Vault & Neon Arcade. Dark charcoal slate canvas with radiant electric cyan, vibrant neon amber/gold, and warning magenta accents.
- **Color Palette**:
  - Background Canvas: Deep Obsidian (`#0A0E17`) & Slate Shadow (`#131B2A`).
  - Primary Accent: Electric Cyan (`#00F0FF`).
  - Secondary / Coin Gold: Cyber Amber (`#FFB800` / `#FFE600`).
  - Warning / Danger: Neon Coral / Magenta (`#FF2E63`).
  - Success / Match: Emerald Glow (`#00FF88`).
  - Surface Containers: Glassmorphic cards with subtle 1dp glowing borders (`#1F2B3E`).
- **Typography & Layout**:
  - Bold, impactful numerals for coin counts and countdown clocks.
  - Clear uppercase badges for statuses (`READY`, `HIDING`, `SEEKING`, `VICTORY`).
  - Minimum touch targets of 56dp for box buttons to ensure comfortable mobile tapping.
- **Interactive Feedback & Motion**:
  - Smooth spring physics for box opening tilts and lid flips.
  - Floating coin gain animations.
  - Animated pulse effect on the active player's avatar.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Real-time Cloud Synchronization (Firebase Firestore)**:
  - *Chosen Approach*: Back match state, rooms, user profiles, and chat messages in Firestore with active snapshot listeners (`addSnapshotListener`).
  - *Why*: Delivers sub-second synchronization between two remote players without requiring custom WebSocket infrastructure. Fully supports cloud saves and offline resilience.
  - *Alternatives Considered*: Local Room database or peer-to-peer Bluetooth. Rejected because the brief mandates true cloud multiplayer with friends, global leaderboards, and cloud persistence.

- **Decision 2: Anti-Cheat & Fair Play Escrow**:
  - *Chosen Approach*: When a match begins, both players' coin balances are decremented by the wager amount into the room's `escrowAmount`. The hider's chosen box ID is stored with a client-obscured token or revealed only upon guess/game-over. If a player disconnects or abandons mid-match after a grace period, the remaining player is awarded the win.
  - *Why*: Prevents cheat tools from inspecting client memory or players quitting without consequences when losing.

- **Decision 3: Authentication & Starting Bonus**:
  - *Chosen Approach*: Google Sign-In via Jetpack Credential Manager. Automatically registers the user profile in `users/{userId}`. On first creation, checks `isNewUser` and credits exactly 50 bonus coins.
  - *Why*: Complies with AI Studio security invariants, avoids fake bot spam, and gives every user instant playability.

- **Decision 4: In-Game Economy & Progression**:
  - *Chosen Approach*: Multi-faceted progression: Coins are used for wagers and cosmetic box skins in the shop. Level XP is earned through participation (+15 XP for a match, +30 XP for a win). Daily login streak grants scaled coin rewards every 24 hours.
  - *Why*: Encourages long-term retention and fair competition on both the Coin Leaderboard and the Level/XP Leaderboard.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├────────────────────────────────────────────────────────────────────────┤
│  SignInScreen  │  HomeScreen  │  RoomLobbyScreen  │  MatchGameScreen   │
│  Leaderboard   │  ShopScreen  │  ChatBottomSheet  │  TutorialDialog    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ UI State & Actions
┌───────────────────────────────────▼────────────────────────────────────┐
│                       Jetpack ViewModels (MVVM)                        │
├────────────────────────────────────────────────────────────────────────┤
│  AuthViewModel   │  GameViewModel   │  RoomViewModel  │ UserViewModel  │
│  - User State    │  - Match State   │  - Room sync    │ - Coins & XP   │
│  - CredentialMgr │  - Box selection │  - Invite code  │ - Daily reward │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Flow / Coroutines
┌───────────────────────────────────▼────────────────────────────────────┐
│                           Repository Layer                             │
├────────────────────────────────────────────────────────────────────────┤
│  UserRepository    │  RoomRepository   │  MatchRepository │ ChatRepo   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Firestore Listeners & Writes
┌───────────────────────────────────▼────────────────────────────────────┐
│                    Cloud Firestore & Firebase Auth                     │
├────────────────────────────────────────────────────────────────────────┤
│  users/{uid}       │  rooms/{roomId}   │  matches/{matchId}│ chat/...  │
│  - coins (50 init) │  - code (6 char)  │  - hiderId        │ - messages│
│  - level & xp      │  - wager          │  - seekerId       │           │
│  - skins & stats   │  - host & guest   │  - hiddenBox      │           │
│                    │  - status         │  - guesses [3]    │           │
└────────────────────────────────────────────────────────────────────────┘
```

### Firestore Schema Details

1. `users/{userId}`
   - `id`: String
   - `displayName`: String
   - `email`: String
   - `avatarId`: String
   - `coins`: Long (Default 50)
   - `xp`: Long (Starts at 0)
   - `level`: Long (Calculated: level = xp / 100)
   - `matchesPlayed`: Long
   - `matchesWon`: Long
   - `lastDailyRewardTimestamp`: Long
   - `ownedSkins`: List<String>
   - `activeBoxSkin`: String
   - `inventory`: Map<String, Int> (Power-ups: `radar`, `extra_guess`)

2. `rooms/{roomId}`
   - `id`: String
   - `code`: String (6-character uppercase code, e.g. "VAULT7")
   - `hostId`: String
   - `hostName`: String
   - `guestId`: String?
   - `guestName`: String?
   - `wager`: Long (e.g. 25)
   - `status`: String ("WAITING", "READY", "IN_MATCH", "COMPLETED")
   - `createdAt`: Long
   - `activeMatchId`: String?

3. `matches/{matchId}`
   - `id`: String
   - `roomId`: String
   - `wager`: Long
   - `pot`: Long (wager * 2)
   - `hiderId`: String
   - `seekerId`: String
   - `hiddenBox`: Int? (1..10)
   - `hidingDeadline`: Long
   - `guesses`: List<Int> (up to 3 box IDs)
   - `revealedBoxes`: List<Int>
   - `status`: String ("HIDING_PHASE", "SEEKING_PHASE", "SEEKER_WON", "HIDER_WON", "ABANDONED")
   - `winnerId`: String?
   - `powerUpUsedBySeeker`: String?

4. `matches/{matchId}/messages/{messageId}`
   - `senderId`: String
   - `senderName`: String
   - `text`: String
   - `timestamp`: Long
   - `isEmote`: Boolean

### Implementation Step Plan
1. **Firebase Integration & Provisioning**:
   - Extract keystore fingerprints with `keytool`.
   - Complete `show_aistudio_ui` terms handshake and provision Firestore via `ProvisionFirebase`.
   - Update `app/build.gradle.kts` to enable Firestore, Auth, Credential Manager, and Google ID libraries.
   - Create `firebase-blueprint.json` and deploy `firestore.rules`.
2. **Core Domain & Data Layer**:
   - Models (`User`, `Room`, `Match`, `ChatMessage`, `ShopItem`).
   - `AuthRepository`, `UserRepository`, `MatchRepository`.
3. **Audio-Visual & Design System**:
   - Custom Cyber Vault color palette, glowing box shapes, dynamic Canvas box animations.
   - Sound/Haptic feedback hooks for box tapping and victory.
4. **Screens & Workflows**:
   - `SignInScreen`: One-tap Google Sign-In with 50-coin bonus alert.
   - `HomeScreen`: Quick stats, Daily Bonus reward modal, Play buttons.
   - `RoomLobbyScreen`: Create/Join by room code, wager selector, ready toggle, in-room chat.
   - `MatchArenaScreen`: 10-box interactive grid, 3-guess counter, real-time sync, winner celebration.
   - `LeaderboardScreen`: Global high coin & win ranking tabs.
   - `ShopScreen`: Unlockable box skins and power-ups.
   - `TutorialDialog`: Interactive rule explanation.
