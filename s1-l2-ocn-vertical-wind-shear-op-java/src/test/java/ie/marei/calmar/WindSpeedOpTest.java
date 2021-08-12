package ie.marei.calmar;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

/**
 * @author Norman
 */
public class WindSpeedOpTest {

    @Test
    public void testFindBand() throws Exception {
        Product product = new Product("dummy", "dummy", 10, 10);
        addBand(product, "a", 500);
        addBand(product, "b", 600);
        addBand(product, "c", 620);
        addBand(product, "d", 700);
        addBand(product, "e", 712);
        addBand(product, "f", 715);
        addBand(product, "g", 799);
        addBand(product, "h", 800);
        addBand(product, "i", 801);
        addBand(product, "j", 899);
        addBand(product, "k", 900);
        addBand(product, "l", 16000);

        //assertEquals("b", 600);
        //assertEquals("h", 800);
    }

    @Test
    public void testFindBand_nothingFound() throws Exception {
        Product product = new Product("dummy", "dummy", 10, 10);
        addBand(product, "a", 500);
        addBand(product, "b", 600);
        addBand(product, "c", 620);
        addBand(product, "d", 700);
        addBand(product, "e", 712);
        addBand(product, "f", 715);
        addBand(product, "g", 799);
        addBand(product, "h", 800);
        addBand(product, "i", 801);
        addBand(product, "j", 899);
        addBand(product, "k", 900);

        //assertNull(400);
        //assertNull(701);
        //assertNull(901);
    }

    @Test
    public void testFindBand_nothingFound_2() throws Exception {
        Product product = new Product("dummy", "dummy", 10, 10);
        addBand(product, "a");
        addBand(product, "b");
        addBand(product, "c");
        addBand(product, "d");
        addBand(product, "e");
        addBand(product, "f");
        addBand(product, "g");
        addBand(product, "h");
        addBand(product, "i");
        addBand(product, "j");
        addBand(product, "k");
        addBand(product, "l");

        //assertNull(600);
    }

    public static void addBand(Product product, String bandName) {
        Band a = new Band(bandName, ProductData.TYPE_FLOAT32, 10, 10);
        product.addBand(a);
    }

    public static void addBand(Product product, String bandName, int wavelength) {
            Band a = new Band(bandName, ProductData.TYPE_FLOAT32, 10, 10);
            a.setSpectralWavelength(wavelength);
            product.addBand(a);
        }
    }
