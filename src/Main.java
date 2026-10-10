import data.DocumentStore;
import search.SearchEngine;
import server.WebServer;
import test.AlgorithmTester;
import ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

/**
 * Main application launcher for the Multilingual Text Retrieval System Website.
 * Intelligent Multilingual Search Engine with Automatic Algorithm Selection.
 * 
 * Default behavior: Starts the Java REST Web Server and opens the modern Web Application in the browser.
 */
public class Main {

    public static void main(String[] args) {
        // Mode 1: Automated Test Suite (CLI)
        if (args.length > 0 && "--test".equalsIgnoreCase(args[0])) {
            System.out.println("Executing automated test suite...\n");
            AlgorithmTester.main(args);
            return;
        }

        // Mode 2: Optional Desktop GUI
        if (args.length > 0 && "--gui".equalsIgnoreCase(args[0])) {
            launchSwingGui();
            return;
        }

        // Mode 3 (DEFAULT): Modern Web Application (Website Mode)
        launchWebApplication();
    }

    private static void launchWebApplication() {
        System.out.println("==================================================================");
        System.out.println("  MULTILINGUAL TEXT RETRIEVAL SYSTEM - WEB APPLICATION");
        System.out.println("  Fast, Intelligent Search Engine with Automatic Query Routing");
        System.out.println("==================================================================");

        DocumentStore store = new DocumentStore();
        store.loadDocuments();
        SearchEngine engine = new SearchEngine(store);

        int port = 8080;
        try {
            WebServer server = new WebServer(port, store, engine);
            server.start();

            String url = "http://localhost:" + port;
            System.out.println("\n✓ Java REST Backend & Web Server running on: " + url);
            System.out.println("✓ Automatic Algorithm Selection Engine: Active");
            System.out.println("✓ Indexed Documents: " + store.size() + " across English, Telugu, Hindi, Tamil");
            System.out.println("✓ Opening website in your default web browser...\n");

            openBrowser(url);

            System.out.println(">> Keep this terminal running to keep the website active.");
            System.out.println(">> Press Ctrl + C to stop the web server.\n");

            // Keep main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Error running web application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception ignored) {
            System.out.println("Please open your browser manually and visit: " + url);
        }
    }

    private static void launchSwingGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
