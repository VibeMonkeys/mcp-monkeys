package com.monkeys.library.application.port.`in`

import com.monkeys.library.domain.model.LibraryStats

interface LibraryStatsUseCase {
    fun getLibraryStats(): LibraryStats
}
