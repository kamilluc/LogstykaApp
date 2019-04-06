package sample;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;



public class Logger {

    private static Logger logger = null;

    private final String logFile = "logs.txt";
    private PrintWriter writer;

    public Logger() {
        try {
            FileWriter fw = new FileWriter(logFile);
            writer = new PrintWriter(fw, true);
        } catch (IOException e) {}
    }

    public static synchronized Logger getInstance(){
        if(logger == null)
            logger = new Logger();
        return logger;
    }

    public void log (String msg) {
        writer.println("***\n" + msg + "\n***\n");
    }


}