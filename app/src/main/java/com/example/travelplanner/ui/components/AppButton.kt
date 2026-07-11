package com.example.travelplanner.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {

    Button(

        modifier = modifier,

        enabled = enabled,

        onClick = onClick,

        shape = RoundedCornerShape(14.dp),

        contentPadding = PaddingValues(
            horizontal = 18.dp,
            vertical = 14.dp
        ),

        colors = ButtonDefaults.buttonColors(

            containerColor = MaterialTheme.colorScheme.tertiary,

            contentColor = MaterialTheme.colorScheme.onTertiary

        )

    ) {

        Text(text)

    }

}