package net.fhirbox.pegacorn.petasos.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirbox.pegacorn.petasos.common.PetasosParcelJSON;
import java.util.concurrent.CompletionStage;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;

// clustered to ensure all nodes get the notification, async (sync=false) to ensure listener
// is notified in a separate thread so our listener processing is non-blocking.
// see https://docs.jboss.org/infinispan/10.1/apidocs/org/infinispan/notifications/Listener.html
@Listener(clustered = true, sync=false)
public class ParcelMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ParcelMonitor.class);

    PetasosNode node;
    
    // Whenever a parcel is added, start a timer based on its expected end date
    // Have a timer wake (expected - now) + buffer ms. If parcel is not completed
    // (if processing outcome is present assume it's complete?)
    // TODO: does Node need to keep its own record of currently active parcel FDNs??
    @CacheEntryCreated
    public CompletionStage<Void> monitorParcel(CacheEntryCreatedEvent<String, String> event) {
        PetasosParcelJSON parcelJSON = new PetasosParcelJSON(event.getValue());
        long instantiationInstantMillis = parcelJSON.getParcelInstantiationInstant();
        long expectedCompletionInstantMillis = parcelJSON.getParcelExpectedCompletionInstant();

        // At the moment it's only a one-step timer. End date plus a buffer.
        // 200ms is just temporary arbitrary amount of ms to add as a buffer
        long millisToWaitForCompletion = expectedCompletionInstantMillis - instantiationInstantMillis + 200;

        if (millisToWaitForCompletion < 0) {
            // The parcel is overdue, need to initiate failover, so set the execution of the
            // timertask to be pretty much immediately
            millisToWaitForCompletion = 1;
        }
        
        ParcelCheckerTimerTask parcelCheckerTask = new ParcelCheckerTimerTask();
        parcelCheckerTask.setCacheReference(event.getCache());
        parcelCheckerTask.setCacheKey(event.getKey());
        Timer timer = new Timer();        
        timer.schedule(parcelCheckerTask, millisToWaitForCompletion);
        
        // Not clear if Infinispan manages non-blocking listener tasks
        // https://infinispan.org/docs/stable/titles/developing/developing.html#synchronicity_of_events
        // https://stackoverflow.com/questions/46641700/what-is-the-correct-way-to-create-an-already-completed-completablefuturevoid/46642974#46642974
        CompletableFuture<Void> cf = CompletableFuture.allOf();
        return cf;
    }
    
    // Listeners are not managed by the container, so not using injection
    public void setNodeReference(PetasosNode node) {
        this.node = node;
    }
    
    // A TimerTask to check whether the parcel has exceeded its end date.
    // If it has, the Node needs to know about it so it can initiate failover.
    public class ParcelCheckerTimerTask extends TimerTask {
        Cache<String,String> petasosParcelCache;
        String uowQualifiedFDN;
        
        @Override
        public void run() {
            // Parcel end date passed. If on the cache it could be
            // that not written to Hestia. Check if there's a processingOutcome and if not
            // assume WUP is not able to complete the UoW. Alert the node there is a problem.
            String parcelString = petasosParcelCache.get(uowQualifiedFDN);
            
            // If not on cache assume UoW completed or failed and has been written out to
            // Hestia and removed.
            if (parcelString == null) {
                return;
            }

            PetasosParcelJSON parcelJSON = new PetasosParcelJSON(parcelString);

            if (parcelJSON.getUoWProcessingOutcome() == null) {
                node.processLateParcel(uowQualifiedFDN);
            }
        }
        
        public void setCacheReference(Cache<String,String> petasosParcelCache) {
            this.petasosParcelCache = petasosParcelCache;
        }
        
        public void setCacheKey(String uowQualifiedFDN) {
            this.uowQualifiedFDN = uowQualifiedFDN;
        }
    }   
}