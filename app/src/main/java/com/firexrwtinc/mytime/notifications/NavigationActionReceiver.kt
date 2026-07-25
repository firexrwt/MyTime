package com.firexrwtinc.mytime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * BroadcastReceiver that handles notification action button taps.
 * Supports opening navigation (for address locations) and URLs (for link locations).
 */
class NavigationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NavigationActionReceiver"
        const val ACTION_NAVIGATE = "com.firexrwtinc.mytime.ACTION_NAVIGATE"
        const val ACTION_OPEN_URL = "com.firexrwtinc.mytime.ACTION_OPEN_URL"
        const val EXTRA_LAT = "nav_lat"
        const val EXTRA_LNG = "nav_lng"
        const val EXTRA_TITLE = "nav_title"
        const val EXTRA_URL = "nav_url"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NAVIGATE -> {
                val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                val lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
                val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(title)})")
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, geoUri).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                } catch (e: Exception) {
                    // No map app available on this device - degrade gracefully.
                    Log.e(TAG, "Failed to launch navigation for geo URI: $geoUri", e)
                }
            }
            ACTION_OPEN_URL -> {
                val url = intent.getStringExtra(EXTRA_URL)?.takeIf { it.isNotBlank() } ?: return
                val urlWithScheme = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else url
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(urlWithScheme)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                } catch (e: Exception) {
                    // No browser app available on this device - degrade gracefully.
                    Log.e(TAG, "Failed to open URL: $urlWithScheme", e)
                }
            }
        }
    }
}
