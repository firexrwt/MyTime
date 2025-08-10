package com.firexrwtinc.mytime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import android.webkit.WebView

data class LocationData(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialLocation: String = "",
    onLocationSelected: (LocationData) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLatitude by remember { mutableStateOf(55.7558) }
    var selectedLongitude by remember { mutableStateOf(37.6176) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите местоположение") },
        text = {
            SimpleMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                initialLatitude = selectedLatitude,
                initialLongitude = selectedLongitude,
                onLocationSelected = { lat, lng ->
                    selectedLatitude = lat
                    selectedLongitude = lng
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onLocationSelected(
                        LocationData(
                            name = "Координаты: ${String.format("%.6f", selectedLatitude)}, ${String.format("%.6f", selectedLongitude)}",
                            latitude = selectedLatitude,
                            longitude = selectedLongitude
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Выбрать место")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun SimpleMapView(
    modifier: Modifier = Modifier,
    initialLatitude: Double = 55.7558,
    initialLongitude: Double = 37.6176,
    onLocationSelected: (Double, Double) -> Unit
) {
    var markerLat by remember { mutableStateOf(initialLatitude) }
    var markerLng by remember { mutableStateOf(initialLongitude) }
    var zoomLevel by remember { mutableStateOf(12) }
    var centerLat by remember { mutableStateOf(initialLatitude) }
    var centerLng by remember { mutableStateOf(initialLongitude) }
    
    // Параметры отображения
    val minZoom = 8
    val maxZoom = 16
    
    Column(modifier = modifier) {
        // Контролы карты
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (zoomLevel < maxZoom) zoomLevel++ },
                modifier = Modifier.size(40.dp)
            ) {
                Text("+")
            }
            
            Text(
                text = "Зум: $zoomLevel",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            
            Button(
                onClick = { if (zoomLevel > minZoom) zoomLevel-- },
                modifier = Modifier.size(40.dp)
            ) {
                Text("-")
            }
        }
        
        // WebView карта с OpenStreetMap
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    val mapHtml = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        body { margin: 0; padding: 0; }
        #map { height: 100vh; width: 100vw; }
        .coord-display { 
            position: absolute; 
            bottom: 10px; 
            left: 10px; 
            background: white; 
            padding: 8px; 
            border-radius: 5px;
            font-size: 12px;
            z-index: 1000;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <div id="coords" class="coord-display">Координаты: ${String.format("%.6f", markerLat)}, ${String.format("%.6f", markerLng)}</div>
    <script>
        var currentLat = $markerLat;
        var currentLng = $markerLng;
        var map, marker;
        
        // Инициализация карты
        setTimeout(function() {
            try {
                map = L.map('map').setView([currentLat, currentLng], 12);
                
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);
                
                marker = L.marker([currentLat, currentLng], {draggable: true}).addTo(map);
                
                map.on('click', function(e) {
                    marker.setLatLng(e.latlng);
                    currentLat = e.latlng.lat;
                    currentLng = e.latlng.lng;
                    updateCoords();
                });
                
                marker.on('dragend', function(e) {
                    var pos = marker.getLatLng();
                    currentLat = pos.lat;
                    currentLng = pos.lng;
                    updateCoords();
                });
                
            } catch(e) {
                document.getElementById('coords').innerHTML = 'Ошибка: ' + e.message;
            }
        }, 100);
        
        function updateCoords() {
            document.getElementById('coords').innerHTML = 
                'Координаты: ' + currentLat.toFixed(6) + ', ' + currentLng.toFixed(6);
        }
        
        function getCurrentCoords() {
            return currentLat + ',' + currentLng;
        }
        
        updateCoords();
    </script>
</body>
</html>
                    """.trimIndent()
                    
                    loadDataWithBaseURL("https://unpkg.com", mapHtml, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            update = { webView ->
                // Периодически получаем координаты через JavaScript
                webView.evaluateJavascript("getCurrentCoords()") { result ->
                    result?.let { coords ->
                        val cleanCoords = coords.replace("\"", "")
                        val parts = cleanCoords.split(",")
                        if (parts.size == 2) {
                            try {
                                val lat = parts[0].toDouble()
                                val lng = parts[1].toDouble()
                                if (lat != markerLat || lng != markerLng) {
                                    markerLat = lat
                                    markerLng = lng
                                    onLocationSelected(lat, lng)
                                }
                            } catch (e: NumberFormatException) {
                                // Ignore parsing errors
                            }
                        }
                    }
                }
            }
        )
        
        // Координаты внизу
        Text(
            text = "Координаты: ${String.format("%.6f", markerLat)}, ${String.format("%.6f", markerLng)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}