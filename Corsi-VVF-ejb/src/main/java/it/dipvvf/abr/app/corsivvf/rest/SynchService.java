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
import it.dipvvf.abr.app.corsivvf.rest.security.JWTSecurityCheck;
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
@JWTSecurityCheck
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
        
        return ok(resourcesToURI(uriInfo, em.createQuery("SELECT s.id FROM Sincronizzazione s WHERE s.stato = :status ORDER BY s.dataora ASC", Integer.class)
                .setParameter("status", status)
                .getResultList()));
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
    @Path("/device/{iddev: \\d+}")
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

        return ok(resourcesToURI(uriInfo, em.createQuery("SELECT s.id FROM Sincronizzazione s JOIN s.idInstallazione i JOIN i.idDispositivo d WHERE d.id = :disp AND s.stato = :status ORDER BY s.dataora ASC", Integer.class)
                .setParameter("disp", idDev)
                .setParameter("status", status)
                .getResultList()));
    }

    /**
     * Return detail about given synchronization for requested device id.
     * 
     * @param idSync* 
     * @param idDev
     * @return
     */
    @GET
    @Path("{idsync: \\d+}/device/{iddev: \\d+}")
    public Response getDeviceSynchDetail(@PathParam("idsync") int idSync, @PathParam("iddev") int idDev) {
        try {
            return ok(em.createQuery("SELECT s FROM Sincronizzazione s JOIN s.idInstallazione i JOIN i.idDispositivo d WHERE d.id = :idDisp AND s.id = :idSync", Sincronizzazione.class)
                    .setParameter("idDisp", idDev)
                    .setParameter("idSync", idSync)
                    .getSingleResult());
        }
        catch(NoResultException nre) {
            return notFound();
        }
    }

    /**
     * Returns all deltas associated to a specific synchronization for a given
     * device.
     * 
     * @param idSync* 
     * @param idDev
     * @param uriInfo
     * @return 
     */
    @GET
    @Path("{idsync: \\d+}/device/{iddev: \\d+}/deltas")
    public Response getDeviceSynchDelta(@PathParam("idsync") int idSync, @PathParam("iddev") int idDev, @Context UriInfo uriInfo) {
        return ok(resourcesToURI(uriInfo, em.createQuery("SELECT d.id FROM Delta d JOIN d.idSincronizzazione s JOIN s.idInstallazione i JOIN i.idDispositivo dev WHERE dev.id = :iddev AND s.id = :idsync", Integer.class)
                .setParameter("iddev", idDev)
                .setParameter("idsync", idSync)
                .getResultList()));
    }
   
    /**
     * Returns detail about a requested delta for a specific synchronization/
     * device.
     * 
     * @param idSync* 
     * @param idDev
     * @param idDelta
     * @return 
     */
    @GET
    @Path("{idsync: \\d+}/device/{iddev: \\d+}/deltas/{iddelta: \\d+}")
    public Response getDeviceSynchDeltaDetail(@PathParam("idsync") int idSync, @PathParam("iddev") int idDev, @PathParam("iddelta") int idDelta) {
        try {
            return ok(em.createQuery("SELECT d FROM Delta d JOIN d.idSincronizzazione s JOIN s.idInstallazione i JOIN i.idDispositivo disp WHERE disp.id = :iddev AND s.id = :idsync AND d.id = :iddelta", Delta.class)
                    .setParameter("iddev", idDev)
                    .setParameter("idsync", idSync)
                    .setParameter("iddelta", idDelta)
                    .getSingleResult());
        }
        catch(NoResultException nre) {
            return notFound();
        }
    }
}
