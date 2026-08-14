package com.pgigi.pumpkintoolkit.utils

/**
 * 拼音工具类
 *
 * 基于 commonMain/composeResources/files/pinyindb/ 下的词库：
 * - unicode_to_hanyu_pinyin.txt: 单字 Unicode→拼音映射（含多音字）
 * - multi_pinyin.txt: 多字词组拼音映射（优先匹配，解决多音字歧义）
 *
 * 支持四种输出格式：
 * - WITH_TONE_NUMBER: zhong1, guo2（音调以数字表示，ü 用 v 代替）
 * - WITHOUT_TONE:     zhong, guo（纯全拼，无声调）
 * - WITH_TONE_MARK:   zhōng, guó（带音调符号）
 * - FIRST_LETTER:     z, g（首字母）
 *
 * 使用示例：
 * ```
 * PinyinUtils.toPinyin('你')                    // "ni"
 * PinyinUtils.toPinyin("重庆")                   // "chong qing"
 * PinyinUtils.toPinyin("中国", Format.WITH_TONE_NUMBER)  // "zhong1 guo2"
 * PinyinUtils.toPinyin("中国", Format.WITH_TONE_MARK)    // "zhōng guó"
 * PinyinUtils.toInitials("中国")                 // "zg"
 * ```
 */
object PinyinUtils {

    enum class Format {
        /** 带音调数字：zhong1 */
        WITH_TONE_NUMBER,

        /** 不带音调：zhong */
        WITHOUT_TONE,

        /** 带音调符号：zhōng */
        WITH_TONE_MARK,

        /** 首字母：z */
        FIRST_LETTER
    }

    private const val NONE = "none0"
    private const val MAX_WORD_LEN = 9

    private val charToPinyin: Map<Int, List<String>> by lazy { loadCharDict() }
    private val wordToPinyin: Map<String, List<String>> by lazy { loadWordDict() }

    // ==================== 公开 API ====================

    /**
     * 将单个汉字转为拼音（取第一个读音）
     * @param c 汉字字符
     * @param format 输出格式
     * @return 拼音字符串；无拼音数据时返回原字符
     */
    fun toPinyin(c: Char, format: Format = Format.WITHOUT_TONE): String {
        val pinyin = charToPinyin[c.code]
        if (pinyin == null || pinyin.isEmpty() || pinyin[0] == NONE) {
            return c.toString()
        }
        return formatPinyin(pinyin[0], format)
    }

    /**
     * 获取汉字的所有读音
     * @param c 汉字字符
     * @param format 输出格式
     * @return 读音列表；无拼音数据时返回空列表
     */
    fun toPinyinAll(c: Char, format: Format = Format.WITHOUT_TONE): List<String> {
        val pinyins = charToPinyin[c.code] ?: return emptyList()
        return pinyins.filter { it != NONE }.map { formatPinyin(it, format) }
    }

    /**
     * 将字符串转为拼音
     *
     * 优先使用词组词库进行最长匹配（解决多音字），非汉字字符保持原样。
     *
     * @param text 输入文本
     * @param format 输出格式
     * @param separator 拼音音节之间的分隔符，默认为空格
     * @return 拼音字符串
     */
    fun toPinyin(
        text: String,
        format: Format = Format.WITHOUT_TONE,
        separator: String = " "
    ): String {
        if (text.isEmpty()) return ""
        return toPinyinList(text, format).joinToString(separator)
    }

