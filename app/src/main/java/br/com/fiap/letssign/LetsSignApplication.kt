package br.com.fiap.letssign

import android.app.Application
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.utils.LocaleManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class LetsSignApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        container = AppContainer(this)
        LocaleManager.apply(this, container.preferences.language.value)
    }
}
