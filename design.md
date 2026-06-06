# AvesLens — Design Reference

Extracted from Figma: https://www.figma.com/design/89YoeDDcvlhdeFzfqQkazr/
Framework target: Kotlin + Jetpack Compose (Material 3)

---

## Screens Inventory

| Frame Name | Node ID | Description |
|---|---|---|
| Auth Screen | `4:2058` | Login / Register |
| Home Screen - Journal | `4:2523` | Observation list with cards |
| Home - Empty State | `4:2717` | Empty journal illustration |
| Home - Loading State | `4:2846` | Skeleton loading UI |
| Form - ML Loading State | `4:2130` | New observation + ML analysis |
| Form - Error State | `4:2770` | Species not recognized |
| Observation Detail | `4:2327` | Full detail view |
| Profile Screen | `4:2626` | User profile + settings |

---

## Color Palette

Extracted from Figma design context. All values are exact hex from the source file.

### Primary (Forest Green)
```
Primary Dark    #154212   — App name, primary buttons, active nav, headings
Primary         #1A5E16   — (gradient start)
Primary Light   #2D5A27   — (gradient end, button gradient)
On Primary      #FFFFFF   — Text/icon on primary buttons
```

### Surface & Background
```
Background      #FAF9F7   — App background (TopAppBar, page bg)
Surface         #F4F3F1   — Card background (auth card, form cards)
Surface Variant #E3E2E0   — Input field background (30% opacity)
Border          #E9E8E6   — Dividers, input borders, avatar border
```

### Text
```
Text Primary    #1A1C1B   — Main body text, headings
Text Secondary  #42493E   — Subtitles, labels, secondary info
Text Tertiary   #53634A   — Form labels, links, muted interactive text
Text Hint       #72796E   — Input placeholder text
```

### Semantic
```
Rare Badge bg   #154212   — "RARE" chip background
Rare Badge text #FFFFFF
Warning/Error   Warm amber — "Species not recognized" error card
```

### Button
```
Primary Button  linear-gradient(167.82deg, #154212 0%, #2D5A27 100%)
Button shadow   rgba(21, 66, 18, 0.12) — 8px blur, 16px spread
Button text     #FFFFFF, Manrope Bold 18sp
```

---

## Typography

Fonts used: **Manrope** (display/brand) and **Inter** (body/UI).

| Role | Font | Weight | Size | Line Height | Tracking |
|---|---|---|---|---|---|
| App Name (AvesLens) | Manrope | ExtraBold (800) | 24sp | 32sp | -1.2px |
| Heading 1 (greeting) | Manrope | Bold (700) | ~32sp | ~40sp | — |
| Heading 2 (section) | Manrope | Bold (700) | 24sp | 32sp | — |
| Heading 3 (card title) | Manrope | SemiBold (600) | ~20sp | ~28sp | — |
| Species Name (detail) | Manrope | Bold (700) | ~48sp | ~56sp | — |
| Body / Label | Inter | Regular (400) | 16sp | normal | — |
| Caption / Tag | Inter | Regular (400) | 14sp | 20sp | — |
| Form Label | Inter | SemiBold (600) | 12sp | 16sp | — |
| Uppercase Tag | Inter | Regular (400) | 14sp | 20sp | 1.4px (uppercase) |
| Scientific Name | Inter | Regular Italic | ~20sp | 28sp | — |

> In Compose: use `GoogleFonts` or bundle Manrope + Inter via `res/font/`.

---

## Spacing & Layout

```
Screen width        390dp (standard phone)
Horizontal margin   24dp  — content padding left/right
Content width       342dp — (390 - 24*2)
Card inner padding  24dp
Card corner radius  32dp  — auth card, form sections
Input corner radius 32dp  — pill-shaped inputs
Button corner radius 9999dp — fully rounded (pill)
Avatar size         40dp  — TopAppBar avatar
Avatar border       2dp, #E9E8E6
Stats card radius   ~16dp
Chip/Badge padding  12dp horizontal, 4dp vertical
FAB size            64dp (main), 56dp (secondary delete)
Bottom nav height   80dp
Top app bar height  72dp
```

---

## Component Specs

### TopAppBar
- Background: `#FAF9F7` with `backdropBlur(12dp)`, opacity 70%
  > **Compose note:** `backdropBlur` requires `RenderEffect` (API 31+). Set `minSdk = 31`. If supporting below API 31, fall back to `#FAF9F7` at 90% opacity (no blur).
- Logo: AvesLens owl icon (20×21.5dp) + "AvesLens" text
- Font: Manrope ExtraBold, 24sp, `#154212`, tracking -1.2px
- Right: Avatar 40×40dp, circular, border 2dp `#E9E8E6`

