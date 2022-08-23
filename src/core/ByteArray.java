package core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Represents an abstraction of a byte array as used in many parts of this framework.
 * 
 */
public class ByteArray implements Externalizable,Cloneable {
	
    private int numRows;
    private int numCols;
    protected short[] array = new short[0];       
    public boolean rowsFirst;       // If true, means that byte positions increases first on the rows than columns, false means otherwise.

    public static void main(String[] args) throws Exception{
        /*ByteArray b2;
        ByteArray b = new ByteArray(8);
        ByteArray row = new ByteArray(4);
        ByteArray col = new ByteArray(2);
        b.randomize();
        b.rowsFirst = false;
        System.out.println(b + " " + b.getShape());
        b.setShape("2x4");
        System.out.println(b + " " + b.getShape());
        //b.setByte(0, 3, 255);
        //System.out.println(b + " " + b.getShape());
        b2 = b.getColumn(3);
        System.out.println(b2 + " " + b2.getShape());
        b2 = b.getRow(1);
        System.out.println(b2 + " " + b2.getShape());
        //b.setRow(0,b2);
        //System.out.println(b + " " + b.getShape());*/
        
        ByteArray b = new ByteArray(8);
        b.randomize();
        b.setShape("2x4");
        System.out.println(b);
        b.rotateRow(1);
        System.out.println(b);
    }
    
    public String getShape() {
        return numRows + "x" + numCols;
    }
    
    public int getNumRows() {
        return numRows;
    }
    
    public int getNumCols() {
        return numCols;
    }
    
    public final void setShape(String shape) throws Exception {
        
        //System.out.println(shape);
        String[] splitedShape = shape.split("x");
        //System.out.println(splitedShape[0] + " " + splitedShape[1]);
        
        if(splitedShape.length != 2) throw new Exception("Illegal shape: must be in the form 'int'x'int' (ex. 4x3)");
        
        int num1, num2;
        try{
            num1 = Integer.parseInt(splitedShape[0]);
            num2 = Integer.parseInt(splitedShape[1]);
        }catch(NumberFormatException ex){
            throw new Exception("Illegal shape: must be in the form 'int'x'int' (ex. 4x3). Instead is '"+splitedShape[0]+"'x'"+splitedShape[1]+"'");
        }
        if(num1*num2 != length()) throw new Exception("Illegal shape: the product of the first("+num1
                                                    +") and second ("+num2+") number must be equal to the length "
                                                    + "("+length()+") of the ByteArray ("+num1*num2+ "!=" +length()+").");
        numRows = num1;
        numCols = num2;
        //System.out.println("Setted values: "+numRows + " " + numCols);
    }
    
    public short getByte(int row, int col) throws Exception{
        if(row < 0 || row >= numRows) throw new Exception("Invalid Row: must be non negative and smaller than "+numRows);
        if(col < 0 || col >= numCols) throw new Exception("Invalid Column: must be non negative and smaller than "+numCols);
        
        if(rowsFirst)   return this.get(row*numCols + col);
        else            return this.get(col*numRows + row);
    }
    
    public void setByte(int row, int col, int value) throws Exception{
        //System.out.println(numRows + " " + numCols);
        if(row < 0 || row >= numRows) throw new Exception("Invalid Row: must be non negative and smaller than "+numRows + "("+row+")");
        if(col < 0 || col >= numCols) throw new Exception("Invalid Column: must be non negative and smaller than "+numCols);
        
        if(rowsFirst)   this.set(row*numCols + col, value);
        else            this.set(col*numRows + row, value);
    }
    
    public ByteArray getColumn(int col) throws Exception{
        //System.out.println(numCols);
        //System.out.println(numRows);
        if(col < 0 || col >= numCols) throw new Exception("Invalid Column: must be non negative and smaller than "+numCols);
        
        ByteArray result = new ByteArray(numRows);
        result.setShape(numRows+"x1");
        for (int i = 0; i < result.numRows; i++) result.setByte(i, 0, getByte(i, col));
        
        
        return result;
    }
    
    public void setColumn(int col, ByteArray value) throws Exception{
        if(value.length() != numRows) throw new Exception("Invalid Value: must have the same length as rows in the target. (Value has length "+value.length()+" but should be "+numRows);
        if(col < 0 || col >= numCols) throw new Exception("Invalid Column: must be non negative and smaller than "+numCols);
        //System.out.println(value);
        for (int i = 0; i < numRows; i++) setByte(i, col, value.get(i));
        
    }
    
