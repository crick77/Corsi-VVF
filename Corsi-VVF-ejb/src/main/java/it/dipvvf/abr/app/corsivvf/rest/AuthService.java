/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.bean.AccessInfo;
import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.wsref.ActiveDirectoryService;
import it.dipvvf.abr.app.corsivvf.wsref.ActiveDirectoryServiceService;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthService extends BaseService {
    @EJB
    MiscServices msb;
    @WebServiceRef
    ActiveDirectoryServiceService adServiceRef;
    ActiveDirectoryService adService;

    @PostConstruct
    public void initialize() {
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
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)    
    public Response loginJson(AccessInfo ai) {            
        if(adService.checkUser(ai.getUsername(), ai.getPassword())) {
            return Response.ok(msb.createToken("admin_id", msb.toMillis(0, 6, 0))).build();
        }
        else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response loginForm(@FormParam("username") String username, @FormParam("password") String password) {
        return loginJson(new AccessInfo(username, password));
    }
}
