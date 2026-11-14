package com.fastscala.scala_xml.utils;

/** Something text can be appended to, as to a {@link StringBuilder}: what {@link XmlEscape} writes to. */
public interface StringBuilderLike {

    /** Appends {@code s}. */
    void append(String s);

    /** Appends {@code s} from {@code start} (inclusive) to {@code end} (exclusive). */
    void append(String s, int start, int end);
}
