/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.model.Delta;
import it.dipvvf.abr.app.corsivvf.model.DeltaConst;
import it.dipvvf.abr.app.corsivvf.model.Sincronizzazione;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Servizi dedcati al lato web-app
 * 
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("synchronizations")
@Produces(MediaType.APPLICATION_JSON)
public class SynchService extends BaseService {
    @PersistenceContext
    EntityManager em;

    /**
     * Retrieve all available synchronization eventually filtered by a status.
     * If no status is supplied, "PENDING" will be used.
     * 
     * @param status
     * @param uriInfo
     * @return 
     */
    @GET
    public Response getAllSynchronizations(@QueryParam("status") String status, @Context UriInfo uriInfo) {
        status = (status != null) ? status.trim().toUpperCase() : DeltaConst.Status.PENDING.toString();
        switch (status) {
            case "DONE": {
                status = DeltaConst.Status.DONE.toString();
                break;
            }
            case "ERROR": {
                status = DeltaConst.Status.ERROR.toString();
                break;
            }
            case "PENDING": {
                status = DeltaConst.Status.PENDING.toString();
                break;
            }
            default: {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
        
        List<Integer> lSinc = em.createQuery("SELECT s.id FROM Sincronizzazione s WHERE s.stato = :status ORDER BY s.dataora ASC", Integer.class)
                .setParameter("status", status)
                .getResultList();

        return Response.ok(resourcesToURI(uriInfo, lSinc)).build();
    }
    
    /**
     * Retrieve all synchronization specific for a device eventually filtered
     * by a status. If status is not supplied "PENDING" will be used.
     * 
     * @param idDev
     * @param status
     * @param uriInfo
     * @return
     */
    @GET
    @Path("{iddev: \\d+}")
    public Response getDeviceSynchronizations(@PathParam("iddev") int idDev, @QueryParam("status") String status, @Context UriInfo uriInfo) {
        status = (status != null) ? status.trim().toUpperCase() : DeltaConst.Status.PENDING.toString();
        switch (status) {
            case "DONE": {
                status = DeltaConst.Status.DONE.toString();
                break;
            }
            case "ERROR": {
                status = DeltaConst.Status.ERROR.toString();
                break;
            }
            case "PENDING": { 
                status = DeltaConst.Status.PENDING.toString();
                break;
            }
            default: {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        List<Integer> lSinc = em.createQuery("SELECT s.id FROM Sincronizzazione s JOIN Dispositivo d WHERE d.id = :disp AND s.stato = :status ORDER BY s.dataora ASC", Integer.class)
                .setParameter("disp", idDev)
                .setParameter("status", status)
                .getResultList();

        return Response.ok(resourcesToURI(uriInfo, lSinc)).build();
    }

    /**
     * Return detail about given synchronization for requested device id.
     * 
     * @param idDev
     * @param idSync
     * @return
     */
    @GET
    @Path("{iddev: \\d+}/{idsync: \\d+}")
    public Response getDeviceSynchDetail(@PathParam("iddev") int idDev, @PathParam("idsync") int idSync) {
        try {
            Sincronizzazione s = em.createQuery("SELECT s FROM Sincronizzazione s JOIN Dispositivo d WHERE d.id = :idDisp AND s.id = :idSync", Sincronizzazione.class)
                    .setParameter("idDisp", idDev)
                    .setParameter("idSync", idSync)
                    .getSingleResult();

            return Response.ok(s).build();
        }
        catch(NoResultException nre) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Returns all deltas associated to a specific synchronization for a given
     * device.
     * 
     * @param idDev
     * @param idSync
     * @param uriInfo
     * @return 
     */
    @GET
    @Path("{iddev: \\d+}/{idsync: \\d+}/deltas")
    public Response getDeviceSynchDelta(@PathParam("iddev") int idDev, @PathParam("idsync") int idSync, @Context UriInfo uriInfo) {
        List<Integer> lDelta = em.createQuery("SELECT d.id FROM Delta d JOIN Sincronizzazione s JOIN Dispositivo d WHERE d.id = :iddev AND s.id = :idsync", Integer.class)
                .setParameter("iddev", idDev)
                .setParameter("idsync", idSync)
                .getResultList();
        
        return Response.ok(resourcesToURI(uriInfo, lDelta)).build();
    }
   
    /**
     * Returns detail about a requested delta for a specific synchronization/
     * device.
     * 
     * @param idDev
     * @param idSync
     * @param idDelta
     * @return 
     */
    @GET
    @Path("{iddev: \\d+}/{idsync: \\d+}/deltas/{iddelta: \\d+}")
    public Response getDeviceSynchDeltaDetail(@PathParam("iddev") int idDev, @PathParam("idsync") int idSync, @PathParam("iddelta") int idDelta) {
        try {
            Delta d = em.createQuery("SELECT d FROM Delta d JOIN Sincronizzazione s JOIN Dispositivo disp WHERE disp.id = :iddev AND s.id = :idsync AND d.id = :iddelta", Delta.class)
                    .setParameter("iddev", idDev)
                    .setParameter("idsync", idSync)
                    .setParameter("iddelta", idDelta)
                    .getSingleResult();
            
            return Response.ok(d).build();
        }
        catch(NoResultException nre) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
