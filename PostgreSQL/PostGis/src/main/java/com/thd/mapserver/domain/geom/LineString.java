package com.thd.mapserver.domain.geom;

import java.util.List;

public class LineString extends Curve{
    private static final String TYPENAME_LINESTRING = "LineString";

    public LineString(List<Point> vertices, int srid){
        super(vertices, srid);
        if (vertices.size() < 2) {
            throw new IllegalArgumentException(String.format("Invalid number of points in LineString (found %s - must be >= 2)", vertices.size()));
        }
    }

    public int numPoints(){
        return vertices.size();
    }

    public Point pointN(int n){
        return vertices.get(n);
    }

    public int getSrid() {
        return srid;
    }
    @Override
    public String asText() {
        var wktBuilder = new StringBuilder();
        wktBuilder.append("LINESTRING(");

        var delimiter = "";
        for(var vertex : vertices){
            var coord = GeometryUtils.convertCoordinatesToWkt(vertex);
            wktBuilder.append(delimiter + coord);
            delimiter = ", ";
        }

        wktBuilder.append(")");
        return wktBuilder.toString();
    }

    @Override
    public Envelope envelope() {
        var startPoint = startPoint();
        var minX = startPoint.getX();
        var minY = startPoint.getY();
        var maxX = startPoint.getX();
        var maxY = startPoint.getY();

        for(var i = 0; i < vertices.size(); i++){
            var vertex = vertices.get(i);
            if(vertex.getX()< minX) {
                minX = vertex.getX();
            }
            else if(vertex.getX() > maxX){
                maxX = vertex.getX();
            }

            if(vertex.getY() < minY){
                minY = vertex.getY();
            }
            else if(vertex.getY() > minY){
                maxY = vertex.getY();
            }
        }

        return new Envelope(minX, minY, maxX, maxY);
    }

    @Override
    public boolean equalsExact(Geometry other) {
        if(!(other instanceof LineString)){
            return false;
        }

        var otherLineString = (LineString) other;
        var otherVertices = otherLineString.vertices;

        if(vertices.size() != otherVertices.size()){
            return false;
        }

        for(var i = 0; i< vertices.size(); i++){
            if(!vertices.get(i).equals(otherVertices.get(i))){
                return false;
            }
        }

        return true;
    }

    @Override
    public String geometryType() {
        return TYPENAME_LINESTRING;
    }

    @Override
    public int dimension() {
        return 1;
    }

}

