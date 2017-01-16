package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.Platform;

/**
 * Interface containing all relevant interfaces for Platform related events.
 *
 * Created by Mael on 11/01/2017.
 */
public interface IPlatformEvents {

    public boolean registerPlatform( Platform platform );

    public boolean updatePlatform( Platform platform );

    public boolean deletePlatform( Platform platform );

    public boolean deletePlatform( String platformId );

}