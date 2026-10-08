package com.snake.squad.adslib.models.nativee.sequence

data class NativeSequenceConfig(
    val keywordsForAdRequest: Set<String> = emptySet(),
    val timeout: Int = DEFAULT_TIMEOUT, // milliseconds
    val waitingDuration: Long = DEFAULT_WAITING_DURATION, // seconds
) {
    companion object {
        const val DEFAULT_TIMEOUT: Int = 10_000
        const val DEFAULT_WAITING_DURATION: Long = 3
    }
}