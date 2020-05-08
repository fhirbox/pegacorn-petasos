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
    PetasosWUPWatchdogState watchdogState;
    FDN supportedFunctionFDN;
    
    public WatchdogEntry(FDN wupFDN, FDN supportedFunctionFDN) {
        watchdogState = new PetasosWUPWatchdogState(wupFDN, ComponentStatusEnum.COMPONENT_STATUS_IDLE, Instant.now());
        this.supportedFunctionFDN = supportedFunctionFDN;
    }
    
    public WatchdogEntry(String watchdogJSONString) {
        JSONObject jso = new JSONObject(watchdogJSONString);
        watchdogState = new PetasosWUPWatchdogState(new FDN(jso.getString("wupFDN")),
                                            ComponentStatusEnum.valueOf(jso.getString("wupStatus")),
                                            Instant.ofEpochMilli(jso.getLong("lastStatusUpdate")));
        supportedFunctionFDN = new FDN(jso.getString("supportedFunctionFDN"));
    }
    
    public String toJSONString() {
        JSONObject jso = new JSONObject().put("supportedFunctionFDN", supportedFunctionFDN.getQualifiedFDN())
                .put("wupFDN", watchdogState.getWupFDN().getQualifiedFDN())
                .put("wupStatus", ComponentStatusEnum.valueOf(watchdogState.getWupStatus().getComponentWatchdogState()))
                .put("lastStatusUpdate", watchdogState.getLastStatusUpdate().toEpochMilli());
        return jso.toString();
    }
}