### BottomNavigationBar (MD3)
- 3 tabs: Journal | Observe | Explorer
- Tab width: 130dp each
- Active tab: pill background (62×24dp), icon + label below
- Active color: `#154212`
- Inactive color: muted gray
- Icon sizes: ~22×16dp (Journal), ~22×20dp (Observe), ~16×16dp (Explorer)
- Label font: Inter Regular, ~14sp, height 18dp

### Observation Card (Home)
- Full width (342dp)
- Image height: 224dp, corner radius top: 0 (full bleed)
- Info section height: 106dp, bg white/surface
- Species name: Manrope SemiBold ~20sp
- Meta row: location icon (9.3×11.7dp) + text, date icon (10.5×11.7dp) + text
- RARE badge: top-right of image, bg `#154212`, text white, 12dp padding h, 4dp v
- Three-dot menu: right side of species name row

### Input Field
- Background: `rgba(227, 226, 224, 0.3)`
- Corner radius: 32dp (pill)
- Padding: 20dp horizontal, 18dp vertical
- Label: Inter SemiBold 12sp, `#53634A`, floated above input
- Placeholder: Inter Regular 16sp, `#72796E`
- Password eye icon: right-aligned inside field

### Primary Button
- Background: `linear-gradient(167.82deg, #154212, #2D5A27)`
- Corner radius: 9999dp (pill)
- Padding: 16dp vertical, full width
- Text: Manrope Bold 18sp, white, centered
- Shadow: `rgba(21, 66, 18, 0.12)` 8dp blur

### Secondary Button (Discard/Return)
- Background: transparent
- Text only: Inter Medium, `#53634A` or muted red for destructive

### FAB (Floating Action Button)
- Primary FAB: 64×64dp, green bg, + icon ~19dp
- Secondary FAB (delete): 56×56dp, subtle warm gray bg, trash icon 16×18dp
- Edit FAB: 64×64dp, green, pencil icon 22.5×22.5dp

### Progress Bar (ML Analysis)
- Container: full width with label row above
- Label: scan icon + "ANALYZING SPECIMEN..." text + percentage right-aligned
- Bar: forest green gradient fill, 8dp height, rounded
- Below bar: skeleton placeholders (species name + subtitle)

### Error Card (Species Not Recognized)
- Background: warm amber/orange tint
- Left accent bar: 4dp wide, amber
- Heading: "Species not recognized" bold
- Body: helper text
- Actions below: Retake Photo (primary btn), Enter Manually (secondary btn), Return to Journal (text link)

### Detail Hero
- Full bleed image: 390×397dp
- Gradient overlay on image bottom
- RARE SIGHTING badge + "Observed Xh ago" label overlaid on image
- Content starts at y=317 (overlaps image by ~80dp)
- Content bg: white, top corners rounded

### Profile Stats Cards
- Two cards side by side: 163dp each, gap 16dp
- Each: number large (Manrope Bold ~40sp `#154212`) + label below (Inter 12sp uppercase)

### Settings List Item
- Height: 72dp
- Icon background: 40×40dp rounded, muted green tint
- Icon: ~18dp
- Label: Inter Medium 16sp, `#1A1C1B`
- Chevron: right-aligned, 7.4×12dp

### Logout Button
- Outlined style: border, no fill
- Text: "Logout" with icon, centered
- Color: muted red/danger

---

## Screens Detail

### Auth Screen (`4:2058`)
- Full screen background with two blurred circle overlays (decorative)
- Bird icons strip at bottom (decorative)
- Logo + tagline centered top section
- Auth card: bg `#F4F3F1`, radius 32dp, padding 32dp
- "Don't have an account? Register" link at bottom

### Home Screen - Journal (`4:2523`)
- TopAppBar + BottomNavBar (Journal active)
- Hero greeting: "Good morning, Birder!" + subtitle
- Search bar: full width pill, search icon left
- "Recent Observations / View All" row
- LazyColumn of observation cards (3 shown: Oriental Magpie-Robin, Common Kingfisher, Olive-backed Sunbird)
- FAB bottom right: 64×64dp

### Home - Empty State (`4:2717`)
- Same chrome (TopAppBar, no BottomNavBar shown in frame)
- Illustration container: rounded bg, bird on branch SVG
- Three dot pagination indicator below illustration
- "Your journal is empty" heading
- "Start your journey by recording your first bird sighting." subtitle
- "Record First Bird" primary button (with camera icon)
- "NEAR YOU: 12 LOCAL SPECIES ACTIVE" tip text below

