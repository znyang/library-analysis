package com.zen.plugin.lib.analysis

import com.android.annotations.Nullable
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.variant.BaseVariantData
import com.zen.plugin.lib.analysis.sdk.SdkResolver
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.lang.reflect.Method

/**
 * 1. 白名单
 * 2. 数据统计
 * 3. 共用标识
 */
class LibraryAnalysisPlugin implements Plugin<Project> {
    public static File sdkLocation = SdkResolver.resolve(null)
    private static final String EXTENSION_NAME = 'libReport';

    @Override
    void apply(Project project) {
        // 通过项目对象找到本地配置文件
        sdkLocation = SdkResolver.resolve(project)
        project.extensions.create(EXTENSION_NAME, LibraryAnalysisExtension)

        project.afterEvaluate {
            createTask(project)
        }
    }

    static final void createTask(Project project) {
        // 判断项目类型，区分Variants
        if (project.plugins.hasPlugin('com.android.application')) {
            applyAndroid(project, (DomainObjectCollection<BaseVariant>) project.android.applicationVariants);
        } else if (project.plugins.hasPlugin('com.android.test')) {
            applyAndroid(project, (DomainObjectCollection<BaseVariant>) project.android.applicationVariants);
        } else if (project.plugins.hasPlugin('com.android.library')) {
            applyAndroid(project, (DomainObjectCollection<BaseVariant>) project.android.libraryVariants);
        } else {
            throw new IllegalArgumentException('Dexcount plugin requires the Android plugin to be configured');
        }
    }

    private static void applyAndroid(Project project,
                                     DomainObjectCollection<BaseVariant> variants) {
        variants.all { variant ->
            // outputs就是构建结束后的文件，aar or apk
            variant.outputs.each { output ->
                // 变种版本名
                def slug = variant.name.capitalize()
                if (variant.outputs.size() > 1) {
                    slug += output.name.capitalize()
                }

                // 创建Task
                def task = project.tasks.create("libraryReport${slug}", LibraryDependencyReportTask)
                task.description = "Outputs dependents data for ${variant.name}."
                task.group = 'Android'
                task.variant = getVariantData(variant)
                task.extension = project.extensions[EXTENSION_NAME] as LibraryAnalysisExtension
            }
        }
    }

    @Nullable
    private static BaseVariantData getVariantData(BaseVariant variant) {
        Method method = BaseVariantImpl.getDeclaredMethod("getVariantData")
        method.setAccessible(true)
        return (BaseVariantData) method.invoke(variant)
    }
}
