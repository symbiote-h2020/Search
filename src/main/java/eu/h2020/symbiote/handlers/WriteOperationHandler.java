//package eu.h2020.symbiote.handlers;
//
//import eu.h2020.symbiote.communication.SearchCommunicationHandler;
//import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
//import eu.h2020.symbiote.core.internal.CoreSspResourceRegisteredOrModifiedEventPayload;
//import eu.h2020.symbiote.filtering.CachedMap;
//import eu.h2020.symbiote.model.mim.Platform;
//
//import java.util.List;
//import java.util.concurrent.*;
//
///**
// * Created by Szymon Mueller on 19/07/2018.
// */
//public class WriteOperationHandler {
//
//    private final ThreadPoolExecutor executorService;
//
//    private final int writerExecutorCoreThreads;
//    private final int writerExecutorMaxThreads;
//    private final int writerExecutorKeepAliveInMinutes;
//    private final ResourceHandler resourceHandler;
//    private final PlatformHandler platformHandler;
//
//
//    public WriteOperationHandler( ResourceHandler resourceHandler, int writerExecutorCoreThreads, int writerExecutorMaxThreads, int writerExecutorKeepAliveInMinutes) {
//        this.resourceHandler = resourceHandler;
//        this.writerExecutorCoreThreads = writerExecutorCoreThreads;
//        this.writerExecutorMaxThreads = writerExecutorMaxThreads;
//        this.writerExecutorKeepAliveInMinutes = writerExecutorKeepAliveInMinutes;
//        this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(writerExecutorCoreThreads);
//        executorService.setMaximumPoolSize(writerExecutorMaxThreads);
//        executorService.setKeepAliveTime(writerExecutorKeepAliveInMinutes, TimeUnit.MINUTES);
////        executorService.setRejectedExecutionHandler();
//    }
//
//    public void scheduleResourceRegistration(CoreResourceRegisteredOrModifiedEventPayload resource ) {
//        Callable<Boolean> callable = () -> {
//            resourceHandler.registerResource(resource);
//            return Boolean.TRUE;
//        };
//        executorService.submit(callable);
//    }
//
//    public void scheduleResourceModification(CoreResourceRegisteredOrModifiedEventPayload resource ) {
//        Callable<Boolean> callable = () -> {
//            resourceHandler.updateResource(resource);
//            return Boolean.TRUE;
//        };
//        executorService.submit(callable);
//    }
//
//    public void scheduleResourceDeletion( List<String> resourceIds ) {
//        Callable<Boolean> callable = () -> {
//            resourceIds.stream().forEach( id -> resourceHandler.deleteResource( id ));
//            return Boolean.TRUE;
//        };
//        executorService.submit(callable);
//    }
//
//    public void schedulePlatformRegistration( Platform platform ) {
//        Callable<Boolean> callable = () -> {
//            platformHandler.registerPlatform(platform);
//            return Boolean.TRUE;
//        };
//        executorService.submit(callable);
//    }
//
//    public void schedulePlatformModification( Platform platform ) {
//        Callable<Boolean> callable = () -> {
//            platformHandler.updatePlatform(platform);
//            return Boolean.TRUE;
//        };
//        executorService.submit(callable);
//    }
//
//    public void schedulePlatformDeleted( String platformId ) {
//        Callable<Boolean> callable = () -> {
//            platformHandler.deletePlatform(platformId);
//            return Boolean.TRUE;
//        };
//        executorService.submit(callable);
//    }
//
//
//
//}
