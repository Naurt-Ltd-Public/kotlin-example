package com.naurt.demo

import org.junit.Test

class NaurtWrapperTest {

    private val naurtWrapper = NaurtWrapper()

    @Test
    fun start() {
        naurtWrapper.start()
    }

    @Test
    fun stop() {
        naurtWrapper.stop()
    }
}