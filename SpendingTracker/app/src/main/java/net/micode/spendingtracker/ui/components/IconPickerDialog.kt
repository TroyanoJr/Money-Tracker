package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.IconCatalog

@Composable
fun IconPickerDialog(
    selectedIconName: String,
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BeigeHeader
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkBrownText)
                    }
                    Text(
                        text = "Icons",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBrownText,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tabs
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFFD7CCC8), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(DarkBrownText, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("FILLED ICONS", color = Color.White, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OUTLINE ICONS", color = DarkBrownText.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }

                // Icon Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    IconCatalog.filledIconSections.forEach { section ->
                        // Section Header
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(7) }) {
                            Text(
                                text = section.name,
                                color = Color.Gray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        
                        // Section Icons
                        items(section.icons) { (name, icon) ->
                            val isSelected = name == selectedIconName
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .background(
                                        if (isSelected) DarkBrownText.copy(alpha = 0.2f) else Color.White, 
                                        RoundedCornerShape(4.dp)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, DarkBrownText, RoundedCornerShape(4.dp))
                                        else Modifier
                                    )
                                    .clickable { onIconSelected(name) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = DarkBrownText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
