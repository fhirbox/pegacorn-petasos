/*
 * Based on the code by Mark A. Hunter at
 * https://github.com/fhirbox/pegacorn-communicate-iris/tree/master/src/main/java/net/fhirbox/pegacorn/communicate/iris/utilities
 * and updated for Infinispan 10.x
 * 
 * This class creates the clustered cache manager and configures the shared cache.
 * 
 */
package net.fhirbox.pegacorn.petasos.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;

import net.fhirbox.pegacorn.petasos.node.ParcelMonitor;
import net.fhirbox.pegacorn.petasos.common.PetasosParcelJSON;
import net.fhirbox.pegacorn.petasos.common.PetasosWUPWatchdogStateJSON;
import net.fhirbox.pegacorn.petasos.model.FDN;
import net.fhirbox.pegacorn.petasos.model.PetasosWUPWatchdogState;

import org.infinispan.Cache;

@ApplicationScoped
public class PetasosNode {
    // not very nice, but to keep simple string lists in Infinispan without having to
    // serialise and deserialise
    public static final String MAP_ENTRY_DELIMITER = "#@#@";

    private FDN nodeFDN;
    // map keyed by UoW FDN with associated list of parcels which share that UoW FDN
    private ConcurrentHashMap<String,List<String>> multicasts = new ConcurrentHashMap<>(128);
    // map keyed by parcel FDN with associated UoW FDN
    private ConcurrentHashMap<String, String> activeMulticasts = new ConcurrentHashMap<>(128);
    private Object multicastSemaphore = new Object();
    private Object capabilitySemaphore = new Object();

    @Resource(name = "DefaultManagedExecutorService")
    ManagedExecutorService executor;
    
    @Inject
    DefaultCacheManager petasosCacheManager;   

    // The clustered cache
    private Cache<String, String> petasosParcelCache;
    private Cache<String, String> petasosWatchdogCache;
    // shared map which contains a map of key => uowFDN, value => list of WUP FDNs (multicast use only)
    private Cache<String, String> uowToWUPMap;
    // shared map which contains a map of key => WUP Function FDNs to => list of WUP FDNs
    private Cache<String, String> capabilityMap;
    
    // TODO: other configured sites and endpoints
    // need to know comms mechanism for types of activity to implement this
    // will contain *other* sites, not itself
    ArrayList<String> siteConnectionEndpoints = new ArrayList<>();

    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosParcelCache = petasosCacheManager.getCache("petasos-parcel-cache", true);
        petasosWatchdogCache = petasosCacheManager.getCache("petasos-watchdog-cache", true);
        uowToWUPMap = petasosCacheManager.getCache("petasos-uow-to-wup-map", true);
        ParcelMonitor parcelMonitor = new ParcelMonitor();
        parcelMonitor.setNodeReference(this);
        petasosParcelCache.addListener(parcelMonitor);
    }
    
    public PetasosNode() {
        // create FDN and register CI Status, deployment name, site and pod need to 
        // come from system vars. The pod name provides the uniqueness in this instance as only
        // one Node per pod and Kubernetes won't allow duplicate pod names.
        nodeFDN = new FDN("deployment=aether.site=site-a.pod="+System.getenv("MY_POD_NAME")+".node=PetasosNode");

        startHeartbeat();
        initialiseHestiaConnection();
        startAuditMonitor();
    }
        
    public void registerWUPWithOtherSites(PetasosWUPWatchdogState watchdogEntry) {
        ArrayList<Callable<Integer>> taskList = new ArrayList<>();
        
        siteConnectionEndpoints.forEach(connectionEndpoint -> {
            CIStatusForwardTask ci = new CIStatusForwardTask(watchdogEntry, (String)connectionEndpoint);
            taskList.add(ci);
        });

        try {
            List<Future<Integer>> forwardTaskOutcomes = executor.invokeAll(taskList);

            for (Future<Integer> forwardTaskOutcome : forwardTaskOutcomes) {
                if (forwardTaskOutcome.get() != 1) {
                    // flag site as unavailable?? or heartbeat should do this?
                    // ArrayList needs FDN (to check status) and endpoint (to connect)?
                    // Need to monitor site status? Keep it simple, heartbeat should
                    // provide updates, all we need to do is try to register the WUP
                    // if it fails, bad luck
                }
            }
        }
        catch (InterruptedException ie) {
            // since this is probably just the container or node being shut down
            // there is probably no point doing anything here
        }
        catch (ExecutionException ee) {
            // Might add a return value. Do we care if it doesn't work? If the forward
            // fails are we assuming there is a problem with the site and need to flag
            // it as unresponsive? If a forward fails then the site/pod endpoint could not
            // be contacted. Heartbeat should pick this up so no action required?
        }
    }

    // capture multicasts so we can keep track of them. The UoW FDN is a functional FDN
    // plus hash and should be the same across all sites.
