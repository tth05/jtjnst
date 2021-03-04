package com.github.tth05.jtjnst.cmd;

import java.util.ArrayDeque;
import java.util.Deque;

public class JTJNSTFormatter {

    public static String format(String code) {
        StringBuilder builder = new StringBuilder();

        BraceCounter counter = new BraceCounter();

        int indent = 0;
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '{') {
                builder.append('{');

                int nextChar = nextNonWhitespaceChar(chars, i + 1);

                if (chars[nextChar] != '}') {
                    indent++;
                    builder.append(System.lineSeparator()).append("\t".repeat(indent));
                } else {
                    i = nextChar;
                    builder.append('}');
                }
            } else if (c == '}') {
                indent--;
                builder.append(System.lineSeparator())
                        .append("\t".repeat(indent))
                        .append('}');
            } else if (c == '(') {
                counter.add();

                int nextChar = nextNonWhitespaceChar(chars, i + 2);

                if (chars[nextChar] == '-' && chars[nextChar + 1] == '>') {
                    builder.append(System.lineSeparator()).append("\t".repeat(indent)).append("() ->");
                    counter.remove();

                    i = nextChar + 1;
                } else {
                    builder.append('(');
                }
            } else if (c == ')') {
                boolean even = counter.remove();

                if (even) {
                    indent--;
                    builder.append(System.lineSeparator()).append("\t".repeat(indent)).append(')');
                } else {
                    builder.append(')');
                }
            } else if (c == '<' && code.regionMatches(i, "<Runnable>of(", 0, 13)) {
                builder.append("<Runnable>of(");
                if (chars[i + 13] != ')') {
                    counter.start();
                    counter.add();
                    indent++;
                    i += 12;
                } else {
                    builder.append(')');
                    i += 13;
                }
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static int nextNonWhitespaceChar(char[] ar, int i) {
        while (i < ar.length) {
            if (ar[i] != ' ')
                return i;
            i++;
        }

        throw new RuntimeException();
    }

    private static final class BraceCounter {

        private final Deque<Integer> counts = new ArrayDeque<>();

        public void start() {
            counts.push(0);
        }

        public void add() {
            if (counts.size() == 0)
                return;

            counts.push(counts.pop() + 1);
        }

        public boolean remove() {
            if (counts.size() == 0)
                return false;

            int newValue = counts.pop() - 1;

            if (newValue == 0)
                return true;

            counts.push(newValue);
            return false;
        }
    }
}
