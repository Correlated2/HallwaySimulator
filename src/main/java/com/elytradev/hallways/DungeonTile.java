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

import java.util.EnumSet;

public class DungeonTile implements ITagSerializable, Cloneable {
	public String palette;
	public TileType type;
	public EnumSet<Cardinal> exits;
	
	public DungeonTile() {
		this.type = TileType.OOB;
		this.exits = EnumSet.noneOf(Cardinal.class);
	}
	
	public DungeonTile(TileType type) {
		this.type = type;
		this.exits = EnumSet.noneOf(Cardinal.class);
	}
	
	@Override
	public DungeonTile clone() {
		DungeonTile result = new DungeonTile(type);
		result.exits = EnumSet.copyOf(exits);
		return result;
	}
	
	public EnumSet<Cardinal> exits() { return exits; }
	
	public void clearExits() {
		exits.clear();
	}
	
	public void setExits(Cardinal... cardinals) {
		exits.clear();
		for(Cardinal cardinal : cardinals) {
			exits.add(cardinal);
		}
	}

	@Override
	public ITagCompound serialize(ITagCompound tag) {
		int i = 0;
		for (Cardinal c : exits) {
			i |= (1 << c.ordinal());
		}
		tag.setByte("Exits", (byte)i);
		tag.setByte("Type", (byte)type.ordinal());
		return tag;
	}

	@Override
	public ITagCompound deserialize(ITagCompound tag) {
		exits.clear();
		int exitsSet = tag.getByte("Exits")&0xFF;
		for (Cardinal c : Cardinal.values()) {
			if ((exitsSet & (1 << c.ordinal())) != 0) {
				exits.add(c);
			}
		}
		type = TileType.values()[tag.getInteger("Type")];
		return tag;
	}
}
