package com.example.geminichat.ui.component

import android.nfc.TagLostException
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geminichat.R
import com.example.geminichat.ui.data.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Preview
@Composable
fun ChatField(
    modifier: Modifier = Modifier,
    mutableConversationFlowList: MutableList<MutableStateFlow<Conversation>> = mutableListOf()
) {
    LazyColumn(modifier = modifier) {
        for (conversation in mutableConversationFlowList) {
            item {
                ChatItem(
                    modifier = modifier,
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
    modifier: Modifier = Modifier,
    name: String = "YOU",
    message: String = "Message"
) {
    Surface {
        Column(
            modifier = modifier.fillMaxWidth(),
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
    }
}