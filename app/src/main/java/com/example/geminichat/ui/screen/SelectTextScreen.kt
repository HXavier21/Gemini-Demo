package com.example.geminichat.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun SelectTextScreen(message: String = "") {
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Select Text",
                style = MaterialTheme.typography.headlineMedium
            )
            SelectionContainer {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}