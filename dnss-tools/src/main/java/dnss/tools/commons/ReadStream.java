package dnss.tools.commons;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;

/**
 * ReadStream that contains only the essential methods needed by any tool.
 * If more methods are needed, add to this.
 *
 * Note: Read is non-blocking
 */
public class ReadStream {
    /**
     * The random file accessor used to navigate around a file
     */
    private RandomAccessFile randomAccessFile;

    /**
     * Opens a file stream to navigate around file
     *
     * @param file the file to read.
     * @throws FileNotFoundException
     *         if the the given string does not denote an
     *         existing regular file, or if some other error
     *         occurs while opening or creating the file.
     */
    public ReadStream(File file) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(file, "r");
    }

    /**
     * Reads an int from file stream in Little Endian notation
     *
     * @return an int in Little Endian.
     * @throws IOException if an I/O error occurs.
     * @throws EOFException
     *         if this file reaches the end before reading four bytes.
     */
    public int readInt() throws IOException, EOFException {
        int ch1 = randomAccessFile.read();
        int ch2 = randomAccessFile.read();
        int ch3 = randomAccessFile.read();
        int ch4 = randomAccessFile.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch4 << 24) | (ch3 << 16) | (ch2 << 8) | ch1);
    }

    /**
     * Reads a short from file stream in Little Endian notation
     *
     * @return a short in Little Endian.
     * @throws IOException if an I/O error occurs.
     * @throws EOFException
     *         if this file reaches the end before reading four bytes.
     */
    public short readShort() throws IOException, EOFException {
        int ch1 = randomAccessFile.read();
        int ch2 = randomAccessFile.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch2 << 8) + ch1);
    }

    /**
     * Reads a float from file stream in Little Endian
     *
     * @return a float in Little Endian.
     * @throws IOException if an I/O error occurs.
     * @throws EOFException
     *         if this file reaches the end before reading four bytes.
     */
    public float readFloat() throws IOException, EOFException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads len bytes and converts it into a String object
     *
     * @param len the number of bytes to read.
     * @return the String of that is the sequential combination of
     *         <code>len</code> bytes read.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public String readString(int len) throws IOException {
        byte[] b = new byte[len];
        randomAccessFile.readFully(b);
        return new String(b);
    }

    /**
     * Changes the file pointer position to <code>pos</code>.
     *
     * @param pos the position to move the file pointer to.
     * @throws IOException if an I/O error occurs.
     */
    public void seek(long pos) throws IOException {
        randomAccessFile.seek(pos);
    }

    /**
     * Skipes <code>n</code> bytes from the current file
     * pointer position, and then returns this object again
     * for using again.
     *
     * @param n the number of bytes to skip from the current
     *          file pointer position.
     * @return this object
     * @throws IOException if an I/O error occurs.
     */
    public ReadStream skip(int n) throws IOException {
        randomAccessFile.skipBytes(n);
        return this;
    }

    /**
     * Gets the current position the file pointer is at.
     *
     * @return the position the current file pointer.
     * @throws IOException if an I/O error occurs.
     */
    public long getPos() throws IOException {
        return randomAccessFile.getFilePointer();
    }

    /**
     * Reads bytes into the <code>b</code> array untl it is
     * full.
     *
     * @param b the byte array to continually read to until full.
     * @throws IOException if an I/O error occurs.
     */
    public void readFully(byte[] b) throws IOException {
        randomAccessFile.readFully(b);
    }

    /**
     * Closes the file stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
