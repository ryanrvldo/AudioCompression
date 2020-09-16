package com.ryanrvldo.audiocompression.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ryanrvldo.audiocompression.R
import com.ryanrvldo.audiocompression.util.BoldiVignaCodes
import com.ryanrvldo.audiocompression.util.EliasDeltaCodes
import com.ryanrvldo.audiocompression.viewmodels.DecompressionViewModel
import com.ryanrvldo.audiocompression.viewmodels.DecompressionViewModel.Companion.runningTime
import kotlinx.android.synthetic.main.fragment_main_layout.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

@ExperimentalUnsignedTypes
class DecompressionFragment : BaseFragment() {

    private val viewModel by viewModels<DecompressionViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLoading.observe(viewLifecycleOwner) { result ->
            result?.let { status ->
                if (status) {
                    super.customDialog.showLoadingDialog()
                } else {
                    super.customDialog.closeLoadingDialog()
                }
            }
        }

        viewModel.initBytes.observe(viewLifecycleOwner) { result ->
            result?.let { bytes ->
                super.initBytes = bytes
                tv_audio_size.text = String.format("%,d bytes", bytes.size)
            }
        }
    }

    override fun selectAudioCallback(bytes: ByteArray) {
        viewModel.setInitBytes(bytes)
    }

    override fun onButtonProcessClicked(view: View) {
        super.onButtonProcessClicked(view)
        lifecycleScope.launch {
            val dictionaryCodes = getDictionaryFile() ?: return@launch
            resultBytes = viewModel.decompressAudio(initBytes, dictionaryCodes, methodCode)
            customDialog.showSuccessDialog()
            btn_process.hide()
            result_layout.visibility = View.VISIBLE

            tv_result_size.text = String.format(
                getString(R.string.size_result), resultBytes.size
            )
            tv_running_time.text = String.format(
                getString(R.string.running_time_result), runningTime
            )
        }
    }

    override fun onButtonSaveClicked(view: View) {
        super.onButtonSaveClicked(view)
        saveResult("${fileName.substringBefore(".")}.wav")
    }

    private fun getDictionaryFile(): ByteArray? {
        if ((fileName.substringAfter(".") == "edc" && methodCode != EliasDeltaCodes.METHOD_CODE) ||
            (fileName.substringAfter(".") == "bvc" && methodCode != BoldiVignaCodes.METHOD_CODE)
        ) {
            showSnackbar(getString(R.string.method_and_file_not_match))
            return null
        }
        val childFile = when (methodCode) {
            EliasDeltaCodes.METHOD_CODE -> "${fileName.substringBefore(".")}.edd"
            else -> "${fileName.substringBefore(".")}.bvd"
        }
        val file = File(requireContext().getExternalFilesDir(null), childFile)
        return if (file.exists()) {
            FileInputStream(file).readBytes()
        } else {
            showSnackbar(getString(R.string.dictionary_file_not_found))
            null
        }
    }
}