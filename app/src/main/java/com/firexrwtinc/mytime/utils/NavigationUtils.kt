package com.firexrwtinc.mytime.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NavigationUtils {
    
    /**
     * Открывает навигационное приложение для прокладывания маршрута к указанному месту
     */
    fun navigateToLocation(context: Context, location: String) {
        try {
            // Пытаемся открыть Google Maps с поиском места
            val encodedLocation = Uri.encode(location)
            val geoUri = Uri.parse("geo:0,0?q=$encodedLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            
            // Проверяем, есть ли приложения, которые могут обработать этот интент
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Если нет приложения для geo: URI, пытаемся открыть через веб-браузер
                val webUri = Uri.parse("https://maps.google.com/maps?q=$encodedLocation")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                
                if (webIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(webIntent)
                } else {
                    Toast.makeText(
                        context, 
                        "Не найдено приложение для навигации", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                context, 
                "Ошибка при открытии навигации: ${e.message}", 
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Открывает навигационное приложение с конкретными координатами
     */
    fun navigateToCoordinates(context: Context, latitude: Double, longitude: Double, label: String = "") {
        try {
            val geoUri = if (label.isNotEmpty()) {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            } else {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
            }
            
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to web browser
                val webUri = Uri.parse("https://maps.google.com/maps?q=$latitude,$longitude")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                
                if (webIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(webIntent)
                } else {
                    Toast.makeText(
                        context, 
                        "Не найдено приложение для навигации", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                context, 
                "Ошибка при открытии навигации: ${e.message}", 
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Проверяет, доступна ли навигация на устройстве
     */
    fun isNavigationAvailable(context: Context): Boolean {
        val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=test"))
        return geoIntent.resolveActivity(context.packageManager) != null
    }
}