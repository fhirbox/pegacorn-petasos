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
import javax.inject.Inject;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;

import net.fhirbox.pegacorn.petasos.node.ParcelMonitor;
import net.fhirbox.pegacorn.petasos.model.FDN;
import org.infinispan.Cache;

@ApplicationScoped
public class PetasosNode {
 
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
        initialiseHestiaConnection();
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
                    // Need to monitor site status? Keep it simple, heartbeat should
                    // provide updates, all we need to do is try to register the WUP
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
    
    private void initialiseHestiaConnection() {
        // need jdbc string, will be over SSL to Postgres
        // Hestia db sharded by service so separate dbs per service
        // Need to be configured per service and Petasos will need them
        // Also remember the Writer will have to break down the parcel to match
        // the db design
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