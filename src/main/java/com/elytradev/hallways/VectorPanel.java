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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class VectorPanel extends JPanel {
	private static final Color OOB = new Color(0.4f, 0.4f, 0.4f);
	
	private final VectorField<DungeonTile> dungeon;
	private int zoom;
	
	public VectorPanel(VectorField<DungeonTile> dungeon, int zoom) {
		super();
		this.dungeon = dungeon;
		this.zoom = zoom;
		
		Dimension actualSize = new Dimension(dungeon.getWidth()*zoom, dungeon.getHeight()*zoom);
		this.setMinimumSize(actualSize);
		this.setPreferredSize(actualSize);
		this.setMaximumSize(actualSize);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(OOB);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int y=0; y<dungeon.getHeight(); y++) {
			for(int x=0; x<dungeon.getWidth(); x++) {
				DungeonTile tile = dungeon.get(x, y);
				//if (tile!=null) {
				if (tile!=null && tile.type!=TileType.OOB) {
					g.setColor(new Color(tile.type.color));
					g.fillRect(x*zoom, y*zoom, zoom, zoom);
					g.setColor(new Color(tile.type.color).darker());
					if (!tile.exits().contains(Cardinal.WEST)) g.fillRect(x*zoom, y*zoom, 2, zoom);
					if (!tile.exits().contains(Cardinal.EAST)) g.fillRect(x*zoom + zoom-2, y*zoom, 2, zoom);
					if (!tile.exits().contains(Cardinal.NORTH)) g.fillRect(x*zoom, y*zoom, zoom, 2);
					if (!tile.exits().contains(Cardinal.SOUTH)) g.fillRect(x*zoom, y*zoom + zoom-2, zoom, 2);
					//g.drawRect(x*zoom, y*zoom, zoom-1, zoom-1);
				}
			}
		}
	}
}
