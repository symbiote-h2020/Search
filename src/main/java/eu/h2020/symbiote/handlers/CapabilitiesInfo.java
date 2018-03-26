package eu.h2020.symbiote.handlers;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;

/**
 * Created by Szymon Mueller on 13/03/2018.
 */
public class CapabilitiesInfo {


    private String capabilityName;
    private Map<String,ParameterInfo> parameters;

    public CapabilitiesInfo(String capabilityName, Map<String, ParameterInfo> parameters) {
        this.capabilityName = capabilityName;
        this.parameters = parameters;
    }

    public String getCapabilityName() {
        return capabilityName;
    }

    public void setCapabilityName(String capabilityName) {
        this.capabilityName = capabilityName;
    }

    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "CapabilitiesInfo{" +
                "capabilityName='" + capabilityName + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CapabilitiesInfo that = (CapabilitiesInfo) o;

        return new EqualsBuilder()
                .append(capabilityName, that.capabilityName)
                .append(parameters, that.parameters)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(capabilityName)
                .append(parameters)
                .toHashCode();
    }
}
