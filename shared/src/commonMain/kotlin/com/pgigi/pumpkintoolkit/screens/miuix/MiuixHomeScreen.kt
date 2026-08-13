package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit


object Navigation {
    val items = listOf("日程","课表", "功能")
    val icons = listOf(MiuixIcons.ListView, MiuixIcons.VerticalSplit, MiuixIcons.GridView)
}

@Composable
fun MiuixHomeScreen(viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {

    val coroutineScope = rememberCoroutineScope()
    var pagerCount by remember { mutableIntStateOf(Navigation.items.size) }
    val pagerState = rememberPagerState(pageCount = { pagerCount }, initialPage = viewModel.currentPagerIndex)

    Scaffold(
        bottomBar = {
            NavigationBar{
                Navigation.items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = viewModel.currentPagerIndex == index,
                        onClick = {
                            coroutineScope.launch {
                                viewModel.currentPagerIndex = index
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = Navigation.icons[index],
                        label = label
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            beyondViewportPageCount = 1
        ) {
            when (it) {
                0 -> TodayScreen()
                1 -> ScheduleScreen()
                2 -> FunctionScreen()
            }
        }
    }
}