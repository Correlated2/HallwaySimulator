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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FieldGenerator {
	private static final int MAX_CONNECT_ITERATIONS = 3000;
	
	Random random = new Random();
	private Set<String> palettes = new HashSet<>();
	private VectorField<DungeonTile> field;
	private ArrayList<Room> rooms = new ArrayList<>();
	private ArrayList<Hall> halls;
	private ArrayList<Door> doors;
	public int maxRoomSize = 7;
	public int minRoomSize = 3;
	public int x1 = 0;
	public int y1 = 0;
	public int x2 = 0;
	public int y2 = 0;
	
	public FieldGenerator withSeed(long seed) {
		random.setSeed(seed);
		return this;
	}
	
	public FieldGenerator withRoomSize(int min, int max) {
		this.maxRoomSize = max;
		this.minRoomSize = min;
		return this;
	}
	
	public FieldGenerator withPalettes(String... ids) {
		palettes.clear();
		for(String id : ids) palettes.add(id);
		return this;
	}
	
	public FieldGenerator withEntrance(int x, int y) {
		x1=x;
		y1=y;
		return this;
	}
	
	public FieldGenerator withExit(int x, int y) {
		x2=x;
		y2=y;
		return this;
	}
	
	
	public VectorField<DungeonTile> generate(int size) {
		return generate(new VectorField<DungeonTile>(size, size));
	}
	
	public VectorField<DungeonTile> generate(VectorField<DungeonTile> in) {
		field = in;
		if (palettes.isEmpty()) palettes.add("normal");
		
		if (x1==x2 && y1==y2) {
			int centerX = in.getWidth()/2;
			int centerY = in.getHeight()/2;
			//kick the exits out to random sides of the box.
			List<Vec2i> sides = new ArrayList<>();
			sides.add(new Vec2i(centerX,      0));
			sides.add(new Vec2i(     0, centerY));
			sides.add(new Vec2i(centerX, in.getHeight()-1));
			sides.add(new Vec2i(in.getWidth()-1, centerY));
			
			Vec2i p1 = sides.get(random.nextInt(sides.size()));
			Vec2i p2 = sides.get(random.nextInt(sides.size()));
			x1=p1.x;
			y1=p1.y;
			x2=p2.x;
			y2=p2.y;
		}
		
		int dungeonSize = in.getWidth();
		int initialCellSize = maxRoomSize+2;
		int cellSize = initialCellSize;
		for(int i=0;i<initialCellSize; i++) {
			cellSize = initialCellSize;
			while (dungeonSize%cellSize>i && cellSize<initialCellSize*2) cellSize++;
			if (dungeonSize%cellSize<=i) break;
		}
		
		int cellsAcross = dungeonSize / cellSize;
		
		List<Room> disconnected = new ArrayList<>();
		for(int y=0; y<cellsAcross; y++) {
			for(int x=0; x<cellsAcross; x++) {
				int width = random.nextInt(maxRoomSize-minRoomSize)+minRoomSize;
				int height = random.nextInt(maxRoomSize-minRoomSize)+minRoomSize;
				int wiggleX = (cellSize-2)-width;
				int roomX = random.nextInt(wiggleX)+1;
				int wiggleY = (cellSize-2)-height;
				int roomY = random.nextInt(wiggleY)+1;
				Room room = new Room(roomX + (x*cellSize), roomY + (y*cellSize), width, height);
				rooms.add(room);
				disconnected.add(room);
				plotRoom(room);
			}
		}
		
		List<Room> connected = new ArrayList<>();
		Room seed = disconnected.remove(random.nextInt(disconnected.size()));
		connected.add(seed);
		
		boolean didConnectInitial = false;
		for(int i=0; i<15; i++) {
			RoomPair pair = tryMakePair(cellSize, seed, disconnected);
			if (tryConnectRooms(pair.a, pair.b)) {
				didConnectInitial = true;
				if (!connected.contains(pair.a)) connected.add(pair.a);
				if (!connected.contains(pair.b)) connected.add(pair.b);
				break;
			}
		}
		
		if (!didConnectInitial) {
			return null; //POOP. How did we not connect the first two rooms? IN FIFTEEN TRIES? (Protip: this has literally never happened)
			//If this actually happened, it'd probably be best to just bail on all data generated so far and call generate again.
		}
		
		int iterations = 0;
		while(iterations<MAX_CONNECT_ITERATIONS && !disconnected.isEmpty()) {
			iterations++;
			RoomPair pair = findRoomPair(cellSize, connected, disconnected);
			if (tryConnectRooms(pair.a, pair.b)) {
				if (!connected.contains(pair.a)) connected.add(pair.a);
				if (!connected.contains(pair.b)) connected.add(pair.b);
				disconnected.remove(pair.a);
				disconnected.remove(pair.b);
			}
		}
		
		
		//SANITY CHECKS - Uncull Faces
		//int unculledFaces = 0;
		for(int y=0; y<field.getHeight(); y++) {
			for(int x=0; x<field.getWidth(); x++) {
				DungeonTile tile = field.get(x, y);
				if (tile==null || tile.type==TileType.OOB) continue;
				EnumSet<Cardinal> brokenExits = EnumSet.noneOf(Cardinal.class);
				for(Cardinal dir : tile.exits) {
					DungeonTile target = field.get(x+dir.xOfs(), y+dir.yOfs());
					if (target==null || target.type==TileType.OOB) {
						brokenExits.add(dir);
						//unculledFaces++;
					}
				}
				tile.exits.removeAll(brokenExits);
			}
		}
		//System.out.println("Unculled Faces: "+unculledFaces);
		//UNCULLED FACES NOW DOWN TO NONE O_O
		
		return in;
	}
	
	private RoomPair tryMakePair(int cellSize, Room unconnected, List<Room> connectedRooms) {
		List<Room> closeEnough = new ArrayList<>();
		for(Room r : connectedRooms) {
			int manhattan = Math.abs(unconnected.x - r.x) + Math.abs(unconnected.y - r.y);
			if (manhattan < cellSize*2) closeEnough.add(r);
		}
		if (closeEnough.isEmpty()) return null;
		
		Room connectTo = closeEnough.get(random.nextInt(closeEnough.size()));
		return new RoomPair(unconnected, connectTo);
	}
	
	private RoomPair findRoomPair(int cellSize, List<Room> connectedRooms, List<Room> unconnectedRooms) {
		Collections.shuffle(connectedRooms);
		Collections.shuffle(unconnectedRooms);
		
		for(Room r : unconnectedRooms) {
			RoomPair pair = tryMakePair(cellSize, r, connectedRooms);
			if (pair!=null) return pair;
		}
		
		return null;
	}
	
	/**
	 * Gets a terminal which sits either vertically or horizontally (but not diagonally) adjacent to Room a,
	 * and inclined towards Room b.
	 */
	private Vec2i getTerminal(Room a, Room b) {
		int aCenterX = a.x+(a.width/2);
		int aCenterY = a.y+(a.height/2);
		int bCenterX = b.x+(b.width/2);
		int bCenterY = b.y+(b.height/2);
		
		int dx = bCenterX-aCenterX;
		int dy = bCenterY-aCenterY;
		
		int xdir = (int)Math.signum(dx);
		int ydir = (int)Math.signum(dy);
		
		if (Math.abs(dx)>Math.abs(dy)) {
			//Node is going to be horizontally adjacent
			int termX = (xdir>0) ? a.x+a.width : a.x-1;
			int termY = a.y + random.nextInt(a.height);
			return new Vec2i(termX, termY);
		} else {
			//Node is going to be vertically adjacent
			int termX = a.x + random.nextInt(a.width);
			int termY = (ydir>0) ? a.y+a.height : a.y-1;
			return new Vec2i(termX, termY);
		}
	}
	
	private static Cardinal dirToRoom(Vec2i terminal, Room room) {
		if (terminal.x<room.x) return Cardinal.EAST;
		if (terminal.y<room.y) return Cardinal.SOUTH;
		if (terminal.x>=room.x+room.width) return Cardinal.WEST;
		return Cardinal.NORTH;
	}
	
	private boolean tryConnectRooms(Room a, Room b) {
		int xdir = (int)Math.signum( (b.x+(b.width/2)) - (a.x+(a.width/2)) );
		int ydir = (int)Math.signum( (b.y+(b.height/2)) - (a.y+(a.height/2)) );
		
		//Based on the directions, find a terminal that's in sort of the right direction
		Vec2i term1 = getTerminal(a, b);
		Vec2i term2 = getTerminal(b, a);
		
		//Make sure this shit is shadowed
		int x1 = term1.x;
		int y1 = term1.y;
		int x2 = term2.x;
		int y2 = term2.y;
		
		
		//int x1 = side(xdir, a.x, a.width);
		//int y1 = side(ydir, a.y, a.height);
		
		//int x2 = side(-xdir, b.x, b.width);
		//int y2 = side(-ydir, b.y, b.height);
		
		Vec2i riseFirst = new Vec2i(x1, y2);
		Vec2i runFirst  = new Vec2i(x2, y1);
		
		Vec2i primary = null;
		Vec2i secondary = null;
		if (random.nextBoolean()) {
			primary = riseFirst;
			secondary = runFirst;
		} else {
			primary = runFirst;
			secondary = riseFirst;
		}
		
		if (line(x1,y1,primary.x,primary.y,true,a.palette) && line(primary.x,primary.y,x2,y2,true,a.palette)) {
			line(x1,y1,primary.x,primary.y,false,a.palette);
			line(primary.x,primary.y,x2,y2,false,a.palette);
			
			//Fix the corner
			DungeonTile corner = field.getOrCreate(primary.x, primary.y, DungeonTile::new);
			Cardinal cornerTo1 = Cardinal.fromTo(primary, new Vec2i(x1,y1));
			Cardinal cornerTo2 = Cardinal.fromTo(primary, new Vec2i(x2,y2));
			if (x1!=primary.x && y1!=primary.y) corner.exits.add(cornerTo1);
			if (x2!=primary.x && y2!=primary.y) corner.exits.add(cornerTo2);
			DungeonTile neighbor1 = field.getOrCreate(primary.x+cornerTo1.xOfs(), primary.y+cornerTo1.yOfs(), DungeonTile::new);
			neighbor1.exits.add(cornerTo1.cw().cw());
			DungeonTile neighbor2 = field.getOrCreate(primary.x+cornerTo2.xOfs(), primary.y+cornerTo2.yOfs(), DungeonTile::new);
			neighbor2.exits.add(cornerTo2.cw().cw());
			
			
			//Make doors
			DungeonTile door1 = field.getOrCreate(x1, y1, DungeonTile::new);
			door1.type = TileType.DOOR;
			Cardinal doorDir1 = dirToRoom(term1, a);
			door1.exits.add(doorDir1);
			DungeonTile roomToDoor1= field.getOrCreate(x1+doorDir1.xOfs(), y1+doorDir1.yOfs(), DungeonTile::new);
			roomToDoor1.exits.add(doorDir1.cw().cw());
			
			DungeonTile door2 = field.getOrCreate(x2, y2, DungeonTile::new);
			door2.type = TileType.DOOR;
			Cardinal doorDir2 = dirToRoom(term2, b);
			door2.exits.add(doorDir2);
			DungeonTile roomToDoor2 = field.getOrCreate(x2+doorDir2.xOfs(), y2+doorDir2.yOfs(), DungeonTile::new);
			roomToDoor2.exits.add(doorDir2.cw().cw());
			//TODO: Add graph notation for the doors so they can be queried later
			
			return true;
		} else if (line(x1,y1,secondary.x,secondary.y,true,a.palette) && line(secondary.x,secondary.y,x2,y2,true,a.palette)) {
			line(x1,y1,secondary.x,secondary.y,false,a.palette);
			line(secondary.x,secondary.y,x2,y2,false,a.palette);
			
			//Fix the corner
			DungeonTile corner = field.getOrCreate(secondary.x, secondary.y, DungeonTile::new);
			Cardinal cornerTo1 = Cardinal.fromTo(secondary, new Vec2i(x1,y1));
			Cardinal cornerTo2 = Cardinal.fromTo(secondary, new Vec2i(x2,y2));
			if (x1!=secondary.x && y1!=secondary.y) corner.exits.add(cornerTo1);
			if (x2!=secondary.x && y2!=secondary.y) corner.exits.add(cornerTo1);
			corner.exits.add(cornerTo2);
			DungeonTile neighbor1 = field.getOrCreate(secondary.x+cornerTo1.xOfs(), secondary.y+cornerTo1.yOfs(), DungeonTile::new);
			neighbor1.exits.add(cornerTo1.cw().cw());
			DungeonTile neighbor2 = field.getOrCreate(secondary.x+cornerTo2.xOfs(), secondary.y+cornerTo2.yOfs(), DungeonTile::new);
			neighbor2.exits.add(cornerTo2.cw().cw());
			
			
			//Make doors
			DungeonTile door1 = field.getOrCreate(x1, y1, DungeonTile::new);
			door1.type = TileType.DOOR;
			Cardinal doorDir1 = dirToRoom(term1, a);
			door1.exits.add(doorDir1);
			DungeonTile roomToDoor1= field.getOrCreate(x1+doorDir1.xOfs(), y1+doorDir1.yOfs(), DungeonTile::new);
			roomToDoor1.exits.add(doorDir1.cw().cw());
			
			DungeonTile door2 = field.getOrCreate(x2, y2, DungeonTile::new);
			door2.type = TileType.DOOR;
			Cardinal doorDir2 = dirToRoom(term2, b);
			door2.exits.add(doorDir2);
			DungeonTile roomToDoor2 = field.getOrCreate(x2+doorDir2.xOfs(), y2+doorDir2.yOfs(), DungeonTile::new);
			roomToDoor2.exits.add(doorDir2.cw().cw());
			
			return true;
		} else {
			return false;
		}
	}
	
	/*
	private boolean line(int x1, int y1, int x2, int y2, boolean simulate, String palette) {
		//This actually is a bresenham line. DO NOT USE diagonals!
		
		float dx = x2-x1;
		float dy = y2-y1;
		int iterations = (Math.abs(dy)>Math.abs(dx)) ? (int)Math.abs(dy) : (int)Math.abs(dx);
		if (iterations<1) iterations=1;
		dx /= (float)iterations;
		dy /= (float)iterations;
		
		float x = x1+0.5f;
		float y = y1+0.5f;
		Vec2i last = new Vec2i((int)x, (int)y);
		boolean firstIter = true;
		for(int i=0; i<iterations; i++) {
			Vec2i cur = new Vec2i((int)x, (int)y);
			//if (i==iterations-1) continue;
			
			DungeonTile tile = field.getOrCreate((int)x, (int)y, DungeonTile::new);
			if (tile.type!=null && tile.type!=TileType.OOB && simulate) return false;
			if (!simulate) {
				tile.type = TileType.HALLWAY;
				if (!firstIter) {
					Cardinal dir = Cardinal.fromTo(last, cur);
					Cardinal back = dir.cw().cw();
					DungeonTile lastTile = field.getOrCreate(last.x, last.y, DungeonTile::new);
					lastTile.exits.add(dir);
					tile.exits.add(back);
				}
				firstIter = false;
				//Figure out the Cardinal for lastx->x
				//if (i==iterations-1) tile.type = TileType.DOOR;
				//TODO: Set the wall types for x and y
			}
			
			x += dx;
			y += dy;
			last = cur;
		}
		return true;
	}*/
	
	private boolean line(int x1, int y1, int x2, int y2, boolean simulate, String palette) {
		int dx = x2-x1;
		int dy = y2-y1;
		if (dx!=0 && dy!=0) return false;
		int iterations = Math.max(Math.abs(dx), Math.abs(dy))+1;
		dx = (int)Math.signum(dx);
		dy = (int)Math.signum(dy);
		int x = x1;
		int y = y1;
		int lastX = x;
		int lastY = y;
		boolean firstIter = true;
		for(int i=0; i<iterations; i++) {
			DungeonTile tile = field.getOrCreate((int)x, (int)y, DungeonTile::new);
			if (tile.type!=null && tile.type!=TileType.OOB && simulate) return false;
			if (!simulate) {
				tile.type = TileType.HALLWAY;
			
				if (!firstIter) {
					Cardinal dir = Cardinal.fromTo(new Vec2i(lastX, lastY), new Vec2i(x, y));
					Cardinal back = dir.cw().cw();
					DungeonTile lastTile = field.getOrCreate(lastX, lastY, DungeonTile::new);
					lastTile.exits.add(dir);
					tile.exits.add(back);
				} else {
					//tile.type = TileType.MARKER_A;
				}
			}
			firstIter = false;
			lastX = x;
			lastY = y;
			if (x==x2 && y==y2) return true;
			x += dx;
			y += dy;
		}
		return true;
	}
	
	
	private void plotRoom(Room r) {
		for(int y=0; y<r.height; y++) {
			for(int x=0; x<r.width; x++) {
				DungeonTile tile = field.getOrCreate(r.x+x, r.y+y, DungeonTile::new);
				tile.exits = EnumSet.allOf(Cardinal.class);
				if (x==0) tile.exits.remove(Cardinal.WEST);
				if (x==r.width-1) tile.exits.remove(Cardinal.EAST);
				if (y==0) tile.exits.remove(Cardinal.NORTH);
				if (y==r.height-1) tile.exits.remove(Cardinal.SOUTH);
				tile.palette = r.palette;
				tile.type = TileType.ROOM;
			}
		}
	}
	
	private static class RoomPair {
		public Room a;
		public Room b;
		
		public RoomPair(Room a, Room b) {
			this.a = a; this.b = b;
		}
	}
	
	public static class Room {
		public String palette;
		public int x = 0;
		public int y = 0;
		public int width = 1;
		public int height = 1;
		
		public Room() {}
		public Room(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}
	
	public static class Door {
		public Room adjacentRoom;
		public int x = 0;
		public int y = 0;
		public Door() {}
		public Door(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public static class Hall {
		Door a;
		Door b;
		public Hall() {}
		public Hall(Door a, Door b) {
			this.a = a;
			this.b = b;
		}
		public Room roomFrom(Room origin) {
			return (a.adjacentRoom.equals(origin)) ? b.adjacentRoom : a.adjacentRoom;
		}
		
		public Door doorFrom(Door origin) {
			return (a.equals(origin)) ? b : a;
		}
	}
}
