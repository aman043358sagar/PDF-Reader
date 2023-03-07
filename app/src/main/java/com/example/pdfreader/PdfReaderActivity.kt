package com.example.pdfreader

import android.content.Context
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pdfreader.databinding.ActivityPdfReaderBinding
import com.example.pdfreader.utils.showSnackbar
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.util.*

class PdfReaderActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var binding: ActivityPdfReaderBinding
    lateinit var uri: Uri

    lateinit var reader: PdfReader
    lateinit var tts: TextToSpeech
    var pageCount: Int = -1
    var currentPage: Int = 1
    var isReading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRead.setColorFilter(
            ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP
        )

        initialize(this)


        binding.btnRead.setOnClickListener {
            if (!isReading) {
                currentPage = 1
                isReading = true
                binding.lpi.visibility = View.VISIBLE
                setFloatingBtnImageResource(R.drawable.ic_speak_off)
                nextPageText()?.let { speakTxt(it) }
            } else {
                isReading = false
                setFloatingBtnImageResource(R.drawable.ic_speak)
                tts.stop()
            }
        }

    }

    private fun setFloatingBtnImageResource(drawable: Int) {
        runOnUiThread {
            binding.btnRead.setImageResource(drawable)
            binding.btnRead.setColorFilter(
                ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    private fun initialize(context: Context) {

        try {
            tts = TextToSpeech(this, this)
            uri = intent.getParcelableExtra("uri")!!
            reader = PdfReader(context.contentResolver.openInputStream(uri))
            pageCount = reader.numberOfPages
        } catch (e: Exception) {
            e.stackTrace
            binding.root.showSnackbar(e.toString())
        }
        binding.pdfView.fromUri(uri).load()
    }


    private fun nextPageText(): String? {
        if (pageCount != -1 && currentPage > pageCount) return null

        if (::reader.isInitialized) {
            binding.pdfView.jumpTo(currentPage - 1)
            return getTextFromPageNo(currentPage++)
        }

        return ""
    }

    private fun getTextFromPageNo(pageNo: Int) = PdfTextExtractor.getTextFromPage(reader, pageNo).trim { it <= ' ' }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            } else {
                tts.setSpeechRate(0.5f)
            }

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    runOnUiThread {
                        binding.lpi.visibility = View.GONE
                    }
                }

                override fun onDone(utteranceId: String?) {
                    nextPageText()?.let { speakTxt(it) }
                    if (currentPage > pageCount) {
                        isReading = false
                        setFloatingBtnImageResource(R.drawable.ic_replay)
                    }
                }

                override fun onError(utteranceId: String?) {

                }

            })
        }
    }

    fun speakTxt(text: String) {
        tts.speak(
            text, TextToSpeech.QUEUE_ADD, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
        )
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}