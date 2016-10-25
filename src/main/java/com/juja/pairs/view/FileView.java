package com.juja.pairs.view;

import org.apache.log4j.Logger;
import static com.juja.pairs.DbMetadataManager.*;

public class FileView implements View {
    private String filePath;
    private final static Logger logger = Logger.getLogger(FileView.class);
    static {
        logger.addAppender(logAppender);
    }

    public FileView(String filePath) {
        this.filePath = filePath;

    }

    @Override
    public void write(String message) {
        //TODO write to file
    }

    @Override
    public String read() {
        //TODO read wrom file
        return "";
    }
}
