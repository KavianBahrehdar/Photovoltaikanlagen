package com.thd.mapserver.domain.geom;

import java.util.List;

public abstract class Curve extends Geometry{
    protected final List<Point> vertices;

    public Curve(List<Point> vertices, int srid) {
        super(srid);
        this.vertices = vertices;
    }

    public Point startPoint(){
        return vertices.get(0);
    }

    public Point endPoint(){
        return vertices.get(vertices.size()-1);
    }

    public boolean isClosed(){
        return vertices.get(0).equals(vertices.get(vertices.size()-1));
    }

    public boolean isSimple(){
        return GeometryUtils.isSimple(vertices);
    }

    public boolean isRing(){
        return isClosed() && isSimple();
    }

    public abstract Envelope envelope();

    public abstract boolean equalsExact(Geometry other);

    public abstract int dimension();
}

