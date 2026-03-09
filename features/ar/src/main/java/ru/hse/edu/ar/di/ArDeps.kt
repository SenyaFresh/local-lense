package ru.hse.edu.ar.di

import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import kotlin.properties.Delegates.notNull

interface ArDeps {
    val arPlacemarksRepository: ArPlacemarksRepository
}

interface ArDepsProvider {

    val deps: ArDeps

    companion object : ArDepsProvider by ArDepsStore
}

object ArDepsStore : ArDepsProvider {
    override var deps: ArDeps by notNull()
}