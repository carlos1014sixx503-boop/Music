package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.state.NavDestination
import com.example.ui.theme.LocalDistritoTheme

/**
 * Barra de navegación Liquid Glass flotante basada en la referencia visual exacta del usuario:
 * - Una cápsula horizontal alargada (Inicio, Explorar, Biblioteca) con píldora blanca iluminada para la pestaña activa.
 * - Una burbuja circular flotante independiente en el lateral derecho para Buscar (🔍).
 */
@Composable
fun DistritoBottomNavigationBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current

    // Color base Liquid Glass ultra oscuro traslúcido
    val glassBg = Color(0xFF16161B).copy(alpha = 0.84f)
    val glassBorder = Color.White.copy(alpha = 0.18f)
    val glassHighlight = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.04f)
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("bottom_nav_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Cápsula Principal Liquid Glass (Inicio, Explorar, Biblioteca)
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color.Black.copy(alpha = 0.7f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                )
                .clip(RoundedCornerShape(36.dp))
                .background(glassBg)
                .border(
                    width = 1.2.dp,
                    brush = glassHighlight,
                    shape = RoundedCornerShape(36.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pestaña INICIO
                NavPillItem(
                    label = "Inicio",
                    icon = Icons.Filled.Home,
                    isSelected = currentDestination == NavDestination.HOME,
                    onClick = { onDestinationSelected(NavDestination.HOME) },
                    testTag = "nav_item_inicio"
                )

                // Pestaña FAVORITOS (con corazón iluminado estilo Liquid Glass)
                NavPillItem(
                    label = "Favoritos",
                    icon = Icons.Filled.Favorite,
                    isSelected = currentDestination == NavDestination.FAVORITES,
                    onClick = { onDestinationSelected(NavDestination.FAVORITES) },
                    testTag = "nav_item_favoritos"
                )

                // Pestaña BIBLIOTECA (icono de barras verticales |||\)
                NavPillCustomItem(
                    label = "Biblioteca",
                    isSelected = currentDestination == NavDestination.LIBRARY,
                    onClick = { onDestinationSelected(NavDestination.LIBRARY) },
                    testTag = "nav_item_biblioteca"
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Burbuja Circular Flotante Liquid Glass para BÚSQUEDA (🔍)
        val isSearchSelected = currentDestination == NavDestination.SEARCH
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = if (isSearchSelected) theme.primaryAccent.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.7f),
                    spotColor = if (isSearchSelected) theme.primaryAccent.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.7f)
                )
                .clip(CircleShape)
                .then(
                    if (isSearchSelected) {
                        Modifier.background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF282832).copy(alpha = 0.95f), Color(0xFF16161B).copy(alpha = 0.95f))
                            )
                        )
                    } else {
                        Modifier.background(glassBg)
                    }
                )
                .border(
                    width = if (isSearchSelected) 1.5.dp else 1.2.dp,
                    brush = if (isSearchSelected) Brush.verticalGradient(listOf(theme.primaryAccent, Color.White.copy(alpha = 0.4f))) else glassHighlight,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDestinationSelected(NavDestination.SEARCH) }
                )
                .testTag("nav_search_bubble"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = if (isSearchSelected) theme.primaryAccent else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun NavPillItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val theme = LocalDistritoTheme.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        if (isSelected) {
            // Píldora blanca iluminada idéntica a la referencia de la captura
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFFA0A0A5),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFA0A0A5),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun NavPillCustomItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Icono triple barra |||\ estilo biblioteca de la captura
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(3.dp).height(14.dp).background(Color.Black, RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color.Black, RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(3.dp).height(12.dp).background(Color.Black, RoundedCornerShape(1.dp)))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(3.dp).height(14.dp).background(Color(0xFFA0A0A5), RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color(0xFFA0A0A5), RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(3.dp).height(12.dp).background(Color(0xFFA0A0A5), RoundedCornerShape(1.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFA0A0A5),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
