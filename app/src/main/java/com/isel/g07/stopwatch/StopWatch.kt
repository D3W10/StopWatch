package com.isel.g07.stopwatch

data class StopWatch(var isRunning: Boolean = false, private var init: Long = 0L, private var elapsed: Long = 0L, private var stored: Long = 0L) {
    fun start() {
        if (!isRunning) {
            isRunning = true
            init = System.currentTimeMillis()
        }
    }

    fun stop() {
        if (isRunning) {
            isRunning = false
            stored += System.currentTimeMillis() - init
        }
    }

    fun reset() {
        isRunning = false
        elapsed = 0L
        stored = 0L
    }

    override fun toString(): String {
        val total = if (isRunning) System.currentTimeMillis() - init + stored else stored

        val hours = Math.floorDiv(total, 3600000)
        val minutes = Math.floorDiv(total, 60000) - hours * 60
        val seconds = Math.floorDiv(total, 1000) - hours * 3600 - minutes * 60
        val miliseconds = total - hours * 3600000 - minutes * 60000 - seconds * 1000

        return if (hours == 0L)
            String.format("%02d:%02d.%03d", minutes, seconds, miliseconds)
        else
            String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, miliseconds)
    }
}