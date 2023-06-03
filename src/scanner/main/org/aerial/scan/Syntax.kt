package org.aerial.scan

//const val KW_DOMAIN = "aerial:domain"
//
//const val KW_COMPONENT = "aerial:component"
//const val KW_COMPONENT_FEATURES = "features:"
//
//const val KW_VARIABLE = "aerial:variable"
//
//const val KW_EXAMPLE = "aerial:example"
//const val KW_HOW_TO = "aerial:how-to"
//const val KW_TODO = "aerial:todo"
//const val KW_EXAMPLE_VARIABLES = "variables:"
//
//const val KW_CROSS_CUT = "aerial:cross-cut"
//
//const val KW_JOURNEY = "aerial:journey"
//
//const val KW_DESC = "desc:"
//const val KW_TAGS = "tags:"
//const val PREFIX_LIST_ITEM = "\\* "
//const val SEP_INLINE_LIST = ","
//const val ANYTHING = ".*"
//val QUOTES_REGEX = Regex(".*?\"(.+)\".*?")
//
//fun containsKeyword(keyword: String, text: String): Boolean {
//    return text.contains(Regex(ANYTHING + keyword + ANYTHING))
//}
//
//@Throws(ParsingException::class)
//fun ensureKeyword(keyword: String, text: String) {
//    if (!containsKeyword(keyword, text)) {
//        throw ParsingException("Expected keyword [$keyword], but failed to find it in text [$text]!")
//    }
//}
//
//fun extractAfterKeyword(keyword: String, text: String): String {
//    val extracted = text.replace(Regex(ANYTHING + keyword), "").trim()
//    when {
//        extracted.isBlank() -> {
//            throw ParsingException("Expected text after keyword [$keyword], but it was blank!")
//        }
//        else -> {
//            return extracted
//        }
//    }
//}
//
//fun splitByKeyword(keyword: String, text: String): Pair<String, String> {
//    val extracted = text.split(keyword)
//    if (extracted.size != 2) {
//        throw ParsingException("Expected keyword [$keyword], but did not find it!")
//    }
//    val indent = extracted[0]
//    val rest = extracted[1].trim()
//    when {
//        rest.isBlank() -> {
//            throw ParsingException("Expected text after keyword [$keyword], but it was blank!")
//        }
//        else -> {
//            return indent to rest
//        }
//    }
//}
//
//@Throws(ParsingException::class)
//fun readAfterKeyword(keyword: String, text: String): String {
//    ensureKeyword(keyword, text)
//    return extractAfterKeyword(keyword, text)
//}
//
//fun isListItem(text: String): Boolean {
//    return containsKeyword(PREFIX_LIST_ITEM, text)
//}
//
//fun extractListItem(text: String): String {
//    return extractAfterKeyword(PREFIX_LIST_ITEM, text).trim()
//}
//
//@Throws(ParsingException::class)
//fun extractWithinQuotes(text: String): String {
//    val match = QUOTES_REGEX.find(text)
//    return match?.groups?.get(1)?.value
//        ?: throw ParsingException("Did not find text within quotes: [$text]")
//}
//
//@Throws(ParsingException::class)
//fun readList(lines: List<String>, start: Int): List<String> {
//    val list = mutableListOf<String>()
//
//    var i = start
//    while (i < lines.size) {
//        val line = lines[i]
//        if (isListItem(line)) {
//            val item = extractListItem(line)
//            if (item.isBlank()) {
//                throw ParsingException("Empty list item!")
//            }
//            list.add(item)
//        } else {
//            // end of list
//            if (list.isEmpty()) {
//                throw ParsingException("List with no items!")
//            }
//            break
//        }
//        i++
//    }
//    return list
//}
//
//fun readInlineListAfterKeyword(keyword: String, text: String): List<String> {
//    val list = readAfterKeyword(keyword, text)
//    return list.split(SEP_INLINE_LIST).map { item -> item.trim() }
//}
//
//class ParsingException(message: String) : Exception(message) {}
