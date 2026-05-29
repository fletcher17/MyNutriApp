package com.nutrisports.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisports.home.domain.DrawerItem

@Composable
fun CustomDrawer(
    onProfileClick: () -> Unit,
    onBlogClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onLocationsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAdminPanelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .fillMaxHeight()
            .padding(12.dp),

    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "NUTRISPORT",
            textAlign = TextAlign.Center,
            color = TextSecondary,
            fontSize = FontSize.EXTRA_LARGE,
            fontFamily = BebasNeueFont()
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Healthy Lifestyle",
            textAlign = TextAlign.Center,
            color = TextPrimary,
            fontSize = FontSize.MEDIUM,
            fontFamily = BebasNeueFont()
        )
        Spacer(modifier = Modifier.height(50.dp))
        DrawerItem.entries.take(5).forEach { drawerItem ->
            DrawerItemCard(
                drawerItem = drawerItem,
                onCLick = {
                    when (drawerItem) {
                        DrawerItem.Profile -> onProfileClick()
                        DrawerItem.Blog -> onBlogClick()
                        DrawerItem.Contact -> onContactUsClick()
                        DrawerItem.Locations -> onLocationsClick()
                        DrawerItem.SignOut -> onSignOutClick()
                        else -> {}
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        DrawerItemCard(
            drawerItem = DrawerItem.Admin,
            onCLick = onAdminPanelClick
        )
        Spacer(modifier = Modifier.height(24.dp))

    }
}