package com.example.gitprojectsfilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadHelper {
    public static String downloadAndUnzip(Project project) {
        // 下载项目的zip文件
        URL url = null;
        String folderName = "";
        try {
            url = new URL(project.getRepoUrl() + Constants.PROJECT_LINK);

            File zipFile = new File(project.getName() + ".zip");
            Files.copy(url.openStream(), Paths.get(zipFile.getPath()));
            // 解压zip文件
            folderName = unzip(zipFile);
            // 删除zip文件
            zipFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Constants.projectFolder + folderName;
    }

    private static String unzip(File zipFile) throws IOException {
        String name = "";
        boolean flag = false;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(Constants.projectFolder + entry.getName());
                if (file.exists()) continue;
                if (!flag) {
                    name = entry.getName().substring(0, entry.getName().length() - 1);
                    flag = true;
                }
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    Files.copy(zis, Paths.get(file.getPath()));
                }
            }
        }
        return name;
    }

    public static void deleteFile(File dirFile) {

        if (!dirFile.exists()) {
            return;
        }

        if (dirFile.isFile()) {
            dirFile.delete();
            return;
        } else {

            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }

        dirFile.delete();
    }
}
