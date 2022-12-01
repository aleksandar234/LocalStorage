package org.raf.local_storage;

import org.raf.specification.Specification;
import org.raf.storage.JsonConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LocalStorage implements Specification {

    private String storagePath;
    private final int sizeOfFile = 1024 * 1024;

    public LocalStorage(String storagePath){
        this.storagePath = storagePath;
    }

//    public static void main(String[] args) {
//        LocalStorage localStorage = new LocalStorage("C:\\Users\\Korisnik\\Desktop\\Cartman4");
////        localStorage.getFilesCreatedInCertainTime("children", "15-11-2022");
//        localStorage.init();
//
////        Ovako se radi sa String...
////        localStorage.getFilesWithName("children", "butters.txt", "kayl.txt");
//    }

    @Override
    public void init() {
        File file = new File(storagePath);
        File configFile = new File(storagePath + File.separator + "config.json");
        int s = sizeOfFile;
        if(!configFile.exists()) {
            file.mkdirs();
            JsonConfig.mkJsonFile(storagePath, s);
        } else {
            System.out.println("Connecting to existing folder");
        }
    }

    @Override
    public void makeDirs(int numberOfDirs) {
        File file = new File(storagePath);
        if(!file.exists()) {
            file.mkdirs();
        }
        for(int i = 0; i < numberOfDirs; i++) {
            File f = new File(file+File.separator+"s"+i);
            if(!f.exists()) {
                f.mkdir();
            }
        }
    }

    @Override
    public void setMaxFileSize(int size) {
        JsonConfig.changePropertyValueJson("max_file_size", size, storagePath);
    }

    @Override
    public void setBlockedExtensions(List<String> list) {
        JsonConfig.changePropertyArrayJson("blocked_extensions", list, storagePath);
    }

    /**
     * Download se vrsi tako sto hocemo nesto da downloadujemo iz naseg korenskog direktorijuma
     * na desktop. Moramo da specificiramo put koji vec postoji. Npr ako hocemo da ga smestimo
     * u neki folder na desktopu, morali bi da napravimo taj folder i da napisemo tacno putanju do tog
     * foldera a potom da damo ime tom fajlu kojieg smo skinuli npr =>
     * download("s2\\Joker.txt", "C:\\Users\\Korisnik\\Desktop\\MojFolder\\jb.txt")
     * @param corePath
     * @param newPath
     */
    @Override
    public void download(String corePath, String newPath) {
        File core = new File(storagePath, corePath);
        File newFile = new File(newPath);
        try {
            Files.copy(core.toPath(), newFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ovom metodom uploudejemo file sa desktop racunara na nas korenski repozitorijum
     * gde moramo da proverimo da li velicina fajla odgovara i da li extenzija odgovara
     * svi uslovima.
     * @param pathFrom
     * @param pathTo
     */
    @Override
    public void upload(String pathFrom, String pathTo) {
        long maxFileSize =(long) JsonConfig.getPropertyJson("max_file_size", storagePath);
        File fileToUpload = new File(pathFrom);
        File fileDownload = new File(storagePath, pathTo + File.separator + fileToUpload.getName());
        String fileExt = fileDownload.getName();
        String extension = fileExt.substring(fileExt.lastIndexOf(".") + 1);
        if(JsonConfig.isExtensionBlocked(extension, storagePath)) {
            System.out.println("This extension is blocked");
            return;
        }
        if(fileToUpload.length() > maxFileSize) {
            System.out.println("This file exceeds max file capacity");
            return;
        }
        try {
            Files.copy(fileToUpload.toPath(), fileDownload.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void rename(String oldName, String newName) {
        File oldNameFile = new File(storagePath, oldName);
        String filePath = storagePath + File.separator + oldName;
        String[] allFiles = filePath.split("\\\\");
        String file = "";
        for(int i = 0; i < allFiles.length-1; i++) {
            file += allFiles[i];
            file += File.separator;
        }
        File newFile = new File(file, newName);
        if(oldNameFile.renameTo(newFile)) {
            System.out.println("File got renamed");
        } else {
            System.out.println("Something went wrong, file did not rename");
        }
    }

    /**
     * Pravimo file na odredjenoj putanji,
     * ("s0\\aleksandar.txt")
     * @param filePath
     */
    @Override
    public void createFile(String filePath) {
        File file = new File(storagePath, filePath);
        try {
            file.createNewFile();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String fileToDelete) {
        File file = new File(storagePath, fileToDelete);
        file.delete();
    }

    @Override
    public void getAllFilesInDir(String directoryPath) {
        File dirPath = new File(storagePath, directoryPath);
        String[] files = dirPath.list();
        try {
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i]);
                File file = new File(storagePath, directoryPath + File.separator + files[i]);
                Path fPath = file.toPath();
                long time = file.lastModified();
                BasicFileAttributes attr = Files.readAttributes(fPath, BasicFileAttributes.class);
                DateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss");
                System.out.println("creationTime: " + sdf.format(time));
                System.out.println("size: " + attr.size());
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void recursivePrint(File[] arr, int index, int level) {
        // terminate condition
        if (index == arr.length)
            return;

        // tabs for internal levels
        for (int i = 0; i < level; i++)
            System.out.print("\t");

        // for files
        if (arr[index].isFile())
            System.out.println(arr[index].getName());

            // for sub-directories
        else if (arr[index].isDirectory()) {
            System.out.println("[" + arr[index].getName()
                    + "]");

            // recursion for sub-directories
            recursivePrint(arr[index].listFiles(), 0,
                    level + 1);
        }

        // recursion for main directory
        recursivePrint(arr, ++index, level);
    }

    /**
     * Uzima sve fajlove koji se nalaze u nekom direktorijumu,
     * a onda ako se u tom direktorijumu nalazi jos neki direktorijum,
     * ulazi i u taj direktorijum dok ne ispise sve fajlove iz tog direktorijuma,
     * i tako rekurzivno dok ne bude bilo vise direktorijuma
     * @param dirPath
     */

    @Override
    public void getAllFilesFromAllDirsInDir(String dirPath) {
        File mainDir = new File(storagePath, dirPath);
        if(mainDir.exists() && mainDir.isDirectory()) {
            File arr[] = mainDir.listFiles();
            recursivePrint(arr, 0, 0);
        }
    }


    @Override
    public void getFilesWithExtension(String pathToDir, String extension) {
        File dirPath = new File(storagePath, pathToDir);
        String[] files = dirPath.list();
        for(int i = 0; i < files.length; i++) {
            File f = new File(storagePath, pathToDir + File.separator + files[i]);
            if(!f.isDirectory()) {
                String ext = files[i].substring(files[i].lastIndexOf(".") + 1);
                if(ext.equals(extension)) {
                    System.out.println(files[i]);
                }
            }
        }
    }

    @Override
    public void getFilesWithSubstring(String pathToDir, String s) {
        File dirPath = new File(storagePath, pathToDir);
        String[] files = dirPath.list();
        for(int i = 0; i < files.length; i++) {
            File f = new File(storagePath, pathToDir + File.separator + files[i]);
            if(!f.isDirectory() && (f.getName().startsWith(s) || f.getName().endsWith(s) || f.getName().contains(s))) {
                System.out.println(files[i]);
            }
        }
    }

    // Vratiti da li odrednjeni direktorijum sadrzi fajl sa odredjenim imenom ili
    // vise fajlova sa zadatom listom imena
    @Override
    public boolean getFilesWithName(String rootToDir, String... fileNames) {
        File dirPath = new File(storagePath, rootToDir);
        int cnt = 0;
        if(dirPath.isDirectory()) {
            for(String s: fileNames){
                File file = new File(dirPath,File.separator + s);
                System.out.println(file.getPath());
                if(!file.exists()){
                    System.out.println("Does not exists");
                    return false;
                }
            }
        }
        System.out.println("Exists");
        return true;
    }

    private boolean searchFile(File file, String search) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                boolean found = searchFile(f, search);
                if (found){
                    return true;
                }
            }
        } else {
            if (search.equals(file.getName())) {
                System.out.println(file.getParentFile().getName());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getDirFromNameOfFile(String s1) {
        File file = new File(storagePath);
        boolean found = searchFile(file, s1);
        if(found){
            return true;
        }
        return false;
    }

    @Override
    public void getAllFilesSortedByName(String dirPath, String order) {
        File file = new File(storagePath, dirPath);
        List<File> f = Arrays.asList(file.listFiles());
        if(order.equals("ascending"))
            Collections.sort(f);
        else if(order.equals("descending"))
            Collections.sort(f, Collections.reverseOrder());
        for(File fs: f) {
            System.out.println(fs.getName());
        }
    }

    @Override
    public void getAllFilesSortedByDate(String pathToDir, String order) {
        File fileDir = new File(storagePath, pathToDir);
        File[] files = fileDir.listFiles();
        if(order.equals("ascending"))
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        else if(order.equals("descending"))
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for(File f: files) {
            System.out.println(f.getName());
        }
    }

    @Override
    public void getFilesCreatedInCertainTime(String pathDir, String date) {
        File dir = new File(storagePath, pathDir);
        File[] files = dir.listFiles();
        List<File> dates = new ArrayList();
        SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy");
        for(File f: files) {
            long lastModified = f.lastModified();
            String dd = sdformat.format(lastModified);
            if(date.equals(dd)) {
                dates.add(f);
            }
        }

        for(File f: dates) {
            System.out.println(f.getName());
        }
    }


}
