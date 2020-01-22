/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.bean.AccessInfo;
import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.ejb.SessionStorage;
import it.dipvvf.abr.app.corsivvf.wsref.ActiveDirectoryService;
import it.dipvvf.abr.app.corsivvf.wsref.ActiveDirectoryServiceService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@DependsOn("SessionStorage")
public class AuthService extends BaseService {
    @Inject
    MiscServices msb;
    @Inject
    SessionStorage ss;
    @WebServiceRef
    ActiveDirectoryServiceService adServiceRef;
    ActiveDirectoryService adService;

    /**
     * 
     */
    @PostConstruct
    void initialize() {
        try {
            adService = adServiceRef.getActiveDirectoryServicePort();            
            if(adService==null) {
                throw new EJBException("Impossibile recuperare la porta wdsl al servizio.");
            }
        }
        catch(EJBException ee) {
            throw ee;
        }
        catch(Exception e) {
            throw new EJBException("Errore nel recupero della porta wsdl al servizio.", e);
        }    
    }
    
    /**
     * This is a sample web service operation
     * @param ai
     * @return 
     */
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)    
    public Response loginJson(AccessInfo ai) {            
        if(adService.checkUser(ai.getUsername(), ai.getPassword())) {
            List<String> groups = adService.getUserGroups(ai.getUsername());
            groups = (groups!=null) ? groups : new ArrayList<>(0);
            System.out.printf("Gruppi dell'utente %s: %s", ai.getUsername(), groups);
            if(groups.contains("GCorsi")) {
                String token = msb.createToken("admin_id", MiscServices.NO_EXPIRE);
                if(ss.add(token))
                    return ok(token);
                else
                    return error("Impossibile utilizzare lo storage di sessione.");
            }
        }
        
        return unauthorized();
    }
    
    /**
     * 
     * @param username
     * @param password
     * @return 
     */
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response loginForm(@FormParam("username") String username, @FormParam("password") String password) {
        return loginJson(new AccessInfo(username, password));
    }
    
    /**
     * 
     * @param token
     * @return 
     */
    @POST
    @Path("logout")
    public Response logout(@HeaderParam("Authorization") String token) {
        if(token!=null) {
            token = token.substring(token.indexOf(":")+1).trim();
            
            ss.invalidate(token);
            
            return ok();
        }
        
        return notModified();
    }
}
