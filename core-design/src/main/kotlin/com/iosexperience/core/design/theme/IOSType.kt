package com.iosexperience.core.design.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Família tipográfica. PENDENTE: embarcar Inter (OFL) em res/font e trocar [family] — o ambiente de build
 * não tinha rede para baixar as fontes. Enquanto isso usa a fonte do sistema (MiSans no HyperOS). Ver docs/LICENSES.md.
 */
object IOSFonts { val family: FontFamily = FontFamily.Default }

/** Escala tipográfica (tudo em sp, segue a escala de fonte do sistema). */
object IOSType {
    val LargeTitle = TextStyle(fontFamily = IOSFonts.family, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
    val Title = TextStyle(fontFamily = IOSFonts.family, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    val Body = TextStyle(fontFamily = IOSFonts.family, fontSize = 17.sp, fontWeight = FontWeight.Normal)
    val Caption = TextStyle(fontFamily = IOSFonts.family, fontSize = 13.sp, fontWeight = FontWeight.Normal)
    val IconLabel = TextStyle(fontFamily = IOSFonts.family, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    val SectionHeader = TextStyle(fontFamily = IOSFonts.family, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    /** Relógio: peso 300 e números tabulares (não "pulam" ao mudar o minuto — se a fonte suportar tnum). */
    fun Clock(size: TextUnit) = TextStyle(fontFamily = IOSFonts.family, fontSize = size, fontWeight = FontWeight.Light, fontFeatureSettings = "tnum")
}
