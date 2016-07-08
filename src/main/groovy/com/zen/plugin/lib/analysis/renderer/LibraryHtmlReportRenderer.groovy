package com.zen.plugin.lib.analysis.renderer;

import com.amazonaws.util.json.JSONArray;
import com.google.gson.Gson
import com.zen.plugin.lib.analysis.conf.StringConstants;
import com.zen.plugin.lib.analysis.model.Library;
import com.zen.plugin.lib.analysis.model.Node;

import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;

import java.io.IOException;

/**
 * LibraryHtmlReportRenderer
 *
 * @author yangz
 * @version 2016/7/8
 */
public class LibraryHtmlReportRenderer extends TextReportRenderer {

    private static final Gson GSON = new Gson();


    public void render(final Library library) throws IOException {
        String json = "[]";
        if (library != null && !library.isEmpty()) {
            json = parseLibrary(library);
        }
        getTextOutput().text(String.format(StringConstants.CONTENT_FORMAT, library == null ? "" : library.getName(), json));
    }

    public void render(final Node root) throws IOException {
        String json = "[]";
        if (root != null) {
            json = GSON.toJson(root);
            getTextOutput().text(String.format(StringConstants.CONTENT_FORMAT, root.getName(), "[" + json + "]"));
        } else {
            getTextOutput().text(String.format(StringConstants.CONTENT_FORMAT, "no dependencies", json));
        }
    }

    private String parseLibrary(Library library) {
        Node root = convertNodeTree(library);
        return GSON.toJson(root);
    }

    private Node convertNodeTree(Library library) {
        Node node = new Node();
        node.name = library.getName();
        node.open = true;
        for (Library lib : library.getLibraries()) {
            node.addNode(convertNodeTree(lib));
        }
        return node;
    }

}
