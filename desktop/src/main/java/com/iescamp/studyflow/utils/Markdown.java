package com.iescamp.studyflow.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small dependency-free Markdown → HTML renderer for the Notes vault.
 * Obsidian-inspired: supports [[WikiLinks]] (rendered as note: links the
 * WebView intercepts), task checkboxes, fenced code, tables excluded.
 */
public final class Markdown {

    private Markdown() {}

    private static final Pattern CODE_SPAN = Pattern.compile("`([^`\\n]+)`");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC = Pattern.compile("(?<!\\*)\\*([^\\*\\n]+)\\*(?!\\*)");
    private static final Pattern STRIKE = Pattern.compile("~~(.+?)~~");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)\\]\\(([^)\"]+)(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)");
    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]|]+)(?:\\|([^\\]]+))?\\]\\]");
    private static final Pattern CHECKBOX = Pattern.compile("^\\s*[-*]\\s+\\[([ xX])\\]\\s+(.*)$");
    private static final Pattern UNORDERED = Pattern.compile("^\\s*[-*+]\\s+(.*)$");
    private static final Pattern ORDERED = Pattern.compile("^\\s*\\d+[.)]\\s+(.*)$");
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern HR = Pattern.compile("^\\s*(-{3,}|\\*{3,}|_{3,})\\s*$");
    private static final Pattern QUOTE = Pattern.compile("^\\s*>\\s?(.*)$");

    public static String toHtml(String markdown) {
        return toHtml(markdown, name -> true);
    }

    /**
     * @param exists answers whether a [[WikiLink]] target already exists;
     *               missing targets render as red links (click still creates them).
     */
    public static String toHtml(String markdown, java.util.function.Predicate<String> exists) {
        if (markdown == null || markdown.isEmpty()) {
            return "<p><i>Empty note</i></p>";
        }

        // Strip YAML frontmatter
        if (markdown.startsWith("---\n") || markdown.startsWith("---\r\n")) {
            int endIdx = markdown.indexOf("\n---", 3);
            if (endIdx != -1) {
                // skip over \n---\n
                int nextLine = markdown.indexOf('\n', endIdx + 4);
                if (nextLine != -1) {
                    markdown = markdown.substring(nextLine + 1);
                } else {
                    markdown = "";
                }
            }
        }

        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

        StringBuilder html = new StringBuilder();
        List<String> paragraph = new ArrayList<>();
        boolean inFence = false;
        StringBuilder fence = new StringBuilder();
        boolean inList = false;
        boolean orderedList = false;
        List<String> quotes = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine;

            if (line.trim().startsWith("```")) {
                if (inFence) {
                    html.append("<pre><code>").append(escape(fence.toString())).append("</code></pre>\n");
                    fence.setLength(0);
                    inFence = false;
                } else {
                    flushParagraph(html, paragraph, exists);
                    closeList(html);
                    inList = false;
                    flushQuotes(html, quotes, exists);
                    inFence = true;
                }
                continue;
            }
            if (inFence) {
                fence.append(line).append('\n');
                continue;
            }

            Matcher quote = QUOTE.matcher(line);
            if (quote.matches()) {
                flushParagraph(html, paragraph, exists);
                closeList(html);
                inList = false;
                quotes.add(quote.group(1));
                continue;
            } else if (!quotes.isEmpty()) {
                flushQuotes(html, quotes, exists);
            }

            if (line.trim().isEmpty()) {
                flushParagraph(html, paragraph, exists);
                closeList(html);
                inList = false;
                continue;
            }

            Matcher heading = HEADING.matcher(line);
            Matcher hr = HR.matcher(line);
            Matcher checkbox = CHECKBOX.matcher(line);
            Matcher unordered = UNORDERED.matcher(line);
            Matcher ordered = ORDERED.matcher(line);

            if (heading.matches()) {
                flushParagraph(html, paragraph, exists);
                closeList(html);
                inList = false;
                int level = heading.group(1).length();
                html.append("<h").append(level).append(">")
                        .append(inline(heading.group(2), exists))
                        .append("</h").append(level).append(">\n");
            } else if (hr.matches()) {
                flushParagraph(html, paragraph, exists);
                closeList(html);
                inList = false;
                html.append("<hr/>\n");
            } else if (checkbox.matches()) {
                flushParagraph(html, paragraph, exists);
                if (!inList || orderedList) {
                    closeList(html);
                    html.append("<ul>\n");
                    inList = true;
                    orderedList = false;
                }
                boolean checked = checkbox.group(1).equalsIgnoreCase("x");
                html.append("<li class=\"task\"><input type=\"checkbox\" disabled")
                        .append(checked ? " checked" : "").append("/> ")
                        .append(inline(checkbox.group(2), exists)).append("</li>\n");
            } else if (unordered.matches()) {
                flushParagraph(html, paragraph, exists);
                if (!inList || orderedList) {
                    closeList(html);
                    html.append("<ul>\n");
                    inList = true;
                    orderedList = false;
                }
                html.append("<li>").append(inline(unordered.group(1), exists)).append("</li>\n");
            } else if (ordered.matches()) {
                flushParagraph(html, paragraph, exists);
                if (!inList || !orderedList) {
                    closeList(html);
                    html.append("<ol>\n");
                    inList = true;
                    orderedList = true;
                }
                html.append("<li>").append(inline(ordered.group(1), exists)).append("</li>\n");
            } else {
                closeList(html);
                inList = false;
                paragraph.add(line.trim());
            }
        }

        if (inFence) {
            html.append("<pre><code>").append(escape(fence.toString())).append("</code></pre>\n");
        }
        flushParagraph(html, paragraph, exists);
        closeList(html);
        flushQuotes(html, quotes, exists);
        return html.toString();
    }

    /** Wraps rendered body in a theme-aware page for the WebView. */
    public static String toPage(String bodyHtml, boolean dark) {
        String bg = dark ? "#1E1E1E" : "#ffffff";
        String fg = dark ? "#E6E6E6" : "#2c3e50";
        String muted = dark ? "#9aa0a6" : "#7f8c8d";
        String accent = dark ? "#7aa2f7" : "#2196f3";
        String codeBg = dark ? "#0D1117" : "#f6f8fa";
        return "<html><head><meta charset=\"utf-8\"/>"
                + "<style>"
                + "body{background:" + bg + ";color:" + fg + ";font-family:'Segoe UI',sans-serif;"
                + "line-height:1.6;max-width:760px;margin:0 auto;padding:8px 20px 40px;}"
                + "h1,h2,h3{border-bottom:1px solid " + muted + "55;padding-bottom:4px;}"
                + "a{color:" + accent + ";text-decoration:none;}"
                + "a.missing{color:#e74c3c;border-bottom:1px dashed #e74c3c;}"
                + "pre{background:" + codeBg + ";padding:12px;border-radius:8px;overflow-x:auto;}"
                + "code{background:" + codeBg + ";padding:2px 5px;border-radius:4px;font-family:Consolas,monospace;}"
                + "pre code{background:none;padding:0;}"
                + "blockquote{border-left:3px solid " + accent + ";margin:8px 0;padding:4px 12px;color:" + muted + ";}"
                + "li.task{list-style:none;margin-left:-20px;}"
                + "input[type=checkbox]{accent-color:" + accent + ";}"
                + "hr{border:none;border-top:1px solid " + muted + "55;}"
                + "</style></head><body>" + bodyHtml + "</body></html>";
    }

    private static void flushParagraph(StringBuilder html, List<String> paragraph, java.util.function.Predicate<String> exists) {
        if (!paragraph.isEmpty()) {
            html.append("<p>").append(inline(String.join(" ", paragraph), exists)).append("</p>\n");
            paragraph.clear();
        }
    }

    private static void closeList(StringBuilder html) {
        String s = html.toString();
        if (s.endsWith("</li>\n")) {
            int lastUl = Math.max(html.lastIndexOf("<ul>\n"), html.lastIndexOf("<ol>\n"));
            if (lastUl >= 0) {
                boolean ordered = html.lastIndexOf("<ol>\n") > html.lastIndexOf("<ul>\n");
                html.append(ordered ? "</ol>\n" : "</ul>\n");
            }
        }
    }

    private static void flushQuotes(StringBuilder html, List<String> quotes, java.util.function.Predicate<String> exists) {
        if (!quotes.isEmpty()) {
            html.append("<blockquote>");
            for (String q : quotes) {
                html.append(inline(q, exists)).append("<br/>");
            }
            html.append("</blockquote>\n");
            quotes.clear();
        }
    }

    /** Inline formatting on already-escaped text. */
    private static String inline(String text, java.util.function.Predicate<String> exists) {
        String out = escape(text);
        // protect code spans first
        List<String> codeSpans = new ArrayList<>();
        Matcher code = CODE_SPAN.matcher(out);
        StringBuffer sb = new StringBuffer();
        while (code.find()) {
            codeSpans.add("<code>" + code.group(1) + "</code>");
            code.appendReplacement(sb, "\u0000" + (codeSpans.size() - 1) + "\u0000");
        }
        code.appendTail(sb);
        out = sb.toString();

        out = replace(out, IMAGE, m -> "🖼 " + m.group(1) + " (" + m.group(2) + ")");
        out = replace(out, WIKILINK, m -> {
            String target = m.group(1).trim();
            String label = m.group(2) != null ? m.group(2).trim() : target;
            String href = "note:" + target.replace(" ", "%20");
            String cls = exists.test(target) ? "" : " class=\"missing\"";
            return "<a href=\"" + href + "\"" + cls + ">" + label + "</a>";
        });
        out = replace(out, LINK, m -> "<a href=\"" + m.group(2) + "\">" + m.group(1) + "</a>");
        out = replace(out, BOLD, m -> "<strong>" + m.group(1) + "</strong>");
        out = replace(out, ITALIC, m -> "<em>" + m.group(1) + "</em>");
        out = replace(out, STRIKE, m -> "<del>" + m.group(1) + "</del>");

        // restore code spans
        for (int i = 0; i < codeSpans.size(); i++) {
            out = out.replace("\u0000" + i + "\u0000", codeSpans.get(i));
        }
        return out;
    }

    private interface Replacer {
        String apply(Matcher m);
    }

    private static String replace(String input, Pattern pattern, Replacer replacer) {
        Matcher m = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(replacer.apply(m)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
