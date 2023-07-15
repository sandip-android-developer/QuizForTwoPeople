package com.example.quizfortwopeople.view.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.quizfortwopeople.R

open class BaseActivity : AppCompatActivity() {
    var customdialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (customdialog != null) {
            customdialog!!.dismiss()
        }
    }

    fun showProgress(ctx: Context) {
        if (customdialog != null) {
            try {
                if (customdialog!!.isShowing()) {
                    customdialog!!.dismiss()
                }
            } catch (e: Exception) {
            }
        }
        customdialog = Dialog(ctx, R.style.ActivityDialog);
        customdialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customdialog!!.getWindow()?.setGravity(Gravity.CENTER)
        customdialog!!.setCancelable(false)
        customdialog!!.setContentView(R.layout.custom_progressbar)
        customdialog!!.show()
    }

    fun hideProgress() {
        try {
            if (customdialog != null && customdialog!!.isShowing) {
                customdialog!!.dismiss()
            }
        } catch (e: Exception) {
        }
    }
}