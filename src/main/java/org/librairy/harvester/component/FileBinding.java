/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */
package org.librairy.harvester.component;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.NoTypeConversionAvailableException;

/**
 * File binding with the {@link java.io.File} type.
 */
public class FileBinding implements GenericFileBinding<File> {
    private File body;
    private byte[] content;

    public Object getBody(GenericFile<File> file) {
        // if file content has been loaded then return it
        if (content != null) {
            return content;
        }
        
        // as we use java.io.File itself as the body (not loading its content into an OutputStream etc.)
        // we just store a java.io.File handle to the actual file denoted by the
        // file.getAbsoluteFilePath. We must do this as the original file consumed can be renamed before
        // being processed (preMove) and thus it points to an invalid file location.
        // GenericFile#getAbsoluteFilePath() is always up-to-date and thus we use it to create a file
        // handle that is correct
        if (body == null || !file.getAbsoluteFilePath().equals(body.getAbsolutePath())) {
            body = new File(file.getAbsoluteFilePath());
        }
        return body;
    }

    public void setBody(GenericFile<File> file, Object body) {
        // noop
    }

    public void loadContent(Exchange exchange, GenericFile<?> file) throws IOException {
        if (content == null) {
            try {
                content = exchange.getContext().getTypeConverter().mandatoryConvertTo(byte[].class, exchange, file);
            } catch (NoTypeConversionAvailableException e) {
                throw new IOException("Cannot load file content: " + file.getAbsoluteFilePath(), e);
            }
        }
    }
}
