package com.payra.calling.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PayraPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick, pressedScale = 0.96f)
            .clip(PayraShapes.Pill)
            .background(PayraColors.Primary)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AvatarCircle(
    initial: String,
    color: Color,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    imageUri: String = "",
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri.isNotEmpty()) {
            PayraImage(model = imageUri, modifier = Modifier.fillMaxSize())
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.4f).sp,
            )
        }
    }
}

/** Counter badge that pops (springs) every time the number changes. */
@Composable
fun UnreadBadge(count: Int) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(count) {
        scale.snapTo(0.5f)
        scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
    }
    Box(
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .background(PayraColors.Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(count.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GlassPillField(placeholder: String, icon: ImageVector, modifier: Modifier = Modifier) {
    GlassSurface(modifier = modifier.fillMaxWidth(), shape = PayraShapes.Field) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = PayraColors.Muted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(placeholder, color = PayraColors.Muted, fontSize = 16.sp)
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)

/** Glass tab bar. The teal pill slides between tabs with a spring; the selected icon pops. */
@Composable
fun GlassTabBar(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
        BoxWithConstraints(modifier = Modifier.padding(8.dp)) {
            val gap = 4.dp
            val tabWidth = (maxWidth - gap * (items.size - 1)) / items.size
            val pillX by animateDpAsState(
                targetValue = (tabWidth + gap) * selectedIndex,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
                label = "tabPill",
            )
            Box(
                modifier = Modifier
                    .offset(x = pillX)
                    .width(tabWidth)
                    .height(58.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PayraColors.Primary),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    val tint by animateColorAsState(
                        targetValue = if (selected) Color.White else PayraColors.Muted,
                        animationSpec = tween(220),
                        label = "tabTint",
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.14f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                        label = "tabIcon",
                    )
                    Column(
                        modifier = Modifier
                            .width(tabWidth)
                            .height(58.dp)
                            .bouncyClick(onClick = { onSelect(index) }, pressedScale = 0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp).graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(item.label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}


/** A search box you can really type in (used on the chat list, contacts, new chat and forward screens). */
@Composable
fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    GlassSurface(modifier = modifier.fillMaxWidth(), shape = PayraShapes.Field) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = PayraColors.Muted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, color = PayraColors.Muted, fontSize = 16.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = PayraColors.Ink, fontSize = 16.sp),
                    cursorBrush = SolidColor(PayraColors.Primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (value.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Clear",
                    tint = PayraColors.Muted,
                    modifier = Modifier.size(20.dp).bouncyClick(onClick = { onValueChange("") }, pressedScale = 0.8f),
                )
            }
        }
    }
}
