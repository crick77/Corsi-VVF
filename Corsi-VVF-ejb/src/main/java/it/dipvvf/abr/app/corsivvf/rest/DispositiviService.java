/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.bean.Counter;
import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.model.Categoria;
import it.dipvvf.abr.app.corsivvf.model.Corso;
import it.dipvvf.abr.app.corsivvf.model.Delta;
import it.dipvvf.abr.app.corsivvf.model.DeltaConst;
import it.dipvvf.abr.app.corsivvf.model.Dispositivo;
import it.dipvvf.abr.app.corsivvf.model.Installazione;
import java.net.URI;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
public class DispositiviService extends BaseService {

    @Inject
    MiscServices ms;
    @PersistenceContext
    EntityManager em;
    @Resource
    EJBContext ctx;

    @GET
    public Response getDevices() {
        List<Dispositivo> lDisp = em.createQuery("SELECT d.id FROM Dispositivo d").getResultList();
        return lDisp.isEmpty() ? Response.noContent().build() : Response.ok(lDisp).build();
    }

    @GET
    @Path("{id}")
    public Response getDettaglioDevice(@PathParam("id") Integer id) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        return (disp == null) ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(disp).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDeviceInfo(@PathParam("id") Integer id, Dispositivo d) {
        try {
            em.persist(d);
            em.flush();
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.CONFLICT).entity(e).build();
        }
    }

    @GET
    @Path("{id}/courses")
    public Response getDeviceCorsi(@PathParam("id") Integer id) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        if (disp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Integer> lInst = em.createQuery("SELECT i.id FROM Installazione i WHERE i.idDispositivo = :idDisp")
                .setParameter("idDisp", disp)
                .getResultList();

        return (lInst.isEmpty()) ? Response.noContent().build() : Response.ok(lInst).build();
    }

    @GET
    @Path("{id}/courses/{idcorso}")
    public Response getDettaglioDeviceCorsi(@PathParam("id") Integer id, @PathParam("idcorso") Integer idCorso) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        if (disp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Installazione> lInst = em.createQuery("SELECT i FROM Installazione i WHERE i.idDispositivo = :id AND i.idCorso = :idCorso")
                .setParameter("id", disp)
                .setParameter("idCorso", corso)
                .getResultList();

        return (lInst.isEmpty()) ? Response.noContent().build() : Response.ok(lInst.get(0)).build();
    }

    @POST
    @Path("{id}/courses/{idcorso}")
    public Response abbinaDeviceCorso(@PathParam("id") Integer idDev, @PathParam("idcorso") Integer idCorso, @Context UriInfo info) {
        Dispositivo disp = em.find(Dispositivo.class, idDev);
        if (disp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            Installazione inst = new Installazione();
            inst.setIdCorso(corso);
            inst.setIdDispositivo(disp);
            inst.setDataInstallazione(new Date());
            em.persist(inst);

            Date dataSinc = new Date();
            Counter order = new Counter();

            Delta d = new Delta();
            
            d.setRisorsa(corso.getTitolo());
            d.setDataSincronizzazione(dataSinc);
            d.setIdDispositivo(disp);
            d.setOperazione(DeltaConst.Operation.ADD);
            d.setTipologia(DeltaConst.ResourceType.COURSE);
            d.setOrdine(order.next());
            d.setStato(DeltaConst.Status.PENDING);
            d.setDimensione(-1);
            em.persist(d);

            List<Categoria> categorie = em.createQuery("SELECT c FROM Categoria c WHERE c.idCorso = :corso", Categoria.class)
                    .setParameter("corso", corso)
                    .getResultList();

            for (Categoria categoria : categorie) {
                d = new Delta();
                
                d.setRisorsa(categoria.getNome());
                d.setDataSincronizzazione(dataSinc);
                d.setIdDispositivo(disp);
                d.setOperazione(DeltaConst.Operation.ADD);
                d.setTipologia(DeltaConst.ResourceType.CATEGORY);
                d.setOrdine(order.next());
                d.setStato(DeltaConst.Status.PENDING);
                d.setDimensione(-1);
                em.persist(d);
            }

            // Get all document from course and each category and insert delta
            
            em.flush();

            return Response.created(URI.create(resourceToURI(info, inst.getId()))).build();
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.CONFLICT).entity(e).build();
        }
    }

    @POST
    public Response registerDevice(@HeaderParam("device-id") String deviceId) {
        if (deviceId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Recupera il device
        Dispositivo d = em.createQuery("SELECT d FROM Dispositivo d WHERE d.deviceid = :devid", Dispositivo.class)
                .setParameter("devid", deviceId)
                .getSingleResult();

        // Non esiste? Restituisce lo stato al client
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Token già generato, restituisce lo stato
        if (d.getToken() != null) {
            return Response.notModified().build();
        }

        // Non abilitato? Segnala al client la mancanza di autorizzazione
        if (!d.getAbilitato()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Genera un token univoco abbinato al deviceid senza scadenza
        // e aggiorna archivio
        String devToken = ms.createToken(deviceId, MiscServices.NO_EXPIRE);
        d.setToken(devToken);

        return Response.ok(devToken).build();
    }
}
