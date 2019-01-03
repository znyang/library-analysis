package com.zen.plugin.lib.analysis.ext

import org.gradle.util.ConfigureUtil

/**
 * @author zen
 * @version 2016/5/29
 */
class LibraryAnalysisExtension {

    private static final ANALYSIS_OUTPUT_PATH = "reports/zen/analysis/library"
    private static final SIZE_STYLES = ['tag-normal', 'tag-warning', 'tag-danger']
    private static final LAST_INDEX = SIZE_STYLES.size() - 1

    final Set<String> cacheIgnoreIds = new HashSet<>()

    String outputPath = ANALYSIS_OUTPUT_PATH
    List<String> ignore
    List<String> output = ["txt", "html"]
    final LimitSizeConfig limit
    boolean showTree = false
    boolean log = false
    List<String> region = [200 * 1024, 1024 * 1024]
    boolean fullTree = false
    boolean showSize = true
    boolean showSupport = true

    LibraryAnalysisExtension() {
        limit = new LimitSizeConfig()
    }

    void limit(Closure closure) {
        ConfigureUtil.configure(closure, limit)
    }

    String getSizeTag(long s) {
        def index = region.findIndexOf {
            s <= it
        }
        index = index < 0 ? LAST_INDEX : index

        SIZE_STYLES[index]
    }

    boolean isIgnore(String id) {
        if (cacheIgnoreIds.contains(id)) {
            return true
        }
        boolean result = ignore?.find {
            id.contains(it)
        }
        if (result) {
            cacheIgnoreIds.add(id)
        }
        result
    }

}
