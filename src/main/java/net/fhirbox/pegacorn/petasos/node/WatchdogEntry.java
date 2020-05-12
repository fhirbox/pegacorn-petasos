/*
 * Based on the code by Mark A. Hunter at
 * https://github.com/fhirbox/pegacorn-communicate-iris/tree/master/src/main/java/net/fhirbox/pegacorn/communicate/iris/utilities
 * and updated for Infinispan 10.x
 * 
 * This class creates the clustered cache manager and configures the shared cache.
 * 
 */
package net.fhirbox.pegacorn.petasos.node;

import java.time.Instant;

import org.json.JSONObject;

import net.fhirbox.pegacorn.petasos.model.PetasosWUPWatchdogState;
import net.fhirbox.pegacorn.petasos.model.ComponentStatusEnum;
import net.fhirbox.pegacorn.petasos.model.FDN;

public class WatchdogEntry {
    private PetasosWUPWatchdogState watchdogState;
    private FDN supportedFunctionFDN; // for WUP type components only
    private FDN currentParcelFDN; // for WUP type components only

    // create a WUP entry which should have a function FDN
    public WatchdogEntry(FDN wupFDN, FDN supportedFunctionFDN) {
        watchdogState = new PetasosWUPWatchdogState(wupFDN, ComponentStatusEnum.COMPONENT_STATUS_IDLE, Instant.now());
        this.supportedFunctionFDN = supportedFunctionFDN;
    }

    // constructor for a non-WUP component
    public WatchdogEntry(FDN componentFDN) {
        watchdogState = new PetasosWUPWatchdogState(componentFDN, ComponentStatusEnum.COMPONENT_STATUS_IDLE, Instant.now());
    }
    
    public WatchdogEntry(FDN componentFDN, ComponentStatusEnum status, Instant statusInstant) {
        watchdogState = new PetasosWUPWatchdogState(componentFDN, status, statusInstant);
    }

    
    public WatchdogEntry(String watchdogJSONString) {
        JSONObject jso = new JSONObject(watchdogJSONString);
        watchdogState = new PetasosWUPWatchdogState(new FDN(jso.getString("wupFDN")),
                                            ComponentStatusEnum.valueOf(jso.getString("wupStatus")),
                                            Instant.ofEpochMilli(jso.getLong("lastStatusUpdate")));
        if (jso.has("supportedFunctionFDN")) {
            supportedFunctionFDN = new FDN(jso.getString("supportedFunctionFDN"));
        }
        if (jso.has("currentParcelFDN")) {
            currentParcelFDN = new FDN(jso.getString("currentParcelFDN"));
        }
    }
    
    public String toJSONString() {
        JSONObject jso = new JSONObject()
                .put("wupFDN", watchdogState.getWupFDN().getQualifiedFDN())
                .put("wupStatus", ComponentStatusEnum.valueOf(watchdogState.getWupStatus().getComponentWatchdogState()))
                .put("lastStatusUpdate", watchdogState.getLastStatusUpdate().toEpochMilli());

        if (supportedFunctionFDN != null) {
            jso.put("supportedFunctionFDN", supportedFunctionFDN.getQualifiedFDN());
        }

        if (currentParcelFDN != null) {
            jso.put("currentParcelFDN", currentParcelFDN.getQualifiedFDN());
        }
        return jso.toString();
    }
        
    public void setCurrentParcelFDN(FDN currentParcelFDN) {
        this.currentParcelFDN = currentParcelFDN;
    }
    
    public PetasosWUPWatchdogState getWatchdogState() {
        return this.watchdogState;
    }
}