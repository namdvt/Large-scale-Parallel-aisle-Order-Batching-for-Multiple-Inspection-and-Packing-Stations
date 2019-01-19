import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nam on 12/6/2016.
 */
public class FileReaderHelper {
    private static int numPosition = 60;
    private File file;

    public String getFileName() {
        return file.getName();
    }

    public FileReaderHelper(String filePath) {
        this.file = new File(filePath);
    }

    public FileReaderHelper() {
    }

    public List<int[]> readOrderPathListFromFile() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<int[]> orderPathList = new ArrayList<int[]>();
        String line;
        int i  =0;
        int []orderPath = new int[6];
        boolean isVrda = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Vrda")) {
                isVrda = true;
            }
            if(isVrda) {
                for (char ch : line.toCharArray()) {
                    if (Character.isDigit(ch)) {
                        orderPath[i] = Integer.parseInt(String.valueOf(ch));
                        i++;
                        if (i==6) {
                            orderPathList.add(orderPath);
                            orderPath = new int[6];
                            i = 0;
                        }
                    }
                }
            }
            if (isVrda && line.startsWith("],")) {
                isVrda= false;
            }
        }
        bufferedReader.close();
        return orderPathList;
    }

    public List<int[]> readPathPositionListFormFile(int depot) throws Exception {
        InputStream is = getClass().getResourceAsStream("/length_" + depot + "_depot.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        List<int[]> pathPositionList = new ArrayList<int[]>();
        String line;
        int i  =0;
        int []pathPosition = new int[6];
        boolean isVoa = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Lrda")) {
                isVoa = true;
            }
            if(isVoa) {
                if (line.contains("]];")) {
                    isVoa= false;
                }
                for (char ch : line.toCharArray()) {
                    if (Character.isDigit(ch)) {
                        pathPosition[i] = Integer.parseInt(String.valueOf(ch));
                        i++;
                        if (i==6) {
                            pathPositionList.add(pathPosition);
                            pathPosition = new int[6];
                            i = 0;
                        }
                    }
                }
            }

        }
        bufferedReader.close();
        return pathPositionList;
    }

    public int[] readLengthPathListFormFile(int depot) throws Exception {
        InputStream is = getClass().getResourceAsStream("/length_" + depot + "_depot.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        List<Integer> lengthPathList = new ArrayList<Integer>();
        String line;
        boolean isLengthPath = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("LT")) {
                isLengthPath = true;
            }
            if(isLengthPath) {
                if (line.contains("];")) {
                    isLengthPath = false;
                }
                String[] parsedLine = line.replace("[", "").replace("]", "").replace(";", "").replace("LT", "")
                        .replace("=", "").replace(" ", "").split(",");
                for (int i = 0; i < parsedLine.length; i++) {
                    lengthPathList.add(Integer.parseInt(parsedLine[i]));
                }
            }
        }
        bufferedReader.close();
        return convertIntegers(lengthPathList);
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    public List<int[]> readOofListFromFile() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<int[]> orderPathList = new ArrayList<int[]>();
        String line;
        int i  =0;
        int []orderPath = new int[numPosition];
        boolean isOof = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Oof")) {
                isOof = true;
            }
            if(isOof) {
                for (char ch : line.toCharArray()) {
                    if (Character.isDigit(ch)) {
                        orderPath[i] = Integer.parseInt(String.valueOf(ch));
                        i++;
                        if (i==numPosition) {
                            orderPathList.add(orderPath);
                            orderPath = new int[numPosition];
                            i = 0;
                        }
                    }
                }
            }
            if (isOof && line.contains(";")) {
                isOof= false;
            }
        }
        bufferedReader.close();
        return orderPathList;
    }

    public int[] readCapaFromFile() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<Integer> capaList = new ArrayList<Integer>();
        String line;
        boolean isCapa = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Capa")) {
                isCapa = true;
            }
            if (isCapa) {
                String[] parsedLine = line.split("=")[1].replace("[", "").replace("]","")
                        .replace(";","").replace(" ", "").split(",");
                for (int i = 0; i < parsedLine.length; i++) {
                    capaList.add(Integer.parseInt(parsedLine[i]));

                }
                if (isCapa && line.contains(";")) {
                    isCapa = false;
                }
            }
        }
        bufferedReader.close();
        return convertIntegers(capaList);
    }

    public int readNumOrder() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int numOrder = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains("numOrder")) {
                numOrder = Integer.parseInt(line.split("=")[1].replace(" ",""));
            }
        }
        return numOrder;
    }

    public int readNumAisle() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int numOrder = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains("numAisle")) {
                numOrder = Integer.parseInt(line.split("=")[1].replace(" ",""));
            }
        }
        return numOrder;
    }

    // for CPLEX (position of oders)
    public List<int[]> readVrda() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        List<int[]> Vrda = new ArrayList<int[]>();
        int [] element = new int[6];
        boolean isVrda = false;
        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains("Vrda")) {
                isVrda = true;
            }
            if (isVrda) {
                for (char ch : line.toCharArray()) {
                    if (Character.isDigit(ch)) {
                        element[i] = Integer.parseInt(String.valueOf(ch));
                        i++;
                        if (i==6) {
                            Vrda.add(element);
                            element = new int[6];
                            i = 0;
                        }
                    }
                }
            }
            if (isVrda && line.startsWith("],")) {
                isVrda= false;
            }
        }
        return Vrda;
    }
}
