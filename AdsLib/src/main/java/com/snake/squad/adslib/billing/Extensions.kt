package com.snake.squad.adslib.billing

internal fun String.indexOfFirstNonAsciiWhitespace(startIndex: Int = 0, endIndex: Int = length): Int {
    for (i in startIndex until endIndex) {
        when (this[i]) {
            '\t', '\n', '\u000C', '\r', ' ' -> Unit
            else -> return i
        }
    }
    return endIndex
}

internal fun String.indexOfLastNonAsciiWhitespace(startIndex: Int = 0, endIndex: Int = length): Int {
    for (i in endIndex - 1 downTo startIndex) {
        when (this[i]) {
            '\t', '\n', '\u000C', '\r', ' ' -> Unit
            else -> return i + 1
        }
    }
    return startIndex
}

internal fun String.trimSubstring(startIndex: Int = 0, endIndex: Int = length): String {
    val start = indexOfFirstNonAsciiWhitespace(startIndex, endIndex)
    val end = indexOfLastNonAsciiWhitespace(start, endIndex)
    return substring(start, end)
}
