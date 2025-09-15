package com.example.stocker2

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.stocker2.databinding.ActivityReproductorMercadoBinding
import com.example.stocker2.utils.ToastUtil
import java.util.Locale

class ReproductorMercado : AppCompatActivity(), MediaPlayer.OnCompletionListener {

    private lateinit var binding: ActivityReproductorMercadoBinding
    private lateinit var btn_atras: ImageView
    private var mediaPlayer: MediaPlayer? = null
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var sonidoActual: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXml()
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }
        val nombre = intent.getStringExtra("nombre")?.lowercase(Locale.ROOT)

        sonidoActual = when (nombre) {
            "mercadona" -> Uri.parse("android.resource://$packageName/" + R.raw.rapmercadona)
            "dia" -> Uri.parse("android.resource://$packageName/" + R.raw.sevidia)
            "carrefour" -> Uri.parse("android.resource://$packageName/" + R.raw.jazzcarrefour)
            "lidl" -> Uri.parse("android.resource://$packageName/" + R.raw.heavylidl)
            else -> {
                ToastUtil.showCustomToast(this, "Supermercado No Reconocido")

                finish()
                return
            }
        }
        binding.seekBar.isEnabled = false
        controlSonido(sonidoActual)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null) {
                result.data!!
                controlSonido(sonidoActual)
            }
        }

        // Restaurar estado si está disponible
        savedInstanceState?.let {
            val mediaPosition = it.getInt("mediaPosition", 0)
            it.getBoolean("mediaPlaying", false)
            iniciarReproduccion(sonidoActual, mediaPosition)
        }
    }

    /**
     * Prepara los eventos para todos los botones de la interfaz
     *
     * @param id Identificador del audio a reproducir
     */
    private fun controlSonido(id: Uri?) {
        binding.playButton.isEnabled = true

        binding.playButton.setOnClickListener {      // Acción al pulsar el botón del play
            iniciarReproduccion(id)
        }

        binding.pauseButton.setOnClickListener {     // Acción al pulsar el botón de pausa
            if (mediaPlayer != null) {                      // Si está cargado el mediaPlayer
                if (mediaPlayer!!.isPlaying()) {            // Si está ejecutándose lo pausamos
                    mediaPlayer!!.pause()
                } else {                                   // Si no está ejecutándose es porque está en pausa, lo volvemos a iniciar
                    mediaPlayer!!.start()
                }
            }
        }

        binding.stopButton.setOnClickListener {      // Acción al pulsar el botón de Stop
            pararReproductor()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {        // Controles de la seekBar
            // Control del avance por parte del usuario
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {                             // Si el cambio ha sido por pulsación del usuario
                    mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
    }

    private fun iniciarReproduccion(id: Uri?, startPosition: Int = 0) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, id)
            mediaPlayer!!.setOnCompletionListener(this)
            inicializarSeekBar()
            mediaPlayer!!.seekTo(startPosition)
            mediaPlayer!!.start()                // Comenzamos la reproducción
            binding.playButton.isEnabled = false  // Habilitamos y deshabilitamos los botones
            binding.stopButton.isEnabled = true
            binding.pauseButton.isEnabled = true
            binding.seekBar.isEnabled = true
        }
    }

    private fun pararReproductor() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
            binding.seekBar.isEnabled = false
            binding.playButton.isEnabled = true
            binding.stopButton.isEnabled = false
            binding.pauseButton.isEnabled = false
        }
    }

    /**
     * Inicializar seek bar
     * Ejecuta en un hilo la actualización de la barra de progreso
     */
    private fun inicializarSeekBar() {
        binding.seekBar.max = mediaPlayer!!.duration
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
            object : Runnable {
                override fun run() {
                    try {
                        binding.seekBar.progress = mediaPlayer!!.currentPosition
                        handler.postDelayed(this, 1000)
                    } catch (e: Exception) {
                        binding.seekBar.progress = 0
                    }
                }
            },
            0
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mediaPlayer?.let {
            outState.putInt("mediaPosition", it.currentPosition)
            outState.putBoolean("mediaPlaying", it.isPlaying)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        pararReproductor()
    }

    private fun crearObjetosDelXml() {
        binding = ActivityReproductorMercadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
