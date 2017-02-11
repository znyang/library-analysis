package com.zen.plugin.lib.analysis.mock

import org.gmock.GMockController
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author zen
 * @version 2016/9/11
 */
class BaseGenerator {

    protected final GMockController gmc

    BaseGenerator(GMockController gmc) {
        this.gmc = gmc
    }
}
