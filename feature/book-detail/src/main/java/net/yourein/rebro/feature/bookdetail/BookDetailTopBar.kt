package net.yourein.rebro.feature.bookdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.yourein.rebro.core.resources.DrawableR
import net.yourein.rebro.core.resources.RebroColor

@Composable
internal fun BookDetailTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(DrawableR.chevron_forward_24dp_fill),
            contentDescription = "Back",
            tint = RebroColor.TextPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .clickable { onBackClick() }
                .padding(8.dp)
                .size(24.dp)
                .scale(scaleX = -1f, scaleY = 1f)
        )
    }
}