package com.catradar.proyectofinal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireActivity().getSharedPreferences("perfil", Context.MODE_PRIVATE)
        val modoTema = prefs.getString("modo_tema", "sistema")

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupTheme)
        val radioSystem = view.findViewById<RadioButton>(R.id.radioSystem)
        val radioLight = view.findViewById<RadioButton>(R.id.radioLight)
        val radioDark = view.findViewById<RadioButton>(R.id.radioDark)

        when (modoTema) {
            "oscuro" -> radioDark.isChecked = true
            "claro" -> radioLight.isChecked = true
            else -> radioSystem.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val nuevoModo = when (checkedId) {
                R.id.radioLight -> "claro"
                R.id.radioDark -> "oscuro"
                else -> "sistema"
            }

            prefs.edit { putString("modo_tema", nuevoModo) }

            AppCompatDelegate.setDefaultNightMode(
                when (nuevoModo) {
                    "oscuro" -> AppCompatDelegate.MODE_NIGHT_YES
                    "claro" -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )

            requireActivity().recreate()
        }
    }
}
