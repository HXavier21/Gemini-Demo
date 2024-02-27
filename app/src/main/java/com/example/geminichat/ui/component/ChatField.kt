package com.example.geminichat.ui.component

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.geminichat.R
import com.example.geminichat.ui.data.Conversation
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout

private const val TAG = "ChatField"

@Preview
@Composable
fun ChatField(
    modifier: Modifier = Modifier,
    mutableConversationFlowList: MutableList<MutableStateFlow<Conversation>> = mutableListOf()
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        for (conversation in mutableConversationFlowList) {
            item {
                ChatItem(
                    name = conversation.collectAsState().value.role,
                    message = conversation.collectAsState().value.message
                )
            }
        }
    }
}

@Preview
@Composable
fun ChatItem(
    name: String = "YOU",
    message: String = "Message"
) {
    var isExpanded by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .pointerInput(Unit) {
                    myDetectTapGestures(
                        onLongPress = {
                            offset = DpOffset(it.x.toDp(), it.y.toDp())
                            isExpanded = true
                        }
                    )
                },
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (name) {
                    "YOU" -> Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .width(20.dp)
                    )

                    else -> Image(
                        painter = painterResource(id = R.drawable.gemini_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .width(20.dp)
                    )
                }
                Text(text = name)
            }
            Text(
                text = message,
                modifier = Modifier.padding(start = 30.dp, end = 10.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Box(modifier = Modifier.offset(offset.x, offset.y)) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                val clipboardManager = LocalClipboardManager.current
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )

                    },
                    text = { Text(text = "Copy") },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(message))
                        isExpanded = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = "Select Text") },
                    onClick = {

                    }
                )
            }
        }
    }
}

suspend fun PointerInputScope.interceptTap(
    pass: PointerEventPass = PointerEventPass.Initial,
    onTap: ((Offset) -> Unit)? = null,
) = coroutineScope {
    if (onTap == null) return@coroutineScope

    awaitEachGesture {
        val down = awaitFirstDown(pass = pass)
        val downTime = System.currentTimeMillis()
        val tapTimeout = viewConfiguration.longPressTimeoutMillis
        val tapPosition = down.position

        do {
            val event = awaitPointerEvent(pass)
            val currentTime = System.currentTimeMillis()

            if (event.changes.size != 1) break // More than one event: not a tap
            if (currentTime - downTime >= tapTimeout) break // Too slow: not a tap

            val change = event.changes[0]

            // Too much movement: not a tap
            if ((change.position - tapPosition).getDistance() > viewConfiguration.touchSlop) break

            if (change.id == down.id && !change.pressed) {
                change.consume()
                onTap(change.position)
            }
        } while (event.changes.any { it.id == down.id && it.pressed })
    }
}