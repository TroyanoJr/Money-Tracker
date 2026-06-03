package net.micode.spendingtracker.ui.theme

import androidx.compose.ui.graphics.Color

// --- SECCIÓN PIZARRA (Se mantiene con su propia identidad de tiza) ---
val BlackboardBlack = Color(0xFF1A1A1A) 
val ChalkWhite = Color(0xFFF5F5F5)
val ChalkGreen = Color(0xFF81C784)
val ChalkRed = Color(0xFFE57373)
val ChalkBlue = Color(0xFF4FC3F7)
val ChalkYellow = Color(0xFFFFD54F)
val ChalkOrange = Color(0xFFFFB74D)

// --- TEMA ARMÓNICO: "Vintage Khaki" (Basado en #D9CEB2) ---
// Logramos armonía usando colores de la misma familia (mismo tono, distinta saturación)
val HeaderBackground = Color(0xFFD9CEB2) // Tu color base (Marrón claro/Caqui)
val HeaderText = Color(0xFF3B3830)       // Marrón Tierra muy oscuro (Regularizado para este fondo)
val AppBackground = Color(0xFFE8E1D1)    // Versión intermedia para el fondo general
val SurfaceColor = Color(0xFFFDFBF5)     // Crema casi blanco para que las filas resalten con suavidad

// Alias para que toda la app se actualice automáticamente
val BeigeHeader = HeaderBackground
val DarkBrownText = HeaderText
