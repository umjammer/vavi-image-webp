/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Iterator;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;


/**
 * Integration test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-07 nsano initial version <br>
 */
public class Integration {

    /**
     *
     * @param args 0: version
     */
    public static void main(String[] args) throws Exception {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        Iterator<ImageReaderSpi> i = iioRegistry.getServiceProviders(ImageReaderSpi.class, true);
        while (i.hasNext()) {
            ImageReaderSpi p = i.next();
            if (p.getClass() == net.sf.javavp8decoder.imageio.WebPImageReaderSpi.class) {
System.err.println("compare: " + p.getVersion() + " and " + args[0]);
                assert p.getVersion().equals(args[0]) : "not equals version: " + p.getVersion() + " and " + args[0];
                return;
            }
        }
        assert false : "no suitable spi";
    }
}
