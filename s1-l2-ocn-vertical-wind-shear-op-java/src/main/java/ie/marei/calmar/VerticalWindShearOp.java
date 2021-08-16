package ie.marei.calmar;

import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.gpf.pointop.*;

import java.awt.image.Raster;

/**
 * The <code>OCN-Vertical-Wind-Shear</code> recalculates wind speed on Sentinel-1 Level-2 OCN products for a given height above sea level.
 *
 * @author Declan Dunne
 */
@OperatorMetadata(
        alias = "OCN-Vertical-Wind-Shear",
        version = "0.1",
        category = "Radar/SAR Applications/Ocean Applications",
        description = "The OCN-Vertical-Wind-Shear operator recalculates wind speed on\n" +
                      "Sentinel-1 Level-2 OCN products for a given height above sea level.",
        authors = "Declan Dunne, Louis de Montera",
        copyright = "Copyright (C) 2021 MaREI")
public class VerticalWindShearOp extends PixelOperator {

    @SourceProduct(alias = "Name", description = "The source product")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(defaultValue = "100.0", description = "Height above sea level")
    private double windHeight;

    @Parameter(defaultValue = "0.1", description = "Shear exponent")
    private double shearExponent;

    private OwiParameters owiParameters = null;

    /**
     * Configures all source samples that this operator requires for the computation of target samples.
     * Source sample are defined by using the provided {@link SourceSampleConfigurer}.
     * <p/>
     * <p/> The method is called by {@link #initialize()}.
     *
     * @param sampleConfigurer The configurer that defines the layout of a pixel.
     * @throws OperatorException If the source samples cannot be configured.
     */
    @Override
    protected void configureSourceSamples(SourceSampleConfigurer sampleConfigurer) throws OperatorException {
        OwiParameters owiParametersInst = getOwiParameters();
        sampleConfigurer.defineSample(0, owiParametersInst.getOwiWindSpeedName());
        sampleConfigurer.defineSample(1, owiParametersInst.getOwiLatName());
        sampleConfigurer.defineSample(2, owiParametersInst.getOwiLonName());
    }

    /**
     * Configures all target samples computed by this operator.
     * Target samples are defined by using the provided {@link TargetSampleConfigurer}.
     * <p/>
     * <p/> The method is called by {@link #initialize()}.
     *
     * @param sampleConfigurer The configurer that defines the layout of a pixel.
     * @throws OperatorException If the target samples cannot be configured.
     */
    @Override
    protected void configureTargetSamples(TargetSampleConfigurer sampleConfigurer) throws OperatorException {
        OwiParameters owiParametersInst = getOwiParameters();
        String windParameterName = owiParametersInst.getOwiWindSpeedName();
        sampleConfigurer.defineSample(0, windParameterName);
    }

    /**
     * Configures the target product via the given {@link ProductConfigurer}. Called by {@link #initialize()}.
     * <p/>
     * Client implementations of this method usually add product components to the given target product, such as
     * {@link Band bands} to be computed by this operator,
     * {@link VirtualBand virtual bands},
     * {@link Mask masks}
     * or {@link SampleCoding sample codings}.
     * <p/>
     * The default implementation retrieves the (first) source product and copies to the target product
     * <ul>
     * <li>the start and stop time by calling {@link ProductConfigurer#copyTimeCoding()},</li>
     * <li>all tie-point grids by calling {@link ProductConfigurer#copyTiePointGrids(String...)},</li>
     * <li>the geo-coding by calling {@link ProductConfigurer#copyGeoCoding()}.</li>
     * </ul>
     * <p/>
     * Clients that require a similar behaviour in their operator shall first call the {@code super} method
     * in their implementation.
     *
     * @param productConfigurer The target product configurer.
     * @throws OperatorException If the target product cannot be configured.
     * @see Product#addBand(Band)
     * @see Product#addBand(String, String)
     * @see Product#addTiePointGrid(TiePointGrid)
     * @see Product#getMaskGroup()
     */
    @Override
    protected void configureTargetProduct(ProductConfigurer productConfigurer) {
        /*
        // metadata options
        super.configureTargetProduct(productConfigurer);
        productConfigurer.copyTimeCoding();
        productConfigurer.copyTiePointGrids();
        productConfigurer.copyGeoCoding();
        */
        productConfigurer.copyMetadata();
        Product tp = productConfigurer.getTargetProduct();

        /*
        // reproject option
        Product tp = productConfigurer.getTargetProduct();
        Map<String, Object> reprojParams = new HashMap<String, Object>();
        reprojParams.put("crs", "EPSG:4326");
        //reprojParams.put("noDataValue", 0d);
        tp = GPF.createProduct("Reproject", reprojParams, tp);
        */

        OwiParameters owiParametersInst = getOwiParameters();
        double no_data = owiParametersInst.getNoData();
        Band wind = owiParametersInst.getWindBand();
        int width = wind.getRasterWidth();
        int height = wind.getRasterHeight();

        // add new bands to the target product
        // 1) add .._001_owiWindSpeed band
        String windParameterName = owiParametersInst.getOwiWindSpeedName();
        final Band windBand = tp.addBand(windParameterName, ProductData.TYPE_FLOAT32);
        windBand.setNoDataValue(no_data);
        windBand.setNoDataValueUsed(true);
        windBand.setUnit("m/s");
        windBand.setDescription("Wind speed adjusted to " + windHeight + " metres height above sea level");

        // 2) get .._001_owiLat data
        RasterDataNode owiLat = sourceProduct.getRasterDataNode(owiParametersInst.getOwiLatName());
        if (owiLat == null) {
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: missing " +
                    owiParametersInst.getOwiLatName() + " band");
        }
        MultiLevelImage owiLatImage = owiLat.getGeophysicalImage();
        Raster latImageData = owiLatImage.getData();
        float[] latData = new float[latImageData.getWidth() * latImageData.getHeight()];
        latData = latImageData.getPixels(0, 0, latImageData.getWidth(), latImageData.getHeight(), latData);

