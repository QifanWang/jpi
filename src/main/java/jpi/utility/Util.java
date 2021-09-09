package jpi.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
}
