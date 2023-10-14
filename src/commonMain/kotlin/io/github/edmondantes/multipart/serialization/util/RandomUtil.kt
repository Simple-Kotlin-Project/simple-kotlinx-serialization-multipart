package io.github.edmondantes.multipart.serialization.util

import kotlin.random.Random

public fun Random.nextChar(from: Char, until: Char): Char =
    nextInt(from.code, until.code).toChar()

public fun Random.nextAlphanumericString(size: Int): String {
    val array = CharArray(size)
    for (i in array.indices) {
        array[i] =
            when (Random.nextInt(0, 3)) {
                0 -> nextChar('a', 'z')
                1 -> nextChar('A', 'Z')
                2 -> nextChar('0', '9')
                else -> error("Can not generate character")
            }
    }

    return array.concatToString()
}
