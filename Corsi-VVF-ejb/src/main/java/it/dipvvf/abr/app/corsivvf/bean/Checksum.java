/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.bean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author riccardo.iovenitti
 */
public class Checksum {
    private MessageDigest md = null;
    private CRC32 crc = null;
    
    public Checksum() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nse) {
            System.err.println("MD5 is not available. Switching to CRC32.");
            crc = new CRC32();
        }
    }
    
    public void update(byte[] data) {
        if(md!=null) 
            md.update(data);
        else
            crc.update(data);
    }
    
    public void update(byte[] data, int offs, int len) {
        if(md!=null) 
            md.update(data, offs, len);
        else
            crc.update(data, offs, len);
    }
    
    public String getCheckum() {
        StringBuilder sb = new StringBuilder();
        if(md!=null) {
            sb.append("MD5:").append(DatatypeConverter.printHexBinary(md.digest()).toUpperCase());
        }
        else {
            sb.append("CRC32:").append(String.format("%08X", crc.getValue()));
        }
        
        return sb.toString();
    }
}
