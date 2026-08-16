package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.isLiquidGlassSupported
import com.pgigi.pumpkintoolkit.components.miuix.MiuixFloatingDropdown
import com.pgigi.pumpkintoolkit.models.CoursePlan
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.PlanViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PlanCard(modifier: Modifier=Modifier, plan: CoursePlan){
    var expand by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { expand = !expand })
    ){
        BasicComponent(
            title = plan.name,
            summary = "学分: ${plan.credit}   总课时: ${plan.hours}课时",
            endActions = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = plan.evaluation,
                        fontSize = MiuixTheme.textStyles.headline1.fontSize,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = plan.attr,
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        )
        AnimatedVisibility(visible = expand) {
            BasicComponent(summary = "课程编号: ${plan.num}\n开课学期: ${plan.term}\n课程性质: ${plan.nature}\n开课单位: ${plan.institution}")
        }
    }
}

@Composable
fun MiuixPlanScreen(viewModel: PlanViewModel = viewModel(factory = PlanViewModel.Factory)) {
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    var loading by remember { mutableStateOf(false) }

    val liquidGlassSupported = isLiquidGlassSupported()
    val useBlur = AppConfig.enableBlurEffect && liquidGlassSupported
    val backdrop = if (useBlur) rememberLayerBackdrop() else null
    val containerColor = MiuixTheme.colorScheme.surfaceContainer

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmallTopAppBar(
                title = "课程执行计划",
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navigator.pop()
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
//                contentPadding = PaddingValues(12.dp, 6.dp, 12.dp, 100.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(viewModel.planList.size) { index ->
                    AnimatedVisibility(viewModel.selectedTerm == 0 || viewModel.planList[index].term == viewModel.termNameList[viewModel.selectedTerm]) {
                        PlanCard(
                            modifier = Modifier.padding(vertical = 4.dp),
                            plan = viewModel.planList[index]
                        )
                    }
                }
            }

            if (loading) {
                InfiniteProgressIndicator(Modifier.fillMaxSize())
            }

            if (viewModel.termNameList.size > 1) {
                MiuixFloatingDropdown(
                    label = viewModel.termNameList.getOrElse(viewModel.selectedTerm) { "全部学期" },
                    items = viewModel.termNameList.toList(),
                    selectedIndex = viewModel.selectedTerm,
                    onSelect = { viewModel.selectedTerm = it },
                    backdrop = backdrop,
                    containerColor = containerColor
                )
            }
        }
    }
    if (viewModel.planList.isEmpty()) {
        LaunchedEffect(Unit) {
            loading = true
            val planList = QZClient.getCoursePlan()
            if (!planList.isNullOrEmpty()) {
                viewModel.planList.clear()
                viewModel.planList.addAll(planList.reversed())
                val termList = planList.map { it.term }.distinct().sortedDescending()
                viewModel.termNameList.clear()
                viewModel.termNameList.add("全部学期")
                viewModel.termNameList.addAll(termList)
            }
            loading = false
        }
    }
}