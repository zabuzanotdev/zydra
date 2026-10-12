package com.zydra.data.cracker

import com.zydra.domain.cracker.TesterFactory
import com.zydra.domain.cracker.TesterResult
import com.zydra.domain.model.CrackTarget
import java.io.File

class TesterFactoryImpl : TesterFactory {

    override fun testerFor(target: CrackTarget, file: File): TesterResult {
        return when (target) {
            is CrackTarget.Zip -> TesterResult.Available(ZipSingleTester(file))
            is CrackTarget.Pdf -> TesterResult.Available(PdfSingleTester(file))
            is CrackTarget.Rar -> if (RarFormatDetector.isRar5(file)) {
                TesterResult.Rar5NotSupported
            } else {
                TesterResult.Available(RarSingleTester(file))
            }
            is CrackTarget.Shadow -> {
                val tester = ShadowSingleTester.fromFile(file)
                if (tester == null) TesterResult.Unavailable else TesterResult.Available(tester)
            }
            is CrackTarget.Unsupported -> TesterResult.Unavailable
        }
    }
}
