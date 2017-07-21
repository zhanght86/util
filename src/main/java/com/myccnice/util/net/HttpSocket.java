/**
 * 基于HTTP协议的Socket实现
 * 主要提供了基于HTTP协议的POST和GET方法
 * 默认采用UTF-8编码，超时30秒
 * URL地址，可以使用完整的地址
 * 例如：http://java.sun.com:80/docs/index.html?name=networking#DOWNLOADING
 * 例子：
 * HttpSocket httpSocket = new HttpSocket();
 * httpSocket.setUrl("http://ynwell.com"); // 设置访问的URL地址，可以不加http://
 * httpSocket.setIpAddress("61.139.8.100"); // 设置访问的IP地址，如果没有设置则使用域名解析的IP地址
 * httpSocket.setTimeout(60000); // 设置连接超时时间，默认30秒
 * httpSocket.setReceiveCharsetName("UTF-8"); // 设置接收数据的编码，默认UTF-8
 * httpSocket.doGet(); // 执行GET访问
 * httpSocket.getResponseHeader(); // 获取HTTP响应头
 * httpSocket.getVisit(); // 获取网站是否可以访问
 * httpSocket.getResponseData(); // 获取网站正文内容
 */
package com.myccnice.util.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.myccnice.util.PlatformUtils;

public final class HttpSocket {
    private static final Logger LOGGER = Logger.getLogger(HttpSocket.class);

