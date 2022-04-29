package org.aerial.lib

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader


inline fun <reified T> Gson.fromJson(reader: Reader): T =
    fromJson(reader, object : TypeToken<T>() {}.type)


inline fun <T, R> Iterable<T>.filtermap(transform: (T) -> R?): List<R> {
    val result = mutableListOf<R>()

    for (item in this) {
        val mapped = transform(item)
        if (mapped != null) {
            result.add(mapped)
        }
    }
    return result
}

fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
    if (lists.isEmpty()) {
        return listOf()
    } else {
        val res = mutableListOf<List<T>>()
        val head = lists[0]
        val tail = cartesianProduct(lists.subList(1, lists.size))
        for (option in head) {
            if (tail.isEmpty()) {
                res.add(listOf(option))
            }
            for (list in tail) {
                val expandedList = mutableListOf<T>()
                expandedList.add(option)
                expandedList.addAll(list)
                res.add(expandedList)
            }
        }
        return res
    }
}

fun <T> pairs(valuesList: List<Set<T>>): MutableSet<Pair<T, T>> {
    val pairs = mutableSetOf<Pair<T, T>>()

    for (i in valuesList.indices) {
        val values = valuesList[i]
        for (j in (i + 1) until valuesList.size) {
            val others = valuesList[j]

            for (value in values) {
                for (other in others) {
                    pairs += Pair(value, other)
                }
            }
        }
    }

    return pairs
}
