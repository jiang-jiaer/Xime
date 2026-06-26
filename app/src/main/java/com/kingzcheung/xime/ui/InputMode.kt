package com.kingzcheung.xime.ui

import com.kingzcheung.xime.keyboard.KeyboardMode

/**
 * 输入模式——将键盘布局与 Rime 方案一对一绑定。
 *
 * 每个 [InputMode] 包含：
 * - [schemaId]：Rime 方案 ID，传给 rimeEngine.switchSchema()
 * - [keyboardMode]：对应的键盘布局
 * - [displayName]：显示名称
 *
 * 代替散落在各处的 `if (schemaId == "t9_pinyin")` 硬编码判断，
 * 保证方案和键盘布局始终一致。
 */
enum class InputMode(
    val schemaId: String,
    val keyboardMode: KeyboardMode,
    val displayName: String
) {
    WUBI86("wubi86", KeyboardMode.FULL, "五笔86"),
    WUBI98("wubi98", KeyboardMode.FULL, "五笔98"),
    PINYIN("pinyin", KeyboardMode.FULL, "拼音"),
    T9_PINYIN("t9_pinyin", KeyboardMode.NINEKEY, "拼音九键"),
    DOUBLE_PINYIN("double_pinyin", KeyboardMode.FULL, "双拼"),
    STROKE("stroke", KeyboardMode.FULL, "五笔划"),
    ;

    companion object {
        /** 已知的九键方案精确匹配列表 */
        private val KNOWN_T9_SCHEMA_IDS = setOf(
            "t9_pinyin",  // 内置方案
            "t9",         // 第三方方案
            "wanxiang_t9" // 第三方方案
        )

        /** 判断是否为九键（T9）方案
         * @param schemaId Rime 方案 ID
         * @param name 方案名称（可选，用于关键词匹配）
         * @return true 表示九键布局
         */
        fun isT9Schema(schemaId: String, name: String = ""): Boolean {
            // 1. 精确匹配已知方案
            if (schemaId in KNOWN_T9_SCHEMA_IDS) return true

            // 2. 关键词匹配：schemaId 或 name 包含 "t9"
            val lowerSchemaId = schemaId.lowercase()
            val lowerName = name.lowercase()
            return lowerSchemaId.contains("t9") || lowerName.contains("t9")
        }

        /** 根据 schemaId 查找对应的输入模式，未知 schema 默认返回全键盘 */
        fun fromSchemaId(schemaId: String): InputMode {
            return entries.find { it.schemaId == schemaId } ?: WUBI86
        }

        /** 根据 schemaId 返回对应的键盘布局 */
        fun keyboardModeFor(schemaId: String): KeyboardMode {
            return fromSchemaId(schemaId).keyboardMode
        }
    }
}
