package com.github.MichalKC.manylands.tiled;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class ZoneShape {
    public abstract boolean contains(Vector2 point);
    public abstract Rectangle getBoundingBox();
    
    public static class RectangleZone extends ZoneShape {
        private final Rectangle rectangle;
        
        public RectangleZone(Rectangle rectangle) {
            this.rectangle = new Rectangle(rectangle);
        }
        
        @Override
        public boolean contains(Vector2 point) {
            return rectangle.contains(point);
        }
        
        @Override
        public Rectangle getBoundingBox() {
            return new Rectangle(rectangle);
        }
        
        public Rectangle getRectangle() {
            return rectangle;
        }
    }
    
    public static class CircleZone extends ZoneShape {
        private final Circle circle;
        
        public CircleZone(Circle circle) {
            this.circle = new Circle(circle);
        }
        
        @Override
        public boolean contains(Vector2 point) {
            return circle.contains(point);
        }
        
        @Override
        public Rectangle getBoundingBox() {
            Rectangle bounds = new Rectangle();
            bounds.x = circle.x - circle.radius;
            bounds.y = circle.y - circle.radius;
            bounds.width = circle.radius * 2;
            bounds.height = circle.radius * 2;
            return bounds;
        }
        
        public Circle getCircle() {
            return circle;
        }
    }
    
    public static class PolygonZone extends ZoneShape {
        private final Polygon polygon;
        private final Rectangle boundingBox;
        
        public PolygonZone(Polygon polygon) {
            this.polygon = new Polygon(polygon.getVertices());
            this.boundingBox = calculateBoundingBox();
        }
        
        @Override
        public boolean contains(Vector2 point) {
            return polygon.contains(point.x, point.y);
        }
        
        @Override
        public Rectangle getBoundingBox() {
            return new Rectangle(boundingBox);
        }
        
        public Polygon getPolygon() {
            return polygon;
        }
        
        private Rectangle calculateBoundingBox() {
            float[] vertices = polygon.getVertices();
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float maxY = Float.MIN_VALUE;
            
            for (int i = 0; i < vertices.length; i += 2) {
                minX = Math.min(minX, vertices[i]);
                maxX = Math.max(maxX, vertices[i]);
                minY = Math.min(minY, vertices[i + 1]);
                maxY = Math.max(maxY, vertices[i + 1]);
            }
            
            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
    }
}
