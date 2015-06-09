////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.json.*;
import javax.json.stream.*;

import java.nio.file.*;

public class Main {

    Server server = null;
    int port = 8888;

    public static void main(String[] args) throws Exception {
        Main app = new Main();
        app.run();
        // app.profile();
    }

    void run() throws Exception {
        if (isAlreadyRunning()) {
            launchBrowser();
        } else {
            startServer();
            minimizeToTray();
            launchBrowser();
            awaitServer();
        }
    }

    // Detects if the program is already running. If the port is occupied, it
    // probably is.
    boolean isAlreadyRunning() {
        try (Socket ignored = new Socket("localhost", port)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    void launchBrowser() throws Exception {
        Desktop.getDesktop().browse(
            new java.net.URI("http://localhost:" + port));
    }

    void startServer() throws Exception {
        WebAppContext app = new WebAppContext(
            getClass().getResource("/serve").toString(), // Get directory
                                                        // embedded in JAR
            "/"); // Run under root
        app.setExtractWAR(false); // Not doing this would cause Jetty to fail to
                                  // extract WAR from EXE
        app.addServlet(new ServletHolder(new AjaxServlet()), "/ajax");

        server = new Server(port);
        server.setHandler(app);
        server.start();
    }

    void awaitServer() throws Exception {
        server.wait();
    }

    void minimizeToTray() throws Exception {
        MenuItem defaultItem = new MenuItem("Launch browser");
        defaultItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    launchBrowser();
                } catch (Exception exception) {
                    // Do nothing hehe
                }
            }});
        
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }});

        PopupMenu popup = new PopupMenu();
        popup.add(defaultItem);
        popup.add(quitItem);

        SystemTray.getSystemTray().add(new TrayIcon(
            Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/serve/favicon.png")),
            "TransJawi",
            popup));
    }

    static class AjaxServlet extends HttpServlet {

        Translator translator;

        AjaxServlet() throws IOException, Lexicon.ParseError {
            Trie<java.util.List<Entry>> trie = Lexicon.load();
            translator = new Translator(trie);
        }
        
        public void doGet(HttpServletRequest req, HttpServletResponse res)
                throws ServletException, IOException {
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            String source = req.getParameter("input");
            java.util.List<Token> input = translator.scan(source);
            java.util.List<Token> output = new ArrayList<Token>();
            String translation = translator.processTokens(source, input,
                output);
            JsonGenerator json = Json.createGenerator(res.getWriter());
            json.writeStartObject();
            json.writeStartArray("input");
            serialiseTokens(input, source, json);
            json.writeEnd();
            json.writeStartArray("output");
            serialiseTokens(output, translation, json);
            json.writeEnd();
            json.writeEnd();
            json.flush();
        }

        void serialiseTokens(java.util.List<Token> tokens, String source,
                JsonGenerator json) {
            int i = 0;
            for (Token token : tokens) {
                json.writeStartArray();
                json.write(token.getClass().getSimpleName());
                json.write(source.substring(i, i + token.length()));
                json.writeEnd();
                i += token.length();
            }
        }
    }
}
