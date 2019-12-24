/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

/**
 *
 * @author riccardo.iovenitti
 */
public final class DeltaConst {
    /**
     * 
     */
    public enum Operation {
        ADD("ADD"),
        UPDATE("UPDATE"),
        REMOVE("REMOVE");
        
        private final String operation;
        
        /**
         * 
         * @param operation 
         */
        private Operation(String operation) {
            this.operation = operation;
        }
        
        /**
         * 
         * @return 
         */
        @Override
        public String toString() {
            return operation;
        }
    };

    /**
     * 
     */
    public enum ResourceType {
        COURSE("COURSE"),
        CATEGORY("CATEGORY"),
        DOCUMENT("DOCUMENT");
                
        private final String resourceType;
        
        /**
         * 
         * @param res 
         */
        private ResourceType(String res) {
            this.resourceType = res;
        }
        
        /**
         * 
         * @return 
         */
        @Override
        public String toString() {
            return resourceType;
        }
    };
        
    /**
     * 
     */
    public enum Status {
        PENDING("PENDING"),
        ERROR("ERROR"),
        DONE("DONE");
        
        private final String status;
        
        /**
         * 
         * @param status 
         */
        private Status(String status) {
            this.status = status;
        }
        
        /**
         * 
         * @return 
         */
        @Override
        public String toString() {
            return status;
        }
    }
    
    /**
     * 
     */
    private DeltaConst() {}
}
