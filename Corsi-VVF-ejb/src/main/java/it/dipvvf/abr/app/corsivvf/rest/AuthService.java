/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.bean.AccessInfo;
import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    /**
     * This is a sample web service operation
     * @param ai
     * @return 
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)    
    public Response hello(AccessInfo ai) {
        if("admin".equalsIgnoreCase(ai.getUsername()) && "pass".equalsIgnoreCase(ai.getPassword())) {
            return Response.ok(msb.createToken("admin_id", msb.toMillis(0, 6, 0))).build();
        }
        else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
