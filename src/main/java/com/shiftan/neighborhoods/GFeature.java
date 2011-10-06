//
// GFeature.java
//
// Copyright (c) 2010 by Nicholas Shiftan.
//

package com.shiftan.neighborhoods;

import java.util.*;
import org.opengis.feature.*;
import org.opengis.referencing.operation.*;

/**
 * GFeature
 */
public abstract class GFeature
{
    protected Map<String, Object> attributes;
    protected MathTransform xform;

    public abstract void parseGeometry(GeometryAttribute geom);

    public void setMathTransform(MathTransform xform)
    {
        this.xform = xform;
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes)
    {
        this.attributes = attributes;
    }
}
