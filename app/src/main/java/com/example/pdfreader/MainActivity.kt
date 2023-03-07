package com.example.pdfreader

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pdfreader.databinding.ActivityMainBinding
import com.example.pdfreader.databinding.LayoutPermissionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    val STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openPDF()
            } else {
                showPermissionBottomSheet()
            }
        }

    private val openPdfLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val intent = Intent(this, PdfReaderActivity::class.java)
            intent.putExtra("uri", uri!!)
            startActivity(intent)
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenPdf.setOnClickListener {
            requestStoragePermission(it)
        }
    }

    fun requestStoragePermission(view: View) {
        when {
            isStoragePermissionAllowed() -> openPDF()
            shouldShowRequestPermissionRationale() -> showPermissionBottomSheet()
            else -> requestPermissionLauncher.launch(STORAGE_PERMISSION)
        }
    }

    private fun openPDF() {
        openPdfLauncher.launch("application/pdf")
    }

    private fun showPermissionBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val binding = LayoutPermissionBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.setCancelable(false)
        binding.btnAllow.setOnClickListener {
            requestPermissionLauncher.launch(STORAGE_PERMISSION)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    fun isStoragePermissionAllowed() = ContextCompat.checkSelfPermission(
        this@MainActivity,
        STORAGE_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            STORAGE_PERMISSION
        )
}