package com.example.stocker2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.stocker2.databinding.FragmentHome2Binding

class HomeFragment2 : Fragment() {

    private var _binding: FragmentHome2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHome2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWelcomeMessage()
    }

    private fun setWelcomeMessage() {
        // Obtener el nombre de usuario desde SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences(
            requireActivity().packageName + "_preferences", Context.MODE_PRIVATE)
        val nombreUsuario = sharedPreferences.getString("nombreUsuario", "Usuario")

        // Establecer el mensaje de bienvenida usando el recurso de cadena con placeholder
        binding.welcome.text = getString(R.string.welcome_message, nombreUsuario)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
