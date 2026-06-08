package com.kingzcheung.xime.settings

import com.charleskorn.kaml.Yaml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardGestureConfigTest {

    @Test
    fun `gestureDef 字符串简写解析为 commit 动作`() {
        val keys = parseKeys("""
            q: { tap: "q" }
        """.trimIndent())
        val kc = keys["q"]!!
        assertEquals("q", kc.tap!!.label)
        assertEquals("commit", kc.tap!!.action)
        assertEquals("q", kc.tap!!.value)
    }

    @Test
    fun `gestureDef 字符串简写用于 swipe_up 和 swipe_down`() {
        val keys = parseKeys("""
            a: { tap: "a", swipe_up: "!", swipe_down: "A" }
        """.trimIndent())
        val kc = keys["a"]!!
        assertEquals("!", kc.swipeUp!!.label)
        assertEquals("commit", kc.swipeUp!!.action)
        assertEquals("!", kc.swipeUp!!.value)
        assertEquals("A", kc.swipeDown!!.label)
    }

    @Test
    fun `gestureDef 对象格式指定 action 为 copy`() {
        val keys = parseKeys("""
            c:
              swipe_up: { label: "复制", action: "copy" }
        """.trimIndent())
        val su = keys["c"]!!.swipeUp!!
        assertEquals("复制", su.label)
        assertEquals("copy", su.action)
    }

    @Test
    fun `gestureDef 对象格式指定 value 与 label 不同`() {
        val keys = parseKeys("""
            x:
              swipe_up: { label: "剪切", action: "commit", value: "x_cut" }
        """.trimIndent())
        val su = keys["x"]!!.swipeUp!!
        assertEquals("剪切", su.label)
        assertEquals("x_cut", su.value)
    }

    @Test
    fun `long_press 支持多值数组`() {
        val keys = parseKeys("""
            a:
              long_press:
                - { label: "大写", action: "commit", value: "A" }
                - { label: "Ä",    action: "commit", value: "ä" }
        """.trimIndent())
        val lp = keys["a"]!!.longPress!!
        assertEquals(2, lp.size)
        assertEquals("大写", lp[0].label)
        assertEquals("A", lp[0].value)
        assertEquals("Ä", lp[1].label)
    }

    @Test
    fun `long_press 单值数组`() {
        val keys = parseKeys("""
            backspace:
              long_press:
                - { label: "清空", action: "command", value: "clear_composition" }
        """.trimIndent())
        val lp = keys["backspace"]!!.longPress!!
        assertEquals(1, lp.size)
        assertEquals("command", lp[0].action)
    }

    @Test
    fun `action 为 null 表示无动作`() {
        val keys = parseKeys("""
            space:
              swipe_down: { label: "", action: null }
        """.trimIndent())
        assertNull(keys["space"]!!.swipeDown!!.action)
    }

    @Test
    fun `完整多键配置解析`() {
        val keys = parseKeys("""
            q: { tap: "q", swipe_up: "1", swipe_down: "Q" }
            a: { tap: "a", swipe_up: "!", swipe_down: "A" }
            z: { tap: "z", swipe_up: "|", swipe_down: "Z" }
            m: { tap: "m", swipe_up: "+", swipe_down: "M" }
        """.trimIndent())
        assertEquals(4, keys.size)
        assertEquals("1", keys["q"]!!.swipeUp!!.label)
        assertEquals("|", keys["z"]!!.swipeUp!!.label)
        assertEquals("+", keys["m"]!!.swipeUp!!.label)
    }

    @Test
    fun `空的 keys 不报错`() {
        assertEquals(0, parseKeys("{}").size)
    }

    @Test
    fun `部分手势缺失不报错`() {
        val a = parseKeys("""a: { tap: "a" }""".trimIndent())["a"]!!
        assertEquals("a", a.tap!!.label)
        assertNull(a.swipeUp)
        assertNull(a.swipeDown)
        assertNull(a.longPress)
    }

    // ── 辅助 ──

    private fun parseKeys(yamlFragment: String): Map<String, KeyGestureConfig> {
        val fullYaml = "keyboard:\n  keys:\n    " + yamlFragment.replace("\n", "\n    ")
        val root = Yaml.default.parseToYamlNode(fullYaml) as com.charleskorn.kaml.YamlMap
        val keyboardNode = root["keyboard"] as? com.charleskorn.kaml.YamlMap ?: return emptyMap()
        val keysNode = keyboardNode["keys"] as? com.charleskorn.kaml.YamlMap ?: return emptyMap()
        val result = mutableMapOf<String, KeyGestureConfig>()
        for ((kNode, vNode) in keysNode.entries) {
            val key = (kNode as com.charleskorn.kaml.YamlScalar).content
            val gestureMap = vNode as com.charleskorn.kaml.YamlMap
            result[key] = parseKeyGestureConfig(gestureMap)
        }
        return result
    }

    private fun parseKeyGestureConfig(map: com.charleskorn.kaml.YamlMap): KeyGestureConfig {
        var tap: GestureDef? = null
        var swipeUp: GestureDef? = null
        var swipeDown: GestureDef? = null
        var longPress: List<GestureDef>? = null
        for ((kNode, vNode) in map.entries) {
            val name = (kNode as com.charleskorn.kaml.YamlScalar).content
            when (name) {
                "tap" -> tap = parseGestureNode(vNode)
                "swipe_up" -> swipeUp = parseGestureNode(vNode)
                "swipe_down" -> swipeDown = parseGestureNode(vNode)
                "long_press" -> longPress = parseGestureList(vNode)
            }
        }
        return KeyGestureConfig(tap, swipeUp, swipeDown, longPress)
    }

    private fun parseGestureNode(node: com.charleskorn.kaml.YamlNode): GestureDef {
        if (node is com.charleskorn.kaml.YamlScalar) {
            val text = node.content
            return GestureDef(label = text, action = "commit", value = text)
        }
        if (node is com.charleskorn.kaml.YamlMap) {
            var label = ""
            var action: String? = "commit"
            var value = ""
            for ((k, v) in node.entries) {
                val key = (k as com.charleskorn.kaml.YamlScalar).content
                val vStr = (v as com.charleskorn.kaml.YamlScalar).content
                when (key) {
                    "label" -> label = vStr
                    "action" -> action = if (vStr == "null") null else vStr
                    "value" -> value = vStr
                }
            }
            return GestureDef(label = label, action = action, value = value)
        }
        return GestureDef()
    }

    private fun parseGestureList(node: com.charleskorn.kaml.YamlNode): List<GestureDef>? {
        val list = node as? com.charleskorn.kaml.YamlList ?: return null
        return list.items.map { parseGestureNode(it) }
    }
}
