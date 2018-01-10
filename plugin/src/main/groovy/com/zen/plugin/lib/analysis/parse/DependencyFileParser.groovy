package com.zen.plugin.lib.analysis.parse

import java.util.zip.ZipEntry

class DependencyFileParser implements IParser {

    @Override
    Result parse(ParseContext context, ZipEntry entry) {
        return null
    }

    interface IParser {
        Result parse(ParseContext context, ZipEntry entry)
    }

    class Result {

    }

    class ParseContext {

    }

}