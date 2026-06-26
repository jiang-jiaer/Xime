package com.kingzcheung.xime.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.rime.T9InputController
import com.kingzcheung.xime.rime.T9PinyinMap
import com.kingzcheung.xime.util.CharInfo
import com.kingzcheung.xime.util.PermissionHelper
import com.kingzcheung.xime.util.SubcharHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NineKeyKeyboardLayout(
    onReplaceFullPinyin: (String) -> Unit,
    onKeyPress: (String) -> Unit,
    keyBackgroundColor: Color,
    keyTextColor: Color,
    specialKeyBackgroundColor: Color,
    keyboardBackgroundColor: Color = Color.Transparent,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier,
    onKeyPressDown: ((String) -> Unit)? = null,
    schemaName: String = "",
    enterKeyText: String = "发送",
    isSttEnabled: Boolean = true,
    onVoiceModeChange: ((Boolean) -> Unit)? = null,
    onCursorMove: ((Int) -> Unit)? = null,
    /** T9 输入控制器实例。由 KeyboardView 层级持有，确保路由切换时状态不丢失。
     *  NineKeyKeyboardLayout 重建时复用同一实例，避免 LaunchedEffect 误触 reset()。 */
    t9Controller: T9InputController
) {
    var swipeState by remember { mutableStateOf(SwipeState()) }
    var keyboardBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }
    var lastKeyBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

    fun processSwipeState(state: SwipeState, bounds: Rect) {
        val newState = if (state.isSwipeDown && state.swipeText != null) {
            state.copy(charInfos = SubcharHelper.parseSwipeDownText(state.swipeText))
        } else {
            state
        }
        swipeState = newState
        lastKeyBounds = Rect(
            left = bounds.left - keyboardBounds.left,
            top = bounds.top - keyboardBounds.top,
            right = bounds.right - keyboardBounds.left,
            bottom = bounds.bottom - keyboardBounds.top
        )
    }

    /** 退格键处理：T9 输入非空时由控制器处理，否则发送原生 delete 事件 */
    fun handleDelete() {
        when (val result = t9Controller.onDeleted()) {
            T9InputController.DeleteResult.UNDO_COMMIT -> {
                // 撤销右侧候选选词：清除 RIME composition 后重新发送完整数字序列。
                // clearRimeAndResend 内部通过 onRightCommitUndone 同步删除已提交文本（如"策"）
                // 并从 partial commit 列表中移除该候选，避免异步 KEYCODE_DEL 干扰新 composition。
                t9Controller.clearRimeAndResend()
            }
            T9InputController.DeleteResult.NOT_CONSUMED -> {
                // 无内容可删 → 发给软键盘框架处理
                onKeyPress("delete")
            }
            else -> {
                // UNDO_CHOICE 和 DELETED 已由控制器内部处理
            }
        }
    }

    Box(
        modifier = modifier
            .background(keyboardBackgroundColor)
            .padding(horizontal = 4.dp)
            .onGloballyPositioned { coordinates ->
                keyboardBounds = coordinates.boundsInRoot()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(keyboardBackgroundColor)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val showCandidates = t9Controller.firstOptions.isNotEmpty()
                    val displayItems: List<String> = if (showCandidates) {
                        t9Controller.firstOptions.map { it.pinyin }
                    } else {
                        listOf("，", "。", "？", "！")
                    }
                    if (showCandidates && displayItems.size > 4) {
                        // 候选项 > 4：可滚动列表
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .padding(end = 3.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            itemsIndexed(displayItems) { index, item ->
                                val option = t9Controller.firstOptions[index]
                                PinyinChoiceKey(
                                    text = option.pinyin,
                                    onClick = { t9Controller.onChoiceSelected(option) },
                                    backgroundColor = keyBackgroundColor,
                                    textColor = keyTextColor,
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    onPress = { onKeyPressDown?.invoke(option.pinyin) },
                                    isFirst = index == 0,
                                    isLast = index == displayItems.lastIndex
                                )
                            }
                        }
                    } else {
                        // 候选项 ≤ 4 或标点模式：等分填充高度，不留空隙
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .padding(end = 3.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            displayItems.forEachIndexed { index, item ->
                                if (showCandidates) {
                                    val option = t9Controller.firstOptions[index]
                                    PinyinChoiceKey(
                                        text = option.pinyin,
                                        onClick = { t9Controller.onChoiceSelected(option) },
                                        backgroundColor = keyBackgroundColor,
                                        textColor = keyTextColor,
                                        modifier = Modifier.weight(1f),
                                        onPress = { onKeyPressDown?.invoke(option.pinyin) },
                                        isFirst = index == 0,
                                        isLast = index == displayItems.lastIndex
                                    )
                                } else {
                                    PunctuationKey2(
                                        text = item,
                                        onClick = { onKeyPress(item) },
                                        backgroundColor = keyBackgroundColor,
                                        textColor = keyTextColor,
                                        modifier = Modifier.weight(1f),
                                        onPress = { onKeyPressDown?.invoke(item) },
                                        isFirst = index == 0,
                                        isLast = index == displayItems.lastIndex
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(4f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NineKeyButton2(
                                digit = "1", letters = "分词",
                                onClick = { t9Controller.onDigitPressed("1") },
                                backgroundColor = keyBackgroundColor, textColor = keyTextColor,
                                modifier = Modifier.weight(1f),
                                onPress = { onKeyPressDown?.invoke("1") }
                            )
                            NineKeyButton2(digit = "2", letters = "ABC", onClick = { t9Controller.onDigitPressed("2") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("2") })
                            NineKeyButton2(digit = "3", letters = "DEF", onClick = { t9Controller.onDigitPressed("3") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("3") })
                            SwipeableIconKeyButton(
                                icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Backspace),
                                onClick = { handleDelete() },
                                backgroundColor = specialKeyBackgroundColor, iconColor = keyTextColor,
                                modifier = Modifier.weight(1f),
                                swipeText = "清空",
                                onSwipe = { onKeyPress("clear_composition") },
                                onLongClick = { handleDelete() },
                                onPress = { onKeyPressDown?.invoke("delete") },
                                swipeUpLabel = "上滑清空",
                                swipeDownLabel = "下滑撤回",
                                onSwipeUp = { onKeyPress("clear_all") },
                                onSwipeDown = { onKeyPress("undo_clear") },
                                onSwipeLeft = { onKeyPress("clear_composition") },
                                onSwipeStateChange = { state, bounds -> processSwipeState(state, bounds) }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NineKeyButton2(digit = "4", letters = "GHI", onClick = { t9Controller.onDigitPressed("4") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("4") })
                            NineKeyButton2(digit = "5", letters = "JKL", onClick = { t9Controller.onDigitPressed("5") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("5") })
                            NineKeyButton2(digit = "6", letters = "MNO", onClick = { t9Controller.onDigitPressed("6") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("6") })
                            // 重输键：主题色背景，Refresh图标居中，"重输"文本在图标之上
                            val resetModifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .shadow(1.dp, RoundedCornerShape(8.dp), ambientColor = Color(0x80000000), spotColor = Color(0x80000000))
                                .clip(RoundedCornerShape(8.dp))
                            Box(
                                modifier = resetModifier
                                    .background(specialKeyBackgroundColor)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                onKeyPressDown?.invoke("clear")
                                            },
                                            onTap = { t9Controller.clearAll() }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "重输",
                                        color = keyTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "重输",
                                        tint = keyTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NineKeyButton2(digit = "7", letters = "PQRS", onClick = { t9Controller.onDigitPressed("7") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("7") })
                            NineKeyButton2(digit = "8", letters = "TUV", onClick = { t9Controller.onDigitPressed("8") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("8") })
                            NineKeyButton2(digit = "9", letters = "WXYZ", onClick = { t9Controller.onDigitPressed("9") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("9") })
                            NineKeyButton2(digit = "", letters = "0", onClick = { onKeyPress("0") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("0") })
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KeyButton(text = "符", onClick = { onKeyPress("symbol") }, backgroundColor = specialKeyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1.2f), onPress = { onKeyPressDown?.invoke("symbol") })
                    KeyButton(text = "123", onClick = { onKeyPress("mode_change") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(0.8f), onPress = { onKeyPressDown?.invoke("mode_change") })

                    // 空格键 - 支持左右滑动控制光标、长按语音
                    val currentOnKeyPress by rememberUpdatedState(onKeyPress)
                    val currentOnKeyPressDown by rememberUpdatedState(onKeyPressDown)
                    val currentOnVoiceModeChange by rememberUpdatedState(onVoiceModeChange)
                    val currentOnCursorMove by rememberUpdatedState(onCursorMove)
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .pointerInput(isSttEnabled) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    currentOnKeyPressDown?.invoke("space")

                                    var longPressTriggered = false
                                    val longPressJob = scope.launch {
                                        delay(400)
                                        longPressTriggered = true
                                        if (isSttEnabled) {
                                            if (!PermissionHelper.hasRecordAudioPermission(context)) {
                                                Toast.makeText(context, "需要麦克风权限才能使用语音输入", Toast.LENGTH_SHORT).show()
                                                PermissionHelper.requestRecordAudioPermission(context)
                                            } else {
                                                currentOnVoiceModeChange?.invoke(true)
                                            }
                                        } else {
                                            while (true) {
                                                currentOnKeyPress("space")
                                                delay(80)
                                            }
                                        }
                                    }

                                    var isHorizontalSwipe = false
                                    val cursorThreshold = 60f

                                    drag(down.id) { change ->
                                        val dx = change.position.x - down.position.x
                                        val dy = change.position.y - down.position.y
                                        if (kotlin.math.abs(dx) > cursorThreshold) {
                                            if (!isHorizontalSwipe) {
                                                isHorizontalSwipe = true
                                                longPressJob.cancel()
                                            }
                                            if (kotlin.math.abs(dx) > kotlin.math.abs(dy) * 2f) {
                                                val steps = (dx / cursorThreshold).toInt()
                                                if (steps != 0) {
                                                    currentOnCursorMove?.invoke(if (steps > 0) 1 else -1)
                                                }
                                            }
                                        }
                                    }

                                    longPressJob.cancel()
                                    if (!longPressTriggered && !isHorizontalSwipe) {
                                        currentOnKeyPress("space")
                                    }
                                }
                            }
                            .padding(horizontal = 2.dp, vertical = 3.dp)
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .shadow(1.dp, RoundedCornerShape(8.dp), ambientColor = Color(0x80000000), spotColor = Color(0x80000000))
                            .clip(RoundedCornerShape(8.dp))
                            .background(keyBackgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = schemaName,
                            color = keyTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        if (isSttEnabled) {
                            Icon(
                                painter = painterResource(com.kingzcheung.xime.R.drawable.voice),
                                contentDescription = "语音输入",
                                tint = keyTextColor.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.BottomStart)
                                    .padding(start = 6.dp, bottom = 2.dp)
                            )
                        } else {
                            Text(
                                text = "空格",
                                color = keyTextColor.copy(alpha = 0.3f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 6.dp, bottom = 2.dp)
                            )
                        }
                    }

                    KeyButton(text = "中", onClick = { onKeyPress("abc") }, backgroundColor = specialKeyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(0.8f), onPress = { onKeyPressDown?.invoke("ime_switch") })
                    KeyButton(text = enterKeyText, onClick = { onKeyPress("enter") }, backgroundColor = specialKeyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1.2f), onPress = { onKeyPressDown?.invoke("enter") })
                }
            }
        }

        SwipeBubble(
            swipeState = swipeState,
            keyBounds = lastKeyBounds,
            isDarkTheme = isDarkTheme,
            keyWidth = if (swipeState.isSwiping || swipeState.isPressed) lastKeyBounds.width else 0f,
            keyboardWidth = keyboardBounds.width
        )
    }
}

