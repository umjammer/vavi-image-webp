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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class WebPMetadata extends IIOMetadata {

    static final String[] extraMetadataFormatClassNames = null;

    static final String[] extraMetadataFormatNames = null;

    static final String nativeMetadataFormatClassName = "net.sf.javavp8decoder.imageio.WebPMetadata";

    static final String nativeMetadataFormatName = "net.sf.javavp8decoder.imageio.WebPMetadata_0.1";

    static final boolean standardMetadataFormatSupported = false;

    // Keyword/value pairs
    List<String> keywords = new ArrayList<>();

    List<String> values = new ArrayList<>();

    public WebPMetadata() {
        super(standardMetadataFormatSupported,
              nativeMetadataFormatName,
              nativeMetadataFormatClassName,
              extraMetadataFormatNames,
              extraMetadataFormatClassNames);
    }

    private static void fatal(Node node, String reason) throws IIOInvalidTreeException {
        throw new IIOInvalidTreeException(reason, node);
    }

    @Override
    public Node getAsTree(String formatName) {
        if (!formatName.equals(nativeMetadataFormatName)) {
            throw new IllegalArgumentException("Bad format name!");
        }

        // Create a root node
        IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);

        // Add a child to the root node for each keyword/value pair
        Iterator<String> keywordIter = keywords.iterator();
        Iterator<String> valueIter = values.iterator();
        while (keywordIter.hasNext()) {
            IIOMetadataNode node = new IIOMetadataNode("KeywordValuePair");
            node.setAttribute("keyword", keywordIter.next());
            node.setAttribute("value", valueIter.next());
            root.appendChild(node);
        }

        return root;
    }

    @Override
    public IIOMetadataFormat getMetadataFormat(String formatName) {
        if (!formatName.equals(nativeMetadataFormatName)) {
            throw new IllegalArgumentException("Bad format name!");
        }
        return WebPMetadataFormat.getDefaultInstance();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (!formatName.equals(nativeMetadataFormatName)) {
            throw new IllegalArgumentException("Bad format name!");
        }

        Node node = root;
        if (!node.getNodeName().equals(nativeMetadataFormatName)) {
            fatal(node, "Root must be " + nativeMetadataFormatName);
        }
        node = node.getFirstChild();
        while (node != null) {
            if (!node.getNodeName().equals("KeywordValuePair")) {
                fatal(node, "Node name not KeywordValuePair!");
            }
            NamedNodeMap attributes = node.getAttributes();
            Node keywordNode = attributes.getNamedItem("keyword");
            Node valueNode = attributes.getNamedItem("value");
            if (keywordNode == null || valueNode == null) {
                fatal(node, "Keyword or value missing!");
            }

            // Store keyword and value
            keywords.add(keywordNode.getNodeValue());
            values.add(valueNode.getNodeValue());

            // Move to the next sibling
            node = node.getNextSibling();
        }
    }

    @Override
    public void reset() {
        this.keywords = new ArrayList<>();
        this.values = new ArrayList<>();
    }
}
