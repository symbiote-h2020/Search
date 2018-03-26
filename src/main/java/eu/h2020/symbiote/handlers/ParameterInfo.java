package eu.h2020.symbiote.handlers;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by Szymon Mueller on 13/03/2018.
 */
public class ParameterInfo {

    private String parameterName;
    private String parameterMandatory;
    private List<DatatypeInfo> datatypes;

    public ParameterInfo(String parameterName, String parameterMandatory, List<DatatypeInfo> datatypes) {
        this.parameterName = parameterName;
        this.parameterMandatory = parameterMandatory;
        this.datatypes = datatypes;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterMandatory() {
        return parameterMandatory;
    }

    public void setParameterMandatory(String parameterMandatory) {
        this.parameterMandatory = parameterMandatory;
    }

    public List<DatatypeInfo> getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(List<DatatypeInfo> datatypes) {
        this.datatypes = datatypes;
    }

    @Override
    public String toString() {
        return "ParameterInfo{" +
                "parameterName='" + parameterName + '\'' +
                ", parameterMandatory='" + parameterMandatory + '\'' +
                ", datatypes=" + datatypes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ParameterInfo that = (ParameterInfo) o;

        return new EqualsBuilder()
                .append(parameterName, that.parameterName)
                .append(parameterMandatory, that.parameterMandatory)
                .append(datatypes, that.datatypes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(parameterName)
                .append(parameterMandatory)
                .append(datatypes)
                .toHashCode();
    }
}