        // 3) get .._001_owiLon data
        RasterDataNode owiLon = sourceProduct.getRasterDataNode(owiParametersInst.getOwiLonName());
        if (owiLon == null) {
            throw new OperatorException("Requires a Sentinel-1 Level-2 OCN source product: missing " +
                    owiParametersInst.getOwiLonName() + " band");
        }
        MultiLevelImage owiLonImage = owiLon.getGeophysicalImage();
        Raster lonImageData = owiLonImage.getData();
        float[] lonData = new float[lonImageData.getWidth() * lonImageData.getHeight()];
        lonData = lonImageData.getPixels(0, 0, lonImageData.getWidth(), lonImageData.getHeight(), lonData);

        /*
        //PixelGeoCoding
        Band latBand = tp.addBand("lat", ProductData.TYPE_FLOAT32);
        Band lonBand = tp.addBand("lon", ProductData.TYPE_FLOAT32);
        latBand.setRasterData(ProductData.createInstance(dataLat));
        lonBand.setRasterData(ProductData.createInstance(dataLon));
        PixelGeoCoding pp = new PixelGeoCoding(latBand,lonBand,null,5);
        GeoCoding pixelGeoCoding = GeoCodingFactory.createPixelGeoCoding(
                tp.getBand("lat"),
                tp.getBand("lon"),
                null, 5);
        tp.setSceneGeoCoding(pixelGeoCoding);
        */

        // Add lat/lon coordinates. Create a TiePointGrid using the lat/lon data from 2) and 3)
        TiePointGrid latGrid = new TiePointGrid("lat", width, height, 0.0, 0.0, 1.0, 1.0, latData);
        TiePointGrid lonGrid = new TiePointGrid("lon", width, height, 0.0, 0.0, 1.0, 1.0, lonData);
        tp.addTiePointGrid(latGrid);
        tp.addTiePointGrid(lonGrid);
        TiePointGeoCoding tiePointGeoCoding = new TiePointGeoCoding(latGrid, lonGrid);
        tp.setSceneGeoCoding(tiePointGeoCoding);
    }

    /**
     * Computes the target samples from the given source samples.
     * <p/>
     * The number of source/target samples is the maximum defined sample index plus one. Source/target samples are
     * defined by using the respective sample configurer in the
     * {@link #configureSourceSamples(SourceSampleConfigurer) configureSourceSamples} and
     * {@link #configureTargetSamples(TargetSampleConfigurer) configureTargetSamples} methods.
     * Attempts to read from source samples or write to target samples at undefined sample indices will
     * cause undefined behaviour.
     *
     * @param x             The current pixel's X coordinate.
     * @param y             The current pixel's Y coordinate.
     * @param sourceSamples The source samples (= source pixel).
     * @param targetSamples The target samples (= target pixel).
     */
    @Override
    protected void computePixel(int x, int y, Sample[] sourceSamples, WritableSample[] targetSamples) {
        OwiParameters owiParametersInst = getOwiParameters();
        double no_data = owiParametersInst.getNoData();

        double windPixel = sourceSamples[0].getDouble();
        if (no_data != 0.0 && windPixel == no_data) {
            targetSamples[0].set(no_data);
        } else {
            // Recalculate wind speed profile at new height.
            final double shearCoeff = Math.pow(((windHeight / 10)), shearExponent);
            final double newWindPixel = windPixel * shearCoeff;
            targetSamples[0].set(newWindPixel);
            try {
            } catch (Exception exception) {
                throw new OperatorException(exception.getMessage());
            }
        }
    }

    /**
     * Initialises owiParameters.
     * <p/>
     * We use this method because other code invokes overridden functions such as configureSourceSamples,
     * configureTargetSamples and configureTargetProduct. We cannot assume one of these function is always called first
     * to initialise owiParameters. Also, a constructor method to initialise owiParameters will not work because
     * this.sourceProduct is not yet assigned a value at that constructor phase.
     * <p/>
     * We use this function to guarantee that this.owiParameters is assigned a value once.
     */
    private OwiParameters getOwiParameters() {
        if (this.owiParameters == null) {
            this.owiParameters = new OwiParameters(this.sourceProduct);
        }
        return this.owiParameters;
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(VerticalWindShearOp.class);
        }
    }
}
