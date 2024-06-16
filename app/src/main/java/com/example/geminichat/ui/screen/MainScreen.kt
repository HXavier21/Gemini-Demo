package com.example.geminichat.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geminichat.R
import com.example.geminichat.ui.component.ChatField
import com.example.geminichat.ui.data.MainScreenViewModel
import com.example.geminichat.ui.data.Sentence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "MainScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Preview
@Composable
fun MainScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel(), onNavigateToSelectText: () -> Unit = {}
) {
    val context = LocalContext.current
    val sentences by mainScreenViewModel.sentencesListFlow.collectAsState()
    val isGenerating by mainScreenViewModel.isGenerating.collectAsState()
    val generatedText by mainScreenViewModel.generatedText.collectAsState()
    var message by remember { mutableStateOf("") }
    val coroutine = rememberCoroutineScope()
    var job by remember { mutableStateOf(Job()) }

    LaunchedEffect(Unit) {
        mainScreenViewModel.initializeGemini(context)
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            TopAppBar(title = {
                Row {
                    Text(
                        "Gemini",
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)
                    )
                    Text(
                        text = "Demo",
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }, navigationIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = null
                )
            }, actions = {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(20.dp))
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            })
            ChatField(
                modifier = Modifier.weight(1f),
                sentences = sentences,
                mainScreenViewModel = mainScreenViewModel,
                onNavigateToSelectText = onNavigateToSelectText,
                generatedText = generatedText
            )
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            ) {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = {
                        Text(
                            "Message"
                        )
                    },
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .padding(10.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Box(
                    modifier = Modifier.padding(end = 20.dp), contentAlignment = Alignment.Center
                ) {
                    val focusManager = LocalFocusManager.current
                    Icon(
                        imageVector = if (!isGenerating) Icons.Default.ArrowUpward
                        else Icons.Default.Stop,
                        tint = if (isGenerating) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(
                                if (!isGenerating) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                MaterialTheme.shapes.small
                            )
                            .padding(10.dp)
                            .clickable(enabled = message.isNotEmpty() or isGenerating, onClick = {
                                if (isGenerating) {
                                    job.parent?.cancel()
                                    mainScreenViewModel.updateSentence(
                                        Sentence(
                                            "GEMINI", generatedText
                                        )
                                    )
                                    mainScreenViewModel.clearGeneratedText()
                                    mainScreenViewModel.changeGenerateState()
                                    message = ""
                                } else {
                                    focusManager.clearFocus()
                                    mainScreenViewModel.changeGenerateState()
                                    val tempMsg = message
                                    message = ""
                                    mainScreenViewModel.addSentence(
                                        Sentence(
                                            "YOU", tempMsg
                                        )
                                    )
                                    job = Job(
                                        coroutine.launch {
                                            Log.d(TAG, "MainScreen: test")
                                            mainScreenViewModel.sendMessage(
                                                tempMsg, context
                                            )
                                        }
                                    )
                                }
                            }, indication = null, interactionSource = remember {
                                MutableInteractionSource()
                            })
                    )
                }
            }
        }
        if (sentences.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gemini_background),
                    contentDescription = null,
                    modifier = Modifier.scale(0.4f)
                )
            }
        }
    }
}