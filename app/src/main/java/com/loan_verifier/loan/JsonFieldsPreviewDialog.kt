package com.loan_verifier.loan

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.loan_verifier.loan.databinding.ActivityBussnessFormBinding

class JsonFieldsPreviewDialog(
    private val jsonObject: JsonObject,
    private val onSubmit: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_json_preview, null)

        val containerFields = view.findViewById<LinearLayout>(R.id.containerFields)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnSubmit = view.findViewById<AppCompatButton>(R.id.btnSubmit)

        // Style buttons
        val themeColor = ContextCompat.getColor(requireContext(), R.color.theme_color)
        val errorColor = ContextCompat.getColor(requireContext(), R.color.error_color_material)
        val textColor = ContextCompat.getColor(requireContext(), R.color.white_color)
        btnSubmit.setBackgroundColor(themeColor)
        btnCancel.setBackgroundColor(errorColor)
        btnSubmit.setTextColor(textColor)
        btnCancel.setTextColor(textColor)

        // Loop through sorted keys
        jsonObject.entrySet()
            .sortedBy { it.key.lowercase() }
            .forEach { (key, value) ->
                var key = key
                var isNotEmptyValue=true
                if (key.equals("proprietorRadioButtonOtherDetails",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("deviceID",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("image_name",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("file_name",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("imagelist",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("lat",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("long",true)) {
                    isNotEmptyValue=false
                }else if (key.equals("neighbour_check_status",true)) {
                    key="neighbour_status"
                }

                if (isNotEmptyValue) {
                    val label = TextView(requireContext()).apply {
                        text = formatKey(key)
                        setTypeface(null, Typeface.BOLD)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        setPadding(0, 12, 0, 4)
                    }
                    containerFields.addView(label)

                    when {
                        // Single value, no array

                        !value.asString.contains("[") -> {
                            var valueText = value.asString.trim().ifEmpty { "-" }
                            val valueView = TextView(requireContext()).apply {
                                if (valueText.equals("proprietorRadioButtonOtherDetails",true)) {
                                    valueText = "proprietor other filed"
                                    isNotEmptyValue=false
                                }else if (valueText.equals("deviceID",true)) {
                                    valueText=""
                                    isNotEmptyValue=false
                                }else if (valueText.equals("image_name",true)) {
                                    valueText=""
                                    isNotEmptyValue=false
                                }
                                text = valueText
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                                setPadding(0, 0, 0, 8)
                            }
                            if (isNotEmptyValue)
                                containerFields.addView(valueView)
                        }

                        // Value contains array data
                        value.asString.contains("[") || value.isJsonArray -> {
                            val array = parseDoubleEncodedJsonArray(value.asString, formatKey(key))

                            // Smaller header for array fields — smaller font, lighter color
                            val sectionHeader = TextView(requireContext()).apply {
                                text = formatKey(key)
                                setTypeface(null, Typeface.BOLD)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f) // smaller than main header
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.material_blue_grey_80))
                                setPadding(0, 12, 0, 6)
                            }
//                        containerFields.addView(sectionHeader)

                            // Filter out empty objects only if *all* fields are empty — but keep any value that has at least one field with data
                            val meaningfulItems = array.filter { element ->
                                if (element.isJsonObject) {
                                    val obj = element.asJsonObject
                                    obj.entrySet().any { it.value.asString.trim().isNotEmpty() }
                                } else {
                                    // For non-objects (strings, numbers), keep if not empty
                                    element.toString().trim().removeSurrounding("\"").isNotEmpty()
                                }
                            }

                            if (meaningfulItems.isEmpty()) {
                                val emptyView = TextView(requireContext()).apply {
                                    text = "No entries available"
                                    setPadding(16, 2, 0, 8)
                                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                                    setTextColor(ContextCompat.getColor(requireContext(), R.color.material_blue_grey_90))
                                }
                                containerFields.addView(emptyView)
                            } else {
                                meaningfulItems.forEachIndexed { index, element ->
                                    val itemView = TextView(requireContext()).apply {
                                        text = buildString {
                                            append("\nEntry ${index + 1}: \n")
                                            if (element.isJsonObject) {
                                                val obj = element.asJsonObject
                                                val fields = obj.entrySet()
                                                    .filter { it.value.asString.trim().isNotEmpty() }
                                                    .joinToString(" | ") {
                                                        "${formatKey(it.key)}: ${it.value.asString}"
                                                    }
                                                append(fields)
                                            } else {
                                                append(element.toString().removeSurrounding("\""))
                                            }
                                        }
                                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                                        setPadding(24, 2, 8, 2)
                                    }
                                    containerFields.addView(itemView)
                                }
                                val widthInDp = 100
                                val widthInPx = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, widthInDp.toFloat(), resources.displayMetrics
                                ).toInt()

                                val linearView = LinearLayout(requireContext()).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    setPadding(0, 3, 0, 3)
                                    setBackgroundColor(Color.parseColor("#666666"))

                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                }
                                containerFields.addView(linearView)

                            }
                        }

                        else -> {
                            val unknownView = TextView(requireContext()).apply {
                                text = value.toString()
                                setPadding(0, 0, 0, 8)
                            }
                            containerFields.addView(unknownView)
                        }
                    }
                }
            }

        btnCancel.setOnClickListener { dismiss() }
        btnSubmit.setOnClickListener {
            dismiss()
            onSubmit()
        }

        builder.setView(view)
        return builder.create()
    }

    fun parseDoubleEncodedJsonArray(jsonString: String?, fieldName: String = "UnknownField"): JsonArray {
        return try {
            val firstParse: JsonElement? = jsonString?.takeIf { it.isNotBlank() && !jsonString.contentEquals("{}") }?.let {
                JsonParser.parseString(it)
            }

            val rawArrayString = if (firstParse?.isJsonPrimitive == true && firstParse.asJsonPrimitive.isString) {
                firstParse.asString
            } else {
                jsonString
            }

            val finalParsed = JsonParser.parseString(rawArrayString)
            if (finalParsed.isJsonArray) {
                finalParsed.asJsonArray
            } else {
                Log.w("JsonParse", "Field '$fieldName' is not a valid JsonArray: $rawArrayString")
                JsonArray()
            }
        } catch (e: Exception) {
            Log.e("JsonParse", "Failed to parse '$fieldName' as double-encoded JsonArray", e)
            JsonArray()
        }
    }

    private fun formatKey(key: String): String {
        return key.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
    }
}
