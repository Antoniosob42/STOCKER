package com.example.stocker2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stocker2.databinding.ActivityRegistroMapasBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

class RegistroMapas : AppCompatActivity() {

    private lateinit var map: MapView
    private val PETICION_PERMISOS_OSM = 0
    private lateinit var locListener: LocationListener
    private lateinit var locManager: LocationManager
    private var ultimaUbicacionMarcador: GeoPoint? = null
    private lateinit var posicion_new: GeoPoint
    private lateinit var marker: Marker
    private var id: String? = null
    private var marcadorActual: Marker? = null
    private var nombre: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val myCollections = db.collection("supermercados")
    private lateinit var binding: ActivityRegistroMapasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXml()

        id = intent.getStringExtra("id")!!
        myCollections.document(id!!).get().addOnSuccessListener { result ->
            nombre = result.getString("nombre")
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET
                ), PETICION_PERMISOS_OSM
            )
        } else {
            setupMap()
        }
    }

    private fun crearObjetosDelXml() {
        binding = ActivityRegistroMapasBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupMap() {
        binding.buttonRegistro.isEnabled = false
        map = binding.map

        binding.buttonRegistro.setOnClickListener {
            actualizarUbicacionEnFirebase()
            ToastUtil.showCustomToast(this, "Ubicacion Actualizada!")
            finish()
        }

        generarMapa()
        anadirAccionesMapa()
        quitarRepeticionYLimitarScroll()
        habilitarMiLocalizacion()
    }

    @SuppressLint("MissingPermission")
    private fun habilitarMiLocalizacion() {
        locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        locListener = LocationListener { location -> pintarRutaLinea(location) }

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0f, locListener)
    }

    private fun pintarRutaLinea(loc: Location) {
        val geoPoints: ArrayList<GeoPoint> = ArrayList()
        marker = Marker(map)

        if (!::posicion_new.isInitialized) {
            posicion_new = GeoPoint(loc.latitude, loc.longitude)
        } else {
            posicion_new = GeoPoint(loc.latitude, loc.longitude)
            geoPoints.add(posicion_new)
        }
        pintarLinea(geoPoints)
        moverAPosicion(posicion_new)
    }

    private fun pintarLinea(geoPoints: ArrayList<GeoPoint>) {
        val line = Polyline()
        line.setPoints(geoPoints)
        map.overlayManager.add(line)
    }

    private fun moverAPosicion(latlngP: GeoPoint) {
        map.controller.animateTo(latlngP, 15.5, 1L, 29.0F, false)
    }

    private fun quitarRepeticionYLimitarScroll() {
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(
            MapView.getTileSystem().maxLatitude,
            MapView.getTileSystem().minLatitude,
            0
        )
        map.setScrollableAreaLimitLongitude(
            MapView.getTileSystem().minLongitude,
            MapView.getTileSystem().maxLongitude,
            0
        )
    }

    private fun anadirMarcador(posicion_new: GeoPoint, ) {
        marcadorActual?.let { map.overlays.remove(it) }
        val marker = Marker(map)
        marker.position = posicion_new
        ultimaUbicacionMarcador = posicion_new
        marker.title = nombre
        marker.snippet = "Este es tu supermercado"
        marker.icon = ContextCompat.getDrawable(map.context, R.drawable.star_full)
        map.overlays.add(marker)
        binding.buttonRegistro.isEnabled = true
        marcadorActual = marker
        map.invalidate()
    }

    private fun anadirAccionesMapa() {
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(loc: GeoPoint?): Boolean {
                loc?.let { mostrarCoordenadasDialogo(it) }
                return true
            }

            override fun longPressHelper(loc: GeoPoint?): Boolean {
                loc?.let { anadirMarcador(it) }
                return true
            }
        }
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(0, mapEventsOverlay)
    }

    private fun mostrarCoordenadasDialogo(loc: GeoPoint) {
        val mensaje = "Latitud: ${loc.latitude}, Longitud: ${loc.longitude}"
        AlertDialog.Builder(this)
            .setTitle("Coordenadas del Punto")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun actualizarUbicacionEnFirebase() {
        marcadorActual?.let { marcador ->
            myCollections.document(id!!)
                .update("latitudubi", marcador.position.latitude)
                .addOnSuccessListener { Log.d("Firestore", "latitud actualizada correctamente") }
                .addOnFailureListener { e -> Log.e("Firestore", "Error al actualizar latitud", e) }

            myCollections.document(id!!)
                .update("longitudubi", marcador.position.longitude)
                .addOnSuccessListener { Log.d("Firestore", "longitud actualizada correctamente") }
                .addOnFailureListener { e -> Log.e("Firestore", "Error al actualizar longitud", e) }
        }
    }

    private fun generarMapa() {
        Configuration.getInstance().load(this, getSharedPreferences(packageName + "osmdroid", Context.MODE_PRIVATE))
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.minZoomLevel = 4.0
        map.controller.setZoom(12.0)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.setMultiTouchControls(true)

        val compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)
    }

    override fun onDestroy() {
        super.onDestroy()
        locManager.removeUpdates(locListener)
        map.onDetach()
    }
}
