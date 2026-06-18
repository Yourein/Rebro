package net.yourein.rebro.feature.registertop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.yourein.rebro.core.resources.DrawableR

@Composable
internal fun RegisterTopAutofillSection() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {}
        ) {
            Icon(
                painter = painterResource(DrawableR.qr_code_scanner_24dp_fill),
                contentDescription = null,
                modifier = Modifier.size(68.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Autofill by\nRebro QR",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.size(60.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {}
        ) {
            Icon(
                painter = painterResource(DrawableR.barcode_scanner_24dp_fill),
                contentDescription = null,
                modifier = Modifier.size(68.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Autofill by\nISDN/ISBN\nbarcode",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}