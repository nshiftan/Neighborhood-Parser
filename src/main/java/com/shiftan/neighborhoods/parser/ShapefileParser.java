//
// ShapefileParser.java
//
// Copyright (c) 2010 by Nicholas Shiftan.
//

package com.shiftan.neighborhoods.parser;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;
import org.geotools.data.*;
import org.geotools.data.shapefile.*;
import org.geotools.feature.*;
import org.geotools.referencing.*;
import org.opengis.feature.*;
import org.opengis.feature.type.*;
import org.opengis.referencing.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.operation.*;
import com.shiftan.neighborhoods.*;

/**
 * ShapefileParser
 *
 */
public class ShapefileParser
{
    private static final String TEMP_DIR = "/var/tmp/neighborhoods";

    private Logger logger;

    private List<GFeature> gFeatures;

    /**
     * Default no-arg constructor.
     */
    public ShapefileParser()
    {
        logger = Logger.getLogger(getClass().getName());
        gFeatures = new ArrayList<GFeature>();
    }

    /**
     * Parse the given zip file.
     */
    public void parse(ZipFile zip, String datum) throws IOException
    {
        ShapefileDataStore shp;
        String typeName;
        FeatureSource featureSource;
        FeatureCollection features;
        FeatureType schema;
        MathTransform xform;

        shp = getShapefile(zip);
        typeName = shp.getTypeNames()[0];
        featureSource = shp.getFeatureSource(typeName);
        features = featureSource.getFeatures();
        schema = featureSource.getSchema();

        xform = getWGS84Transform(schema, datum);

        for (FeatureIterator it = features.features(); it.hasNext(); )
        {
            Feature feature;
            GeometryAttribute geom;
            GFeatureType type;
            GFeature gFeature = null;
            Map<String, Object> attributes;

            feature = it.next();
            geom = feature.getDefaultGeometryProperty();
            try
            {
                type = GFeatureType.valueOf(geom.getType().getName().getLocalPart());
            }
            catch (RuntimeException e)
            {
                logger.warning("Unsupported GFeatureType [" + geom.getType().getName() + "]");
                continue;
            }

            attributes = new HashMap<String, Object>();
            for (PropertyDescriptor pd : schema.getDescriptors())
            {
                Name name;
                Property prop;

                name = pd.getName();
                prop = feature.getProperty(name);

                if (!prop.equals(geom))
                {
                    attributes.put(name.toString(), prop.getValue());
                }
            }

            switch (type)
            {
                case MultiPolygon:
                    gFeature = new GMultiPolygon();
                    break;

                default:
                    throw new RuntimeException("Unsupported GeometryType");
            }

            gFeature.setMathTransform(xform);
            gFeature.parseGeometry(geom);
            gFeature.setAttributes(attributes);
            gFeatures.add(gFeature);
        }
    }

    public List<GFeature> getFeatures()
    {
        return gFeatures;
    }

    // <editor-fold defaultstate="collapsed" desc="Encapsulated getters & setters">

    /**
     * Given a feature source, return the transformation necessary to transform
     * its coordinate into Google Map-friendly coordinatres.
     *
     * Note that the destination CRS we use here isn't exactly right.
     */
    private static MathTransform getWGS84Transform(FeatureType schema, String datum)
    {
        try
        {
            CoordinateReferenceSystem originCRS;
            CoordinateReferenceSystem destinationCRS;

            originCRS = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
            destinationCRS = CRS.decode("EPSG:4326");

            if (originCRS == null && datum != null)
            {
                originCRS = CRS.decode(datum);
            }
            if (originCRS == null)
            {
                throw new RuntimeException("Missing an origin datum.  Please supply one via the command line.");
            }

            return CRS.findMathTransform(originCRS, destinationCRS, true);
        }
        catch (NoSuchAuthorityCodeException e)
        {
            throw new RuntimeException(e);
        }
        catch (FactoryException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given a ZipFile name, return the corresponding ShapefileDataStore.
     */
    private static ShapefileDataStore getShapefile(ZipFile zip)
                                            throws IOException
    {
        File dir;
        File shp = null;

        dir = createTempDir("shp");
        dir.mkdir();

        for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); )
        {
            ZipEntry entry;
            String name;
            File file;
            InputStream is;
            OutputStream os;
            int b;

            entry = e.nextElement();
            is = zip.getInputStream(entry);
            name = entry.getName();

            file = new File(dir, name);
            os = new FileOutputStream(file);
            while ((b = is.read()) != -1)
            {
                os.write(b);
            }

            os.close();
            is.close();

            if (name.endsWith(".shp"))
            {
                shp = file;
            }
        }

        if (shp == null)
        {
            return null;
        }

        return new ShapefileDataStore(shp.toURI().toURL());
    }

    /**
     *
     */
    private static File createTempDir(String prefix)
                               throws IOException
    {
        File f;
        File dir;
        String path;

        dir = new File(TEMP_DIR);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        f = File.createTempFile(prefix, null, dir);
        path = f.getAbsolutePath();
        path = path.substring(0, path.length() - 4);
        f.delete();
        f = new File(path);
        f.mkdir();
        return f;
    }

    // </editor-fold>

}
