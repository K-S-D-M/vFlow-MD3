package com.chaomixian.vflow.ui.common

import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.chaomixian.vflow.R

object ThemeUtils {

    private const val PREFS_NAME = "vFlowPrefs"
    private const val KEY_DYNAMIC_COLOR_ENABLED = "dynamicColorEnabled"
    const val KEY_COLORFUL_WORKFLOW_CARDS_ENABLED = "colorfulWorkflowCardsEnabled"
    const val KEY_THEME_MODE = "themeMode"

    val Md3Primary = Color(0xFFFB7299)
    val Md3OnPrimary = Color(0xFFFFFFFF)
    val Md3PrimaryContainer = Color(0xFFFFD5E2)
    val Md3OnPrimaryContainer = Color(0xFF3C001D)
    val Md3Secondary = Color(0xFF74565F)
    val Md3OnSecondary = Color(0xFFFFFFFF)
    val Md3SecondaryContainer = Color(0xFFFFD5E2)
    val Md3OnSecondaryContainer = Color(0xFF2B151C)
    val Md3Tertiary = Color(0xFF7C5635)
    val Md3OnTertiary = Color(0xFFFFFFFF)
    val Md3TertiaryContainer = Color(0xFFFFDCC1)
    val Md3OnTertiaryContainer = Color(0xFF2E1500)
    val Md3Error = Color(0xFFBA1A1A)
    val Md3OnError = Color(0xFFFFFFFF)
    val Md3ErrorContainer = Color(0xFFFFDAD6)
    val Md3OnErrorContainer = Color(0xFF410002)
    val Md3Background = Color(0xFFFFFBFF)
    val Md3OnBackground = Color(0xFF201A1B)
    val Md3Surface = Color(0xFFFFFBFF)
    val Md3OnSurface = Color(0xFF201A1B)
    val Md3SurfaceVariant = Color(0xFFF2DDE1)
    val Md3OnSurfaceVariant = Color(0xFF514347)
    val Md3Outline = Color(0xFF837377)
    val Md3OutlineVariant = Color(0xFFD5C2C6)

    val Md3PrimaryDark = Color(0xFFFB7299)
    val Md3OnPrimaryDark = Color(0xFF5E002E)
    val Md3PrimaryContainerDark = Color(0xFF7E0045)
    val Md3OnPrimaryContainerDark = Color(0xFFFFD5E2)
    val Md3SecondaryDark = Color(0xFFE3BDC6)
    val Md3OnSecondaryDark = Color(0xFF422730)
    val Md3SecondaryContainerDark = Color(0xFF5A3D46)
    val Md3OnSecondaryContainerDark = Color(0xFFFFD5E2)
    val Md3TertiaryDark = Color(0xFFEFBD9E)
    val Md3OnTertiaryDark = Color(0xFF4A2800)
    val Md3TertiaryContainerDark = Color(0xFF6A3C1B)
    val Md3OnTertiaryContainerDark = Color(0xFFFFDCC1)
    val Md3ErrorDark = Color(0xFFFFB4AB)
    val Md3OnErrorDark = Color(0xFF690005)
    val Md3ErrorContainerDark = Color(0xFF93000A)
    val Md3OnErrorContainerDark = Color(0xFFFFDAD6)
    val Md3BackgroundDark = Color(0xFF1C1014)
    val Md3OnBackgroundDark = Color(0xFFECE0E2)
    val Md3SurfaceDark = Color(0xFF1C1014)
    val Md3OnSurfaceDark = Color(0xFFECE0E2)
    val Md3SurfaceVariantDark = Color(0xFF514347)
    val Md3OnSurfaceVariantDark = Color(0xFFD5C2C6)
    val Md3OutlineDark = Color(0xFF9E8E92)
    val Md3OutlineVariantDark = Color(0xFF514347)

