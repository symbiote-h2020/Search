package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.mim.InformationModel;

/**
 * Created by Szymon Mueller on 04/10/2018.
 */
public interface IModelEvents {

    void registerInformationModel( InformationModel model );

    void deleteInformationModel( InformationModel model );

    void updateInformationModel( InformationModel model );

}
