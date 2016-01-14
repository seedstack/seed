/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.grapher.*;
import com.google.inject.grapher.graphviz.*;
import com.google.inject.spi.InjectionPoint;

import java.io.PrintWriter;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * {@link com.google.inject.grapher.InjectorGrapher} implementation that writes out a Graphviz DOT file of the graph.
 * <p>
 * Specify the {@link PrintWriter} to output to with {@link #setOut(PrintWriter)}.
 * <p>
 * Based on {@link com.google.inject.grapher.graphviz.GraphvizGrapher} which is licensed under Apache 2.0 terms
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * @author phopkins@gmail.com (Pete Hopkins)
 * @author adrien.lauer@mpsa.com
 */
class SeedGraphvizGrapher extends AbstractInjectorGrapher {
    private final Map<NodeId, GraphvizNode> nodes = Maps.newHashMap();
    private final List<GraphvizEdge> edges = Lists.newArrayList();
    private final NameFactory nameFactory;
    private final PortIdFactory portIdFactory;

    private String rankdir = "TB";
    private Pattern filter;
    private PrintWriter out;

    @Inject
    SeedGraphvizGrapher(NameFactory nameFactory, PortIdFactory portIdFactory) {
        this.nameFactory = nameFactory;
        this.portIdFactory = portIdFactory;
    }

    @Override
    protected void reset() {
        nodes.clear();
        edges.clear();
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void setRankdir(String rankdir) {
        this.rankdir = rankdir;
    }

    public void setFilter(String filter) {
        this.filter = Pattern.compile(filter);
    }

    @Override
    protected void postProcess() {
        start();

        for (GraphvizNode node : nodes.values()) {
            renderNode(node);
        }

        for (GraphvizEdge edge : edges) {
            renderEdge(edge);
        }

        finish();

        out.flush();
    }

    protected Map<String, String> getGraphAttributes() {
        Map<String, String> attrs = Maps.newHashMap();
        attrs.put("rankdir", rankdir);
        return attrs;
    }

    protected void start() {
        out.println("digraph injector {");

        Map<String, String> attrs = getGraphAttributes();
        out.println("graph " + getAttrString(attrs) + ";");
    }

    protected void finish() {
        out.println("}");
    }

    protected void renderNode(GraphvizNode node) {
        Map<String, String> attrs = getNodeAttributes(node);
        out.println(node.getIdentifier() + " " + getAttrString(attrs));
    }

    protected Map<String, String> getNodeAttributes(GraphvizNode node) {
        Map<String, String> attrs = Maps.newHashMap();

        attrs.put("label", getNodeLabel(node));
        // remove most of the margin because the table has internal padding
        attrs.put("margin", "0.02");
        attrs.put("shape", node.getShape().toString());
        attrs.put("style", node.getStyle().toString());

        return attrs;
    }

    /**
     * Creates the "label" for a node. This is a string of HTML that defines a
     * table with a heading at the top and (in the case of
     * {@link ImplementationNode}s) rows for each of the member fields.
     */
    protected String getNodeLabel(GraphvizNode node) {
        String cellborder = node.getStyle() == NodeStyle.INVISIBLE ? "1" : "0";

        StringBuilder html = new StringBuilder();
        html.append("<");
        html.append("<table cellspacing=\"0\" cellpadding=\"5\" cellborder=\"");
        html.append(cellborder).append("\" border=\"0\">");

        html.append("<tr>").append("<td align=\"left\" port=\"header\" ");
        html.append("bgcolor=\"" + node.getHeaderBackgroundColor() + "\">");

        String subtitle = Joiner.on("<br align=\"left\"/>").join(node.getSubtitles());
        if (subtitle.length() != 0) {
            html.append("<font color=\"").append(node.getHeaderTextColor());
            html.append("\" point-size=\"10\">");
            html.append(subtitle).append("<br align=\"left\"/>").append("</font>");
        }

        html.append("<font color=\"" + node.getHeaderTextColor() + "\">");
        html.append(htmlEscape(node.getTitle())).append("<br align=\"left\"/>");
        html.append("</font>").append("</td>").append("</tr>");

        for (Map.Entry<String, String> field : node.getFields().entrySet()) {
            html.append("<tr>");
            html.append("<td align=\"left\" port=\"").append(htmlEscape(field.getKey())).append("\">");
            html.append(htmlEscape(field.getValue()));
            html.append("</td>").append("</tr>");
        }

        html.append("</table>");
        html.append(">");
        return html.toString();
    }

    protected void renderEdge(GraphvizEdge edge) {
        GraphvizNode headNode = nodes.get(edge.getHeadNodeId());
        GraphvizNode tailNode = nodes.get(edge.getTailNodeId());

        if (headNode == null || tailNode == null) {
            return;
        }

        Map<String, String> attrs = getEdgeAttributes(edge);

        String tailId = getEdgeEndPoint(tailNode.getIdentifier(), edge.getTailPortId(), edge.getTailCompassPoint());

        String headId = getEdgeEndPoint(headNode.getIdentifier(), edge.getHeadPortId(), edge.getHeadCompassPoint());

        out.println(tailId + " -> " + headId + " " + getAttrString(attrs));
    }

    protected Map<String, String> getEdgeAttributes(GraphvizEdge edge) {
        Map<String, String> attrs = Maps.newHashMap();

        attrs.put("arrowhead", getArrowString(edge.getArrowHead()));
        attrs.put("arrowtail", getArrowString(edge.getArrowTail()));
        attrs.put("style", edge.getStyle().toString());

        return attrs;
    }

    private String getAttrString(Map<String, String> attrs) {
        List<String> attrList = Lists.newArrayList();

        for (Entry<String, String> attr : attrs.entrySet()) {
            String value = attr.getValue();

            if (value != null) {
                attrList.add(attr.getKey() + "=" + value);
            }
        }

        return "[" + Joiner.on(", ").join(attrList) + "]";
    }

    /**
     * Turns a {@link List} of {@link ArrowType}s into a {@link String} that
     * represents combining them. With Graphviz, that just means concatenating
     * them.
     */
    protected String getArrowString(List<ArrowType> arrows) {
        return Joiner.on("").join(arrows);
    }

    protected String getEdgeEndPoint(String nodeId, String portId, CompassPoint compassPoint) {
        List<String> portStrings = Lists.newArrayList(nodeId);

        if (portId != null) {
            portStrings.add(portId);
        }

        if (compassPoint != null) {
            portStrings.add(compassPoint.toString());
        }

        return Joiner.on(":").join(portStrings);
    }

    protected String htmlEscape(String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    protected List<String> htmlEscape(List<String> elements) {
        List<String> escaped = Lists.newArrayList();
        for (String element : elements) {
            escaped.add(htmlEscape(element));
        }
        return escaped;
    }

    @Override
    protected void newInterfaceNode(InterfaceNode node) {
        NodeId nodeId = node.getId();

        if (filter != null && !filter.matcher(nodeId.getKey().getTypeLiteral().toString()).matches()) {
            return;
        }

        GraphvizNode gnode = new GraphvizNode(nodeId);
        gnode.setStyle(NodeStyle.DASHED);
        Key<?> key = nodeId.getKey();
        gnode.setTitle(nameFactory.getClassName(key));
        gnode.addSubtitle(0, nameFactory.getAnnotationName(key));
        addNode(gnode);
    }

    @Override
    protected void newImplementationNode(ImplementationNode node) {
        NodeId nodeId = node.getId();

        if (filter != null && !filter.matcher(nodeId.getKey().getTypeLiteral().toString()).matches()) {
            return;
        }

        GraphvizNode gnode = new GraphvizNode(nodeId);
        gnode.setStyle(NodeStyle.SOLID);

        gnode.setHeaderBackgroundColor("#000000");
        gnode.setHeaderTextColor("#ffffff");
        gnode.setTitle(nameFactory.getClassName(nodeId.getKey()));

        for (Member member : node.getMembers()) {
            gnode.addField(portIdFactory.getPortId(member), nameFactory.getMemberName(member));
        }

        addNode(gnode);
    }

    @Override
    protected void newInstanceNode(InstanceNode node) {
        NodeId nodeId = node.getId();

        if (filter != null && !filter.matcher(nodeId.getKey().getTypeLiteral().toString()).matches()) {
            return;
        }

        GraphvizNode gnode = new GraphvizNode(nodeId);
        gnode.setStyle(NodeStyle.SOLID);

        gnode.setHeaderBackgroundColor("#000000");
        gnode.setHeaderTextColor("#ffffff");
        gnode.setTitle(nameFactory.getClassName(nodeId.getKey()));

        gnode.addSubtitle(0, nameFactory.getSourceName(node.getSource()));

        gnode.setHeaderBackgroundColor("#aaaaaa");
        gnode.setHeaderTextColor("#ffffff");
        gnode.setTitle(nameFactory.getInstanceName(node.getInstance()));

        for (Member member : node.getMembers()) {
            gnode.addField(portIdFactory.getPortId(member), nameFactory.getMemberName(member));
        }

        addNode(gnode);
    }

    @Override
    protected void newDependencyEdge(DependencyEdge edge) {
        GraphvizEdge gedge = new GraphvizEdge(edge.getFromId(), edge.getToId());
        InjectionPoint fromPoint = edge.getInjectionPoint();
        if (fromPoint == null) {
            gedge.setTailPortId("header");
        } else {
            gedge.setTailPortId(portIdFactory.getPortId(fromPoint.getMember()));
        }
        gedge.setArrowHead(ImmutableList.of(ArrowType.NORMAL));
        gedge.setTailCompassPoint(CompassPoint.EAST);

        edges.add(gedge);
    }

    @Override
    protected void newBindingEdge(BindingEdge edge) {
        GraphvizEdge gedge = new GraphvizEdge(edge.getFromId(), edge.getToId());
        gedge.setStyle(EdgeStyle.DASHED);
        switch (edge.getType()) {
            case NORMAL:
                gedge.setArrowHead(ImmutableList.of(ArrowType.NORMAL_OPEN));
                break;

            case PROVIDER:
                gedge.setArrowHead(ImmutableList.of(ArrowType.NORMAL_OPEN, ArrowType.NORMAL_OPEN));
                break;

            case CONVERTED_CONSTANT:
                gedge.setArrowHead(ImmutableList.of(ArrowType.NORMAL_OPEN, ArrowType.DOT_OPEN));
                break;
            default:
                // unsupported edge type (therefore left unstyled)
                break;
        }

        edges.add(gedge);
    }

    private void addNode(GraphvizNode node) {
        node.setIdentifier("x" + nodes.size());
        nodes.put(node.getNodeId(), node);
    }
}
