package com.company.util;

import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

public class Util {
    public static List<List<String>> deepCopyResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nColumns = rsmd.getColumnCount();
        List<List<String>> copy = new ArrayList<>();
        int i;
        Vector<String> toAdd = new Vector<>(nColumns);

        for (i = 0; i < nColumns; i++) {
            toAdd.add(rsmd.getColumnName(i + 1));
        }

        copy.add(toAdd);

        rs.beforeFirst();

        while (rs.next()) {
            toAdd = new Vector<>(nColumns);

            for (i = 0; i < nColumns; i++) {
                toAdd.add(rs.getString(i + 1));
            }

            copy.add(toAdd);
        }

        return copy;
    }

    public static void prettyPrintResults(List<List<String>> results) {
        // Find max string length of each column including column names (N+1)*(M+1)
        int nColumns = results.get(0).size();
        Vector<Integer> maxWidths = new Vector<>(nColumns);
        int i = 0;

        for (List<String> row : results) {
            for (i = 0; i < nColumns; i++) {
                int length;
                if (row.get(i) == null) {
                    length = 4; // will print "null"
                } else {
                    length = row.get(i).length();
                }

                if (maxWidths.size() == i) {
                    maxWidths.add(length);
                } else if (length > maxWidths.get(i)) {
                    maxWidths.set(i, length);
                }
            }
        }

        // Print values with left padding corresponding to max width
        Vector<String> formatStrings = new Vector<>(nColumns);
        int totalWidth = 0;
        for (i = 0; i < nColumns; i++) {
            formatStrings.add(String.format("%%-%ds", maxWidths.get(i)));
            totalWidth += maxWidths.get(i);
        }

        for (i = 0; i < results.size(); i++) {
            List<String> row = results.get(i);

            for (int j = 0; j < nColumns; j++) {
                if (j > 0) {
                    System.out.print(" | ");
                }

                System.out.format(formatStrings.get(j), row.get(j));
            }

            System.out.println();

            if (i == 0) {
                System.out.println(repeat("-", totalWidth + (nColumns - 1) * 3));
            }
        }
    }

    private static String repeat(String toRepeat, int times) {
        // From http://stackoverflow.com/a/4903603
        return new String(new char[times]).replace("\0", toRepeat);
    }
}
