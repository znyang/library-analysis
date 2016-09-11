package com.zen.plugin.lib.analysis

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.task.DependencyTreeReportTask
import com.zen.plugin.lib.analysis.util.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 1. 白名单
 * 2. 数据统计
 * 3. 共用标识
 */
class LibraryAnalysisPlugin implements Plugin<Project> {
    private static final def EXTENSION_NAME = 'libReport'
    private static final def BASE_GROUP     = 'reporting'
    private static final def TASK_PREFIX    = 'libReport'

    private LibraryAnalysisExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, LibraryAnalysisExtension)

        project.afterEvaluate {
            createTask(project)
        }
    }

    void createTask(Project project) {
        def configurations = project.configurations

        configurations.findAll {
            return !it.allDependencies.isEmpty()
        }.each {
            def conf = it.getName();
            def task = project.tasks.create("${TASK_PREFIX}_${conf}", DependencyTreeReportTask)
            task.configuration = it
            task.group = BASE_GROUP
            task.extension = extension
            task.log = extension.log ? new Logger(true) : null
        }
    }
}
