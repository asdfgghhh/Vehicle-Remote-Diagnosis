package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.service.HdfsService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class HdfsServiceImpl implements HdfsService {

    @Value("${hadoop.namenode}")
    private String namenode;

    @Value("${hadoop.user}")
    private String user;

    private FileSystem getFileSystem() throws Exception {
        Configuration config = new Configuration();
        config.set("fs.defaultFS", namenode);
        return FileSystem.get(URI.create(namenode), config, user);
    }

    @Override
    public void saveToHdfs(String data, String path) {
        try (FileSystem fs = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            Path parent = hdfsPath.getParent();
            if (parent != null && !fs.exists(parent)) {
                fs.mkdirs(parent);
            }
            
            try (OutputStream os = fs.create(hdfsPath);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                writer.write(data);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save to HDFS: " + e.getMessage(), e);
        }
    }

    @Override
    public String readFromHdfs(String path) {
        try (FileSystem fs = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fs.exists(hdfsPath)) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            try (InputStream is = fs.open(hdfsPath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from HDFS: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listFiles(String directory) {
        List<String> files = new ArrayList<>();
        try (FileSystem fs = getFileSystem()) {
            Path path = new Path(directory);
            
            if (!fs.exists(path)) {
                return files;
            }
            
            FileStatus[] status = fs.listStatus(path);
            for (FileStatus file : status) {
                files.add(file.getPath().toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
        return files;
    }

    @Override
    public void deleteFromHdfs(String path) {
        try (FileSystem fs = getFileSystem()) {
            fs.delete(new Path(path), true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from HDFS: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        try (FileSystem fs = getFileSystem()) {
            return fs.exists(new Path(path));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getFileSize(String path) {
        try (FileSystem fs = getFileSystem()) {
            Path hdfsPath = new Path(path);
            if (fs.exists(hdfsPath)) {
                return fs.getFileStatus(hdfsPath).getLen();
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file size: " + e.getMessage(), e);
        }
    }

    @Override
    public void createDirectory(String path) {
        try (FileSystem fs = getFileSystem()) {
            Path dirPath = new Path(path);
            if (!fs.exists(dirPath)) {
                fs.mkdirs(dirPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create directory: " + e.getMessage(), e);
        }
    }
}
