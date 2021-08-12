import numpy
from snappy import jpy, Band, ProductIO, ProductUtils

class OwiParameters:
    _possibleOwiParameterNames = [["vv_001_owiWindSpeed", "vv_001_owiLat", "vv_001_owiLon"],
                                  ["hh_001_owiWindSpeed", "hh_001_owiLat", "hh_001_owiLon"]]

    def __init__(self, source_product):
        self._source_product = None
        self._wind_band = None
        self._no_data = 0.0
        self._owi_wind_speed_name = ""
        self._owi_lat_name = ""
        self._owi_lon_name = ""

        if source_product is None:
            raise RuntimeError("Source product is missing")
        self._source_product = source_product

        wind = None
        for parameters in self._possibleOwiParameterNames:
            check_band = source_product.getBand(parameters[0])
            if check_band is not None:
                self._owi_wind_speed_name = parameters[0]
                self._owi_lat_name = parameters[1]
                self._owi_lon_name = parameters[2]
                wind = check_band
                break
        if wind is None:
            raise RuntimeError("Requires a Sentinel 1 Level 2 OCN source product")
        self._no_data = wind.getGeophysicalNoDataValue()
        self._wind_band = wind

    def get_wind_band(self):
        return self._wind_band

    def get_no_data(self):
        return self._no_data

    def get_owi_wind_speed_name(self):
        return self._owi_wind_speed_name

    def get_owi_lat_name(self):
        return self._owi_lat_name

    def get_owi_lon_name(self):
        return self._owi_lon_name