    /** 换行符识别标志，默认为回车符+换行符 */
    public static final String CRLF = "\r\n";
    public static final String UTF8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final String HTTP_HEADER_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1)";
    public static final String HTTP_HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml,*/*";
    public static final String HTTP_HEADER_ACCEPT_LANGUAGE = "zh-CN";
    public static final Pattern HEADER_CHARSET_PATTERN = Pattern.compile("(.*)charset=(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern CHARSET_PATTERN = Pattern.compile("(.*)<meta[^>]*?charset=[\"|']?(gb2312|utf-8|gbk|gb_2312-80)[\\s|\\S]*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern TITLE_PATTERN = Pattern.compile(".*<(title).*>(.*)</(title)>.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** URL地址 */
    private String url;

    /** HTTP头信息，可以额外添加HTTP头，默认是空字符串 */
    private String header = "";

    /** 连接超时，默认30秒 */
    private int timeout = 30 * 1000;

    /** POST 数据 */
    private Map<String, String> postData = new HashMap<>();

    /** 附件数据 */
    private Map<String, Attachment> attachmentData = new HashMap<>();

    private String ipAddress;

    /** 发送数据所使用的编码 */
    private String sendCharsetName = UTF8;

    /** 接收数据所使用的编码 */
    private String receiveCharsetName = UTF8;

    /** Socket接收数据 */
    private String socketReceive = "";
    private byte[] socketReceiveByte;

    /** 是否发送重定向，如果该值为false，则不处理重定向情况，反之，则处理存在重定向的情况;默认为不处理 */
    private boolean sendRedirect = false;

    /** 响应头--响应地址 */
    private String responseHeaderHttpVersion = "";

    /** 响应头--响应码 */
    private String responseHeaderCode = "";

    /** 响应头--响应服务器名称 */
    private String responseHeaderServer = "";

    /** 响应头--响应地址 */
    private String responseHeaderLocation = "";

    public HttpSocket() {
    }

    public HttpSocket(String url, Map<String, String> postData) {
        setUrl(url);
        this.postData = postData;
    }

    /**
     * 基于HTTP协议的POST实现
     * @param post 报文内容
     * @throws Exception
     */
    public void doPost(String post) throws Exception {
        URL aURL = new URL(url);

        // 构造HTTP协议
        StringBuilder http = new StringBuilder(2048);
        http.append("POST ").append(!(aURL.getPath() == null) ? "/" : aURL.getFile()).append(" HTTP/1.0").append(CRLF);
        http.append("Host: ").append(getHostFromUrl(aURL)).append(CRLF);
        http.append("Accept: ").append(HTTP_HEADER_ACCEPT).append(CRLF);
        http.append("Accept-Language: ").append(HTTP_HEADER_ACCEPT_LANGUAGE).append(CRLF);
        http.append("User-Agent: ").append(HTTP_HEADER_USER_AGENT).append(CRLF);
        http.append("Content-Type: application/x-www-form-urlencoded").append(CRLF);
        http.append("Content-Length: ").append(post.length()).append(CRLF);
        http.append(header);
        http.append("Connection: close").append(CRLF);
        http.append(CRLF);
        http.append(post);
        http.append(CRLF);
        LOGGER.debug(http.toString());
        doSocket(aURL, http.toString().getBytes());
        // 解析响应数据
        parseResponseHeader();
        // 处理重定向问题
        sendRedirect();
    }

    /**
     * 基于HTTP协议的POST实现
     *
     * @throws Exception
     */
    public void doPost() throws Exception {
        URL aURL = new URL(url);

        String post = buildPostFormat(postData);

        // 构造HTTP协议
        StringBuilder http = new StringBuilder(2048);
        http.append("POST ").append(!PlatformUtils.hasText(aURL.getPath()) ? "/" : aURL.getFile()).append(" HTTP/1.0").append(CRLF);
        http.append("Host: ").append(getHostFromUrl(aURL)).append(CRLF);
        http.append("Accept: ").append(HTTP_HEADER_ACCEPT).append(CRLF);
        http.append("Accept-Language: ").append(HTTP_HEADER_ACCEPT_LANGUAGE).append(CRLF);
        http.append("User-Agent: ").append(HTTP_HEADER_USER_AGENT).append(CRLF);
        http.append("Content-Type: application/x-www-form-urlencoded").append(CRLF);
        http.append("Content-Length: ").append(post.length()).append(CRLF);
        http.append(header);
        http.append("Connection: close").append(CRLF);
        http.append(CRLF);
        http.append(post);
        http.append(CRLF);
        LOGGER.debug(http.toString());
        doSocket(aURL, http.toString());
        // 解析响应数据
        parseResponseHeader();
        // 处理重定向问题
        sendRedirect();
    }

    /**
     * 基于HTTP协议的POST实现（带附件信息）
     *
     * @throws Exception
     */
    public void doPostMultipart() throws Exception {
        URL aURL = new URL(url);

        // 二个中横线，表示POST数据开始与结束
        String twoHyphens = "--";

        // POST数据之间的分界线
        String boundary = "---------------------------";

        ByteArrayOutputStream outPostData = new ByteArrayOutputStream();
        boolean hasPostData = false;

        // POST数据
        StringBuilder builder = new StringBuilder(1024);
        for (Map.Entry<String, String> entry : postData.entrySet()) {
            if (PlatformUtils.hasText(entry.getValue())) {
                hasPostData = true;
                builder.setLength(0);

                // POST开始分界线
                builder.append(twoHyphens);
                builder.append(boundary);
                builder.append(CRLF);

                // POST数据KEY部分
                builder.append("Content-Disposition: form-data; name=\"");
                builder.append(entry.getKey());
                builder.append("\"");
                builder.append(CRLF);
                builder.append(CRLF);

                outPostData.write(builder.toString().getBytes(sendCharsetName));
                outPostData.write(entry.getValue().getBytes(sendCharsetName));
                outPostData.write(CRLF.getBytes(sendCharsetName));
            }
        }

        // 附件信息
        if (attachmentData != null) {
            for (Map.Entry<String, Attachment> entry : attachmentData.entrySet()) {
                hasPostData = true;
                builder.setLength(0);

                // POST开始分界线
                builder.append(twoHyphens);
                builder.append(boundary);
                builder.append(CRLF);

                // POST数据KEY部分
                builder.append("Content-Disposition: form-data; name=\"");
                builder.append(entry.getKey());
                builder.append("\"; filename=\"");
                builder.append(entry.getValue().getFileName());
                builder.append("\"");
                builder.append(CRLF);
                builder.append("Content-Type: ");
                builder.append(entry.getValue().getContentType());
                builder.append(CRLF);
                builder.append(CRLF);

                outPostData.write(builder.toString().getBytes(sendCharsetName));
                outPostData.write(entry.getValue().getFileData());
                outPostData.write(CRLF.getBytes(sendCharsetName));
            }
        }

        if (hasPostData) {
            // POST数据完成分界线
            builder.setLength(0);
            builder.append(twoHyphens);
            builder.append(boundary);
            builder.append(twoHyphens);
            builder.append(CRLF);
            outPostData.write(builder.toString().getBytes(sendCharsetName));
        }

        // 构造HTTP协议
        ByteArrayOutputStream outHttpData = new ByteArrayOutputStream();
        builder.setLength(0);
        builder.append("POST ").append(!PlatformUtils.hasText(aURL.getPath()) ? "/" : aURL.getFile()).append(" HTTP/1.0").append(CRLF);
        builder.append("Host: ").append(getHostFromUrl(aURL)).append(CRLF);
        builder.append("Accept: ").append(HTTP_HEADER_ACCEPT).append(CRLF);
        builder.append("Accept-Language: ").append(HTTP_HEADER_ACCEPT_LANGUAGE).append(CRLF);
        builder.append("User-Agent: ").append(HTTP_HEADER_USER_AGENT).append(CRLF);
        builder.append("Content-Type: multipart/form-data; boundary=").append(boundary).append(CRLF);
        builder.append("Content-Length: ").append(outPostData.size()).append(CRLF);
        builder.append(header);
        builder.append("Connection: close").append(CRLF);
        builder.append(CRLF);

        // 将POST数据流附加到HTTP数据流
        outHttpData.write(builder.toString().getBytes(sendCharsetName));
        outPostData.writeTo(outHttpData);
        outHttpData.write(CRLF.getBytes(sendCharsetName));
        LOGGER.debug(builder.toString());
        doSocket(aURL, outHttpData.toByteArray());
    }

    /**
     * 基于HTTP协议的GET实现
     *
     * @throws Exception
     */
    public void doGet() throws Exception {
        URL aURL = new URL(url);

        // 构造HTTP协议
        StringBuilder http = new StringBuilder(1024);
        http.append("GET ").append(!PlatformUtils.hasText(aURL.getPath()) ? "/" : aURL.getFile()).append(" HTTP/1.0").append(CRLF);
        http.append("Host: ").append(getHostFromUrl(aURL)).append(CRLF);
        http.append("Accept: ").append(HTTP_HEADER_ACCEPT).append(CRLF);
        http.append("Accept-Language: ").append(HTTP_HEADER_ACCEPT_LANGUAGE).append(CRLF);
        http.append("User-Agent: ").append(HTTP_HEADER_USER_AGENT).append(CRLF);
        http.append(header);
        http.append("Connection: close").append(CRLF);
        http.append(CRLF);
        LOGGER.debug(http.toString());
        doSocket(aURL, http.toString());
    }

    /**
     * 实现基础Socket连接
     */
    private void doSocket(URL aURL, String httpString) throws Exception {
        doSocket(aURL, httpString.getBytes());
    }

    /**
     * 实现基础Socket连接
     */
    private void doSocket(URL aURL, byte[] httpByte) throws Exception {
        Socket socket = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            if (ipAddress == null) {
                ipAddress = IDN.toASCII(aURL.getHost());
            }
            socket = new Socket(ipAddress, aURL.getPort() == -1 ? 80 : aURL.getPort());
            socket.setSoTimeout(timeout);
            socket.getOutputStream().write(httpByte);
            socket.getOutputStream().flush();
            inputStream = new BufferedInputStream(socket.getInputStream());
            baos = new ByteArrayOutputStream();
            int i;
            while ((i = inputStream.read()) != -1) {
                baos.write(i);
            }
            socketReceiveByte = baos.toByteArray();
            socketReceive = baos.toString(receiveCharsetName);
            LOGGER.debug(socketReceive);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignore) {
                    LOGGER.error("socket close", ignore);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                    LOGGER.error("socket close", ignore);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignore) {
                    LOGGER.error("socket close", ignore);
                }
            }
        }
    }

    /**
     * 向HTTP协议中填写Header信息
     *
     * @param name  HTTP头
     * @param value 值
     */
    public void addHeader(String name, String value) {
        LOGGER.debug("Add Header:" + name + ": " + value);
        StringBuilder builder = new StringBuilder(128);
        builder.append(name);
        builder.append(": ");
        builder.append(value);
        builder.append(CRLF);
        this.header += builder.toString();
    }

    /**
     * @param formName    表单名称
     * @param fileName    文件名称，例如 abc.jpg
     * @param contentType 数据类型（标准MIME类型），例如：text/plain 、 image/jpeg 等
     * @param data        数据流
     * @throws Exception 向HTTP协议中增加附件信息
     */
    public void addAttachment(String formName, String fileName, String contentType, InputStream data) throws Exception {
        InputStream inData = new BufferedInputStream(data);
        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 10];
        for (int numRead; (numRead = inData.read(buffer)) > 0; ) {
            outData.write(buffer, 0, numRead);
        }
        Attachment attachment = new Attachment(fileName, contentType, outData.toByteArray());
        attachmentData.put(formName, attachment);
    }

    /**
     * @param formName    表单名称
     * @param fileName    文件名称，例如 abc.jpg
     * @param contentType 数据类型（标准MIME类型），例如：text/plain 、 image/jpeg 等
     * @param data        文件
     * @throws Exception 向HTTP协议中增加附件信息
     */
    public void addAttachment(String formName, String fileName, String contentType, File data) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(data);
        addAttachment(formName, fileName, contentType, fileInputStream);
    }

    /**
     * @param ipAddress 设置HTTP连接的IP地址，如果没有设置则使用解析出来的IP地址
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return 获取HTTP响应数据
     */
    public String getResponseData() {
        String text = "";
        int i = socketReceive.indexOf(CRLF + CRLF);
        if (i != -1) {
            text = socketReceive.substring(i + CRLF.length() + CRLF.length(), socketReceive.length());
        }
        return text;
    }

    /**
     * 获取HTTP响应数据（二进制数据）
     *
     * @return 字节数组
     */
    public byte[] getResponseByteData() {
        int i = socketReceive.indexOf(CRLF + CRLF);
        int begin = i + 2 * CRLF.length();
        int end = socketReceiveByte.length;
        if (end > begin) {
            return Arrays.copyOfRange(socketReceiveByte, i + CRLF.length() + CRLF.length(), socketReceiveByte.length);
        }
        return new byte[]{0};
    }

    /**
     * @return 获取HTTP全部响应头
     */
    public String getResponseHeader() {
        String head = "";
        int i = socketReceive.indexOf(CRLF + CRLF);
        if (i != -1) {
            head = socketReceive.substring(0, i + CRLF.length());
        }
        return head;
    }

    /**
     * @return 获取指定HTTP响应头
     */
    public String getResponseHeader(String headName) {
        String ret = "";
        if (PlatformUtils.hasText(headName)) {
            for (String s : getResponseHeader().split(CRLF)) {
                int len = headName.length();
                if (s.length() >= len && s.substring(0, len + 1).equals(headName + ":")) {
                    ret = s.substring(len + 1, s.length()).trim();
                }
            }
        }
        return ret;
    }

    /**
     * @return 获取指定HTTP响应头
     */
    public List<String> getResponseHeaderList(String headName) {
        List<String> retList = new ArrayList<>();
        if (PlatformUtils.hasText(headName)) {
            for (String s : getResponseHeader().split(CRLF)) {
                int len = headName.length();
                if (s.length() >= len && s.substring(0, len + 1).equals(headName + ":")) {
                    String ret = s.substring(len + 1, s.length()).trim();
                    retList.add(ret);
                }
            }
        }
        return retList;
    }

    public String getWebTitle() {
        String charset = getWebCharset();
        if (!PlatformUtils.hasText(charset)) {
            return null;
        }

        String result;
        if (charset.contains("gb2312") || charset.contains("gb_2312-80")) {
            result = new String(getResponseByteData(), Charset.forName("gb2312"));
        } else if (charset.contains("gbk")) {
            result = new String(getResponseByteData(), Charset.forName(HttpSocket.GBK));
        } else {
            result = new String(getResponseByteData(), Charset.forName(HttpSocket.UTF8));
        }

        // 获取页面标题
        int length = 3096;
        int indexof = result.indexOf("</title>");
        if (indexof == -1) {
            indexof = result.indexOf("</TITLE>");
        }
        if (indexof != -1) {
            length = indexof + 10;
        }
        if (result.length() > length) {
            result = result.substring(0, length);
        }

        Matcher m = TITLE_PATTERN.matcher(result);
        if (m.matches()) {
            return m.group(2).trim();
        }
        return null;
    }

    public String getWebCharset() {
        Matcher m = HEADER_CHARSET_PATTERN.matcher(getResponseHeader());
        if (m.matches()) {
            String charset = m.group(2);
            if (PlatformUtils.hasText(charset)) {
                return charset.trim().toLowerCase();
            }
        }

        String result = getResponseData();
        int length = 2048;
        int indexof = result.indexOf("charset=");
        if (indexof != -1) {
            length = indexof + 20;
        }

        // 获取页面编码
        if (result.length() > length) {
            result = result.substring(0, length);
        }
        m = CHARSET_PATTERN.matcher(result);
        if (m.matches()) {
            String charset = m.group(2);
            if (PlatformUtils.hasText(charset)) {
                return charset.trim().toLowerCase();
            }
        }
        return null;
    }

    /**
     * @return 获取Socket接收到的数据
     */
    public String getSocketReceive() {
        return socketReceive;
    }

    /**
     * @param str 源字符串
     * @return 获取网站正文内容是否包含指定字符串
     */
    public boolean getContainStr(String str) {
        String data = getResponseData();
        return data != null && data.contains(str);
    }

    /**
     * @return 可访问：true，不可访问：false
     * 网站是否可访问
     */
    public boolean getVisit() {
        String head = getResponseHeader();
        if (head == null) {
            return false;
        }

        String[] heads = head.split(CRLF);
        for (String headStr : heads) {
            if (headStr.contains("HTTP/") && (headStr.contains("200") || headStr.contains("301") || headStr.contains("302"))) {
                return true;
            }
        }

        return false;
    }

    /** 解析HTTPSocket 请求后的响应数据 */
    private void parseResponseHeader() {
        String head = getResponseHeader();
        // 响应头数据存在，则解析
        if (PlatformUtils.hasText(head)) {
            String[] headerArray = head.split(CRLF);
            for (String headerStr : headerArray) {
                // HTTP 版本和响应码
                if (headerStr.startsWith("HTTP")) {
                    String[] firstLineArray = headerStr.split(" ");
                    // HTTP版本
                    setResponseHeaderHttpVersion(firstLineArray[0].substring(firstLineArray[0].indexOf('/') + 1, firstLineArray[0].length()));
                    // 响应码
                    setResponseHeaderCode(firstLineArray[1]);
                }
                // HTTP Response Server
                if (headerStr.startsWith("Server")) {
                    setResponseHeaderServer(headerStr.substring(headerStr.indexOf(':') + 1, headerStr.length()));
                }
                // HTTP Response Location
                if (headerStr.startsWith("Location")) {
                    headerStr = headerStr.substring(headerStr.indexOf(':') + 1, headerStr.length());
                    if (PlatformUtils.hasText(headerStr)) {
                        String[] newHeaderArray = headerStr.split("\\?");
                        setResponseHeaderLocation(newHeaderArray[0]);
                        if (sendRedirect && getResponseHeaderCode().startsWith("3")) {
                            // 重新初始化参数
                            String[] newParameters = newHeaderArray[1].split("&");
                            postData = new HashMap<>();
                            for (String parameters : newParameters) {
                                String[] parameter = parameters.split("=");
                                if (parameter.length == 2) {
                                    postData.put(parameter[0], parameter[1]);
                                } else if (parameter.length == 1) {
                                    postData.put(parameter[0], "");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @throws Exception 处理重定向的问题, 响应码以3开头，表示需要重定向
     */
    private void sendRedirect() throws Exception {
        if (sendRedirect && getResponseHeaderCode().startsWith("3")) {
            // 重定向URL
            url = getResponseHeaderLocation();

            doPost();
        }
    }

    /**
     * 设置URL地址
     *
     * @param url 访问地址
     */
    public void setUrl(String url) {
        String path = url;
        // 如果输入的URL没有包含协议，则自动增加http://协议
        if (path != null && path.length() > 0 && !path.toLowerCase().contains("http://") && !path.toLowerCase().contains("https://")) {
            path = "http://" + path;
        }
        LOGGER.debug("Url: " + path);
        this.url = path;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间
     */
    public void setTimeout(int timeout) {
        LOGGER.debug("TimeOut: " + timeout);
        this.timeout = timeout;
    }

    /**
     * 设置POST数据
     *
     * @param postData 提交的数据
     */
    public void setPostData(Map<String, String> postData) {
        LOGGER.debug("POST Data: " + postData);
        this.postData = postData;
    }

    /**
     * 设置发送数据的编码
     *
     * @param sendCharsetName 发送字符串编码
     */
    public void setSendCharsetName(String sendCharsetName) {
        LOGGER.debug("SendCharsetName: " + sendCharsetName);
        this.sendCharsetName = sendCharsetName;
    }

    /**
     * 设置接收数据的编码
     *
     * @param receiveCharsetName 接受的字符串编码
     */
    public void setReceiveCharsetName(String receiveCharsetName) {
        LOGGER.debug("ReceiveCharsetName: " + receiveCharsetName);
        this.receiveCharsetName = receiveCharsetName;
    }

    public String getResponseHeaderHttpVersion() {
        return responseHeaderHttpVersion;
    }

    public void setResponseHeaderHttpVersion(String responseHeaderHttpVersion) {
        this.responseHeaderHttpVersion = responseHeaderHttpVersion;
    }

    public String getResponseHeaderCode() {
        return responseHeaderCode;
    }

    public void setResponseHeaderCode(String responseHeaderCode) {
        this.responseHeaderCode = responseHeaderCode;
    }

    public String getResponseHeaderServer() {
        return responseHeaderServer;
    }

    public void setResponseHeaderServer(String responseHeaderServer) {
        this.responseHeaderServer = responseHeaderServer;
    }

    public String getResponseHeaderLocation() {
        return responseHeaderLocation;
    }

    public void setResponseHeaderLocation(String responseHeaderLocation) {
        this.responseHeaderLocation = responseHeaderLocation;
    }

    public boolean isSendRedirect() {
        return sendRedirect;
    }

    public void setSendRedirect(boolean sendRedirect) {
        this.sendRedirect = sendRedirect;
    }

    /**
     * 构造POST数据格式
     *
     * @param postData 提交的数据
     * @return url格式的字符串
     */
    public String buildPostFormat(Map<String, String> postData) throws Exception {
        StringBuilder post = new StringBuilder();
        for (Map.Entry<String, String> entry : postData.entrySet()) {
            post.append(entry.getKey());
            post.append("=");
            if (PlatformUtils.hasText(entry.getValue())) {
                post.append(URLEncoder.encode(entry.getValue(), sendCharsetName));
            }
            post.append("&");
        }
        if (post.length() > 1) {
            post.delete(post.length() - 1, post.length());
        }
        return post.toString();
    }

    /** 附件信息类 */
    private class Attachment {

        private String fileName;
        private String contentType;
        private byte[] fileData;

        Attachment(String fileName, String contentType, byte[] fileData) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileData = fileData;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getFileData() {
            return fileData;
        }
    }

    /**
     * @category 从URL中获取主机地址
     * @param url
     * @return
     */
    private String getHostFromUrl(URL url) {
        if (url != null) {
            int hostPost = url.getPort();
            String host = IDN.toASCII(url.getHost());
            return hostPost == -1 ? host : (host +":" + hostPost);
        }
        return null;
    }
}
