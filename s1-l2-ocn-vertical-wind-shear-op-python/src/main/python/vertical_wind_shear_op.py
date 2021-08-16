import owi_parameters
import numpy
import snappy

from snappy import jpy
from snappy import FlagCoding, TiePointGrid, TiePointGeoCoding, RasterDataNode

#Float = jpy.get_type('java.lang.Float')

class VerticalWindShearOp:
    def __init__(self):
        self.wind_height = None
        self.shear_exponent = None
        self.owi_wind_speed_band = None
        self.wind_band = None

    def initialize(self, context):
        source_product = context.getSourceProduct()
        if source_product is None:
            raise RuntimeError("Source product is missing")
        print('vertical_wind_shear_op initialize: source product location is', source_product.getFileLocation())

        # Retrieve parameters defined in vertical_wind_shear_op-info.xml
        self.wind_height = context.getParameter('windHeight')
        self.shear_exponent = context.getParameter('shearExponent')
        print('initialize vertical_wind_shear_op: wind_height =', self.wind_height, ', shear_exponent =', self.shear_exponent)

        width = source_product.getSceneRasterWidth()
        height = source_product.getSceneRasterHeight()
        print('initialize vertical_wind_shear_op: width =', width, ', height =', height)

        # Create the target product
        wind_speed_product = snappy.Product(context.getId(), 'vertical_wind_shear_op', width, height)

        # copy metadata from source product to the new product
        snappy.ProductUtils.copyMetadata(source_product, wind_speed_product)

        owi_parameters_inst = owi_parameters.OwiParameters(source_product)
        self.owi_wind_speed_band = owi_parameters_inst.get_wind_band()

        # add new bands to the target product
        # 1) add .._001_owiWindSpeed band
        wind_parameter_name = owi_parameters_inst.get_owi_wind_speed_name()
        self.wind_band = wind_speed_product.addBand(wind_parameter_name, snappy.ProductData.TYPE_FLOAT32)
        # self.wind_band.setNoDataValue(Float.NaN)
        self.wind_band.setNoDataValue(owi_parameters_inst.get_no_data())
        self.wind_band.setNoDataValueUsed(True)
        self.wind_band.setUnit("m/s")
        self.wind_band.setDescription('Wind speed adjusted to ' + str(self.wind_height) + ' metres height above sea level')

        # 2) get .._001_owiLat data
        owi_lat = source_product.getRasterDataNode(owi_parameters_inst.get_owi_lat_name())
        if owi_lat is None:
            raise RuntimeError("Requires a Sentinel-1 Level-2 OCN source product: missing " +
                               owi_parameters_inst.get_owi_lat_name() + " band")
        owi_lat_image = owi_lat.getGeophysicalImage()
        lat_image_data = owi_lat_image.getData()
        lat_data = numpy.zeros(lat_image_data.getWidth() * lat_image_data.getHeight(), numpy.float32)
        lat_data = lat_image_data.getPixels(0, 0, lat_image_data.getWidth(), lat_image_data.getHeight(), lat_data)

        # 3) get .._001_owiLon data
        owi_lon = source_product.getRasterDataNode(owi_parameters_inst.get_owi_lon_name())
        if owi_lon is None:
            raise RuntimeError("Requires a Sentinel-1 Level-2 OCN source product: missing " +
                               owi_parameters_inst.get_owi_lon_name() + " band")
        owi_lon_image = owi_lon.getGeophysicalImage()
        lon_image_data = owi_lon_image.getData()
        lon_data = numpy.zeros(lon_image_data.getWidth() * lon_image_data.getHeight(), numpy.float32)
        lon_data = lon_image_data.getPixels(0, 0, lon_image_data.getWidth(), lon_image_data.getHeight(), lon_data)

        # Add lat/lon coordinates. Create a TiePointGrid using the lat/lon data from 2) and 3)
        lat_grid = TiePointGrid("lat", width, height, 0.0, 0.0, 1.0, 1.0, lat_data)
        lon_grid = TiePointGrid("lon", width, height, 0.0, 0.0, 1.0, 1.0, lon_data)
        wind_speed_product.addTiePointGrid(lat_grid)
        wind_speed_product.addTiePointGrid(lon_grid)
        tie_point_geocoding = TiePointGeoCoding(lat_grid, lon_grid)
        wind_speed_product.setSceneGeoCoding(tie_point_geocoding)

        # Provide the created target product to the framework so the computeTileStack method can be called
        context.setTargetProduct(wind_speed_product)

    def computeTileStack(self, context, target_tiles, target_rectangle):
        # The required source data for the computation can be retrieved by getSourceTile(...) via the context object
        tile = context.getSourceTile(self.owi_wind_speed_band, target_rectangle)

        # Retrieve the actual data
        samples = tile.getSamplesFloat()

        # Recalculate wind speed profile at new height. Convert the data into numpy data to do the operation
        shear_coeff = pow(((self.wind_height / 10.0)), self.shear_exponent)
        data = numpy.array(samples, dtype=numpy.float32) * shear_coeff

        # The target tile which shall be filled with data are provided as parameter to this method
        wind_speed_tile = target_tiles.get(self.wind_band)

        # Set the result to the target tiles
        wind_speed_tile.setSamples(data)

    def dispose(self, context):
        pass
