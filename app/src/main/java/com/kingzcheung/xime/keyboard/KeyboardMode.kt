package com.kingzcheung.xime.keyboard

/**
 * 键盘模弝
 *
 * @deprecated 已由 [KeyboardLayoutState] 坖代�?
 * [KeyboardLayoutState] 将全键盘进一步拆分为 Chinese / English / Split�?
 * 消除原本�?[KeyboardView] 中依�?[isAsciiMode] + 横屝检测的夝杂分支�?
 * 详觝 [KeyboardLayoutState.transition]�?
 */
@Deprecated("Use KeyboardLayoutState instead")
enum class KeyboardMode {
    FULL,       // ???????
    NINEKEY,    // ??????
    NUMBER,     // ???????
    SYMBOL      // ????
}