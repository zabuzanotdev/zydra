package com.zydra

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class ZydraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
    }
}
