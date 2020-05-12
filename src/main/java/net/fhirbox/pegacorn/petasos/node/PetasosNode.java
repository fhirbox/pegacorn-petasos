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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;

import net.fhirbox.pegacorn.petasos.node.ParcelMonitor;
import net.fhirbox.pegacorn.petasos.model.FDN;
import net.fhirbox.pegacorn.petasos.model.PetasosParcel;

import org.infinispan.Cache;

@ApplicationScoped
public class PetasosNode {
 
    private PetasosNode node;
    private FDN nodeFDN;

    @Resource(name = "DefaultManagedExecutorService")
    ManagedExecutorService executor;
    
    @Inject
    DefaultCacheManager petasosCacheManager;   

    // The clustered cache
    private Cache<String, String> petasosParcelCache;
    private Cache<String, String> petasosWatchdogCache;
    
    // TODO: other configured sites and endpoints
    // need to know comms mechanism for types of activity to implement this
    // will contain *other* sites, not itself
    ArrayList<String> siteConnectionEndpoints = new ArrayList<>();

    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosParcelCache = petasosCacheManager.getCache("petasos-parcel-cache", true);
        petasosWatchdogCache = petasosCacheManager.getCache("petasos-watchdog-cache", true);
        ParcelMonitor parcelMonitor = new ParcelMonitor();
        parcelMonitor.setNodeReference(this);
        petasosParcelCache.addListener(parcelMonitor);

    }
    
    public PetasosNode() {
        // create FDN and register CI Status, deployment name, site and pod need to 
        // come from system vars. The pod name provides the uniqueness in this instance as only
        // one Node per pod and Kubernetes won't allow duplicate pod names.
        nodeFDN = new FDN("deployment=aether.site=site-a.pod="+System.getenv("MY_POD_NAME")+".component=PetasosNode");
        WatchdogEntry watchdogEntry = new WatchdogEntry(nodeFDN.getQualifiedFDN());
        startHeartbeat();
    }
        
    public void registerWUPWithOtherSites(WatchdogEntry watchdogEntry) {
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
                    // Need to monitor site status? no, heartbeat should provide updates,
                    // just need to check available site
                }
            }
        }
        catch (InterruptedException ie) {
            // since this is probably just the container or node being shut down
            // there is probably no point doing anything here
        }
        catch (ExecutionException ee) {
            // might add a return value. Do we care if it doesn't work? If the forward
            // fails are we assuming there is a problem with the site and need to flag
            // it as unresponsive. This should be done in the task itself so maybe 
            // don't do anything for now.
        }
    }

    public void updateCIStatus(WatchdogEntry watchdogEntry) {
        petasosWatchdogCache.put(watchdogEntry.getWatchdogState().getWupFDN().getQualifiedFDN(), watchdogEntry.toJSONString());
        // TODO: forward to other sites
    }
    
    public void processLateParcel(String uowQualifiedFDN) {
        // failover processing by owning node?
        // check if owning node state is ok. If owning node is OK let it do the
        // reallocation, else we need an owning node to flag the parcel is theirs as
        // all bets are off and all Nodes try to inject into their
        // own version of the WUP and whoever gets there first, wins.
        // TODO: assumption is that a WUP will generate a new parcel
        // Do we need to force to Hestia and remove unfinished one since the Writer can't
        // go around removing unfinished parcels unless a decent buffer from the expected
        // end date is added.
        //Plus the parcel will remain on the cache.
        String parcelJSON = petasosParcelCache.get(uowQualifiedFDN);
        if (parcelJSON == null) {
            // UoW ended up finishing (only way it could be removed) so do nothing
            return;
        }
    }
    
    // kick off a neverending thread which will do the heartbeat process
    private void startHeartbeat() {
        HeartbeatMonitor heartbeat = new HeartbeatMonitor();
        executor.submit(heartbeat);
    }
    
    public class HeartbeatMonitor implements Callable<Integer> {
        // needs access to cache (updating shared cache)
        // needs access to endpoints (forwarding status to other sites)
        // needs node FDN (updating shared cache)
        // move to separate class file once Node-to-Node communication mechanism is known
        // If using a cache listener, needs to ignore own FDN, needs to keep track of all
        // petasos nodes in an internal quick lookup map. If status of one changes to failed
        // or not responsive - what to do, how to shutdown pod?
        public Integer call() {
            return new Integer(1);
        }
    }
    
    
    public class CIStatusForwardTask implements Callable<Integer> {
        // TBD: this will be either REST or server:port combo?
        // TODO: change the var name once comm type is known
        private static final int NUM_CONNECTION_RETRIES = 3;
        String connectionEndpoint;
        WatchdogEntry ciStatus;
        
        public CIStatusForwardTask(WatchdogEntry ciStatus, String connectionEndpoint) {
            this.ciStatus = ciStatus;
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