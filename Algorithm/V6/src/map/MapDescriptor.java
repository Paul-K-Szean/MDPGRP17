package map;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import Exploration.MapRef;
import common.GridVector;

public class MapDescriptor {
    
    private static String _getFilePath(String fileName) throws IOException {
        String rootPath = new File(".").getCanonicalPath() + "//";
        String packagePath = MapDescriptor.class.getPackage().getName().replace(".", "//") + "//";
        String mapDirPath = "test//";
        String fileExtension = ".txt";
        return rootPath + packagePath + mapDirPath + fileName + fileExtension;
    }
    
    private static String _binToHex(String bin) {
        String result = "";
        
        if (!bin.isEmpty()) {
            // ensure bin length is multiples of 4
            String fixedBin = bin;
            while (fixedBin.length() % 4 != 0) {
                fixedBin += "0";
            }

            // translate each blocks of 4 bits
            for (int i = 0; i < fixedBin.length(); i += 4) {
                String curSection = fixedBin.substring(i, i + 4);
                int sectionDec = 0;
                for (int j = 0; j < curSection.length(); j++) {
                    if (curSection.charAt(curSection.length() - 1 - j) == '1') {
                        sectionDec += Math.pow(2, j);
                    }
                }
                if (sectionDec > 9) {
                    // A -> F
                    result += (char) (sectionDec % 9 + 64);
                } else {
                    // 0 -> 9
                    result += sectionDec;
                }
            }
        }
        
        return result;
    }
    
    public static String[] toHex(String desc) {
        String[] result = new String[2];
        int resultIndex = 0;
        for (String curLine : desc.split("\n")) {
            if (curLine.equals("11")) {
                if (result[0] != null && !result[0].isEmpty()) {
                    resultIndex++;
                }
            } else {
                if (result[resultIndex] == null) result[resultIndex] = "";
                result[resultIndex] += curLine;
            }            
        }
        result[0] = _binToHex("11" + result[0] + "11");
//        result[0]=_binToHex(result[0]);
        result[1] = _binToHex(result[1] != null ? result[1] : "");
        return result;
    }
    
    public static String stringify(Map map, int[][] explored) {
        String result = "";
        
        result += "11\n";
        
        for (int descI = 0; descI < Map.COL; descI++) {
            for (int descJ = 0; descJ < Map.ROW; descJ++) {
                result += explored[descJ][descI] >= 1 ? "1" : "0";
            }
            result += "\n";
        }
        
        result += "11\n";
        
        for (int descI = 0; descI < Map.COL; descI++) {
            for (int descJ = 0; descJ < Map.ROW; descJ++) {
                int curPoint = explored[descJ][descI];
                if (curPoint >= 1) {
                    result += curPoint == 2 ? "1" : "0";
                }
            }
            result += "\n";
        }
        return result;
    }
    
    public static String stringify(Map map, boolean[][] explored) {
        String result = "";
        
        result += "11\n";
        
        int[] descRowLength = new int[Map.COL];
        for (int descI = 0; descI < Map.COL; descI++) {
            descRowLength[descI] = 0;
        }
        
        for (int descI = 0; descI < Map.COL; descI++) {
            for (int descJ = 0; descJ < Map.ROW; descJ++) {
                result += explored[descJ][descI] ? "1" : "0";
                descRowLength[descI] += explored[descJ][descI] ? 1 : 0;
            }
            result += "\n";
        }
        
        result += "11\n";
        
        for (int descI = 0; descI < Map.COL; descI++) {
            for (int descJ = 0; descJ < descRowLength[descI]; descJ++) {
                boolean isObstacle = map
                    .getPoint(new GridVector(Map.ROW - 1 - descJ, Map.COL - 1 - descI))
                        .obstacleState().equals(WPObstacleState.IsActualObstacle);
                result += isObstacle ? "1" : "0";
            }
            result += "\n";
        }
        
        return result;
    }    
    
    public static void saveToFile(String filePath, Map map, boolean[][] explored) throws IOException {
        PrintWriter writer;
        
        if (filePath.contains("\\")) {
            writer = new PrintWriter(filePath);
        } else {
            writer = new PrintWriter(_getFilePath(filePath));
        }
        
        writer.write(stringify(map, explored));
        writer.close();
    }
    
    public static String readFile(String filePath) throws IOException {
        String result = "";
        
        File file;
        if (filePath.contains("//")) {
            file = new File(filePath);
        } else {
            file = new File(_getFilePath(filePath));
        }
        
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                result += scanner.nextLine() + "\n";
            }
        }
        
        return result;
    }
    
    public static Map parseFromFile(String filePath) throws IOException {
        Map result = new Map();
        
        File file;
        if (filePath.contains("//")) {
            file = new File(filePath);
        } else {
            file = new File(_getFilePath(filePath));
        }
        
        try (Scanner scanner = new Scanner(file)) {
            ArrayList<ArrayList<GridVector>> descExplored = new ArrayList<>();
            
            scanner.nextLine();
            
            // NOTE: descriptor format switch i & j coordinates
            // part 1
            for (int descI = 0; descI < Map.COL; descI++) {
                String curLine = scanner.nextLine();
                ArrayList<GridVector> descRow = new ArrayList<>();
                for (int descJ = 0; descJ < curLine.length(); descJ++) {
                    if (curLine.charAt(descJ) == '1') {
                        descRow.add(new GridVector(descI, descJ));
                    }
                }
                descExplored.add(descRow);
            }
            
            scanner.nextLine();
            
            // part2
            descExplored.forEach((descRow) -> {
                String curLine = scanner.nextLine();
                for (int descJ = 0; descJ < curLine.length(); descJ++) {
                    if (curLine.charAt(descJ) == '1') {
                        GridVector descPos = descRow.get(descJ);
                        result.addObstacle(new GridVector(
                            Map.ROW - 1 - descPos.y(),
                            Map.COL - 1 - descPos.x()
                        ));
                    }
                }
            });
        }
        
        return result;
    }
    
}