    private val md3LightColorScheme = lightColorScheme(
        primary = Md3Primary,
        onPrimary = Md3OnPrimary,
        primaryContainer = Md3PrimaryContainer,
        onPrimaryContainer = Md3OnPrimaryContainer,
        secondary = Md3Secondary,
        onSecondary = Md3OnSecondary,
        secondaryContainer = Md3SecondaryContainer,
        onSecondaryContainer = Md3OnSecondaryContainer,
        tertiary = Md3Tertiary,
        onTertiary = Md3OnTertiary,
        tertiaryContainer = Md3TertiaryContainer,
        onTertiaryContainer = Md3OnTertiaryContainer,
        error = Md3Error,
        onError = Md3OnError,
        errorContainer = Md3ErrorContainer,
        onErrorContainer = Md3OnErrorContainer,
        background = Md3Background,
        onBackground = Md3OnBackground,
        surface = Md3Surface,
        onSurface = Md3OnSurface,
        surfaceVariant = Md3SurfaceVariant,
        onSurfaceVariant = Md3OnSurfaceVariant,
        outline = Md3Outline,
        outlineVariant = Md3OutlineVariant,
    )

    private val md3DarkColorScheme = darkColorScheme(
        primary = Md3PrimaryDark,
        onPrimary = Md3OnPrimaryDark,
        primaryContainer = Md3PrimaryContainerDark,
        onPrimaryContainer = Md3OnPrimaryContainerDark,
        secondary = Md3SecondaryDark,
        onSecondary = Md3OnSecondaryDark,
        secondaryContainer = Md3SecondaryContainerDark,
        onSecondaryContainer = Md3OnSecondaryContainerDark,
        tertiary = Md3TertiaryDark,
        onTertiary = Md3OnTertiaryDark,
        tertiaryContainer = Md3TertiaryContainerDark,
        onTertiaryContainer = Md3OnTertiaryContainerDark,
        error = Md3ErrorDark,
        onError = Md3OnErrorDark,
        errorContainer = Md3ErrorContainerDark,
        onErrorContainer = Md3OnErrorContainerDark,
        background = Md3BackgroundDark,
        onBackground = Md3OnBackgroundDark,
        surface = Md3SurfaceDark,
        onSurface = Md3OnSurfaceDark,
        surfaceVariant = Md3SurfaceVariantDark,
        onSurfaceVariant = Md3OnSurfaceVariantDark,
        outline = Md3OutlineDark,
        outlineVariant = Md3OutlineVariantDark,
    )

    @JvmStatic
    fun getThemeResId(context: Context, transparent: Boolean = false): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR_ENABLED, false)

        return when {
            transparent && useDynamicColor -> R.style.Theme_vFlow_Transparent_Dynamic
            transparent && !useDynamicColor -> R.style.Theme_vFlow_Transparent_Default
            !transparent && useDynamicColor -> R.style.Theme_vFlow_Dynamic
            else -> R.style.Theme_vFlow
        }
    }

    @JvmStatic
    fun createThemedContext(context: Context, transparent: Boolean = false): ContextThemeWrapper {
        val themeResId = getThemeResId(context, transparent)
        return ContextThemeWrapper(context, themeResId)
    }

    @JvmStatic
    fun isDynamicColorEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DYNAMIC_COLOR_ENABLED, false)
    }

    @JvmStatic
    fun isColorfulWorkflowCardsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_COLORFUL_WORKFLOW_CARDS_ENABLED, false)
    }

    @JvmStatic
    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
    }

    @JvmStatic
    fun setThemeMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    @Composable
    fun getAppColorScheme(darkTheme: Boolean? = null): ColorScheme {
        val context = LocalContext.current
        val useDynamicColor = isDynamicColorEnabled(context)
        val themeMode = getThemeMode(context)
        val isDarkTheme = darkTheme ?: when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

        return when {
            useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            isDarkTheme -> md3DarkColorScheme
            else -> md3LightColorScheme
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