// ── 子组件 ──

@Composable
private fun PinyinChoiceKey(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onPress: (() -> Unit)? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPress by rememberUpdatedState(onPress)
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 8.dp else 0.dp,
        topEnd = if (isFirst) 8.dp else 0.dp,
        bottomStart = if (isLast) 8.dp else 0.dp,
        bottomEnd = if (isLast) 8.dp else 0.dp
    )
    Box(modifier = modifier.fillMaxWidth().clip(shape).background(if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor).pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                isPressed = true
                currentOnPress?.invoke()
                tryAwaitRelease()
                isPressed = false
            },
            onTap = { currentOnClick() }
        )
    }, contentAlignment = Alignment.Center) {
        Text(text = text, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PunctuationKey2(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onPress: (() -> Unit)? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPress by rememberUpdatedState(onPress)
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 8.dp else 0.dp,
        topEnd = if (isFirst) 8.dp else 0.dp,
        bottomStart = if (isLast) 8.dp else 0.dp,
        bottomEnd = if (isLast) 8.dp else 0.dp
    )
    Box(modifier = modifier.fillMaxWidth().clip(shape).background(if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor).pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                isPressed = true
                currentOnPress?.invoke()
                tryAwaitRelease()
                isPressed = false
            },
            onTap = { currentOnClick() }
        )
    }, contentAlignment = Alignment.Center) {
        Text(text = text, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NineKeyButton2(
    digit: String,
    letters: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onPress: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPress by rememberUpdatedState(onPress)
    Box(modifier = modifier.fillMaxHeight().shadow(1.dp, RoundedCornerShape(8.dp), ambientColor = Color(0x80000000), spotColor = Color(0x80000000)).clip(RoundedCornerShape(8.dp)).background(if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor).pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                isPressed = true
                currentOnPress?.invoke()
                tryAwaitRelease()
                isPressed = false
            },
            onTap = { currentOnClick() }
        )
    }) {
        if (digit.isNotEmpty()) Text(text = digit, color = textColor.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.End, modifier = Modifier.align(Alignment.TopEnd).padding(top = 0.dp, end = 6.dp))
        if (letters.isNotEmpty()) Text(text = letters, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.align(Alignment.Center))
    }
}
