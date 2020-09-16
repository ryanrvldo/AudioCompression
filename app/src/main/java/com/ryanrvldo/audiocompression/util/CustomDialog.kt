package com.ryanrvldo.audiocompression.util

import android.content.Context
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ryanrvldo.audiocompression.R

class CustomDialog(context: Context) {
    private val loadingDialog = MaterialAlertDialogBuilder(
        context,
        R.style.ThemeOverlay_App_MaterialAlertDialog
    ).setView(View.inflate(context, R.layout.custom_loading_dialog, null))
        .setCancelable(false)
        .create()

    fun showLoadingDialog() = loadingDialog.show()

    fun closeLoadingDialog() = loadingDialog.dismiss()

    private val successDialog = MaterialAlertDialogBuilder(context)
        .setView(View.inflate(context, R.layout.custom_success_dialog, null))
        .setTitle(context.getString(R.string.process_finished))
        .setPositiveButton(context.getString(R.string.okay)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        .setCancelable(false)
        .create()

    fun showSuccessDialog() {
        successDialog.show()
    }
}