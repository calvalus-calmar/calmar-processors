package ie.marei.calmar;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.OperatorException;

public class OwiParameters {

    private final String[][] possibleOwiParameterNames = {
            {"vv_001_owiWindSpeed", "vv_001_owiLat", "vv_001_owiLon"},
            {"hh_001_owiWindSpeed", "hh_001_owiLat", "hh_001_owiLon"}
    };

    private Product sourceProduct;
    private Band windBand;
    private double noData = 0.0;
    private String owiWindSpeedName = "";
    private String owiLatName = "";
    private String owiLonName = "";

    public OwiParameters(Product sourceProduct) {

        if (sourceProduct == null) {
            throw new OperatorException("Source product is missing");
        }
        this.sourceProduct = sourceProduct;

        Band wind = null;
        for (String[] parameters : this.possibleOwiParameterNames) {
            Band checkBand = sourceProduct.getBand(parameters[0]);
            if (checkBand != null) {
                this.owiWindSpeedName = parameters[0];
                this.owiLatName = parameters[1];
                this.owiLonName = parameters[2];
                wind = checkBand;
                break;
            }
        }
        if (wind == null) {
            throw new OperatorException("Requires a Sentinel 1 Level 2 OCN source product");
        }
        this.noData = wind.getGeophysicalNoDataValue();
        this.windBand = wind;
    }

    public Band getWindBand() {
        return windBand;
    }

    public double getNoData() {
        return noData;
    }

    public String getOwiWindSpeedName() {
        return owiWindSpeedName;
    }

    public String getOwiLatName() {
        return owiLatName;
    }

    public String getOwiLonName() {
        return owiLonName;
    }
}
