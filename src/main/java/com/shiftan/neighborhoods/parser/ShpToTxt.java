//
// ShpToTxt.java
//
// Copyright (c) 2010 by Nicholas Shiftan.
//

package com.shiftan.neighborhoods.parser;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.*;
import com.shiftan.neighborhoods.*;
import org.apache.commons.cli.*;

/**
 * ShpToTxt
 */
public class ShpToTxt
{
    public static void main(String[] args)
    {
        Option oDatum;
        Options opts;

        String fileName = null;
        String datum = null;

        oDatum = OptionBuilder.withLongOpt("datum")
                                   .hasArg()
                                   .withDescription("authority:code (i.e. EPSG:4617 for NAD83)")
                                   .create("d");

        opts = new Options();
        opts.addOption(oDatum);

        try
        {
            CommandLineParser parser;
            CommandLine line;

            parser = new PosixParser();
            line = parser.parse(opts, args);

            fileName = line.getArgs()[0];

            if (line.hasOption("datum"))
            {
                datum = line.getOptionValue("datum");
            }
        }
        catch (Exception e)
        {
            HelpFormatter help;
            help = new HelpFormatter();
            help.printHelp("shp2txt shapefile_zip", opts);
            System.exit(-1);
        }

        parse(fileName, System.out, datum);
    }

    /**
     *
     */
    private static void parse(String fileName, PrintStream stream, String datum)
    {
        try
        {
            NumberFormat nf;
            ZipFile zip;
            ShapefileParser parser;
            List<GFeature> features;

            nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(5);
            zip = new ZipFile(fileName);
            parser = new ShapefileParser();
            parser.parse(zip, datum);
            features = parser.getFeatures();

            for (GFeature feature : features)
            {
                if (feature instanceof GMultiPolygon)
                {
                    GMultiPolygon polygon;
                    String name;

                    polygon = (GMultiPolygon)feature;
                    name = (String)feature.getAttributes().get("NAME");

                    stream.print(name + ",");

                    for (Iterator<GPoint> it = polygon.getPts().iterator(); it.hasNext(); )
                    {
                        GPoint pt;
                        pt = it.next();
                        stream.print(nf.format(pt.getLatitude()) + "d" + nf.format(pt.getLongitude()));
                        if (it.hasNext())
                        {
                            stream.print(";");
                        }
                    }
                    stream.println("");

                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
