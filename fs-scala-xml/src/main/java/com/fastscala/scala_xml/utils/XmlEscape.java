package com.fastscala.scala_xml.utils;

/**
 * Java version of {@code XmlStream2.escape}: same output as {@code scala.xml.Utility.escape} ({@code &}, {@code <},
 * {@code >} and {@code "} become entities; control characters other than \n, \r and \t are dropped), appending
 * unchanged runs in bulk.
 */
public final class XmlEscape {

    private XmlEscape() {
    }

    // Define pre-calculated replacement strings indexed by action code
    private static final String[] REPLACEMENTS = new String[]{
            "",       // ACTION_KEEP (unused directly for replacement, handled as no-op)
            "",       // ACTION_DROP
            "&amp;",  // ACTION_AMP
            "&lt;",   // ACTION_LT
            "&gt;",   // ACTION_GT
            "&quot;"  // ACTION_QUOT
    };

    private static final byte ACTION_KEEP = 0;
    private static final byte ACTION_DROP = 1;
    private static final byte ACTION_AMP = 2;
    private static final byte ACTION_LT = 3;
    private static final byte ACTION_GT = 4;
    private static final byte ACTION_QUOT = 5;

    private static final byte[] ESCAPE_ACTIONS = new byte[128];

    static {
        for (int i = 0; i < 32; i++) {
            ESCAPE_ACTIONS[i] = ACTION_DROP;
        }
        ESCAPE_ACTIONS['\t'] = ACTION_KEEP;
        ESCAPE_ACTIONS['\n'] = ACTION_KEEP;
        ESCAPE_ACTIONS['\r'] = ACTION_KEEP;
        ESCAPE_ACTIONS['&'] = ACTION_AMP;
        ESCAPE_ACTIONS['<'] = ACTION_LT;
        ESCAPE_ACTIONS['>'] = ACTION_GT;
        ESCAPE_ACTIONS['"'] = ACTION_QUOT;
    }

    public static StringBuilder escape(String text, StringBuilder sb) {
        final int len = text.length();
        int runStart = 0;

        for (int i = 0; i < len; i++) {
            final char c = text.charAt(i);
            final int action = c < 128 ? ESCAPE_ACTIONS[c] : ACTION_KEEP;

            if (action == ACTION_KEEP) {
                continue;
            }

            // Append the safe run accumulated so far
            sb.append(text, runStart, i);

            // Append the replacement string if it's not a drop action
            if (action != ACTION_DROP) {
                sb.append(REPLACEMENTS[action]);
            }

            runStart = i + 1;
        }
        return sb.append(text, runStart, len);
    }
}
