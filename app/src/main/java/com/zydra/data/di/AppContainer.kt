package com.zydra.data.di

import android.content.Context
import com.zydra.data.cracker.TesterFactoryImpl
import com.zydra.data.file.CacheFileResolver
import com.zydra.data.wordlist.ContentWordlistRepository
import com.zydra.domain.cracker.TesterFactory
import com.zydra.domain.engine.ParallelCrackEngine
import com.zydra.domain.repository.FileResolver
import com.zydra.domain.repository.WordlistRepository
import com.zydra.domain.usecase.CrackWithBruteForceUseCase
import com.zydra.domain.usecase.CrackWithDictionaryUseCase
import com.zydra.domain.usecase.DetectTargetUseCase

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val fileResolver: FileResolver = CacheFileResolver(appContext)

    val wordlistRepository: WordlistRepository = ContentWordlistRepository()

    val testerFactory: TesterFactory = TesterFactoryImpl()

    val crackEngine: ParallelCrackEngine = ParallelCrackEngine()

    val detectTarget: DetectTargetUseCase = DetectTargetUseCase()

    val crackWithDictionary: CrackWithDictionaryUseCase =
        CrackWithDictionaryUseCase(testerFactory, crackEngine)

    val crackWithBruteForce: CrackWithBruteForceUseCase =
        CrackWithBruteForceUseCase(testerFactory, crackEngine)
}
