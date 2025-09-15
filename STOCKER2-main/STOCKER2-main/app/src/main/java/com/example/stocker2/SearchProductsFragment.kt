package com.example.stocker2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stocker2.databinding.FragmentSearchProductsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SearchProductsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val myCollectionp = db.collection("Productos")
    private val myCollections = db.collection("supermercados")
    private var _binding: FragmentSearchProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btFiltrar.setOnClickListener {
            realizarBusqueda()
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun realizarBusqueda() {
        val filtro = binding.textfiltro.text.toString().lowercase(Locale.ROOT)

        myCollections.get().addOnSuccessListener { supermercados ->
            val linearLayoutContainer = binding.linearLayoutContaine
            linearLayoutContainer.removeAllViews()

            for (supermercado in supermercados) {
                val productosStock = supermercado.data["productosStock"] as? List<*> ?: continue
                val productosStockList = productosStock.filterIsInstance<Map<String, Any>>()

                for (productoMap in productosStockList) {
                    val idProducto = productoMap["idProducto"] as? String ?: continue
                    val cantidad = (productoMap["cantidad"] as? Number)?.toInt() ?: 0

                    if (cantidad > 0) {
                        myCollectionp.document(idProducto).get().addOnSuccessListener { productoDoc ->
                            if (productoDoc.exists()) {
                                val nombreProducto = productoDoc.getString("nombre")?.lowercase(Locale.ROOT)
                                if (nombreProducto != null && (esSimilar(filtro, nombreProducto) || nombreProducto.startsWith(filtro))) {
                                    val nombreS = supermercado.getString("nombre")
                                    val direccionS = supermercado.getString("direccion")

                                    val textView = TextView(requireContext())
                                    if (Locale.getDefault().language == "es") {
                                        textView.text = getString(R.string.producto_info_es, nombreS, direccionS, cantidad, nombreProducto)
                                    } else {
                                        textView.text = getString(R.string.producto_info_en, nombreS, direccionS, cantidad, nombreProducto)
                                    }

                                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                    textView.setTextColor(Color.parseColor("#B68D8D"))
                                    linearLayoutContainer.addView(textView)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun esSimilar(query: String, target: String): Boolean {
        val maxDistance = 3
        return calcularDistanciaLevenshtein(query, target) <= maxDistance
    }

    private fun calcularDistanciaLevenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) {
            for (j in 0..b.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                }
            }
        }
        return dp[a.length][b.length]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