    /**
     * 将字符串转为拼音列表（每个元素对应一个汉字的拼音或一段非汉字文本）
     *
     * @param text 输入文本
     * @param format 输出格式
     * @return 拼音片段列表
     */
    fun toPinyinList(text: String, format: Format = Format.WITHOUT_TONE): List<String> {
        if (text.isEmpty()) return emptyList()
        val segments = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            // 优先尝试词组匹配（最长优先）
            val matched = tryWordMatch(text, i, format)
            if (matched != null) {
                segments.addAll(matched.pinyins)
                i += matched.length
            } else {
                val c = text[i]
                if (isChinese(c)) {
                    segments.add(toPinyin(c, format))
                    i++
                } else {
                    // 累积连续非汉字字符为单个片段
                    val sb = StringBuilder()
                    sb.append(c)
                    i++
                    while (i < text.length && !isChinese(text[i])) {
                        sb.append(text[i])
                        i++
                    }
                    segments.add(sb.toString())
                }
            }
        }
        return segments
    }

    /**
     * 获取拼音首字母
     * @param text 输入文本
     * @param separator 首字母之间的分隔符，默认无分隔
     * @return 首字母字符串
     */
    fun toInitials(text: String, separator: String = ""): String {
        return toPinyin(text, Format.FIRST_LETTER, separator)
    }

    /**
     * 判断字符是否有拼音数据
     */
    fun hasPinyin(c: Char): Boolean {
        val pinyins = charToPinyin[c.code] ?: return false
        return pinyins.isNotEmpty() && pinyins[0] != NONE
    }

    // ==================== 词组匹配 ====================

    private data class WordMatch(val pinyins: List<String>, val length: Int)

    private fun tryWordMatch(text: String, start: Int, format: Format): WordMatch? {
        val maxLen = minOf(MAX_WORD_LEN, text.length - start)
        for (len in maxLen downTo 2) {
            val word = text.substring(start, start + len)
            val pinyins = wordToPinyin[word] ?: continue
            val formatted = pinyins.map { formatPinyin(it, format) }
            return WordMatch(formatted, len)
        }
        return null
    }

    // ==================== 格式转换 ====================

    private fun formatPinyin(pinyin: String, format: Format): String {
        if (pinyin == NONE) return ""
        return when (format) {
            Format.WITH_TONE_NUMBER -> pinyin.replace("u:", "v")
            Format.WITHOUT_TONE -> stripTone(pinyin).replace("u:", "v")
            Format.WITH_TONE_MARK -> toToneMark(pinyin)
            Format.FIRST_LETTER -> {
                val letter = pinyin.firstOrNull { it.isLetter() }
                letter?.toString() ?: ""
            }
        }
    }

    private fun stripTone(pinyin: String): String {
        return pinyin.filter { !it.isDigit() }
    }

    /**
     * 将数字音调转为音调符号，如 "zhong1" → "zhōng", "lu:3" → "lǚ"
     */
    private fun toToneMark(pinyin: String): String {
        val tone = pinyin.lastOrNull { it.isDigit() }?.digitToInt() ?: 0
        val base = pinyin.filter { !it.isDigit() }.replace("u:", "ü")
        if (tone == 0 || tone == 5) return base
        return applyToneMark(base, tone)
    }

    private val toneMarks = mapOf(
        'a' to charArrayOf('ā', 'á', 'ǎ', 'à'),
        'e' to charArrayOf('ē', 'é', 'ě', 'è'),
        'i' to charArrayOf('ī', 'í', 'ǐ', 'ì'),
        'o' to charArrayOf('ō', 'ó', 'ǒ', 'ò'),
        'u' to charArrayOf('ū', 'ú', 'ǔ', 'ù'),
        'ü' to charArrayOf('ǖ', 'ǘ', 'ǚ', 'ǜ')
    )

    private val vowels = charArrayOf('a', 'e', 'i', 'o', 'u', 'ü')

    /**
     * 按拼音注音规则在正确的元音上标注音调符号：
     * 1. 有 a 则标在 a 上
     * 2. 有 e 则标在 e 上
     * 3. 有 ou 则标在 o 上
     * 4. 否则标在最后一个元音上
     */
    private fun applyToneMark(base: String, tone: Int): String {
        val chars = base.toCharArray()
        var target = -1

        fun findChar(ch: Char) {
            if (target == -1) {
                target = chars.indexOfFirst { it == ch }
            }
        }

        findChar('a')
        findChar('e')
        // ou 的情况标在 o 上
        if (target == -1) {
            for (i in chars.indices) {
                if (chars[i] == 'o' && i + 1 < chars.size && chars[i + 1] == 'u') {
                    target = i
                    break
                }
            }
        }
        // 否则标在最后一个元音上
        if (target == -1) {
            for (i in chars.indices.reversed()) {
                if (chars[i] in vowels) {
                    target = i
                    break
                }
            }
        }

        if (target >= 0) {
            val marks = toneMarks[chars[target]]
            if (marks != null && tone in 1..4) {
                chars[target] = marks[tone - 1]
            }
        }
        return chars.concatToString()
    }

    // ==================== 汉字判断 ====================

    private fun isChinese(c: Char): Boolean {
        val code = c.code
        return code == 0x3007 || code in 0x4E00..0x9FFF
    }

    // ==================== 词库加载 ====================

    private fun loadCharDict(): Map<Int, List<String>> {
        val text = ResourceUtils.readText("pinyindb/unicode_to_hanyu_pinyin.txt") ?: return emptyMap()
        val map = HashMap<Int, List<String>>(21000)
        for (line in text.lineSequence()) {
            if (line.isBlank()) continue
            val spaceIdx = line.indexOf(' ')
            if (spaceIdx < 0) continue
            val hex = line.substring(0, spaceIdx)
            val inner = line.substring(spaceIdx + 1).removeSurrounding("(", ")")
            val code = hex.toIntOrNull(16) ?: continue
            val pinyins = inner.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (pinyins.isNotEmpty()) map[code] = pinyins
        }
        return map
    }

    private fun loadWordDict(): Map<String, List<String>> {
        val text = ResourceUtils.readText("pinyindb/multi_pinyin.txt") ?: return emptyMap()
        val map = HashMap<String, List<String>>(10000)
        for (line in text.lineSequence()) {
            if (line.isBlank()) continue
            val spaceIdx = line.indexOf(' ')
            if (spaceIdx < 0) continue
            val word = line.substring(0, spaceIdx)
            val inner = line.substring(spaceIdx + 1).removeSurrounding("(", ")")
            val pinyins = inner.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (word.isNotEmpty() && pinyins.isNotEmpty()) map[word] = pinyins
        }
        return map
    }
}
