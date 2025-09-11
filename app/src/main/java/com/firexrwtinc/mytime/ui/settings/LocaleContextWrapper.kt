package com.firexrwtinc.mytime.ui.settings

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * Context wrapper for applying locale changes dynamically.
 * This allows the app to change language without requiring a full app restart.
 */
class LocaleContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        /**
         * Wraps the context with the appropriate locale based on language setting.
         * @param context The base context to wrap
         * @param language The language to apply
         * @return Context with the applied locale
         */
        fun wrap(context: Context, language: Language): Context {
            val locale = when (language) {
                Language.ENGLISH -> Locale("en")
                Language.RUSSIAN -> Locale("ru")
                Language.SYSTEM -> {
                    // Use system default locale
                    val systemLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        context.resources.configuration.locales.get(0)
                    } else {
                        @Suppress("DEPRECATION")
                        context.resources.configuration.locale
                    }
                    systemLocale
                }
            }

            val config = Configuration(context.resources.configuration)
            
            // Apply locale to configuration
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                config.setLocale(locale)
                config.setLocales(android.os.LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }

            // Create context with updated configuration
            val updatedContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            } else {
                // For older Android versions, update resources directly
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                context
            }

            return LocaleContextWrapper(updatedContext)
        }

        /**
         * Gets the current locale from the context.
         * @param context The context to get locale from
         * @return Current locale
         */
        fun getCurrentLocale(context: Context): Locale {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
        }

        /**
         * Checks if the current context locale matches the desired language.
         * @param context The context to check
         * @param language The desired language
         * @return True if locales match, false otherwise
         */
        fun isLocaleMatching(context: Context, language: Language): Boolean {
            val currentLocale = getCurrentLocale(context)
            return when (language) {
                Language.ENGLISH -> currentLocale.language == "en"
                Language.RUSSIAN -> currentLocale.language == "ru"
                Language.SYSTEM -> true // System language is always considered matching
            }
        }
    }
}