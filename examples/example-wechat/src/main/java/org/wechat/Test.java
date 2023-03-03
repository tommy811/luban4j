package org.wechat;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class Test {
    public static void main(String[] args) throws IOException {
        File root = new File("/Users/tommy/temp/Luban-js/public/heads/");
        File[] files = root.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith("png"))
                Files.move(files[i].toPath(), new File(root, i + ".png").toPath());
        }
    }
}
