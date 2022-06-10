package ie.marei.calmar;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.OperatorException;

public class OwiParameters {

    private final String[][] possibleOwiParameterNames = {
            {"vv_001_owiLat", "vv_001_owiLon", "vv_001_owiWindSpeed", "vv_001_owiWindDirection", "vv_001_owiWindQuality",  "vv_001_owiInversionQuality", "vv_001_owiLandFlag", "vv_001_owiMask", "vv_001_owiIncidenceAngle"},
            {"hh_001_owiLat", "hh_001_owiLon", "hh_001_owiWindSpeed", "hh_001_owiWindDirection", "hh_001_owiWindQuality",  "hh_001_owiInversionQuality", "hh_001_owiLandFlag", "hh_001_owiMask", "hh_001_owiIncidenceAngle"}
    };

    private Product sourceProduct;
    private String owiLatName = "";
    private String owiLonName = "";
    private String owiWindSpeedName = "";
    private String owiWindDirectionName = "";
    private String owiIncidenceAngleName = "";
    private String owiWindQualityName = "";
    private String owiInversionQualityName = "";
    private String owiMaskName = "";

    public OwiParameters(Product sourceProduct) {

        if (sourceProduct == null) {
            throw new OperatorException("Source product is missing");
        }
        this.sourceProduct = sourceProduct;

        int nameIndex = -1;
        for (int i = 0; i < this.possibleOwiParameterNames.length; i++) {
            String[] parameters = this.possibleOwiParameterNames[i];
            Band checkBand = sourceProduct.getBand(parameters[0]);
            if (checkBand != null) {
                nameIndex = i;
                break;
            }
        }

        if (nameIndex == -1) {
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiLat not found");
        }

        String[] parameters = this.possibleOwiParameterNames[nameIndex];

        // owiLat
        this.owiLatName = parameters[0];

        // owiLon
        if (sourceProduct.getBand(parameters[1]) != null)
            this.owiLonName = parameters[1];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiLon not found");

        // owiWindSpeed
        if (sourceProduct.getBand(parameters[2]) != null)
            this.owiWindSpeedName = parameters[2];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiWindSpeed not found");

        // owiWindDirection
        if (sourceProduct.getBand(parameters[3]) != null)
            this.owiWindDirectionName = parameters[3];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiWindDirection not found");

        // owiWindQuality
        if (sourceProduct.getBand(parameters[4]) != null)
            this.owiWindQualityName = parameters[4];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiWindQuality not found");

        // owiInversionQuality
        if (sourceProduct.getBand(parameters[5]) != null)
            this.owiInversionQualityName = parameters[5];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiInversionQuality not found");

        // owiMask
        if (sourceProduct.getBand(parameters[6]) != null)
            this.owiMaskName = parameters[6];
        else if (sourceProduct.getBand(parameters[7]) != null)
            this.owiMaskName = parameters[7];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiLandFlag or owiMask not found");

        // owiIncidenceAngle
        if (sourceProduct.getBand(parameters[8]) != null)
            this.owiIncidenceAngleName = parameters[8];
        else
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: owiIncidenceAngle not found");
    }

    // lat
    public String getOwiLatName() {
        return this.owiLatName;
    }

    public Band getOwiLatBand() {
        return this.sourceProduct.getBand(this.owiLatName);
    }

    // lon
    public String getOwiLonName() {
        return this.owiLonName;
    }

    public Band getOwiLonBand() {
        return this.sourceProduct.getBand(this.owiLonName);
    }

    // owiWindSpeed
    public String getOwiWindSpeedName() {
        return this.owiWindSpeedName;
    }

    public Band getOwiWindSpeedBand() {
        return this.sourceProduct.getBand(this.owiWindSpeedName);
    }

    // owiWindDirection
    public String getOwiWindDirectionName() {
        return this.owiWindDirectionName;
    }

    public Band getOwiWindDirectionBand() {
        return this.sourceProduct.getBand(this.owiWindDirectionName);
    }

    // owiWindQuality
    public String getOwiWindQualityName() {
        return this.owiWindQualityName;
    }

    public Band getOwiWindQualityBand() {
        return this.sourceProduct.getBand(this.owiWindQualityName);
    }

    // owiInversionQuality
    public String getOwiInversionQualityName() {
        return this.owiInversionQualityName;
    }

    public Band getOwiInversionQualityBand() {
        return this.sourceProduct.getBand(this.owiInversionQualityName);
    }

    // owiLandFlag
    public String getOwiMaskName() {
        return this.owiMaskName;
    }

    public Band getMaskNameBand() {
        return this.sourceProduct.getBand(this.owiMaskName);
    }

    // owiIncidenceAngle
    public String getOwiIncidenceAngleName() {
        return this.owiIncidenceAngleName;
    }

    public Band getOwiIncidenceAngleBand() {
        return this.sourceProduct.getBand(this.owiIncidenceAngleName);
    }
}
