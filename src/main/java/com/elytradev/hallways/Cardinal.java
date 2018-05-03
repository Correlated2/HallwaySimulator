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

public enum Cardinal {
	NORTH( 0,-1),
	EAST ( 1, 0),
	SOUTH( 0, 1),
	WEST (-1, 0);
	
	private int x;
	private int y;
	
	Cardinal(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int xOfs() { return x; }
	public int yOfs() { return y; }
	
	public Cardinal cw() {
		switch(this) {
		case NORTH: return EAST;
		case EAST: return SOUTH;
		case SOUTH: return WEST;
		default:
		case WEST: return NORTH;
		}
	}
	
	public Cardinal ccw() {
		switch(this) {
		case NORTH: return WEST;
		case EAST: return NORTH;
		case SOUTH: return EAST;
		default:
		case WEST: return SOUTH;
		}
	}
	
	public static Cardinal fromTo(Vec2i from, Vec2i to) {
		boolean we = from.x==to.x;
		boolean ns = from.y==to.y;
		if (ns&&we || !ns&&!we) return NORTH; //These vectors are not arranged in cardinal directions
		
		if (to.x>from.x) return EAST;
		if (to.x<from.x) return WEST;
		if (to.y<from.y) return NORTH;
		if (to.y>from.y) return SOUTH;
		
		return NORTH; //this code *really* shouldn't happen.
	}
}
