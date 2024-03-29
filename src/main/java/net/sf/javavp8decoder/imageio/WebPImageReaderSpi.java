/*
 * This file is part of javavp8decoder.
 *
 * javavp8decoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * javavp8decoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with javavp8decoder.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.javavp8decoder.imageio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


public class WebPImageReaderSpi extends ImageReaderSpi {

    static {
        try {
            try (InputStream is = WebPImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image-webp/pom.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    version = props.getProperty("version", "undefined in pom.properties");
                } else {
                    version = System.getProperty("vavi.test.version", "undefined");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static final String version;
    static final String[] extraImageMetadataFormatClassNames = null;
    static final String[] extraImageMetadataFormatNames = null;
    static final String[] extraStreamMetadataFormatClassNames = null;
    static final String[] extraStreamMetadataFormatNames = null;
    static final String[] MIMETypes = {
        "image/webp"
    };
    static final String[] names = {
        "webp", "WEBP"
    };
    static final String nativeImageMetadataFormatClassName = "net.sf.javavp8decoder.imageio.WebPMetadata_0.1";
    static final String nativeImageMetadataFormatName = "net.sf.javavp8decoder.imageio.WebPMetadata_0.1";
    static final String nativeStreamMetadataFormatClassName = null;
    static final String nativeStreamMetadataFormatName = null;
    static final String readerClassName = "net.sf.javavp8decoder.WebPImageReader";
    static final String[] suffixes = {
        "webp"
    };
    static final boolean supportsStandardImageMetadataFormat = false;
    // Metadata formats, more information below
    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String vendorName = "https://github.com/umjammer/vavi-image-webp";
    static final String[] writerSpiNames = {
        "net.sf.javavp8decoder.WebPImageReader"
    };

    public WebPImageReaderSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              MIMETypes,
              readerClassName,
              new Class[] { ImageInputStream.class },
              writerSpiNames,
              supportsStandardStreamMetadataFormat,
              nativeStreamMetadataFormatName,
              nativeStreamMetadataFormatClassName,
              extraStreamMetadataFormatNames,
              extraStreamMetadataFormatClassNames,
              supportsStandardImageMetadataFormat,
              nativeImageMetadataFormatName,
              nativeImageMetadataFormatClassName,
              extraImageMetadataFormatNames,
              extraImageMetadataFormatClassNames);
    }

    @Override
    public boolean canDecodeInput(Object input) {
        if (!(input instanceof ImageInputStream stream)) {
            return false;
        }

        byte[] b = new byte[8];
        try {
            stream.mark();
            stream.readFully(b);
            stream.reset();
        } catch (IOException e) {
            return false;
        }
        // Cast unsigned character constants prior to comparison
        return (b[0] == (byte) 'R' && b[1] == (byte) 'I' && b[2] == (byte) 'F' && b[3] == (byte) 'F');
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new WebPImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        // Localize as appropriate
        return "Description goes here";
    }
}
