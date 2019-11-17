package net.ravendb.demo.model.DTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.flow.server.StreamResource;

public class ProfilePicture {

    String name;
    byte[] bytes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public StreamResource getStreamResource() {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return new StreamResource(name, () -> bis);
    }

}
