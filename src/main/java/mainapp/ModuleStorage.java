package mainapp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleStorage {

    private static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "modules.txt";

    public static class Module {
        public String title;
        public String startDate;
        public String endDate;
        public String sModules;

        public Module(String title, String startDate, String endDate, String sModules) {
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.sModules = sModules;
        }
    }

    public static void saveModule(Module module) throws IOException { // saves as a text file: module.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(module.title + "|" + module.startDate + "|" + module.endDate + "|" + module.sModules);
            writer.newLine();
        }
    }

    public static List<Module> loadModules() {
        List<Module> modules = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return modules;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    modules.add(new Module(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modules;
    }
}
