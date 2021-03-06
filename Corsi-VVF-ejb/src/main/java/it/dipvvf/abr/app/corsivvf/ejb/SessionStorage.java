/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.ejb;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author riccardo.iovenitti
 */

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class SessionStorage {
    @Inject
    MiscServices ms;
    private Map<String, DecodedJWT> session;
    
    /**
     * 
     */
    @PostConstruct
    void startup() {
        session = new HashMap<>();
        System.out.println("SessionStorage created.");
    }
    
    /**
     * 
     */
    @PreDestroy
    @Lock(LockType.WRITE)
    void shutdown() {
        flush();
    }
    
    /**
     * 
     * @param token 
     * @return  
     */
    public boolean add(String token) {
        if(token!=null) {
            try {
                DecodedJWT jwt = ms.decodeToken(token);
                if(jwt!=null) {
                    session.put(token, jwt);
                    return true;
                }
            }
            catch(Exception e) {
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @param token
     * @return 
     */
    @Lock(LockType.WRITE)
    public boolean isValid(String token) {
        if(token!=null) {
            return (session.get(token)!=null);
        }
        return false;
    }
    
    /**
     * 
     * @param token
     * @return 
     */
    public DecodedJWT get(String token) {
        if(token!=null) {
            return session.get(token);
        }
        return null;
    }
    
    /**
     * 
     * @param token 
     * @return  
     */
    @Lock(LockType.WRITE)
    public boolean invalidate(String token) {
        if(token!=null) {
            return (session.remove(token)!=null);
        }
        return false;
    }
    
    /**
     * 
     */
    @Lock(LockType.WRITE)
    public void flush() {
        session.clear();        
    }
}
