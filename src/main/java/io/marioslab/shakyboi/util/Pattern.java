package io.marioslab.shakyboi.util;

public class Pattern {
    final String[] values;
    final String pattern;
    final boolean wildcards;
    String value;
    int index;

    public Pattern(String pattern) {
        pattern = pattern.replace('\\', '/');
        pattern = pattern.replaceAll("\\*\\*(?=[^/])", "**/*");
        pattern = pattern.replaceAll("(?<=[^/])\\*\\*", "*/**");
        pattern = pattern.toLowerCase();
        this.pattern = pattern;

        wildcards = pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1;
        values = pattern.split("/");
    }

    public boolean matchesPath(String path) {
        path = path.toLowerCase();
        reset();
        String[] files = path.split("[\\\\/]");
        for (int i = 0, n = files.length; i < n; i++) {
            String file = files[i];
            if (i > 0 && isExhausted()) return false;
            if (!matchFile(file)) return false;
            if (!incr(file) && isExhausted()) return true;
        }
        return wasFinalMatch();
    }

    boolean matchFile(String file) {
        if (value.equals("**")) return true;

        // Shortcut if no wildcards.
        if (!wildcards) return file.equals(value);

        int i = 0, j = 0;
        while (i < file.length() && j < value.length() && value.charAt(j) != '*') {
            if (value.charAt(j) != file.charAt(i) && value.charAt(j) != '?') return false;
            i++;
            j++;
        }

        // If reached end of pattern without finding a * wildcard, the match has to fail if not same length.
        if (j == value.length()) return file.length() == value.length();

        int cp = 0;
        int mp = 0;
        while (i < file.length()) {
            if (j < value.length() && value.charAt(j) == '*') {
                if (j++ >= value.length()) return true;
                mp = j;
                cp = i + 1;
            } else if (j < value.length() && (value.charAt(j) == file.charAt(i) || value.charAt(j) == '?')) {
                j++;
                i++;
            } else {
                j = mp;
                i = cp++;
            }
        }

        // Handle trailing asterisks.
        while (j < value.length() && value.charAt(j) == '*')
            j++;

        return j >= value.length();
    }

    boolean incr(String file) {
        if (value.equals("**")) {
            if (index == values.length - 1) return false;
            incr();
            if (matchFile(file))
                incr();
            else {
                decr();
                return false;
            }
        } else
            incr();
        return true;
    }

    void incr() {
        index++;
        if (index >= values.length)
            value = null;
        else
            value = values[index];
    }

    void decr() {
        index--;
        if (index > 0 && values[index - 1].equals("**")) index--;
        value = values[index];
    }

    void reset() {
        index = 0;
        value = values[0];
    }

    boolean isExhausted() {
        return index >= values.length;
    }

    boolean isLast() {
        return index >= values.length - 1;
    }

    boolean wasFinalMatch() {
        return isExhausted() || (isLast() && value.equals("**"));
    }

    public String toString() {
        return pattern;
    }
}