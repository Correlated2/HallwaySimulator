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

import java.util.UUID;

public interface ITagCompound {

	boolean containsKey(String key);
	
	byte getByte(String key);
	short getShort(String key);
	int getInteger(String key);
	long getLong(String key);
	UUID getUUID(String key);
	boolean getBoolean(String key);
	float getFloat(String key);
	double getDouble(String key);
	String getString(String key);
	ITagCompound getTag(String key);
	
	void setByte(String key, byte value);
	void setShort(String key, short value);
	void setInteger(String key, int value);
	void setLong(String key, long value);
	void setUUID(String key, UUID value);
	void setBoolean(String key, boolean value);
	void setFloat(String key, float value);
	void setDouble(String key, double value);
	void setString(String key, String value);
	
}
