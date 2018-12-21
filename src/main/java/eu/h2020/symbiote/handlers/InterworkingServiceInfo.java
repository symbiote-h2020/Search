package eu.h2020.symbiote.handlers;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Class storing info abour interworking service: its url and iri.
 * <p>
 * Created by Szymon Mueller on 23/06/2018.
 */
@Document
public class InterworkingServiceInfo {

    @Id
    private String interworkingServiceIRI;

    private String interworkingServiceURL;

    public String platformId;

    public String informationModeIri;

    public InterworkingServiceInfo() {
    }

    public InterworkingServiceInfo(String interworkingServiceIRI, String interworkingServiceURL, String platformId, String informationModelIri) {
        this.interworkingServiceIRI = interworkingServiceIRI;
        this.interworkingServiceURL = interworkingServiceURL;
        this.platformId = platformId;
        this.informationModeIri = informationModelIri;
    }

    public String getInterworkingServiceIRI() {
        return interworkingServiceIRI;
    }

    public void setInterworkingServiceIRI(String interworkingServiceIRI) {
        this.interworkingServiceIRI = interworkingServiceIRI;
    }

    public String getInterworkingServiceURL() {
        return interworkingServiceURL;
    }

    public void setInterworkingServiceURL(String interworkingServiceURL) {
        this.interworkingServiceURL = interworkingServiceURL;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getInformationModeIri() {
        return informationModeIri;
    }

    public void setInformationModeIri(String informationModeIri) {
        this.informationModeIri = informationModeIri;
    }
}
