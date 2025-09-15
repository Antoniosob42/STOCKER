package com.example.stocker2

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.VideoView
import com.example.stocker2.databinding.ActivityVerVideoBinding
import com.google.firebase.firestore.FirebaseFirestore

class VerVideo : AppCompatActivity() {

    // Declaración de variables miembro
    private lateinit var binding: ActivityVerVideoBinding
    private lateinit var btn_atras: ImageView
    private var mVideoView: VideoView? = null
    private val db = FirebaseFirestore.getInstance()
    private var id: String? = null
    private var urlVideo: String? = null
    private val myCollections = db.collection("supermercados")

    private var pos: Int = 0

    companion object {
        // Controla que haya una reproducción en proceso
        var isPlaying = false
        // Controla el estado de pausa
        var isPaused = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXML()
        val objIntent: Intent = intent
        id = objIntent.getStringExtra("id")!!
        if (mVideoView == null) {
            mVideoView = binding.videoView
            mVideoView!!.setOnCompletionListener { pararReproduccion() }
            reproducirDesdeUrlVideoDeFirestore()
        }
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            // Si es el primer onCreate, configuramos los botones y la reproducción inicial
            binding.playButton.isEnabled = true
            binding.stopButton.isEnabled = false
            binding.pauseButton.isEnabled = false
            binding.retrButton.isEnabled = false
            binding.avanButton.isEnabled = false
            controlVideo()
        } else {
            if (isPlaying) {
                // Habilita o deshabilita botones según el estado de reproducción
                binding.playButton.isEnabled = false
                binding.stopButton.isEnabled = true
                binding.pauseButton.isEnabled = true
                binding.retrButton.isEnabled = true
                binding.avanButton.isEnabled = true
            } else {
                // Habilita o deshabilita botones según el estado de reproducción
                binding.playButton.isEnabled = true
                binding.stopButton.isEnabled = false
                binding.pauseButton.isEnabled = false
                binding.retrButton.isEnabled = false
                binding.avanButton.isEnabled = false
            }
        }
    }

    private fun reproducirDesdeUrlVideoDeFirestore() {
        id?.let { it ->
            myCollections.document(it).get().addOnSuccessListener { documento ->
                if (documento.exists() && documento.contains("urlVideo")) {
                    urlVideo = documento.getString("urlVideo")
                    urlVideo?.let { url ->
                        val videoUri = Uri.parse(url)
                        mVideoView?.setVideoURI(videoUri)
                        mVideoView?.requestFocus()
                        mVideoView?.start()
                    }
                } else {
                    Log.d("Firestore", "Documento no encontrado o no contiene 'urlVideo'")
                }
            }.addOnFailureListener {
                Log.e("Firestore", "Error al obtener datos", it)
            }
        }
    }

    private fun crearObjetosDelXML() {
        binding = ActivityVerVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun controlVideo() {
        binding.playButton.setOnClickListener {
            isPlaying = true
            cargarMultimedia()
        }

        binding.stopButton.setOnClickListener {
            isPlaying = false
            pararReproduccion()
        }

        binding.pauseButton.setOnClickListener {
            if (mVideoView != null) {
                if (mVideoView!!.isPlaying) {
                    isPaused = true
                    mVideoView!!.pause()
                } else {
                    isPaused = false
                    mVideoView!!.start()
                }
            }
        }

        binding.retrButton.setOnClickListener {
            retrocederReproduccion()
        }

        binding.avanButton.setOnClickListener {
            avanzarReproduccion()
        }
    }

    private fun cargarMultimedia() {
        if (mVideoView == null) {
            mVideoView = binding.videoView
            mVideoView!!.setOnCompletionListener { pararReproduccion() }
            reproducirDesdeUrlVideoDeFirestore()
        }
        mVideoView!!.start()
        // Habilita o deshabilita botones según el estado de reproducción
        binding.playButton.isEnabled = false
        binding.stopButton.isEnabled = true
        binding.pauseButton.isEnabled = true
        binding.retrButton.isEnabled = true
        binding.avanButton.isEnabled = true
    }

    private fun retrocederReproduccion() {
        if (mVideoView != null) {
            val newPosition = (mVideoView!!.currentPosition - 10000).coerceIn(0, mVideoView!!.duration.takeIf { it > 0 } ?: 0)
            mVideoView!!.seekTo(newPosition)
        }
    }

    private fun avanzarReproduccion() {
        if (mVideoView != null) {
            val newPosition = (mVideoView!!.currentPosition + 10000).coerceIn(0, mVideoView!!.duration.takeIf { it > 0 } ?: 0)
            mVideoView!!.seekTo(newPosition)
        }
    }

    private fun pararReproduccion() {
        if (mVideoView != null) {
            mVideoView!!.pause()
            pos = 0
            mVideoView!!.seekTo(pos)
            mVideoView = null
            // Habilita o deshabilita botones según el estado de reproducción
            binding.playButton.isEnabled = true
            binding.stopButton.isEnabled = false
            binding.pauseButton.isEnabled = false
            binding.retrButton.isEnabled = false
            binding.avanButton.isEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (mVideoView != null) {
            pos = mVideoView!!.currentPosition
            mVideoView!!.pause()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mVideoView != null) {
            outState.putInt("posicion", pos)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mVideoView != null) {
            mVideoView = null
        }
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        super.onRestoreInstanceState(bundle)
        pos = bundle.getInt("posicion")
    }

    override fun onResume() {
        super.onResume()
        if (mVideoView != null) {
            if (isPlaying && !isPaused) {
                mVideoView!!.start()
            }
            mVideoView!!.seekTo(pos)
            controlVideo()
        }
    }
}
