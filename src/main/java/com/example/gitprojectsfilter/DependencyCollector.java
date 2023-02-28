package com.example.gitprojectsfilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import picocli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DependencyCollector {

    private static HashMap<String, Integer> jarUsage;
    private static HashMap<String, Set<String>> dependencyUsage;
    private static HashSet<String> set;

    private static HashSet<String> largeProjects;

    @CommandLine.Option(names = {"-a",
            "--artifact"}, paramLabel = "ARTIFACT")

    static String artifact;

    public static void main(String[] args) {

        List<Project> projects = readProjects(args[1]);
        jarUsage = new HashMap<>();
        largeProjects = new HashSet<>();
        set = new HashSet<>();
        dependencyUsage = new HashMap<>();

        for (Project p : projects) {
            String folderName =
//                    DownloadHelper.downloadAndUnzip(p);
                    gitDownload(p);
            if (Objects.equals(folderName, "")) continue;
            if (!getJarToCoordMap(folderName)) {
                set.add(folderName);
            }

        }
        try {
            writeHashMapToCsv();
            writeHashSetToCsv();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Project> readProjects(String filepath) {
        // 读取文件
        String json = readFile(filepath);
        // 解析项目列表
        ObjectMapper mapper = new ObjectMapper();
        List<Project> projects = new ArrayList<>();
        try {
            projects.addAll(mapper.readValue(json, new TypeReference<List<Project>>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("projects not found");
        }

        return projects;
    }

    public static String gitDownload(Project project) {

        String path = Constants.projectFolder + project.getName();
//        String projectFolder = "/Users/ljystu/Desktop/projects/";
//        String path = "/Users/ljystu/Desktop/projects/" + project.getName();
        try {

            String cloneCommand = "git clone " + project.getRepoUrl() + " " + project.getName();
            System.out.println("git cloning " + project.getName());
            Process cloneProcess = Runtime.getRuntime().exec(cloneCommand, null, new File(Constants.projectFolder));
            cloneProcess.waitFor();
            cloneProcess.destroy();

//
//            String cdCommand = "cd " + path;
//            Process cdProcess = Runtime.getRuntime().exec(cdCommand);
//            cdProcess.waitFor();
////
////            //check release
//            String describeCommand = "git for-each-ref refs/tags --sort=-taggerdate --format '%(refname:short)' | head";
//            Process describeProcess = Runtime.getRuntime().exec(describeCommand, null, new File(path));
//            BufferedReader describeInput = new BufferedReader(new InputStreamReader(describeProcess.getInputStream()));
//            String describeOutput = describeInput.readLine();
//
//
//            describeProcess.waitFor();
//            describeInput.close();
//
//            if (describeOutput == null || describeOutput.startsWith("fatal")) {
//                return path;
//            }
//
//            System.out.println("Latest tag: " + describeOutput);
//
//            String switchCommand = "git checkout " + describeOutput.substring(1, describeOutput.length() - 1);
//            Process switchProcess = Runtime.getRuntime().exec(switchCommand, null, new File(path));
//            BufferedReader checkInput = new BufferedReader(new InputStreamReader(switchProcess.getInputStream()));
//            String line;
//            while ((line = checkInput.readLine()) != null) {
//                System.out.println(line);
//            }
//            switchProcess.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return path;
    }

    // 读取文件内容
    private static String readFile(String filepath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            System.out.println("Failed to read file:" + filepath);
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static void writeHashMapToCsv() throws IOException {

        StringWriter output = new StringWriter();
        CSVWriter writer = new CSVWriter(new FileWriter(Constants.outputCsv));
        for (Map.Entry<String, Integer> entry : jarUsage.entrySet()) {
            String[] data = {entry.getKey(), String.valueOf(entry.getValue())};
            writer.writeNext(data);
        }

        writer = new CSVWriter(new FileWriter(Constants.outputDependencyUsageCsv, true));
        for (String key : dependencyUsage.keySet()) {
            Set<String> record = dependencyUsage.get(key);
            String[] recordArray = new String[record.size()];
            recordArray = record.toArray(recordArray);
            writer.writeNext(new String[]{key, Arrays.toString(recordArray)});
        }
        writer.close();

        System.out.println(output);
    }

    public static void writeHashSetToCsv() throws IOException {

        StringWriter output = new StringWriter();
        CSVWriter writer = new CSVWriter(new FileWriter(
                Constants.projectsCsv, true));
        for (String project : set) {

            writer.writeNext(new String[]{project});
        }
        writer.close();

        System.out.println(output);
    }

    public static boolean getJarToCoordMap(String rootPath) {

        System.out.println(rootPath);
        Set<String> dependencies = new HashSet<>();
        boolean errorFound = false;
        try {

            String dependencyList = execCmd(" mvn dependency:list -T 4｜", rootPath);
            if (dependencyList == null) {
                return true;
            }
            String[] lines = dependencyList.split("\n");
            Pattern pattern = Pattern.compile("    (.*):(compile|runtime|test)");
            Pattern errorPattern = Pattern.compile("(FAILURE|ERROR).*");


            for (String line : lines) {
                if (line == null) continue;
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String info = matcher.group(1);
                    dependencies.add(info);
                }
                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) {
                    errorFound = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println(rootPath + " dependency size:" + dependencies.size());
//        if (dependencies.size() == 0) {
//            errorFound = false;
//
//            try {
////                switchBranch("git for-each-ref refs/tags --sort=-taggerdate --format '%(refname:short)' | head", rootPath);
//
////                switchBranch("git for-each-ref --sort=-committerdate refs/heads/ --format='%(refname:short)' --count=1", rootPath);
//                String dependencyList = execCmd(" mvn dependency:list -T 4 ", rootPath);
//                if (dependencyList == null) {
//                    return true;
//                }
//                String[] lines = dependencyList.split("\n");
//                Pattern pattern = Pattern.compile("    (.*):(compile|runtime|test)");
//                Pattern errorPattern = Pattern.compile("(FAILURE|ERROR).*");
//
//
//                for (String line : lines) {
//                    if (line == null) continue;
//                    Matcher matcher = pattern.matcher(line);
//                    if (matcher.find()) {
//                        String info = matcher.group(1);
//                        dependencies.add(info);
//                    }
//                    Matcher errorMatcher = errorPattern.matcher(line);
//                    if (errorMatcher.find()) {
//                        errorFound = true;
//                    }
//
//                }
//                System.out.println(rootPath + " dependency size:" + dependencies.size());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        dependencies.clear();
        if (dependencies.isEmpty()) {
            DownloadHelper.deleteFile(new File(rootPath));
        }
        extractCoordinate(rootPath, dependencies);
        return errorFound;
    }

    private static boolean switchBackToMaster(String describeCommand, String rootPath) throws IOException, InterruptedException {

        Process describeProcess = Runtime.getRuntime().exec(describeCommand, null, new File(rootPath));
        BufferedReader describeInput = new BufferedReader(new InputStreamReader(describeProcess.getInputStream()));
        String describeOutput = describeInput.readLine();


        describeProcess.waitFor();
        describeInput.close();


        if (describeOutput == null || describeOutput.startsWith("fatal")) {
            return true;
        }
        return false;
    }

    private static boolean switchBranch(String describeCommand, String path) throws IOException, InterruptedException {

        Process describeProcess = Runtime.getRuntime().exec(describeCommand, null, new File(path));
        BufferedReader describeInput = new BufferedReader(new InputStreamReader(describeProcess.getInputStream()));
        String describeOutput = describeInput.readLine();


        describeProcess.waitFor();
        describeInput.close();

        if (describeOutput == null || describeOutput.startsWith("fatal")) {
            return false;
        }

        System.out.println("tag: " + describeOutput);

        String switchCommand = "git checkout " + describeOutput.substring(1, describeOutput.length() - 1);
        Process switchProcess = Runtime.getRuntime().exec(switchCommand, null, new File(path));
        BufferedReader checkInput = new BufferedReader(new InputStreamReader(switchProcess.getInputStream()));
        String line;
        while ((line = checkInput.readLine()) != null) {
            System.out.println(line);
        }
        switchProcess.waitFor();
        checkInput.close();
        return true;
    }

    public static String execCmd(String cmd, String dir) {
        StringBuilder result = new StringBuilder();
        String[] command = new String[]{"/bin/sh", "-c", "mvn dependency:list -Dmaven.javadoc.skip=true -DincludeScope=runtime -T 4 | grep 'compile\\|test'"};

//        String[] env = new String[]{"JAVA_HOME=" +
//                Constants.JAVA_HOME};

//        if (dir == null) return result;
//        try (InputStream inputStream = Runtime.getRuntime().exec(cmd,null, new File(dir)).getInputStream(); Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
//            result = s.hasNext() ? s.next() : null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(dir));
            Process process = processBuilder.start();
            boolean b = process.waitFor(30, TimeUnit.SECONDS);
            System.out.println("mvn dependency list");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            if (!b) {
                largeProjects.add(dir.substring(dir.lastIndexOf("/")));
                System.out.println(dir + " TTL");
                DownloadHelper.deleteFile(new File(dir));
                result.setLength(0);
            }
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void extractCoordinate(String path, Set<String> dependencies) {
        HashSet<String> dependenciesUsed = new HashSet<>();
        for (String dependency : dependencies) {
            String[] split = dependency.split(":");
            if (split.length != 4) continue;
            String artifactId = split[1];

            String coordinate = split[0] + ":" + artifactId + ":" + split[3];
            jarUsage.put(coordinate, jarUsage.getOrDefault(coordinate, 0) + 1);
            dependenciesUsed.add(coordinate);
        }
        dependencyUsage.put(path, dependenciesUsed);

    }


}

