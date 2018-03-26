package eu.h2020.symbiote.handlers;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Created by Szymon Mueller on 13/03/2018.
 */
public class DatatypeInfo {

    private String datatype;

    private String dataPred;

    private String dataObj;

    public DatatypeInfo(String datatype, String dataPred, String dataObj) {
        this.datatype = datatype;
        this.dataPred = dataPred;
        this.dataObj = dataObj;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDataPred() {
        return dataPred;
    }

    public void setDataPred(String dataPred) {
        this.dataPred = dataPred;
    }

    public String getDataObj() {
        return dataObj;
    }

    public void setDataObj(String dataObj) {
        this.dataObj = dataObj;
    }

    @Override
    public String toString() {
        return "DatatypeInfo{" +
                "datatype='" + datatype + '\'' +
                ", dataPred='" + dataPred + '\'' +
                ", dataObj='" + dataObj + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DatatypeInfo that = (DatatypeInfo) o;

        return new EqualsBuilder()
                .append(datatype, that.datatype)
                .append(dataPred, that.dataPred)
                .append(dataObj, that.dataObj)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(datatype)
                .append(dataPred)
                .append(dataObj)
                .toHashCode();
    }
}
