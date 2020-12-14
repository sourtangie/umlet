package com.baselet.element;

import java.util.Collection;

import com.baselet.control.basics.geom.Rectangle;
import com.baselet.element.interfaces.GridElement;

public class GridElementUtils {

	public static Rectangle getGridElementsRectangle(Collection<GridElement> gridElements) {
		int x = Integer.MAX_VALUE;
		int y = Integer.MAX_VALUE;
		int x2 = Integer.MIN_VALUE;
		int y2 = Integer.MIN_VALUE;
		for (GridElement ge : gridElements) {
			x = Math.min(ge.getRectangle().getX(), x);
			y = Math.min(ge.getRectangle().getY(), y);
			x2 = Math.max(ge.getRectangle().getX2(), x2);
			y2 = Math.max(ge.getRectangle().getY2(), y2);
		}
		return new Rectangle(x, y, x2 - x, y2 - y);
	}

}
