package com.zen.plugin.lib.analysis.ext

import org.gradle.util.ConfigureUtil

/**
 * @author zen
 * @version 2016/5/29
 */
class LibraryAnalysisExtension {

    private static final String ANALYSIS_OUTPUT_PATH = "report/zen/analysis/library"
    private static final def    SIZE_STYLES          = ['tag-normal', 'tag-warning', 'tag-danger']
    static final def            LAST_INDEX           = SIZE_STYLES.size() - 1

    final Set<String> cacheIgnoreIds = new HashSet<>()

          String          outputPath = ANALYSIS_OUTPUT_PATH
          List<String>    ignore
    final LimitSizeConfig limit
          boolean         showTree   = false
          boolean         log        = false
          List<String>    size       = [200 * 1024, 1024 * 1024]

    LibraryAnalysisExtension() {
        limit = new LimitSizeConfig()
    }

    void limit(Closure closure) {
        ConfigureUtil.configure(closure, limit)
    }

    String getSizeTag(long s) {
        def index = size.findIndexOf {
//            println "${s} <? ${it}"
            s <= it
        }
//        println index
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
