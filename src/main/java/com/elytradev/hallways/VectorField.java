/*
 * MIT License
 *
 * Copyright (c) 2017-2018 Isaac Ellingson (Falkreon), Una Thompson (unascribed)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.hallways;
import static java.lang.Math.*;

import java.util.function.Supplier;

public class VectorField<T> {
	
	private int width = 1;
	private int height = 1;
	private T[] cells;
	private ITagCompound tag = null;
	
	@SuppressWarnings("unchecked")
	public VectorField(int width, int height) {
		this.width = width;
		this.height = height;
		if (this.width <1) this.width=1;
		if (this.height<1) this.height=1;
		if (this.width >65534) this.width=65534; //64KiB should be enough for everybody
		if (this.height>65534) this.height=65534;
		cells = (T[]) new Object[width*height];
	}
	
	@SuppressWarnings("unchecked")
	public void clear() {
		cells = (T[]) new Object[width*height];
	}
	
	/**
	 * Runs down a line of your choosing, calling a function for each cell involved.
	 * NORMALLY does not double-visit rooms, but it is neither guaranteed to avoid
	 * revisiting rooms, nor is it guaranteed to ensure that it will visit the last
	 * room at x2,y2.
	 * 
	 * <p>There's still an outstanding off-by-one bug in this code, hastily patched by
	 * adding 1 to numSteps, but ideally we'd get the 
	 */
	public void visitLine(int x1, int y1, int x2, int y2, CellCallable<T> function ) {
		//This algorithm is basically a Bresenham line
		
		//We start by getting the X and Y delta, basically a vector to travel along
		float dx = x2-x1;
		float dy = y2-y1;
		
		//Now we SORT OF normalize that vector so that each time we step, we go 1 x or y, and some fraction of the other axis.
		float scale = max( abs(dx), abs(dy));
		float xstep = dx / scale;
		float ystep = dy / scale;
		
		int numSteps = (int)scale; //the number of iterations we'll need to travel *should* be equal to the larger of the two cardinal distances
		
		float curX = x1+0.5f;
		float curY = y1+0.5f;
		
		for(int i=0; i<numSteps; i++) {
			function.call(this, (int)curX, (int)curY);
			curX+= xstep;
			curY+= ystep;
		}
		
	}
	
	/**
	 * Simple rectangle visitor, guaranteed to visit every cell in the rectangle once and only once.
	 */
	public void visitRect(int x1, int y1, int width, int height, CellCallable<T> function) {
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				function.call(this, x1+x, y1+y);
			}
		}
	}
	
	public T get(int x, int y) {
		if (x<0 || y<0 || x>=width || y>=height) return null; //SILENT BUT DEADLY
		return cells[y*width+x];
	}
	
	public T getOrCreate(int x, int y, Supplier<T> supplier) {
		if (x<0 || y<0 || x>=width || y>=height) return supplier.get();
		T result = cells[y*width+x];
		if (result==null) {
			result = supplier.get();
			cells[y*width+x] = result;
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return whatever previously occupied the cell.
	 */
	public T put(int x, int y, T t) {
		if (x<0 || y<0 || x>=width || y>=height) return null;
		int index = y*width+x;
		T result = cells[index];
		cells[index] = t;
		return result;
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public boolean isInBounds(int x, int y) {
		return
				x>=0 &&
				y>=0 &&
				x<width &&
				y<height;
	}
	
	public boolean hasTag() {
		return tag != null;
	}
	public ITagCompound getTag() {
		return tag;
	}
	public void setTag(ITagCompound tag) {
		this.tag = tag;
	}

}
