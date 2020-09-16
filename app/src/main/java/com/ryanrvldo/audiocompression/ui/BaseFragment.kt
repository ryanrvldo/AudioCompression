package com.ryanrvldo.audiocompression.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ryanrvldo.audiocompression.R
import com.ryanrvldo.audiocompression.util.BoldiVignaCodes
import com.ryanrvldo.audiocompression.util.CustomDialog
import com.ryanrvldo.audiocompression.util.EliasDeltaCodes
import kotlinx.android.synthetic.main.fragment_main_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException

open class BaseFragment : Fragment(R.layout.fragment_main_layout) {

    var methodCode = 0

    lateinit var initBytes: ByteArray
    lateinit var resultBytes: ByteArray
    lateinit var fileName: String
    lateinit var customDialog: CustomDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        customDialog = CustomDialog(requireContext())
        result_layout.visibility = View.GONE

        val fragmentId = findNavController().currentDestination?.id
        fragmentId?.let {
            btn_process.text = when (fragmentId) {
                R.id.compressionFragment -> getString(R.string.compress)
                R.id.decompressionFragment -> {
                    tv_rc.visibility = View.GONE
                    tv_cr.visibility = View.GONE
                    tv_ss.visibility = View.GONE
                    tv_br.visibility = View.GONE
                    getString(R.string.decompress)
                }
                else -> getString(R.string.error)
            }
        }

        chip_group_method.setOnCheckedChangeListener { _, id ->
            methodCode = when (id) {
                R.id.chip_elias_method -> EliasDeltaCodes.METHOD_CODE
                else -> BoldiVignaCodes.METHOD_CODE
            }
        }

        btn_choose_file.setOnClickListener { selectAudio() }

        btn_process.setOnClickListener {
            if (!this::initBytes.isInitialized) {
                showSnackbar(getString(R.string.please_init_audio))
            } else if (methodCode == 0) {
                showSnackbar(getString(R.string.please_choose_method))
            } else {
                onButtonProcessClicked(it)
            }
        }

        btn_save_file.setOnClickListener { onButtonSaveClicked(it) }

    }

    private fun selectAudio() {
        selectAudioResultLauncher.launch("*/*")
    }

    private val selectAudioResultLauncher = registerForActivityResult(GetContent()) { result ->
        result?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                inputStream?.apply {
                    selectAudioCallback(readBytes())
                    close()
                }
            } catch (e: FileNotFoundException) {
                showSnackbar(getString(R.string.error))
            }

            fileName = it.lastPathSegment?.substringAfterLast("/").toString()
            et_file_path.setText(fileName)
        }
    }

    fun saveResult(stringExtras: String) {
        saveAudioResultLauncher.launch(stringExtras)
    }

    private val saveAudioResultLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            uri?.also {
                saveResultCallback(it)
            }
        }

    open fun onButtonProcessClicked(view: View) = Unit

    open fun selectAudioCallback(bytes: ByteArray) = Unit

    open fun onButtonSaveClicked(view: View) = Unit

    open fun saveResultCallback(uri: Uri) {
        if (this::resultBytes.isInitialized) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val outputStream = requireContext().contentResolver.openOutputStream(uri)
                    outputStream?.apply {
                        write(resultBytes)
                        close()
                    }
                } catch (e: IOException) {
                    showSnackbar("${getString(R.string.something_wrong_process)} ${e.message}")
                }
            }
        }
    }

    fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .setAnchorView(R.id.bottom_navigation)
            .show()
    }
}