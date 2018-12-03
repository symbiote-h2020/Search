package eu.h2020.symbiote.mappings;

import eu.h2020.symbiote.core.cci.InfoModelMappingRequest;
import eu.h2020.symbiote.core.cci.InfoModelMappingResponse;
import eu.h2020.symbiote.core.internal.GetAllMappings;
import eu.h2020.symbiote.core.internal.GetSingleMapping;
import eu.h2020.symbiote.core.internal.MappingListResponse;
import eu.h2020.symbiote.model.mim.OntologyMapping;
import eu.h2020.symbiote.query.DeleteMappingRequestGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.update.UpdateRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Szymon Mueller on 20/09/2018.
 */
@Component
public class MappingManager {

    private static final Log log = LogFactory.getLog(MappingManager.class);

    private final MappingRepository mappingRepository;

    @Autowired
    public MappingManager(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public InfoModelMappingResponse registerMapping(InfoModelMappingRequest infoModelMappingRequest, SearchStorage searchStorage) {
        InfoModelMappingResponse response = null;

        String ontologyMappingId = String.valueOf(ObjectId.get());
        log.debug("Ontology id generated " + ontologyMappingId);

        OntologyMapping body = infoModelMappingRequest.getBody();
        body.setId(ontologyMappingId);

        //Saving
        OntologyMappingInternal saved = mappingRepository.save(new OntologyMappingInternal(body.getId(),body));
        searchStorage.registerMapping(infoModelMappingRequest);

        response = new InfoModelMappingResponse(HttpStatus.OK.value(), "Model registered correctly", saved.getOntologyMapping());

        return response;
    }


    public InfoModelMappingResponse deleteMapping(InfoModelMappingRequest infoModelMappingRequest, SearchStorage searchStorage) {
        InfoModelMappingResponse response;

        if (infoModelMappingRequest != null && infoModelMappingRequest.getBody() != null) {
            log.debug("Deleting mapping with id " + infoModelMappingRequest.getBody().getId());
            try {
                mappingRepository.delete(infoModelMappingRequest.getBody().getId());
                response = new InfoModelMappingResponse(HttpStatus.OK.value(), "Model deleted correctly", infoModelMappingRequest.getBody());
                DeleteMappingRequestGenerator updateRequest = new DeleteMappingRequestGenerator(infoModelMappingRequest.getBody().getId());
                searchStorage.getTripleStore().executeUpdate(updateRequest.generateRequest());
            } catch (Exception e) {
                log.error("Error occurred during deleting of the mapping : " + e.getMessage(), e);
                response = new InfoModelMappingResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred during deleting of the mapping : " + e.getMessage(), infoModelMappingRequest.getBody());
            }

        } else {
            log.info("Deleting mapping not executed - " + infoModelMappingRequest == null ? "null info model" : "null body of info model");
            response = new InfoModelMappingResponse(HttpStatus.OK.value(), "Model not deleted: could not parse info id", null);
        }

        return response;
    }

    public MappingListResponse findSingleMapping(GetSingleMapping getSingleMapping) {
        MappingListResponse response;
        if (getSingleMapping != null && StringUtils.isNotEmpty(getSingleMapping.getMappingId())) {
            log.debug("Finding single mapping with id " + getSingleMapping.getMappingId());
            try {
                Optional<OntologyMappingInternal> one = mappingRepository.findById(getSingleMapping.getMappingId());
                //TODO check if found ?
                response = new MappingListResponse(HttpStatus.OK.value(), "Found single mapping", one.isPresent()?Stream.of(one.get()).map(s->s.getOntologyMapping()).collect(Collectors.toSet()):Collections.emptySet());
            } catch( Exception e ) {
                log.error("Error occurred when finding single mapping : " + e.getMessage(), e);
                response = new MappingListResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred when finding single mapping: " + e.getMessage(), Collections.emptySet());
            }
        } else {
            log.error("Could not search for single mapping: " + getSingleMapping == null ? "Single mapping is null" : "mapping id is empty");
            response = new MappingListResponse(422, "Wrong input - could not parse mapping id for search", null);
        }

        return response;
    }

    public MappingListResponse findAllMappings(GetAllMappings getAllMapping) {
        MappingListResponse response;
        try {
            List<OntologyMappingInternal> all = mappingRepository.findAll();
            response = new MappingListResponse(HttpStatus.OK.value(), "Found all mappings", all.stream().map(s->s.getOntologyMapping()).collect(Collectors.toSet()));
        } catch( Exception e ) {
            log.error("Error occurred when finding all mappings : " + e.getMessage(), e);
            response = new MappingListResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred when finding all mappings: " + e.getMessage(), Collections.emptySet());
        }

        return response;
    }

    public List<OntologyMappingInternal> findByOntologyMappingSourceModelId(String sourceModelId) {
        return mappingRepository.findByOntologyMappingSourceModelId(sourceModelId);
    }
}
