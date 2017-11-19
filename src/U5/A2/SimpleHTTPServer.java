package U5.A2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class SimpleHTTPServer {

    public static void main(String args[]) {
        int port;
        String documentRoot;
        ServerSocket server = null;

        try {
            port = Integer.parseInt(args[0]);
            documentRoot = System.getProperty("user.dir")+args[1];
        } catch(Exception e) {
            port = 8000;
            documentRoot = System.getProperty("user.dir");
        }

        try {
            server = new ServerSocket(port);
            System.out.println("HTTP Server listening on port "+server.getLocalPort());
            System.out.println("U5/A1/documentRoot " +documentRoot);

            while(true) {
                Socket socket = server.accept();
                RequestThread thread = new RequestThread(socket, documentRoot);
                thread.start();
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (Exception e) {
                    // will not be thrown
                }
            }
        }
    }
}


class RequestThread extends Thread {

    Socket socket;
    String documentRoot;

    String CRLF = "\r\n";

    public RequestThread(Socket socket, String documentRoot) {
        this.socket = socket;
        this.documentRoot = documentRoot;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();
            BufferedOutputStream outstream = new BufferedOutputStream(out);

            String request = in.readLine();
            StringTokenizer requestst = new StringTokenizer(request);

            String header = requestst.nextToken();
            String fileName = requestst.nextToken();
            String filePath = documentRoot+fileName;

            try {
                File file = new File(filePath);
                if(file.isDirectory()) {
                    filePath = filePath + "\\index.html";
                }

                FileInputStream fis = new FileInputStream(filePath);

                String ServerLine="Simple HTTP Server";
                String StatusLine="HTTP/0.9 200 OK"+CRLF;
                String ContentTypeLine="Content-type: text"+CRLF;
                String ContentLengthLine="Content-Length: "+(new Integer(fis.available()).toString())+CRLF;
                outstream.write(StatusLine.getBytes());
                outstream.write(ServerLine.getBytes());
                outstream.write(ContentTypeLine.getBytes());
                outstream.write(ContentLengthLine.getBytes());
                outstream.write(CRLF.getBytes());

                byte[] buffer = new byte[1024];
                while (fis.read(buffer) != -1) {
                    outstream.write(buffer);
                }

                fis.close();
            } catch(Exception e) {
                String StatusLine="";
                outstream.write(StatusLine.getBytes());
            }

            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = new Date();
            System.out.println(dt.format(date)+" "+header+" "+fileName+" "+socket.getInetAddress().getCanonicalHostName()+" "+socket.getPort());

            outstream.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}