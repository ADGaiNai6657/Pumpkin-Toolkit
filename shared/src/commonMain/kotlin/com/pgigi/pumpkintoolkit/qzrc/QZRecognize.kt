package com.pgigi.pumpkintoolkit.qzrc

/**
 * QZ 验证码识别 —— Kotlin Multiplatform 移植版。
 *
 * 原始项目: https://github.com/shuo747/QZRecognize
 * 原始逻辑: BufferedImage -> 裁剪 -> 分割4块 -> 二值化 -> 转01数组 -> 与训练模板逐一比较
 *
 * 本文件暴露两个函数:
 * 1. [preprocessImage] —— expect/actual，输入图片二进制流，灰度+二值化后返回 01 数组
 * 2. [recognizeCaptcha] —— RC 函数，参数从 BufferedImage 改为 01 数组，其余逻辑与原函数一致
 */

// ──────────────────────────────────────────────────────────────────────────
// 函数一：expect 声明
// ──────────────────────────────────────────────────────────────────────────

/**
 * 将图片二进制流灰度化、二值化后转换为 01 图片数组。
 *
 * 各平台通过 expect/actual 实现图片解码逻辑:
 * - Android: 使用 BitmapFactory 解码
 * - iOS:     使用 UIImage + CoreGraphics 解码
 *
 * 二值化阈值与原始 binaryImage 一致: avg = (R + G + B) / 3, avg < 192 → 1(黑), 否则 → 0(白)
 *
 * @param imageBytes 图片二进制流 (JPEG / PNG 等)
 * @return 二维 01 数组，`result[row][col]` (即 `result[y][x]`)，1 = 前景(黑)，0 = 背景(白)
 */
expect fun preprocessImage(imageBytes: ByteArray): Array<IntArray>

// ──────────────────────────────────────────────────────────────────────────
// 函数二：RC 识别函数（公共逻辑，不依赖平台 API）
// ──────────────────────────────────────────────────────────────────────────

/**
 * RC 识别函数 —— 参数从原始的 BufferedImage 改为 01 数组，其余逻辑与原函数保持一致。
 *
 * 流程 (与原始 QZRC.RC 对应):
 * 1. 裁剪:   getSubimage(0, 9, 80, 24) → 取第 9 行起的 24 行
 * 2. 分割4块: getSubimage(4,0,20,h) / (22,0,20,h) / (40,0,20,h) / (58,0,20,h)
 * 3. 比较:   每块与 [CharacterTemplates] 中 34 个模板逐一比较, 取命中率最高者
 *
 * @param imageArray 由 [preprocessImage] 返回的 01 图片数组 (整张图的 `height` `width`)
 * @return 识别出的 4 位验证码字符串
 */
fun recognizeCaptcha(imageArray: Array<IntArray>): String {
    // ===== 1. 裁剪: 对应 image.getSubimage(0, 9, 80, 24) =====
    val cropY = 9
    val cropHeight = 24
    val cropped = Array(cropHeight) { row ->
        imageArray[cropY + row]
    }

    // ===== 2. 分割4块: 每块 20 列宽 =====
    // 对应: getSubimage(4,0,20,h), getSubimage(22,0,20,h), getSubimage(40,0,20,h), getSubimage(58,0,20,h)
    val subWidth = 20
    val startXs = intArrayOf(4, 22, 40, 58)
    val subArrays = Array(4) { idx ->
        val startX = startXs[idx]
        Array(cropHeight) { row ->
            cropped[row].copyOfRange(startX, startX + subWidth)
        }
    }

    // ===== 3. 与训练模板逐一比较 (与原始 RC 逻辑完全一致) =====
    val sb = StringBuilder()
    var count = -1
    while (++count < 4) {
        var chCount = 0
        var target = 0      // 最接近的模板索引
        var temp = -1.0     // 当前最高命中率

        while (chCount < CharacterTemplates.templates.size) {
            var total = 1.0  // 从 1 开始, 避免除零 (与原始一致)
            var hit = 0

            val input = subArrays[count]
            val template = CharacterTemplates.templates[chCount]

            for (i in input.indices) {
                for (j in input[i].indices) {
                    if (template[i][j] == 1) {
                        total++
                        if (input[i][j] == template[i][j]) {
                            hit++
                        }
                    }
                }
            }

            val ratio = hit / total
            if (ratio > temp) {
                target = chCount
                temp = ratio
            }
            chCount++
        }

        // 比例最大的给他
        sb.append(CharacterTemplates.labels[target])
    }

    return sb.toString()
}
