package com.zen.plugin.lib.analysis.mock

import org.gmock.GMockController
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author zen
 * @version 2016/9/11
 */
class DependencyGenerator extends BaseGenerator {

    static final def DEP  = [
            "support_v4"         : [
                    'id'      : 'com.android.support:support-v4:24.1.1',
                    'fileName': 'support-v4-24.1.1.aar',
                    'file'    : 'D:\\.gradle\\caches\\modules-2\\files-2.1\\com.android.support\\support-v4\\24.1.1\\57c006a017aa2cbe3d32511e5ac28ee76cf9f51b\\support-v4-24.1.1.aar',
                    'size'    : 1000 * 1024,
                    'children': null
            ],
            "recyclerview_v7"    : [
                    'id'      : 'com.android.support:recyclerview-v7:24.1.1',
                    'fileName': 'recyclerview-v7-24.1.1.aar',
                    'file'    : 'D:\\.gradle\\caches\\modules-2\\files-2.1\\com.android.support\\recyclerview-v7\\24.1.1\\eb14224a80834c4eb124970a12e3e46d0a5d20f2\\recyclerview-v7-24.1.1.aar',
                    'size'    : 300 * 1024,
                    'children': null
            ],
            "support_annotations": [
                    'id'      : 'com.android.support:support-annotations:24.1.1',
                    'fileName': 'support-annotations-24.1.1.jar',
                    'file'    : 'D:\\.gradle\\caches\\modules-2\\files-2.1\\com.android.support\\support-annotations\\24.1.1\\3af19f153122737b372622fa6c81dd11a1c6b999\\support-annotations-24.1.1.jar',
                    'size'    : 20 * 1024,
                    'children': null
            ]
    ]
    static final def ROOT = [
            DEP.recyclerview_v7
    ]

    static {
        DEP.support_v4.children = [DEP.support_annotations]
        DEP.recyclerview_v7.children = [DEP.support_v4, DEP.support_annotations]
    }

    static def integer = new AtomicInteger()

    DependencyGenerator(GMockController gmc) {
        super(gmc)
    }

    RenderableDependency mockRootDependency() {
        RenderableDependency root = gmc.mock(RenderableDependency)
        root.getId().returns("root").stub()
        root.getName().returns("Root").stub()

        create(ROOT, root)
        root
    }

    void create(def data, RenderableDependency parent) {
        Set<RenderableDependency> children = new HashSet<>()
        data?.each {
            RenderableDependency dependency = gmc.mock(RenderableDependency)
            dependency.getId().returns(it.id).stub()
            dependency.getName().returns(it.id).stub()
            create(it.children, dependency)

            children.add(dependency)
        }
        parent.children.returns(children).stub()
    }

    static int getSize() {
        if (integer.get() < 30) {
            return (30 - integer) >>> 1
        }
        return 0
    }

    FileCollection mockFileCollection() {
        Set<File> files = new HashSet<>()
        DEP.each {
            File file = gmc.mock(File)
            file.name.returns(it.value.fileName).stub()
            file.path.returns(it.value.file.replace('\\', File.separator)).stub()
            file.size().returns(it.value.size).stub()
            files.add(file)
        }

        FileCollection collection = gmc.mock(FileCollection)
        collection.getFiles().returns(files).stub()
        collection
    }

}
