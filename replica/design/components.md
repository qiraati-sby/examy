# Component Specifications - Examy

Rebuilt design primitives and component patterns for the Examy kiosk client. Clean-room implementation, WCAG AA compliant.

---

### 1. PrimaryButton
- **Variants**: `primary` (accent fill), `secondary` (surface-variant fill), `danger` (b91c1c fill)
- **Sizes**: `md` (48px height, touch-friendly min size), `lg` (56px full-width)
- **States**: `default`, `pressed/active`, `disabled` (opacity 0.5, pointer-events none), `loading` (spinner with accessible text)
- **Tokens**: `bg-accent`, `text-on-accent`, `radius-md`, `font-base/bold`
- **A11y**: Android `android:contentDescription`, minimum 48x48dp touch target, high-contrast focus ring
- **Used on**: `MainActivity` (Mulai Ujian, Pindai QR), `CreateExamActivity` (Generate Token), `DialogExitPin`

### 2. FormInputField
- **Variants**: `text`, `numberPassword` (PIN mask), `url`, `time`
- **States**: `default` (border 1dp border-input), `focused` (border 2dp accent), `error` (border 2dp danger), `disabled`
- **Tokens**: `bg-surface`, `text-primary`, `border-border`, `radius-sm`
- **A11y**: `android:hint`, `android:inputType` appropriate for virtual keyboard, error message linked via live region
- **Used on**: `MainActivity` (Server URL / Token), `CreateExamActivity` (Mapel, Link, 3-tier PINs, Durasi), `DialogExitPin`

### 3. SurfaceCard
- **Variants**: `flat` (border 1dp), `elevated` (shadow-card)
- **States**: `default`, `selected` (border accent)
- **Tokens**: `bg-surface`, `border-border`, `radius-md`, `shadow-card`
- **Used on**: Status volume banner, recent exam URL chips, exam summary modal

### 4. VolumeStatusIndicator
- **Variants**: `compliant` (volume >= 80%, text-success), `violation` (volume < 80%, text-danger, warning icon)
- **States**: `realtime` (dynamically listens to Android AudioManager stream volume)
- **Tokens**: `bg-danger-surface`, `text-danger`, `radius-sm`
- **Used on**: `MainActivity` (header block & gatekeeper dialog)

### 5. SirenOverlayModal
- **Variants**: `grace-period` (5s countdown banner, warning yellow/orange), `lockdown-active` (fullscreen red danger #b91c1c, pulsating icon)
- **States**: `grace-running` (5s countdown text), `locked-alarm` (looping siren audio + 1000ms vibration pulses)
- **Tokens**: `bg-danger`, `text-on-accent`, `radius-none` (covers 100% viewport)
- **A11y**: Blocks all touches underneath, requires supervisor PIN to dismiss
- **Used on**: `ExamActivity`

### 6. PinUnlockModal
- **Variants**: `student-verify`, `proctor-siren-override`, `exit-exam`
- **States**: `input`, `invalid-pin` (shakes + clears input + shows error toast), `verified`
- **Tokens**: `bg-surface`, `shadow-modal`, `radius-lg`
- **Used on**: `ExamActivity` and `MainActivity`

### 7. CountdownTimerHeader
- **Variants**: `normal` (text-muted), `urgent` (< 10 minutes remaining, text-danger, bold)
- **States**: `ticking` (updates every 1s via Coroutine/CountDownTimer), `time-up` (triggers auto submission prompt)
- **Tokens**: `text-primary`, `font-mono`, `type-sm`
- **Used on**: `ExamActivity` top navigation bar
