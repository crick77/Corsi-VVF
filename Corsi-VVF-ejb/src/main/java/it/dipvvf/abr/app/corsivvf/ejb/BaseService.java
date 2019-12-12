/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author riccardo.iovenitti
 */
public abstract class BaseService {
    public List<String> resourcesToURI(UriInfo uriInfo, List<? extends Object> resources) {
        List<String> uris = new ArrayList<>(resources!=null ? resources.size() : 1);
        
        if(resources!=null) {                
            resources.stream().map((res) -> {
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                builder.path(String.valueOf(res));
                return builder;
            }).forEachOrdered((builder) -> {
                uris.add(builder.build().toASCIIString());
            });
        }
        
        return uris;
    }
    
    public List<String> resourcesToURI(UriInfo uriInfo, Collection<? extends Object> resources) {       
        List<? extends Object> l;   
        if(resources!=null) {
            l = new ArrayList<>(resources);       
        }
        else {
            l = new ArrayList<>();
        }
        
        return resourcesToURI(uriInfo, l);
    }
    
    public String resourceToURI(UriInfo uriInfo, Object resource) {                       
        ArrayList<Object> resources = new ArrayList<>();
        resources.add(resource);
        return resourcesToURI(uriInfo, resources).get(0);
    }
    
    public Response downloadFile(String fileName, byte[] data) {
        return Response.ok(data).header("Content-Disposition", "attachment; filename=\""+fileName+"\"").build();
    }
}
