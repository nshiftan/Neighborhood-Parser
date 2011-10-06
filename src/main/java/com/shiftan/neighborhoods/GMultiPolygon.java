//
// GMultiPolygon.java
//
// Copyright (c) 2010 by Nicholas Shiftan.
//

package com.shiftan.neighborhoods;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.geometry.*;
import org.opengis.feature.*;
import org.opengis.geometry.*;
import org.opengis.referencing.operation.*;

/**
 * GMultiPolygon
 */
public class GMultiPolygon extends GFeature
{
    private List<GPoint> pts;

    public GMultiPolygon()
    {
        pts = new ArrayList<GPoint>();
    }

    @Override
    public void parseGeometry(GeometryAttribute geom)
    {
        MultiPolygon polygon;

        polygon = (MultiPolygon) geom.getValue();
        for (Coordinate coord : polygon.getCoordinates())
        {
            if (xform == null)
            {
                GPoint pt;
                pt = new GPoint();
                pt.setLatitude(coord.y);
                pt.setLongitude(coord.x);
                pts.add(pt);
            }
            else
            {
                try
                {
                    DirectPosition origin;
                    DirectPosition destination;
                    GPoint pt;

                    origin = new GeneralDirectPosition(coord.x, coord.y);
                    destination = xform.transform(origin, null);

                    pt = new GPoint();
                    pt.setLatitude(destination.getCoordinate()[0]);
                    pt.setLongitude(destination.getCoordinate()[1]);
                    pts.add(pt);
                }
                catch (TransformException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<GPoint> getPts()
    {
        return pts;
    }

    public void setPts(List<GPoint> pts)
    {
        this.pts = pts;
    }

}
