package com.ryanrvldo.audiocompression.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ryanrvldo.audiocompression.R
import com.ryanrvldo.audiocompression.util.EliasDeltaCodes
import com.ryanrvldo.audiocompression.viewmodels.CompressionViewModel
import com.ryanrvldo.audiocompression.viewmodels.CompressionViewModel.Companion.BR
import com.ryanrvldo.audiocompression.viewmodels.CompressionViewModel.Companion.CR
import com.ryanrvldo.audiocompression.viewmodels.CompressionViewModel.Companion.RC
import com.ryanrvldo.audiocompression.viewmodels.CompressionViewModel.Companion.SS
import com.ryanrvldo.audiocompression.viewmodels.CompressionViewModel.Companion.runningTime
import kotlinx.android.synthetic.main.fragment_main_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CompressionFragment : BaseFragment() {

    private val viewModel by viewModels<CompressionViewModel>()

    private lateinit var dictionaryCodes: ByteArray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isLoading.observe(viewLifecycleOwner) { value ->
            value?.let { status ->
                if (status) {
                    super.customDialog.showLoadingDialog()
                } else {
                    super.customDialog.closeLoadingDialog()
                }
            }
        }

        viewModel.initBytes.observe(viewLifecycleOwner) { value ->
            value?.let { bytes ->
                super.initBytes = bytes
                tv_audio_size.text = String.format("%,d bytes", bytes.size)
            }
        }

        viewModel.dictionaryCodes.observe(viewLifecycleOwner) { value ->
            value?.let { bytes -> this.dictionaryCodes = bytes }
        }
    }

    override fun selectAudioCallback(bytes: ByteArray) {
        super.selectAudioCallback(bytes)
        viewModel.setInitBytes(bytes)
    }

    override fun onButtonProcessClicked(view: View) {
        super.onButtonProcessClicked(view)
        lifecycleScope.launch {
            resultBytes = viewModel.compressAudio(initBytes.toTypedArray(), methodCode)
            customDialog.showSuccessDialog()
            btn_process.hide()
            result_layout.visibility = View.VISIBLE

            tv_result_size.text = String.format(
                getString(R.string.size_result), resultBytes.size
            )
            tv_running_time.text = String.format(
                getString(R.string.running_time_result), runningTime
            )
            tv_rc.text = String.format(getString(R.string.rc_result), RC)
            tv_cr.text = String.format(getString(R.string.cr_result), CR)
            tv_ss.text = String.format(getString(R.string.ss_result), SS)
            tv_br.text = String.format(getString(R.string.br_result), BR)
        }
    }

    override fun onButtonSaveClicked(view: View) {
        super.onButtonSaveClicked(view)
        if (methodCode == EliasDeltaCodes.METHOD_CODE) {
            saveResult("${fileName.substringBefore(".")}.edc")
        } else {
            saveResult("${fileName.substringBefore(".")}.bvc")
        }
    }

    override fun saveResultCallback(uri: Uri) {
        super.saveResultCallback(uri)
        saveDictionary()
    }

    private fun saveDictionary() {
        if (!this::dictionaryCodes.isInitialized) {
            showSnackbar("${getString(R.string.something_wrong_process)} ${getString(R.string.dictionary_not_init)}")
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val childFile = when (methodCode) {
                EliasDeltaCodes.METHOD_CODE -> "${fileName.substringBefore(".")}.edd"
                else -> "${fileName.substringBefore(".")}.bvd"
            }
            viewModel.saveDictionaryFile(
                File(requireContext().getExternalFilesDir(null), childFile),
                dictionaryCodes
            )
        }
    }
}