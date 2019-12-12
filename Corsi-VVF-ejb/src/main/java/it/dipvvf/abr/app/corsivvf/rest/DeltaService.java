/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.model.Delta;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** 
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("delta")
@Produces(MediaType.APPLICATION_JSON)
public class DeltaService {
    @PersistenceContext
    EntityManager em;
    
    @GET
    @Path("{iddisp}")
    public Response getDeviceDeltas(@PathParam("iddisp") Integer idDispositivo) {        
        Object deltaCount = em.createQuery("SELECT COUNT(d.id) FROM Delta d JOIN Dispositivo dp WHERE dp.id = :idDisp AND d.stato <> 'DONE'")
                                        .setParameter("idDisp", idDispositivo)
                                        .getSingleResult();
        
        return Response.ok(String.valueOf(deltaCount)).build();
    }
    
    @GET
    @Path("{iddisp}/items")
    public Response getDettaglioDeltas(@PathParam("iddisp") Integer idDispositivo) {        
        List<Integer> lDelta = em.createQuery("SELECT d.id FROM Delta d JOIN Dispositivo dp WHERE dp.id = :idDisp AND d.stato <> 'DONE'")
                .setParameter("idDisp", idDispositivo)
                .getResultList();
        
        return (lDelta.isEmpty()) ? Response.noContent().build() : Response.ok(lDelta).build();
    }
    
    @GET
    @Path("{iddisp}/items/{iditem}")
    public Response getDettaglioDeltas(@PathParam("iddisp") Integer idDispositivo, @PathParam("iditem") Integer idItem) {        
        List<Delta> lDelta = em.createQuery("SELECT d FROM Delta d JOIN Dispositivo dp WHERE dp.id = :idDisp AND d.id = :idItem")
                .setParameter("idDisp", idDispositivo)
                .setParameter("idItem", idItem)
                .getResultList();
        
        return (lDelta.isEmpty()) ? Response.noContent().build() : Response.ok(lDelta).build();
    }
}
