package com.juja.pairs;

import com.juja.pairs.controller.MetadataReader;
import com.juja.pairs.controller.MetadataReaderFactory;
import com.juja.pairs.model.ConnectionParameters;
import com.juja.pairs.view.FileView;
import com.juja.pairs.view.View;
import org.apache.log4j.*;


public class DbMetadataManager {
    public static Appender logAppender;
    final static Logger logger = Logger.getLogger(DbMetadataManager.class);

    public static void main(String[] args) {

        logAppender = defineAppender(args[2]);
        logger.addAppender(logAppender);

        View inputView = new FileView(args[0]);
        inputView.read();
        ConnectionParameters parameters = ConnectionParameters.parseFromFile(inputView.read());
        String metadata = "";
        try (MetadataReader reader = MetadataReaderFactory.getReader(parameters)) {
            metadata = reader.read();
        } catch (Exception e) {
            logger.error(e);
        }
        View outputView = new FileView(args[1]);
        outputView.write(metadata);
    }

    private static Appender defineAppender(String logPath) {
        //TODO какие нибудь проверки есть ли путь, файл
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(logPath);
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.DEBUG);
        fa.setAppend(true);
        fa.activateOptions();
        return fa;
    }

}
