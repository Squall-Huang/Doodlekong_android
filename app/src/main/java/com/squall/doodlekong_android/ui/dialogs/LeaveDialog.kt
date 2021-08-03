package com.squall.doodlekong_android.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squall.doodlekong_android.R

class LeaveDialog : DialogFragment() {


    private var onPositiveClickLister: (() -> Unit)? = null

    fun setPositiveClickListerner(listener: () -> Unit) {
        onPositiveClickLister = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_leave_title)
            .setMessage(R.string.dialog_leave_message)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                onPositiveClickLister?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton(R.string.dialog_leave_no) { dialogInterface, _ ->
                dialogInterface?.cancel()
            }
            .create()
    }
}