package com.nutrisport.shared.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.BorderError
import com.nutrisport.shared.BorderIdle
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconSecondary
import com.nutrisport.shared.SurfaceDarker
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    enabled: Boolean = true,
    error: Boolean = false,
    expanded: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text
    )
) {

    val borderColor by animateColorAsState(
        targetValue = if (error) BorderError else BorderIdle
    )

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(size = 6.dp)
            )
            .clip(RoundedCornerShape(10.dp)),
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        placeholder = if (placeholder != null) {
            {
                Text(
                    text = placeholder,
                    fontSize = FontSize.REGULAR
                )
            }
        } else null,
        singleLine = !expanded,
        shape = RoundedCornerShape(size = 6.dp),
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            disabledTextColor = TextPrimary.copy(Alpha.DISABLED),
            unfocusedContainerColor = SurfaceLighter,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = SurfaceLighter,
            focusedPlaceholderColor = TextPrimary.copy(Alpha.HALF),
            unfocusedPlaceholderColor = TextPrimary.copy(Alpha.HALF),
            disabledPlaceholderColor = TextPrimary.copy(Alpha.DISABLED),
            disabledContainerColor = SurfaceDarker,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            selectionColors = TextSelectionColors(
                handleColor = IconSecondary,
                backgroundColor = Color.Unspecified
            )
        )
    )

}