/*    public void registerMulticastParcel(String uowQualifiedFDN, String parcelFDN) {
        synchronized (this.multicastSemaphore) {
            if (this.multicasts.containsKey(uowQualifiedFDN) == false) {
                multicasts.put(uowQualifiedFDN, new ArrayList<String>());
            }
            this.multicasts.get(uowQualifiedFDN).add(parcelFDN);
        }
    }*/

    public void registerMulticastParcel(String uowQualifiedFDN, PetasosParcelJSON parcelJSON) {
        synchronized (this.multicastSemaphore) {
            if (uowToWUPMap.containsKey(uowQualifiedFDN) == false) {
                uowToWUPMap.put(uowQualifiedFDN, parcelJSON.getWUPFDN());
            } else {
                // not nice but Infinispan will serialize/deserialize lists and needs a whitelist'
                // of classes to serialize/deserialize
                StringBuilder wupFDNs = new StringBuilder().append(MAP_ENTRY_DELIMITER).append(parcelJSON.getWUPFDN());
                uowToWUPMap.replace(uowQualifiedFDN, wupFDNs.toString());
            }
        }
    }
    
    public void registerWUPCapability(FDN wupFDN, FDN functionFDN) {
        synchronized (this.capabilitySemaphore) {
            if (capabilityMap.containsKey(functionFDN.getQualifiedFDN()) == false) {
                capabilityMap.put(functionFDN.getQualifiedFDN(), wupFDN.getQualifiedFDN());
            } else {
                // not nice but Infinispan will serialize/deserialize lists and needs a whitelist'
                // of classes to serialize/deserialize
                StringBuilder wupFDNs = new StringBuilder().append(MAP_ENTRY_DELIMITER).append(wupFDN.getQualifiedFDN());
                uowToWUPMap.replace(functionFDN.getQualifiedFDN(), wupFDNs.toString());
            }
        }
    }

    // removes all multicast parcels from the Node's internal register, basically
    // this should be called when a multicast parcel has hit the finished state.
/*    public void deregisterMulticastParcels(String uowQualifiedFDN) {
        this.multicasts.remove(uowQualifiedFDN);
    }
    
    public List<String> getMulticast(String uowQualifiedFDN) {
        return this.multicasts.get(uowQualifiedFDN);
    }
    
    // when a multicast activity is started, it should be added to the map
    public void addActiveMulticast(String parcelFDN, String uowQualifiedFDN) {
        activeMulticasts.put(parcelFDN, uowQualifiedFDN);
    }

    public void removeActiveMulticast(String parcelFDN) {
        activeMulticasts.remove(parcelFDN);
    }
*/    
    public void updateCIStatus(PetasosWUPWatchdogState watchdogEntry) {
        petasosWatchdogCache.put(watchdogEntry.getWupFDN().getQualifiedFDN(), new PetasosWUPWatchdogStateJSON(watchdogEntry).toJSONString());
        // TODO: forward to other sites
    }
    
    private void initialiseHestiaConnection() {
        // need jdbc string, will be over SSL to Postgres
        // Hestia db sharded by service so separate dbs per service
        // Need to be configured per service and Petasos will need them
        // Also remember the Writer will have to break down the parcel to match
        // the db design
    }
    
    private void startAuditMonitor() {
        // create a writer and let it go.
        // Currently allowing 1 writer per node type per site. Not sure how to manager
        // multiple writers as have not found anything in Infinispan API to get owner or
        // (more importantly) the back up owner (i.e. the node that wrote the entry) of a
        // cache entry, so not sure how multiple writers repeatedly passing over the cache
        // could be co-ordinated efficiently.
        // Writer registers itself and checks against CI Status Map for any active writers.
        // If none, updates its status to active and checks again to make sure none has started
        // in the meantime. If one has, it checks the status time and if its last status update is 
        // earliest (i.e. it started first) it keeps going, else it puts itself back in an idle
        // state and then periodically checks the active writer's status. If the active writer's
        // status is no longer active whenever it checks, it takes over following the same
        // process as above.
    }
    
    // kick off a neverending thread which will do the heartbeat process
    private void startHeartbeat() {
        HeartbeatMonitor heartbeat = new HeartbeatMonitor();
        executor.submit(heartbeat);
    }
    
    public class HeartbeatMonitor implements Callable<Integer> {
        // needs access to cache (updating watchdog shared cache)
        // needs access to endpoints (forwarding status to other sites)
        // needs node FDN (updating watchdog shared cache)
        // move to separate class file once Node-to-Node communication mechanism is known
        // If using a cache listener, needs to ignore own FDN, needs to keep track of all
        // petasos nodes in an internal quick lookup map. If status of one changes to failed
        // or not responsive - what to do, how to shutdown pod?
        public Integer call() {
            return new Integer(1);
        }
    }
    
    // just placeholder, there will be a heartbeat client, server and status/parcel
    // update server and client for each node (although parcels only move between
    // site endpoints). These are apparently to be implemented using Netty.
    public class CIStatusForwardTask implements Callable<Integer> {
        // TBD: this will be either REST or server:port combo?
        // TODO: change the var name once comm type is known
        private static final int NUM_CONNECTION_RETRIES = 3;
        String connectionEndpoint;
        PetasosWUPWatchdogState watchdogState;
        
        public CIStatusForwardTask(PetasosWUPWatchdogState watchdogState, String connectionEndpoint) {
            this.watchdogState = watchdogState;
            this.connectionEndpoint = connectionEndpoint;
        }
        
        public Integer call() {
            int connectionAttempts = 0;
//            try {
                // connection and forwarding
                // number of retries and timeout
                while (connectionAttempts < NUM_CONNECTION_RETRIES) {
                    // connect and send
                    connectionAttempts++;
                }
                
                if (connectionAttempts == NUM_CONNECTION_RETRIES) {
                    // might not need to do anything, if problem connecting to another
                    // Node, heartbeat should take care of it
                }
//            }
//            catch () {
  //              return new Integer(0);
    //        }
            return new Integer(1);
        }
    }
}