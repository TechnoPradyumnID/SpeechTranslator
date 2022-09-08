package com.example.speechtranslator

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity(),TextToSpeech.OnInitListener {
    lateinit var sourceEdt: TextInputEditText
    lateinit var translatedTv: TextView
    lateinit var tts: TextToSpeech
    lateinit var buttonSpeak: Button


    var fromLanguages = arrayOf<String?>("From","English","Hindi","Bengali","Gujarati","Tamil","Telugu","Kannada",
        "Marathi","Urdu","Arabic","Russian","Ukrainian","Vietnamese","Turkish","Swedish","Japanese","Catalan","Czech","Welsh")
    var toLanguages = arrayOf<String?>("To","English","Hindi", "Bengali","Gujarati","Tamil","Telugu","Kannada",
        "Marathi","Urdu","Arabic","Russian","Ukrainian","Vietnamese","Turkish","Swedish","Japanese","Catalan","Czech","Welsh")
    var languageCode = 0
    var fromLanguageCode = 0
    var toLanguageCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSpeak = this.button_speak
        translatedTv = this.translatedTxt

        buttonSpeak.isEnabled = false
        tts = TextToSpeech(this,this)

        buttonSpeak.setOnClickListener {
            speak()
        }

        val fromSpinner = findViewById<Spinner>(R.id.idFromSpinner)
        val toSpinner = findViewById<Spinner>(R.id.idToSpinner)
        sourceEdt = findViewById(R.id.inputTxt)
        val micIV = findViewById<ImageView>(R.id.idMic)
        val translateBtn = findViewById<MaterialButton>(R.id.btnTranslate)
        translatedTv = findViewById(R.id.translatedTxt)
        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                fromLanguageCode = getLanguageCode(fromLanguages[position])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        val fromAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.spinner_item, fromLanguages)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = fromAdapter
        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                toLanguageCode = getLanguageCode(toLanguages[position])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        val toAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.spinner_item, toLanguages)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toSpinner.adapter = toAdapter
        translateBtn.setOnClickListener {
            translatedTv.text = ""
            if (sourceEdt.text.toString().isEmpty()) {
                Toast.makeText(this@MainActivity,
                    "Please enter Your text to Translate",
                    Toast.LENGTH_SHORT).show()
            } else if (fromLanguageCode == 0) {
                Toast.makeText(this@MainActivity,
                    "Please Select source language",
                    Toast.LENGTH_SHORT).show()
            } else if (toLanguageCode == 0) {
                Toast.makeText(this@MainActivity,
                    "Please Select the language to make translation",
                    Toast.LENGTH_SHORT).show()
            } else {
                translateText(fromLanguageCode, toLanguageCode, sourceEdt.text.toString())
            }
        }
        micIV.setOnClickListener {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text")
            try {
                startActivityForResult(i, REQUEST_PERMISSION_CODE)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                sourceEdt.setText(result!![0])
            }
        }
    }

    private fun translateText(fromLanguageCode: Int, toLanguageCode: Int, source: String) {
        translatedTv.text = "Translating..."
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(fromLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener {
            translatedTv.text = "Translating.."
            translator.translate(source).addOnSuccessListener { s -> translatedTv.text = s }
                .addOnFailureListener {
                    Toast.makeText(this@MainActivity,
                        "Fail to translate : ",
                        Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this@MainActivity,
                "Fail to download language Model ",
                Toast.LENGTH_SHORT).show()
        }
    }

    //  String[] toLanguages = {"To","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Tamil","Czech",
    //     "Welsh","Hindi","Urdu"};
    fun getLanguageCode(language: String?): Int {
        var languageCode = 0
        languageCode = when (language) {
            "English" -> FirebaseTranslateLanguage.EN
            "Hindi" -> FirebaseTranslateLanguage.HI
//            "Punjabi" -> FirebaseTranslateLanguage.P
            "Bengali" -> FirebaseTranslateLanguage.BN
            "Gujarati" -> FirebaseTranslateLanguage.GU
            "Marathi" -> FirebaseTranslateLanguage.MR
            "Tamil" -> FirebaseTranslateLanguage.TA
            "Telugu" -> FirebaseTranslateLanguage.TE
            "Kannada" -> FirebaseTranslateLanguage.KN
            "Urdu" -> FirebaseTranslateLanguage.UR
            "Arabic" -> FirebaseTranslateLanguage.AR
            "Russian" -> FirebaseTranslateLanguage.RU
            "Ukrainian" -> FirebaseTranslateLanguage.UK
            "Vietnamese" -> FirebaseTranslateLanguage.VI
            "Turkish" -> FirebaseTranslateLanguage.TR
            "Swedish" -> FirebaseTranslateLanguage.SV
            "Japanese" -> FirebaseTranslateLanguage.JA
            "Catalan" -> FirebaseTranslateLanguage.CA
            "Czech" -> FirebaseTranslateLanguage.CS
            "Welsh" -> FirebaseTranslateLanguage.CY
            else -> 0
        }
        return languageCode
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 1
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS){
            val result = tts.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(this,"The Language Specified is not supported",Toast.LENGTH_SHORT).show()
            }else{
                buttonSpeak.isEnabled = true
            }
        }else{
            Toast.makeText(this,"Initialization Failed",Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak() {
        val text = translatedTv.text.toString()
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    override fun onDestroy() {
        if (tts != null){
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}