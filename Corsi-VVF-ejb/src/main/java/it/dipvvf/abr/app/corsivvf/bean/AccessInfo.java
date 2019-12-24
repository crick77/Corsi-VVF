/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.bean;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author riccardo.iovenitti
 */
@XmlRootElement
public class AccessInfo implements Serializable {
    private String username;
    private String password;

    /**
     * 
     */
    public AccessInfo() {
    }

    /**
     * 
     * @param username
     * @param password 
     */
    public AccessInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 
     * @return 
     */
    public String getUsername() {
        return username;
    }

    /**
     * 
     * @param username 
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * 
     * @return 
     */
    public String getPassword() {
        return password;
    }

    /**
     * 
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
