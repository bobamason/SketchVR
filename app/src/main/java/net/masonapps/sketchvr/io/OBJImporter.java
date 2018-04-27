package net.masonapps.sketchvr.io;

import net.masonapps.sketchvr.modeling.SketchNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 2/28/2018.
 */

public class OBJImporter {

    public static List<SketchNode> loadFile(File file) throws IOException {
        final ArrayList<SketchNode> nodes = new ArrayList<>();
        final String fileName = FileUtils.nameWithoutExtension(file);
        final File mtlFile = new File(file.getParentFile(), fileName + ".mtl");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } finally {
            if (reader != null)
                reader.close();
        }
        return nodes;
    }
}
