/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.model.DeltaConst;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

        List<Integer> lSinc = em.createQuery("SELECT s.id FROM Sincronizzazione s WHERE s.idDispositivo = :disp AND s.stato = :status ORDER BY s.dataora ASC", Integer.class)
                .setParameter("disp", idDev)
                .setParameter("status", status)
                .getResultList();

        return Response.ok(resourcesToURI(uriInfo, lSinc)).build();
    }

    /**
     *
     * @param idDev
     * @param idSync
     * @return
     */
    @GET
    @Path("{iddev: \\d+}/{idsync: \\d+}")
    public Response getDeviceDeltaDetail(@PathParam("iddev") int idDev, @PathParam("idsync") int idSync) {
        int deltaCount = em.createQuery("SELECT s FROM ", Integer.class)
                .setParameter("idDisp", idDev)
                .getSingleResult();

        return Response.ok(String.valueOf(deltaCount)).build();
    }

    
}
