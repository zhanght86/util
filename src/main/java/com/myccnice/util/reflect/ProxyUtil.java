package com.myccnice.util.reflect;

import java.io.FileOutputStream;

import sun.misc.ProxyGenerator;

public class ProxyUtil {

    /**
     * 创建代理文件
     * @param clazz 文件类型对象
     * @param name 文件保存全路径名
     */
    public static void createProxyClassFile(Class<?> clazz, String name) {
        byte[] data = ProxyGenerator.generateProxyClass( name, new Class[] { clazz} );
        try (FileOutputStream out = new FileOutputStream(name + ".class" )){
            out.write( data );
            out.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
}
