package com.example.stocker2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.stocker2.databinding.ActivitySubirVideoBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SubirVideo : AppCompatActivity() {

    private lateinit var binding: ActivitySubirVideoBinding
    private lateinit var btn_atras: ImageView
    private var mVideoView: VideoView? = null

    val PETICION_PERMISO_CAMARA = 321
    val storageRef = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()
    private var id: String? = null
    private var FILEURI: Uri? = null
    private var urlVideo: String? = null
    private val myCollections = db.collection("supermercados")
    private var pos: Int = 0

    lateinit var activityResultLauncherCargarVideoDeGaleria: ActivityResultLauncher<Intent>

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
        reproducirDesdeUrlVideoDeFirestore()

        // Verificar y solicitar permisos si es necesario
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PETICION_PERMISO_CAMARA
            )
        }

        if (mVideoView == null) {
            mVideoView = binding.videoView
            mVideoView!!.setOnCompletionListener { pararReproduccion() }
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

        // Configurar el botón para seleccionar un video de la galería
        binding.btSubirVid.setOnClickListener {
            seleccionarVideoDeGaleria()
        }

        // Configurar el launcher para la selección de video
        activityResultLauncherCargarVideoDeGaleria =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.data != null) {
                    val data: Intent = result.data!!
                    if (result.resultCode == RESULT_OK) {
                        val fileUri = data.data
                        val riversRef = storageRef.child("videos/${fileUri?.lastPathSegment}")
                        val uploadTask = riversRef.putFile(fileUri!!)
                        uploadTask.addOnFailureListener {
                            Log.e("Firebase", "Error al subir archivo", it)
                        }.addOnSuccessListener { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                ToastUtil.showCustomToast(this, "Video Subido con exito a la Firebase $downloadUrl")

                                // Obtener el ID del supermercado
                                // Actualizar el campo 'urlVideo' en Firestore
                                myCollections.document(id!!)
                                    .update("urlVideo", downloadUrl.toString())
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "urlVideo actualizado correctamente")
                                        FILEURI = fileUri
                                        mVideoView!!.setVideoURI(fileUri)
                                        mVideoView!!.requestFocus()
                                        mVideoView!!.start()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error al actualizar urlVideo", e)
                                    }
                            }
                        }
                    }
                }
            }
    }

    private fun reproducirDesdeUrlVideoDeFirestore() {
        id?.let {
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

    private fun seleccionarVideoDeGaleria() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            activityResultLauncherCargarVideoDeGaleria.launch(intent)
        }
    }

    private fun crearObjetosDelXML() {
        binding = ActivitySubirVideoBinding.inflate(layoutInflater)
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
        if (mVideoView != null && mVideoView!!.duration > 0) {
            val newPosition = (mVideoView!!.currentPosition - 10000).coerceIn(0, mVideoView!!.duration)
            mVideoView!!.seekTo(newPosition)
        }
    }

    private fun avanzarReproduccion() {
        if (mVideoView != null && mVideoView!!.duration > 0) {
            val newPosition = (mVideoView!!.currentPosition + 10000).coerceIn(0, mVideoView!!.duration)
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

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        if (mVideoView != null) {
            bundle.putInt("posicion", pos)
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
