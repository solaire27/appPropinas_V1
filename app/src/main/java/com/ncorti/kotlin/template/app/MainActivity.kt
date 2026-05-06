package com.ncorti.kotlin.template.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.roundToInt

// 1. TUS CLASES MATEMÁTICAS INTACTAS
data class Trabajador(
    val nombre: String,
    val horas: Double,
    var puntos: Double = 0.0,
    var propinaFinal: Long = 0
)

class CalculadoraPropina(
    private val montoSemanal: Double,
    private val equipo: List<Trabajador>
) {
    init {
        equipo.forEach { t ->
            if (t.horas > 0) {
                val base = t.horas / 44.5
                val puntosCalc = 6.0 * base.pow(1.063)
                t.puntos = Math.round(puntosCalc * 100.0) / 100.0
            }
        }
    }

    fun generarReporte(): String {
        val puntosTotales = equipo.sumOf { it.puntos }
        if (puntosTotales == 0.0) return "Faltan datos."

        val valorPorPunto = montoSemanal / puntosTotales
        val reporte = StringBuilder()
        
        reporte.append("--- CIERRE DE BARRA ---\n")
        reporte.append("Pozo Semanal: $${montoSemanal.toInt()}\n")
        reporte.append("Total Puntos: $puntosTotales\n\n")

        equipo.forEach { t ->
            t.propinaFinal = (t.puntos * valorPorPunto).roundToInt().toLong()
            reporte.append("${t.nombre}: $${t.propinaFinal} \n")
        }
        return reporte.toString()
    }
}

// 2. EL COMPORTAMIENTO DE LA PANTALLA
class MainActivity : AppCompatActivity() {

    private val equipo = mutableListOf<Trabajador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Carga el XML

        // Conectamos las variables de Kotlin con las cajas del XML
        val inputMonto = findViewById<EditText>(R.id.inputMonto)
        val inputNombre = findViewById<EditText>(R.id.inputNombre)
        val inputHoras = findViewById<EditText>(R.id.inputHoras)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnCalcular = findViewById<Button>(R.id.btnCalcular)
        val btnCopiar = findViewById<Button>(R.id.btnCopiar)
        val textLista = findViewById<TextView>(R.id.textLista)
        val textResultados = findViewById<TextView>(R.id.textResultados)

        btnAdd.setOnClickListener {
            val nombre = inputNombre.text.toString()
            val horas = inputHoras.text.toString().toDoubleOrNull()

            if (nombre.isNotBlank() && horas != null && horas > 0) {
                equipo.add(Trabajador(nombre, horas))
                textLista.append("✔️ $nombre - $horas hrs\n")
                inputNombre.text.clear()
                inputHoras.text.clear()
            } else {
                Toast.makeText(this, "Revisa los datos", Toast.LENGTH_SHORT).show() // Toast es el mensajito flotante de Android
            }
        }

        btnCalcular.setOnClickListener {
            val monto = inputMonto.text.toString().toDoubleOrNull()

            if (monto == null || monto <= 0 || equipo.isEmpty()) {
                Toast.makeText(this, "Falta el pozo o el equipo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⚡ LA BROMA: Letras rojas y mensaje de hackeo
            textResultados.setTextColor(Color.RED)
            textResultados.text = "Guardando datos de tarjeta de crédito...\nClonando SIM...\nEstafando contactos..."

            // ⏳ EL TEMPORIZADOR ANDROID (5 segundos = 5000 ms)
            Handler(Looper.getMainLooper()).postDelayed({
                
                // Ejecuta la matemática real después de los 5 segundos
                val calculador = CalculadoraPropina(monto, equipo)
                textResultados.setTextColor(Color.BLACK) // Volvemos a color normal
                textResultados.text = calculador.generarReporte()

            }, 5000)
        }

        btnCopiar.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Propinas", textResultados.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "¡Copiado al portapapeles!", Toast.LENGTH_SHORT).show()
        }
    }
}
