package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import top.yukonga.miuix.kmp.nav.core.NavKey
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.models.CustomCourse
import com.pgigi.pumpkintoolkit.utils.CustomCourseStore

class AppViewModel : ViewModel(){
    val backStack = mutableStateListOf<NavKey>(Route.Home)
    val courseList = mutableStateListOf<Course>()
    val customCourseList = mutableStateListOf<CustomCourse>()
    var currentWeek by mutableIntStateOf(1)

    //自定义课表只需要再首次被创建时进行一次加载，不会随着教务一起刷新。故，放在此类下，保证第一次被加载时就初始化customCourseList，而不是和正常的courseList一起刷新、加载
    init {
        customCourseList.addAll(CustomCourseStore.loadCustomCourseCache())
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppViewModel()
            }
        }
    }

    //此函数用于合并教务课程与自定义课程
    //从customCourseList中抽取出所有的course，与原courseList合并成新列表后返回
    fun mergedCourses(): List<Course> {
        return courseList + customCourseList.map { it.course }
    }
}