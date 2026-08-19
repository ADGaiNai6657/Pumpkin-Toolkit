package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.components.FloatingBottomBar
import com.pgigi.pumpkintoolkit.components.FloatingBottomBarColors
import com.pgigi.pumpkintoolkit.components.FloatingBottomBarMode
import com.pgigi.pumpkintoolkit.components.isLiquidGlassSupported
import com.pgigi.pumpkintoolkit.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit

object M3Navigation {
    val items = listOf("日程", "课表", "功能")
    val icons = listOf(MiuixIcons.ListView, MiuixIcons.VerticalSplit, MiuixIcons.GridView)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3HomeScreen(viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    var pagerCount by remember { mutableIntStateOf(M3Navigation.items.size) }
    val pagerState = rememberPagerState(pageCount = { pagerCount }, initialPage = viewModel.currentPagerIndex)
    val liquidGlassSupported = isLiquidGlassSupported()
    val useBlur = AppConfig.floatingNavigation && AppConfig.enableBlurEffect && liquidGlassSupported
    val backdrop = if (useBlur) rememberLayerBackdrop() else null

    val colors = FloatingBottomBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        activeContentColor = MaterialTheme.colorScheme.primary,
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!AppConfig.floatingNavigation && !isLandscape) {
                    NavigationBar {
                        M3Navigation.items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        viewModel.currentPagerIndex = index
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = { Icon(M3Navigation.icons[index], contentDescription = null) },
                                label = { Text(item) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!AppConfig.floatingNavigation && isLandscape) {
                    NavigationRail {
                        M3Navigation.items.forEachIndexed { index, item ->
                            NavigationRailItem(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        viewModel.currentPagerIndex = index
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = { Icon(M3Navigation.icons[index], contentDescription = null) },
                                label = { Text(item) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                        beyondViewportPageCount = 1
                    ) {
                        when (it) {
                            0 -> Material3TodayScreen()
                            1 -> Material3ScheduleScreen()
                            2 -> Material3FunctionScreen()
                        }
                    }

                    if (AppConfig.floatingNavigation) {
                        FloatingBottomBar(
                            items = M3Navigation.items,
                            selectedIndex = { pagerState.currentPage },
                            onSelected = { index ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    viewModel.currentPagerIndex = index
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            backdrop = backdrop,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            mode = if (useBlur) FloatingBottomBarMode.LiquidGlass else FloatingBottomBarMode.None,
                            colors = colors,
                            iconContent = { _, index ->
                                Icon(M3Navigation.icons[index], contentDescription = null)
                            },
                            labelContent = { item, _ ->
                                Text(item)
                            }
                        )
                    }
                }
            }
        }
    }
}
