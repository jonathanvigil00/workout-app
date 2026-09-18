package com.jvigil.hoofmode.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette
val HoofBackground = Color(0xFF092328)
val HoofSurfaceVariant = Color(0xFF12544F)
val HoofPrimary = Color(0xFF2A835F)
val HoofAccent = Color(0xFF8BBB92)

// Derived tones (kept close to the four brand colors so the palette stays cohesive)
val HoofSurface = Color(0xFF0D2B31)
val HoofSurfaceHigh = Color(0xFF163B3D)
val HoofOnPrimary = Color(0xFF04120F)
val HoofOnSurface = Color(0xFFE3EFE6)
val HoofOnSurfaceMuted = Color(0xFFA9C2B9)
val HoofOutline = Color(0xFF3A6B60)
val HoofError = Color(0xFFCF6679)
val HoofPrBadge = HoofAccent

// Tonal surface-container ramp — Material3 components (Card, Dialog, NavigationBar, BottomSheet)
// default to these "surfaceContainer*" roles, not `surface`. Material3's darkColorScheme() only
// fills them from neutral grey defaults if left unset, which is why cards/dialogs were rendering
// as flat grey instead of the teal-tinted look used everywhere else. Explicitly tinting the whole
// ramp keeps every elevated surface consistent with the brand palette.
val HoofSurfaceDim = Color(0xFF081F24)
val HoofSurfaceBright = Color(0xFF2F5A5F)
val HoofSurfaceContainerLowest = Color(0xFF061A1E)
val HoofSurfaceContainerLow = Color(0xFF0D2B30)
val HoofSurfaceContainer = Color(0xFF11333A)
val HoofSurfaceContainerHigh = Color(0xFF163C42)
val HoofSurfaceContainerHighest = Color(0xFF1C474E)
val HoofInverseSurface = Color(0xFFDCEAE4)
val HoofInverseOnSurface = Color(0xFF0D2B31)
val HoofInversePrimary = Color(0xFF5BAE85)
