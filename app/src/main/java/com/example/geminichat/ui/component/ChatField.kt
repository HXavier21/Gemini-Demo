package com.example.geminichat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geminichat.R
import com.example.geminichat.ui.data.MainScreenViewModel
import com.example.geminichat.ui.data.Sentence
import dev.jeziellago.compose.markdowntext.MarkdownText

private const val TAG = "ChatField"

@Preview
@Composable
fun ChatField(
    modifier: Modifier = Modifier,
    sentences: MutableList<Sentence> = mutableListOf(),
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    onNavigateToSelectText: () -> Unit = {},
    generatedText: String = ""
) {
    val listState = rememberLazyListState()
    LaunchedEffect(generatedText) {
        listState.scrollToItem(listState.layoutInfo.totalItemsCount)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState
    ) {
        for (sentence in sentences) {
            item {
                ChatItem(
                    name = sentence.role,
                    message = sentence.message.ifEmpty { generatedText },
                    mainScreenViewModel = mainScreenViewModel,
                    onNavigateToSelectText = onNavigateToSelectText
                )
            }
        }
        item{

        }
    }
}

@Preview
@Composable
fun ChatItem(
    name: String = "YOU",
    message: String = "Message",
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    onNavigateToSelectText: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    Box(modifier = Modifier.wrapContentSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 5.dp)
            ) {
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
            MarkdownText(
                markdown = message,
                modifier = Modifier.padding(start = 30.dp, end = 10.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Box(modifier = Modifier
            .matchParentSize()
            .clickable { }
            .pointerInput(Unit) {
                myDetectTapGestures(onLongPress = {
                    offset = DpOffset(it.x.toDp(), it.y.toDp())
                    isExpanded = true
                })
            })
        Box(modifier = Modifier.offset(offset.x, offset.y)) {
            DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                val clipboardManager = LocalClipboardManager.current
                DropdownMenuItem(leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy, contentDescription = null
                    )

                }, text = { Text(text = "Copy") }, onClick = {
                    clipboardManager.setText(AnnotatedString(message))
                    isExpanded = false
                })
                DropdownMenuItem(leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.SelectAll, contentDescription = null
                    )
                }, text = { Text(text = "Select Text") }, onClick = {
                    mainScreenViewModel.setTextToSelect(message)
                    onNavigateToSelectText()
                })
            }
        }
    }
}