    public ByteArray getRow(int row) throws Exception{
        if(row < 0 || row >= numRows) throw new Exception("Invalid Row: must be non negative and smaller than "+numRows);
        
        ByteArray result = new ByteArray(numCols);
        for (int i = 0; i < result.numCols; i++)  result.setByte(0, i, getByte(row, i));
        
        return result;
    }
    
    public void setRow(int row, ByteArray value) throws Exception{
        if(value.length() != numCols) throw new Exception("Invalid Value: must have the same length as rows in the target. (Value has length "+value.length()+" but should be "+numRows);
        if(row < 0 || row >= numRows) throw new Exception("Invalid Row: must be non negative and smaller than "+numRows);
        
        for (int i = 0; i < numCols; i++) setByte(row, i, value.get(i));
    }
    
    public void rotateRow(int row) throws Exception{
        ByteArray aux_row = getRow(row);
        int aux = aux_row.get(0);
        for (int i = 0; i < aux_row.length()-1; i++) 
            aux_row.set(i, aux_row.get(i+1));
        
        aux_row.set(aux_row.length()-1, aux);
        setRow(row, aux_row);
    }
    
    /**
     * Creates a new byte array.
     */
    public ByteArray() {

    }

    /**
     * Creates a new byte array, and initializes it with values, where each value in the given array 
     * is treated as a byte.
     */
    public ByteArray(short[] values) {
        this.array = new short[values.length];
        for (int i = 0; i < values.length; i++) {
                array[i] = (short)(values[i] & 0xFF);
        }
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array, and initializes it with values, where each value in the given array 
     * is treated as a byte, i.e., only its least-significant eight bits are used. 
     */
    public ByteArray(int[] values) {
        this.array = new short[values.length];

        for (int i = 0; i < values.length; i++) {
                array[i] = (short)(values[i] & 0xFF);
        }
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array, and initializes it with a given array, where each value is 
     * treated as a byte. The given values are placed in positions <code>position</code>..
     * <code>position + delta.length</code>; the first <code>position</code> bytes are filled up
     * with zeroes.
     */
    public ByteArray(int[] values, int position) {
        this.array = new short[values.length + position];

        for (int i = 0; i < values.length; i++) {
                array[i + position] = (short)(values[i] & 0xFF);
        }
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array and initializes it with <code>length</code> zeroes.
     */
    public ByteArray(int length) {
        this.array = new short[length];
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array and initializes it with n bytes which represent the first
     * n bits of the bit representation of the given value.
     */
    public ByteArray(int value, int numBytes) {
        this.setValue((long)value, numBytes);
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array and initializes it with n bytes which represent the first
     * n bits of the bit representation of the given value.
     */
    public ByteArray(long value, int numBytes) {
        this.setValue(value, numBytes);
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array, and initializes it with a given array, where each long value is 
     * treated as a sequence of eight bytes.
     */
    public ByteArray(long[] words) {
        this.array = new short[words.length * 8];
        writeLongs(0, words);
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates a new byte array, and initializes it with a given array, where each long value is 
     * treated as a sequence of eight bytes.
     */
    public ByteArray(long[][] wordArrays) {
        int totalSize = 0;
        int numBytesPerLong = Long.SIZE / Byte.SIZE;

        for (int i = 0; i < wordArrays.length; i++) {
                totalSize += wordArrays[i].length * numBytesPerLong; 
        }

        this.array = new short[totalSize];
        int position = 0;
        long[] element;

        for (int i = 0; i < wordArrays.length; i++) {
                element = wordArrays[i];
                writeLongs(position, element);
                position += element.length * numBytesPerLong;
        }
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    public ByteArray(byte[] input) {
        array = new short[input.length];

        for (int i = 0; i < input.length; i++) {
                array[i] = input[i];
        }
        try {
            setShape(""+1+"x"+array.length);
        } catch (Exception ex) {
            //Never happens
        }
        rowsFirst = true;
    }

    /**
     * Creates an empty byte array of size bytes length.
     * @param size
     * @return An empty byte array of size bytes length.
     */
    public static ByteArray createEmpty(int size) {
            return new ByteArray(size);
    }

    /**
     * Applies the logical AND operation to every i-th byte of the calling ByteArray and every i-th byte of the given
     * ByteArray. Modifies the object.
     * @param other The second parameter of the AND operation.
     */
    public ByteArray and(ByteArray other) {
            for (int i = 0; i < array.length; i++) {
                    array[i] &= other.array[i];
            }

            return this;
    }

    /**
     * Creates a deep copy of the current ByteArray.
     */
    public ByteArray clone() {
            ByteArray copy = new ByteArray(length());

            for (int i = 0; i < array.length; i++) {
                    copy.set(i, get(i));
            }
        try {
            copy.setShape(getShape());
        } catch (Exception ex) {
            //never happens
        }
            return copy;
    }

    /**
     * Appends the byte of the given <code>other</code> byte array to this byte array.
     * Modifies the current byte array; the other byte array is not modified.
     */
    public void concat(ByteArray other) {
            short[] temp = new short[length() + other.length()];

            for (int i = 0; i < array.length; i++) {
                    temp[i] = array[i];
            }

            for (int i = 0; i < other.length(); i++) {
                    temp[i + array.length] = other.array[i];
            }

            this.array = temp;
    }

    /**
     * Copies the byte value from position <code>sourceIndex</code> in the given byte array <code>source</code>
     * at the position <code>destinationIndex</code> in the current byte array. 
     * Modifies the current byte array; the other byte array is not modified.
     */
    public void copyByte(ByteArray source, int sourceIndex, int destinationIndex) {
            array[destinationIndex] = source.array[sourceIndex];
    }

    /**
     * Calls {@link ByteArray#copyBytes(ByteArray, int, int, int)} with arguments 
     * <code>source, 0, 0, source.length()</code>.
     */
    public void copyBytes(ByteArray source) {
            copyBytes(source, 0, 0, source.length());
    }

    /**
     * Calls {@link ByteArray#copyBytes(ByteArray, int, int, int)} with arguments 
     * <code>source, sourceFrom, 0, source.length()</code>.
     */
    public void copyBytes(ByteArray source, int sourceFrom) {
            int numBytes = source.length() - sourceFrom; 
            copyBytes(source, sourceFrom, 0, numBytes);
    }

    /**
     * Calls {@link ByteArray#copyBytes(ByteArray, int, int, int)} with arguments 
     * <code>source, sourceFrom, destinationIndex, source.length()</code>.
     */
    public void copyBytes(ByteArray source, int sourceFrom, int destinationIndex) {
            int numBytes = source.length() - sourceFrom; 
            copyBytes(source, sourceFrom, destinationIndex, numBytes);
    }

    /**
     * Copies a sequence of <code>numBytes</code> bytes from position <code>sourceFrom</code>
     * in the given byte array <code>source</code>, 
     * to starting from the position <code>destinationIndex</code> in the current byte array. 
     * Modifies the current byte array; the other byte array is not modified.
     * @throws ArrayIndexOutOfBoundsException If <code>sourceFrom</code> or <code>destinationIndex</code>
     * are less than zero, or if the length of the source array or the current array is not great enough.   
     */
    public void copyBytes(ByteArray source, int sourceFrom, int destinationIndex, int numBytes) {
            if (source.length() < sourceFrom + numBytes) {
                    throw new ArrayIndexOutOfBoundsException();
            } else if (length() < destinationIndex + numBytes) {
                    throw new ArrayIndexOutOfBoundsException();
            } else if (sourceFrom < 0) {
                    throw new ArrayIndexOutOfBoundsException();
            } else if (destinationIndex < 0) {
                    throw new ArrayIndexOutOfBoundsException();
            }

            for (int i = 0; i < numBytes; i++) {
                    array[destinationIndex + i] = source.array[sourceFrom + i];
            }
    }

    /**
     * Counts and returns the number of non-zero bits in the byte array.  
     */
    public int countNumActiveBits() {
            int numActiveBits = 0;
            int value;
            final int mask = 1; 

            for (int i = 0; i < array.length; i++) {
                    value = array[i];
                    for (int j = 0; j < 8; j++) {
                            if ((value & mask) != 0) {
                                    numActiveBits++;
                            }
                            value >>= 1;
                    }
            }

            return numActiveBits;
    }

    /**
     * Counts and returns the number of non-zero bytes in the byte array.  
     */
    public int countNumActiveBytes() {
            int numActiveBytes = 0;

            for (int i = 0; i < array.length; i++) {
                    if (array[i] != 0) {
                            numActiveBytes++;
                    }
            }

            return numActiveBytes;
    }

    /**
     * Counts and returns the number of non-zero nibbles (half-bytes) in the byte array. 
     */
    public int countNumActiveNibbles() {
            int numActiveNibbles = 0;

            for (int i = 0; i < array.length; i++) {
                    if ((array[i] & 0x0F) != 0) {
                            numActiveNibbles++;
                    }

                    if ((array[i] & 0xF0) != 0) {
                            numActiveNibbles++;
                    }
            }

            return numActiveNibbles;
    }

    /**
     * Tests if all bytes in the byte array are equal to the given <code>value</code>.
     * Returns <code>true</code> if all bytes are equal to <code>value</code>, 
     * returns <code>false</code> otherwise.  
     */
    public boolean equals(int value) {
            for (int i = 0; i < length(); i++) {
                    if (get(i) != value) {
                            return false;
                    }
            }

            return true;
    }

    /**
     * Returns <code>true</code>, if the value at any position i in the byte array is equal to 
     * the value at any position i in the given array, and if the length of the byte array and 
     * the given <code>other</code> array are equal. Returns <code>false</code> otherwise.
     */
    public boolean equals(int[] other) {
            if (length() != other.length) {
                    return false;
            }

            for (int i = 0; i < length(); i++) {
                    if (get(i) != (other[i] & 0xFF)) {
                            return false;
                    }
            }

            return true;
    }

    /**
     * Returns <code>true</code>, if the value at any position i in the byte array is equal to 
     * the value at any position i in the given byte array, and if the length of the byte array and 
     * the given <code>other</code> byte array are equal. Returns <code>false</code> otherwise.
     */
    public boolean equals(ByteArray other) {
            if (array.length != other.array.length) {
                    return false;
            }

            return Arrays.equals(array, other.array);
    }

    public boolean equals(Object object) {
            try {
                    ByteArray other = (ByteArray)object;

                    if (other == null) {
                            return false;
                    } else {
                            return equals(other);
                    }
            } catch (Exception e) {
                    return false;
            }
    }

    public int hashCode() {
            assert false : "hashCode not implemented";
            return -1;
    }

    /**
     * Returns the byte value at the given position.
     */
    public short get(int position) {
            return array[position];
    }

    /**
     * Returns a reference to the internal array.
     */
    public short[] getArray() {
            return array;
    }

    public short[] toByteArray(int ini, int end){
        short[] result = new short[end-ini];
        for (int i = ini,j = 0; i < end; i++,j++) {
            result[j] = this.get(i);
        }
        return result;
    }

    /**
     * Returns a copy of the internal array's values from position <code>from</code> to position <code>to</code>.
     * In the result, the values are located from position <code>0</code> to position <code>to - from - 1</code>.
     */
    public short[] getArray(int from, int to) {
            short[] result = new short[to - from];
            int position = 0;

            for (int i = from; i < to; i++) {
                    result[position++] = array[i];
            }

            return result;
    }

    /**
     * Treats the byte array as a sequence of bits.  
     * Returns the bit value at <code>position % 8</code> from the byte at <code>position / 8</code>.
     */
    public boolean getBit(int position) {
            int bitIndex = position % 8;
            int mask = getByteMaskWithOneActiveBit(bitIndex);
            return (array[position / 8] & mask) != 0;
    }

    /**
     * Returns the byte-wise XOR difference of this byte array and the given <code>other</code> byte array.
     * @return Returns a copy, does not modify the current byte array, does not modify the other byte array.
     * @throws Error If the lengths are not equal.
     */
    public ByteArray getDifference(ByteArray other) {
            if (other.length() != length()) {
                    throw new RuntimeException("Lengths do not match: " + length() + " and " + other.length() + ". "
                            + "To retrieve a difference, the lengths need to match.");
            }

            ByteArray result = this.clone();
            result.xor(other);
            return result;
    }

    public int getNibble(int position) {
            if ((position & 1) == 0) {
                    return (array[position / 2]  >> 4) & 0xF;
            } else {
                    position--;
                    return array[position / 2] & 0xF;
            }
    }

    /**
     * Tests if all bytes in the byte array are not equal to zero.
     * @return Returns <code>true</code>, if there is no byte equal to zero in the current byte array;
     * <code>false</code> otherwise.  
     */
    public boolean isFullyActive() {
            for (int i = 0; i < array.length; i++) {
                    if (array[i] == 0) {
                            return false;
                    }
            }

            return true;
    }

    /**
     * Returns the length of the current byte array.
     */
    public int length() {
            return array.length;
    }

    /**
     * Applies the logical OR between every i-th byte of the current ByteArray and every i-th
     * byte of the other array. Modifies the current ByteArray.
     * @param other
     */
    public ByteArray or(ByteArray other) {
            if (array.length <= other.array.length) {
                    return or(other, 0, array.length);
            } else {
                    return or(other, 0, other.array.length);
            }
    }

    public ByteArray or(ByteArray other, int from, int to) {
            for (int i = from; i < to; i++) {
                    array[i] |= other.array[i];
            }

            return this;
    }

    /**
     * Sets every byte in the current ByteArray to a random value between [0 ... 255].
     */
    public void randomize() {
            for (int i = 0; i < array.length; i++) {
                    array[i] = (short)(Math.round(Math.random() * 0xFF) & 0xFF);
            }
    }

    /**
     * Creates a 64-bit long value from eight bytes of the current ByteArray, starting at the given 
     * position in the ByteArray. For instance, if the position is 5, and the length of the ByteArray
     * is 20, the returned long contains the bytes from positions 5 || 6 || 7 || 8 || 9 || 10 || 11 || 12, 
     * where the byte at position 5 is used as the most significant eight bits of the return value.
     * @throws ArrayIndexOutOfBoundsException If the length of the ByteArray is less or equal to position + 8.
     */
    public long readLong(int position) {
            long word = 0;

            for (int i = 0; i < 8; i++) {
                    word |= ((long) (array[position + 7 - i] & 0xFF)) << ((i) * 8);
            }

            return word;
    }

    /**
     * Calls {@link #readLongs(int)} position set to 0.
     */
    public long[] readLongs() {
            return readLongs(0);
    }

    /**
     * Reads all bytes from the given position until the end of the ByteArray. 
     */
    public long[] readLongs(int position) {
            int numToRead = (length() - position) / 8;
            return readLongs(position, numToRead);
    }

    public long[] readLongs(int position, int numToRead) {
            long[] results = new long[numToRead];
            long word = 0;

            for (int i = 0; i < numToRead; i++) {
                    word = readLong(position + i * 8);
                    results[i] = word;
            }

            return results;
    }

    public void set(int position, boolean value) {
            array[position] = value ? (short)0xFF : 0;
    }

    public void set(int position, short value) {
            array[position] = (short)(value & 0xFF);
    }

    public void set(int position, int value) {
            array[position] = (short)(value & 0xFF);
    }

    public void setArray(short[] shorts) {
            array = new short[shorts.length];

            for (int i = 0; i < shorts.length; i++) {
                    array[i] = (short)(shorts[i] & 0xFF);
            }
    }

    public void setAtEnd(int position, boolean value) {
            int byteIndex = array.length - position - 1;
            array[byteIndex] = value ? (short)0xFF : 0;
    }

    public void setAtEnd(int position, short value) {
            int byteIndex = array.length - position - 1;
            array[byteIndex] = (short)(value & 0xFF);
    }

    public void setBit(int position, boolean value) {
            int mask = 0;
            int bitIndex = position % 8;

            if (value) {
                    mask = getByteMaskWithOneActiveBit(bitIndex);
                    array[position / 8] |= mask;
            } else {
                    mask = getByteMaskWithOneNonActiveBit(bitIndex);
                    array[position / 8] &= mask;
            }
    }

    public void setBitAtEnd(int bitPosition, boolean value) {
            int bytePosition = array.length 
                    - (int)Math.ceil((double)(bitPosition + 1) / Byte.SIZE);

            if (bytePosition < 0 || bytePosition >= array.length) {
                    throw new ArrayIndexOutOfBoundsException();
            }

            int mask = 1 << (bitPosition % 8);

            if (value) {
                    array[bytePosition] |= (mask & 0xFF);
            } else {
                    mask ^= 0xFF;
                    array[bytePosition] &= (mask & 0xFF);
            }
    }

    public void setNibble(int position, int value) {
            setNibble(position, (short)value);
    }

    public void setNibble(int position, short value) {
            int byteIndex = (int)(position / 2);

            if ((position & 0x1) == 0) { // is even => set higher four bits
                    array[byteIndex] = (short)((array[byteIndex] & 0x0F) | ((value << 4) & 0xF0));
            } else { // is odd => set lower four bits
                    array[byteIndex] = (short)((array[byteIndex] & 0xF0) | (value & 0xF));
            }
    }

    public void setNibbleAtEnd(int position, boolean value) {
            int byteIndex = array.length - (int)(position / 2) - 1;

            if ((position & 0x1) == 0) { // is even => set lower four bits
                    if (value) {
                            array[byteIndex] = (short)((array[byteIndex] & 0xF0) | 0xF);
                    } else {
                            array[byteIndex] = (short)(array[byteIndex] & 0xF0);
                    }
            } else { // is odd => set higher four bits
                    if (value) {
                            array[byteIndex] = (short)((array[byteIndex] & 0x0F) | 0xF0);
                    } else {
                            array[byteIndex] = (short)(array[byteIndex] & 0x0F);
                    }
            }
    }

    public void setNibbleAtEnd(int position, short value) {
            int byteIndex = array.length - (int)(position / 2) - 1;

            if ((position & 0x1) == 0) { // is even => set lower four bits
                    array[byteIndex] = (short)((array[byteIndex] & 0xF0) | (value & 0xF));
            } else { // is odd => set higher four bits
                    array[byteIndex] = (short)((array[byteIndex] & 0x0F) | ((value << 4) & 0xF0));
            }
    }

    public void setValue(long value, int numBytes) {
            if (this.length() != numBytes) {
                    this.array = new short[numBytes];
            }

            if (value > 0) {
                    if (numBytes > 32) {
                            numBytes = 32;
                    }

                    for (int i = 0; i < numBytes; i++) {
                            if ((value & (1 << i)) != 0) {
                                    array[i] = 1;
                            } else {
                                    array[i] = 0;
                            }
                    }
            }
    }

    /**
     * Returns <code>true</code>, if this byte array shares any active bit with 
     * the given byte array. Returns false otherwise.
     */
    public boolean sharesActiveBitsWith(ByteArray other) {
            if (length() != other.length()) {
                    return true;
            }

            for (int i = 0; i < length(); i++) {
                    if ((array[i] & other.array[i]) != 0) {
                            return true;
                    }
            }

            return false;
    }

    /**
     * Returns <code>true</code>, if this byte array shares any active byte with 
     * the given byte array. Returns false otherwise.
     */
    public boolean sharesActiveBytesWith(ByteArray other) {
            if (length() != other.length()) {
                    return true;
            }

            for (int i = 0; i < length(); i++) {
                    if (array[i] != 0 && other.array[i] != 0) {
                            return true;
                    }
            }

            return false;
    }

    public boolean sharesActiveNibblesWith(ByteArray other) {
            if (length() != other.length()) {
                    return true;
            }

            for (int i = 0; i < length(); i++) {
                    if ((array[i] & 0xF0) != 0 && (other.array[i] & 0xF0) != 0) {
                            return true;
                    } else if ((array[i] & 0x0F) != 0 && (other.array[i] & 0x0F) != 0) {
                            return true;
                    }
            }

            return false;
    }

    /**
     * Calls {@link ByteArray#splice(int,int)} with the second parameter set to 
     * the length of the current array.
     */
    public ByteArray splice(int from) {
            return splice(from, length());
    }

    /**
     * Returns a deep copy which copies the elements of the current byte array 
     * from index <code>from</code> to index <code>to</code>, including <code>from</code>, 
     * and excluding <code>to</code>.
     * In the result, these values are placed in the range of <code>0 .. (to - from - 1)</code>.
     */
    public ByteArray splice(int from, int to) {
            ByteArray result = new ByteArray(to - from);
            int max = to - from;

            for (int i = 0; i < max; i++) {
                    result.array[i] = array[i + from];
            }

            return result;
    }

    /**
     * Creates a bit string representation of the byte array.
     */
    public String toBitString() {
            if (array == null) {
                    return "";
            }

            String output = "";
            String v = "";

            for (int i = 0; i < length(); i++) {
                    if (i > 0) {
                            if (i % 16 == 0) {
                                    output += "\n";
                            } else if (i % 4 == 0) {
                                    output += ",";
                            } else {
                                    output += ",";
                            }
                    }

                    v = Integer.toBinaryString(get(i));

                    if (v.length() > 8) {
                            v = v.substring(v.length() - 8);
                    }

                    while(v.length() < 8) {
                            v = "0" + v;
                    }

                    output += v;
            }

            return output;
    }

    /**
     * Creates a hex string representation of the individual bytes of the byte array.
     */
    public String toHexString() throws Exception {
            if (array == null) {
                    return "";
            }

            String output = "";
            String v;
            //System.out.println(length());
            for (int i = 0; i < numRows; i++) {
                if(i > 0) output += "\n ";
                for (int j = 0; j < numCols; j++) {
                    if (j > 0) {
                        if (j % 4 == 0) {
                                output += "|";
                        } else {
                                output += ",";
                        }
                    }

                    v = Integer.toHexString(getByte(i, j));

                    if (v.length() < 2) {
                            v = "0" + v;
                    } else if (v.length() > 2) {
                            v = v.substring(v.length() - 2);
                    }

                    output += v;
                }
            }

            return output;
    }

    /**
     * Creates a hex string representation of the individual bytes of the byte array.
     */
    public String toString() {
        try {
            return "[" + toHexString() + "]";
        } catch (Exception ex) {
            System.err.println(Arrays.toString(ex.getStackTrace()));
        }
        return "";
    }

    /**
     * Writes the given bytes in the current byte array in the given order, starting from the given position
     * in the current byte array. If the capacity of the current byte array can not take all bytes, 
     * then its capacity is automatically extended.
     */
    public void writeBytes(int position, short[] newBytes) {
            int newLength = position + newBytes.length;

            if (array.length <= newLength) {
                    short[] newArray = new short[newLength];

                    for (int i = 0; i < array.length; i++) {
                            newArray[i] = array[i];
                    }

                    array = newArray;
            }

            for (int i = position; i < newLength; i++) {
                    array[i] = (short)(newBytes[i - position] & 0xff);
            }
    }

    /**
     * 
     */
    public void writeLong(long word) {
            writeLong(0, word);
    }

    public void writeLong(int position, long word) {
            for (int j = 7; j >= 0; j--) {
                    array[position + j] = (short)(word & 0xff);
                    word >>= 8;
            }
    }

    public void writeLongs(long[] words) {
            writeLongs(0, words);
    }

    public void writeLongs(int position, long[] words) {
            int numWords = words.length;
            int numBytesInLong = Long.SIZE / Byte.SIZE;
            int maxPosition = position + numWords * numBytesInLong;

            if (array.length < maxPosition) {
                    throw new RuntimeException(
                            "ByteArray is too small (" + array.length + ") to write longs from " 
                            + position + " to " + maxPosition + "."
                    );
            }

            for (int i = 0; i < numWords; i++) {
                    long word = words[i];

                    for (int j = numBytesInLong - 1; j >= 0; j--) {
                            array[position + i * numBytesInLong + j] = (short)(word & 0xff);
                            word >>= 8;
                    }
            }
    }

    public ByteArray xor(ByteArray other) {
            if (array.length <= other.array.length) {
                    return xor(other, 0, array.length);
            } else {
                    return xor(other, 0, other.array.length);
            }
    }

    public static ArrayList<ByteArray> xor(ArrayList<ByteArray> one, ArrayList<ByteArray> other) {
            ArrayList<ByteArray> result = new ArrayList<>();

            if (one.size() < other.size())  result.addAll(one);
            else result.addAll(other);

            for (int i = 0; i < result.size(); i++) {
                result.get(i).xor(other.get(i));
            }
            return result;
    }

    public ByteArray xor(ByteArray other, int from, int to) {
            for (int i = from; i < to; i++) {
                    array[i] ^= other.array[i];
            }

            return this;
    }

    public ByteArray xor(ByteArray other, int from, int to, int xorWithOffset) {
            for (int i = from; i < to; i++) {
                    array[i] ^= other.get(i + xorWithOffset);
            }

            return this;
    }

    public void xorByte(int xorWith, int position) {
            array[position] ^= xorWith & 0xFF;
    }

    public void xorByte(short xorWith, int position) {
            array[position] ^= xorWith & 0xFF;
    }

    private int getByteMaskWithOneActiveBit(int bitIndex) {
            return 1 << (7 - bitIndex);
    }

    private int getByteMaskWithOneNonActiveBit(int bitIndex) {
            return 0xff ^ getByteMaskWithOneActiveBit(bitIndex);
    }

    public int readUInt() {
            return readUInt(0);
    }

    public int readUInt(int position) {
            return (array[position] & 0xff) << 24 
                    | (array[position + 1] & 0xff) << 16
                    | (array[position + 2] & 0xff) << 8
                    | (array[position + 3] & 0xff);
    }

    public void writeUInt(int value) {
            writeUInt(0, value);
    }

    public void writeUInt(int position, int value) {
            array[position] = (short)((value >>> 24) & 0xff);
            array[position + 1] = (short)((value >>> 16) & 0xff);
            array[position + 2] = (short)((value >>> 8) & 0xff);
            array[position + 3] = (short)(value & 0xff);
    }

    public int[] readUInts() {
            final int numWords = array.length / 4;
            final int[] result = new int[numWords];

            for (int i = 0, index = 0; i < array.length; i += 4, index++) {
                    result[index] = (array[i] & 0xff) << 24
                            | (array[i + 1] & 0xff) << 16
                            | (array[i + 2] & 0xff) << 8
                            | (array[i + 3] & 0xff);
            }

            return result;
    }

    public int[] readUInts(int position, int numToRead) {
            int[] results = new int[numToRead];
            int word = 0;

            for (int i = 0; i < numToRead; i++) {
                    word = readUInt(position + i * 4);
                    results[i] = word;
            }

            return results;
    }

    public void writeUInts(int[] words) {
            writeUInts(words, 0);
    }

    public void writeUInts(int[] words, int position) {
            for (int i = 0; i < words.length; i++) {
                    writeUInt(position + 4 * i, words[i]);
            }
    }

    public void invertIntwise() {
            int word;

            for (int i = 0; i < array.length; i += 4) {
                    word = (array[i] & 0xff) 
                            | (array[i + 1] & 0xff) << 8
                            | (array[i + 2] & 0xff) << 16
                            | (array[i + 3] & 0xff) << 24;
                    array[i] = (short)((word >>> 24) & 0xff);
                    array[i + 1] = (short)((word >>> 16) & 0xff);
                    array[i + 2] = (short)((word >>> 8) & 0xff);
                    array[i + 3] = (short)(word & 0xff);
            }
    }

    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
            final int length = input.readInt();
            array = new short[length];

            for (int i = 0; i < length; i++) {
                    array[i] = input.readShort();
            }
    }

    public void writeExternal(ObjectOutput output) throws IOException {
            if (array == null) {
                    output.writeInt(0);
            } else {
                    output.writeInt(array.length);

                    for (int i = 0; i < array.length; i++) {
                            output.writeShort(array[i]);
                    }
            }
    }

    public ArrayList<ByteArray> split(int chunkSize){
        ArrayList<ByteArray> result = new ArrayList<>();
        ByteArray aux;
        for (int i = 0; i < this.length()/chunkSize; i++) {
            aux = new ByteArray(chunkSize);
            aux.copyBytes(this,i*chunkSize, 0,chunkSize);
            result.add(aux);
        }
        return result;
    }

    public ArrayList<Integer> getActiveNibbles(){
            ArrayList<Integer> result = new ArrayList<>();
            
            for (int i = 0; i < this.length()*2; i++) 
                if(this.getNibble(i)!= 0) result.add(i);
            
            return result;
        }
    
    public static ByteArray concatenateAll(List<ByteArray> list){
        int totalSize = 0;
        for (ByteArray elem : list) totalSize+=elem.length();
        
        ByteArray result = new ByteArray(totalSize);
        int index = 0;
        
        for (ByteArray elem : list) {
            result.copyBytes(elem, 0, index);
            index+=elem.length();
        }
        System.out.println(result);
        return result;
    }
        
}
