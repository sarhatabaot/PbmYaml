package fr.medialo.api.pbmyaml;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PbmYaml {

    private final DumperOptions yamlOptions = new DumperOptions();
    //    private final LoaderOptions loaderOptions = new LoaderOptions();
    //    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml;
    private Map<String,Object> values;
    private File file;
    private List<String> footer;
    private List<String> header;

    public PbmYaml() {
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlOptions.setPrettyFlow(true);
        this.yaml = new Yaml(yamlOptions);
    }

    public void load(File file){
        this.file=file;
        try ( BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file))){
            this.values = this.yaml.load(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearFooter(){
        this.footer.clear();
    }

    public void setFooter(List<String> list){
        this.footer = list;
    }

    public void addFooter(String... str){
        if (this.footer == null)
            this.footer = new ArrayList<>();
        this.footer.addAll(Arrays.asList(str));
    }


    public void clearHeader(){
        this.header.clear();
    }

    public void setheader(List<String> list){
        this.header = list;
    }

    public void addHeader(String... str){
        if (this.header == null)
            this.header = new ArrayList<>();
        this.header.addAll(Arrays.asList(str));
    }


    public void createFileAndParents(File file){
        createFileAndParents(file.toPath(),file.getName());
    }

    public void createFileAndParents(File file, String fileName){
        createFileAndParents(file.toPath(),fileName);
    }

    public void createFileAndParents(File getDataFolder,String parentsFolder, String fileName){
        createFileAndParents(new File(getDataFolder,parentsFolder).toPath(),fileName);
    }

    public void createFileAndParents(Path path,String fileName){
        try {
            if(!Files.exists(path))
                Files.createDirectories(path);
            if(!Files.exists(path.resolve(fileName)))
                Files.createFile(path.resolve(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload(){
        load(this.file);
    }

    //Idea in progress ...
//    public boolean validate(){
//        FileInputStream fi = null;
//        InputStream stream = null;
//        try {
//            fi = new FileInputStream(file);
//            stream = new BufferedInputStream(fi);
////             while (int )
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if(fi != null)
//                    fi.close();
//                if(stream != null)
//                    stream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return  true;
//    }


    public void save(){
        Map<String, List<String>> commentsList = new HashMap<>();
        List<String> lineToWrite = null;
        try {
            List<String> footer = new ArrayList<>();
            List<String> commentsTemp = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\b.+[:]");
            Matcher matcher ;
            List<String> lines = Files.readAllLines(this.file.toPath());
            for (String line : lines) {
                String ll = line.trim();
                matcher = pattern.matcher(ll);
                if (ll.startsWith("#")){
                    commentsTemp.add(ll);
                } else if (matcher.find() && !commentsTemp.isEmpty() ) {
                    commentsList.put( StringUtils.chop(matcher.group(0)),new ArrayList<>(commentsTemp));
                    System.out.println(commentsTemp.size());
                    commentsTemp.clear();
                }
            }
            if(!commentsTemp.isEmpty()){
                footer = commentsTemp;

            }
//-------------------------------------------------------------
            lineToWrite = new ArrayList<>(Arrays.asList(yaml.dump(this.values).split("\n")));
            for (int i = 0; i < lineToWrite.size(); i++){
                String line = lineToWrite.get(i);
                matcher = pattern.matcher(line);
                if(matcher.find()){
                    String key = StringUtils.chop(matcher.group(0));
                    List<String> commentsLines = commentsList.get(key);
                    if (commentsLines != null){
                        lineToWrite.addAll(i,commentsLines);
                        commentsList.remove(key);
                        i += commentsLines.size()-1;
                    }
                }
            }
            lineToWrite.addAll(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (lineToWrite != null) {
            if(this.header != null){
                this.header.replaceAll(this::commentCheck);
                lineToWrite.addAll(0,this.header);
            }
            if(this.footer != null){
                this.footer.replaceAll(this::commentCheck);
                lineToWrite.addAll(this.footer);
            }
            try {
                Files.write(file.toPath(), lineToWrite, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void set(String url,Object value){
            this.values.replace(url,value);
    }

    //Not tested yet
    public void setOrAdd(String url,Object value){
        this.values.put(url,value);
    }

    public void remove(String url){
        this.values.remove(url);
    }

    public boolean getBoolean(String url){
        return this.values.get(url) != null && (boolean) this.values.get(url);
    }


    public float getFloat(String url){
        return this.values.get(url) == null ? 0.0f : (float) this.values.get(url);
    }

    public double getDouble(String url){
        return this.values.get(url) == null ? 0.0 : (double) this.values.get(url);
    }


    public char getChar(String url){
        return this.values.get(url) == null ? '0' : (char) this.values.get(url);
    }

    public byte getByte(String url){
        return this.values.get(url) == null ? -1 : (byte) this.values.get(url);
    }

    public short getShort(String url){
        return this.values.get(url) == null ? 0 : (short) this.values.get(url);
    }

    public int getInt(String url){
        return this.values.get(url) == null ? 0 : (int) this.values.get(url);
    }

    public long getLong(String url){
        return this.values.get(url) == null ?  0 : (long) this.values.get(url);
    }

    public String getString(String url){
        return this.values.get(url) == null ? null : (String) this.values.get(url);
    }


    public  List<?> getList(String url){
        return this.values.get(url) == null ? null : (List<?>) this.values.get(url);
    }


    public Object getObject(String url){
        return this.values.get(url);
    }

    public <T> T getOrDefault(String url, T object){
        return this.values.get(url) == null ? object : (T) this.values.get(url);
    }

    private String commentCheck(String str){
        return (!str.startsWith("#")) ? "#"+str : str;

    }


    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public void addValues(String url, Map<String, Object> values){
        values.forEach((s, o) -> this.values.put(url+s,o));
    }

}
