package com.android.voyageur.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable search bar component with consistent styling and search icon.
 *
 * @param placeholderId Resource ID for placeholder text
 * @param onQueryChange Callback triggered when search query changes
 * @param modifier Optional modifiers for the search bar
 */
@Composable
fun SearchBar(
    placeholderId: Int,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)

    TextField(
        value = query,
        onValueChange = {
            query = it
            onQueryChange(it)
        },
        placeholder = { Text(text = stringResource(placeholderId), style = textStyle) },
        singleLine = true,
        textStyle = textStyle,
        shape = RoundedCornerShape(30.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier.height(50.dp).fillMaxWidth()
    )
}