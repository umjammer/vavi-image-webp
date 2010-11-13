package net.sf.javavp8decoder.imageio;


import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class WebPMetadataFormat extends IIOMetadataFormatImpl {

	// Create a single instance of this class (singleton pattern)
	private static WebPMetadataFormat defaultInstance =
		new WebPMetadataFormat();

	// Make constructor private to enforce the singleton pattern
	private WebPMetadataFormat() {
		// Set the name of the root node
		// The root node has a single child node type that may repeat
		super("net.sf.javavp8decoder.imageio.WebPMetadata_0.1",
		      CHILD_POLICY_REPEAT);

		// Set up the "KeywordValuePair" node, which has no children
		addElement("KeywordValuePair",
		           "net.sf.javavp8decoder.imageio.WebPMetadata_0.1",
		           CHILD_POLICY_EMPTY);

		// Set up attribute "keyword" which is a String that is required
		// and has no default value
		addAttribute("KeywordValuePair", "keyword", DATATYPE_STRING,
		             true, null);
		// Set up attribute "value" which is a String that is required
		// and has no default value
		addAttribute("KeywordValuePair", "value", DATATYPE_STRING,
		             true, null);
	}

	// Check for legal element name
	public boolean canNodeAppear(String elementName,
	                             ImageTypeSpecifier imageType) {
		return elementName.equals("KeywordValuePair");
	}

	// Return the singleton instance
	public static WebPMetadataFormat getDefaultInstance() {
		return defaultInstance;
	}
}
