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
import it.dipvvf.abr.app.corsivvf.model.Documento;
import it.dipvvf.abr.app.corsivvf.model.Installazione;
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

    /**
     * 
     * @return 
     */
    @GET
    public Response getDevices() {
        List<Dispositivo> lDisp = em.createQuery("SELECT d.id FROM Dispositivo d").getResultList();
        return lDisp.isEmpty() ? Response.noContent().build() : Response.ok(lDisp).build();
    }

    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("{id: \\d+}")
    public Response getDeviceDetail(@PathParam("id") int id) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        return (disp == null) ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(disp).build();
    }

    /**
     * 
     * @param id
     * @param d
     * @return 
     */
    @PUT
    @Path("{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDevice(@PathParam("id") int id, Dispositivo d) {
        try {
            em.persist(d);
            em.flush();
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.CONFLICT).entity(e).build();
        }
    }

    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("{id: \\d+}/courses")
    public Response getDeviceCourses(@PathParam("id") int id) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        if (disp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Integer> lInst = em.createQuery("SELECT i.id FROM Installazione i WHERE i.idDispositivo = :idDisp")
                .setParameter("idDisp", disp)
                .getResultList();

        return (lInst.isEmpty()) ? Response.noContent().build() : Response.ok(lInst).build();
    }

    /**
     * 
     * @param id
     * @param idCorso
     * @return 
     */
    @GET
    @Path("{id: \\d+}/courses/{idcourse: \\d+}")
    public Response getDettaglioDeviceCorsi(@PathParam("id") int id, @PathParam("idcourse") int idCorso) {
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

    /**
     * 
     * @param idDev
     * @param idCorso
     * @param info
     * @return 
     */
    @POST
    @Path("{id: \\d+}/courses/{idcourse: \\d+}")
    public Response installDeviceCourse(@PathParam("id") int idDev, @PathParam("idcourse") int idCorso, @Context UriInfo info) {
        Dispositivo disp = em.find(Dispositivo.class, idDev);
        if (disp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            Date dataSinc = new Date();
            Counter order = new Counter();
            
            Installazione inst = new Installazione();
            inst.setIdCorso(corso);
            inst.setIdDispositivo(disp);
            inst.setDataInstallazione(dataSinc);
            em.persist(inst);

            Delta d = new Delta();
            
            d.setRisorsa(corso.getTitolo());
            d.setDataSincronizzazione(dataSinc);
            d.setIdDispositivo(disp);
            d.setOperazione(DeltaConst.Operation.ADD.toString());
            d.setTipologia(DeltaConst.ResourceType.COURSE.toString());
            d.setOrdine(order.next());
            d.setStato(DeltaConst.Status.PENDING.toString());
            d.setDimensione(-1);
            d.setUidRisorsa(corso.getUidRisorsa());
            em.persist(d);

            List<Categoria> categorie = em.createQuery("SELECT c FROM Categoria c WHERE c.idCorso = :corso", Categoria.class)
                    .setParameter("corso", corso)
                    .getResultList();

            for (Categoria categoria : categorie) {
                d = new Delta();
                
                d.setRisorsa(categoria.getNome());
                d.setDataSincronizzazione(dataSinc);
                d.setIdDispositivo(disp);
                d.setOperazione(DeltaConst.Operation.ADD.toString());
                d.setTipologia(DeltaConst.ResourceType.CATEGORY.toString());
                d.setOrdine(order.next());
                d.setStato(DeltaConst.Status.PENDING.toString());
                d.setDimensione(-1);
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
                d.setUidRisorsaPadre(categoria.getIdCorso().getUidRisorsa());
                d.setUidRisorsa(categoria.getUidRisorsa());
                em.persist(d);
            }

            List<Documento> documenti = em.createQuery("SELECT d FROM Documento d WHERE d.idCorso = :corso", Documento.class)
                                        .setParameter("corso", corso)
                                        .getResultList();
            for(Documento documento : documenti) {
                d = new Delta();
                
                d.setRisorsa(documento.getNomefile());
                d.setDataSincronizzazione(dataSinc);
                d.setIdDispositivo(disp);
                d.setOperazione(DeltaConst.Operation.ADD.toString());
                d.setTipologia(DeltaConst.ResourceType.DOCUMENT.toString());
                d.setOrdine(order.next());
                d.setStato(DeltaConst.Status.PENDING.toString());
                d.setDimensione(documento.getDimensione());
                d.setMd5(documento.getChecksum());
                d.setUidRisorsaPadre(documento.getIdCorso().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
                d.setUidRisorsa(documento.getUidRisorsa());
                em.persist(d);
            }
            
            documenti = em.createQuery("SELECT d FROM Documento d JOIN d.idCategoria c WHERE c.idCorso = :corso", Documento.class)
                                        .setParameter("corso", corso)
                                        .getResultList();
            for(Documento documento : documenti) {
                d = new Delta();
                
                d.setRisorsa(documento.getNomefile());
                d.setDataSincronizzazione(dataSinc);
                d.setIdDispositivo(disp);
                d.setOperazione(DeltaConst.Operation.ADD.toString());
                d.setTipologia(DeltaConst.ResourceType.DOCUMENT.toString());
                d.setOrdine(order.next());
                d.setStato(DeltaConst.Status.PENDING.toString());
                d.setDimensione(documento.getDimensione());
                d.setMd5(documento.getChecksum());
                d.setUidRisorsaPadre(documento.getIdCategoria().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.CATEGORY.toString());
                d.setUidRisorsa(documento.getUidRisorsa());
                em.persist(d);
            }
            
            em.flush();

            return Response.status(Response.Status.CREATED).entity(resourceToURI(info, inst.getId())).build();
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.CONFLICT).entity(e).build();
        }
    }

    /**
     * 
     * @param deviceId
     * @return 
     */
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