### Home - Loading State (`4:2846`)
- Skeleton shimmer placeholders for heading, search bar, cards
- Rounded rectangles with gradient shimmer animation
- Card skeleton: image placeholder + title + subtitle placeholders

### Form - ML Loading State (`4:2130`)
- "New Observation" heading with back arrow
- Image upload area (placeholder or selected image)
- Progress bar: "ANALYZING SPECIMEN... 74%" with green fill
- Skeleton below bar for auto-fill preview
- Form fields: Species Name (auto-filled "Mountain Bluebird" + edit icon), Location (with pin icon), Field Notes (textarea)
- "Save Observation ✓" primary button + "Discard Draft" text button

### Form - Error State (`4:2770`)
- Uploaded image shown with blur overlay + visibility-issue icon
- Error card: "Species not recognized / We couldn't identify this bird..."
- Action cluster: Retake Photo, Enter Manually, Return to Journal
- Field Photography Tips section with two tips (lighting + eye focus)

### Observation Detail (`4:2327`)
- Hero image 390×397dp full bleed
- "RARE SIGHTING" green badge + "Observed 2h ago" overlaid
- Species: "Common Kingfisher" large heading + "Alcedo atthis" italic
- Location card with map thumbnail
- Date card: "Oct 24 / 2023 / 08:42 AM · Clear Sky"
- Field notes card with quoted text
- Metadata chips: habitat, observation type, proximity
- Edit FAB (green) + Delete FAB (gray) stacked bottom right
- BottomNavBar

### Profile Screen (`4:2626`)
- Avatar 128×128dp circular with edit badge (bottom right 26.5dp)
- Display name: "The Triple T" Manrope Bold ~28sp
- Location: pin icon + "Pacific Northwest, USA"
- Stats: 24 BIRDS OBSERVED | 12 SPECIES IDENTIFIED (side by side cards)
- Account Settings section: Edit Profile, Privacy, Help (each 72dp row)
- Logout button outlined (danger)
- Version text: "Version 2.4.1 — Proudly Open Source"

---

## Compose Theme Mapping

```kotlin
// Color.kt
val PrimaryDark    = Color(0xFF154212)
val Primary        = Color(0xFF1A5E16)
val PrimaryLight   = Color(0xFF2D5A27)
val Background     = Color(0xFFFAF9F7)
val Surface        = Color(0xFFF4F3F1)
val SurfaceVariant = Color(0x4DE3E2E0) // 30% opacity
val BorderColor    = Color(0xFFE9E8E6)
val TextPrimary    = Color(0xFF1A1C1B)
val TextSecondary  = Color(0xFF42493E)
val TextTertiary   = Color(0xFF53634A)
val TextHint       = Color(0xFF72796E)

// Shape.kt
val InputShape  = RoundedCornerShape(32.dp)
val CardShape   = RoundedCornerShape(32.dp)
val ButtonShape = RoundedCornerShape(9999.dp)
val ChipShape   = RoundedCornerShape(9999.dp)

// Type.kt
// Use Manrope for display (ExtraBold, Bold)
// Use Inter for body (Regular, SemiBold, Medium)
```

### Primary Button Gradient (Compose)
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(9999.dp))
        .background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF154212), Color(0xFF2D5A27)),
                start = Offset(0f, 0f),
                end = Offset(0f, Float.POSITIVE_INFINITY)
            )
        )
        .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(9999.dp),
            ambientColor = Color(0xFF154212).copy(alpha = 0.12f)
        )
        .padding(vertical = 16.dp),
    contentAlignment = Alignment.Center
) {
    Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
}
```

---

## Assets & Icons

All icons in the design are vector (SVG paths). In Compose, use `androidx.compose.material.icons` where possible, or custom `ImageVector` for custom shapes.

| Icon | Usage | Approx size |
|---|---|---|
| Owl logo | TopAppBar, Auth header | 20×21.5dp |
| Search | Search bar leading | 18×18dp |
| Location pin | Location field, profile | 9.3×11.7dp, 12×15dp |
| Calendar | Date field, card meta | 10.5×11.7dp |
| Eye / Eye-off | Password visibility toggle | 18.3×12.5dp |
| Camera | Form upload, FAB | 22×20dp |
| Pencil/Edit | Edit FAB, field edit icon | 22.5×22.5dp |
| Trash | Delete FAB | 16×18dp |
| Chevron right | Settings list item | 7.4×12dp |
| Plus (+) | Main FAB | ~19dp |
| Back arrow | Form back navigation | 16×16dp |
| Three-dot menu | Card context menu | 4×16dp |
| Field notes icon | Detail section label | 13.5×12dp |
| Feather/quill | Form field notes label | 18×16dp |
