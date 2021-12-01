package jpi.utility;

import java.io.*;

public class Util {
    private Util() {}

    public static void writeOutput(String path, StringBuilder sb) {
        try {

            File f = new File(path + "/jpi-output.txt");
            FileWriter fw = new FileWriter(f);
            fw.write(sb.toString());
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String rmHeader(String oldStr) {
        if(oldStr == null) return null;

        if(oldStr.startsWith("file://"))
            return oldStr.substring(7);
        else if(oldStr.startsWith("jrt://"))
            return oldStr.substring(6);
        else if(oldStr.startsWith("jar://"))
            return oldStr.substring(6);
        else
            return oldStr;
    }

}
