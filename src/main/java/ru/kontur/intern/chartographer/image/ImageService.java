package ru.kontur.intern.chartographer.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;


@Component
@Scope("prototype")
public class ImageService {

  private static final int PIXEL_DEPTH = 3;
  private static final int HEADER_FILE_SIZE_OFFSET = 2;
  private static final int HEADER_WIDTH_OFFSET = 18;
  private static final int HEADER_HEIGHT_OFFSET = 22;
  private static final int HEADER_IMAGE_SIZE_OFFSET = 34;
  private static final int DEFAULT_HEADER_SIZE = 54;
  private static final String PROMPT_HEADER_PATH = "static/bmpheader";
  private static final String SUFF = ".bmp";
  private final String workingDirectoryPath;

  private final byte[] promptHeader;
  private final Seq seq;

  private int w;
  private int h;

  public ImageService(@Value("${work.directory}") String workingDirectoryPath) throws IOException {
    this.workingDirectoryPath = workingDirectoryPath;
    this.seq = new Seq();

    InputStream s = getClass().getClassLoader().getResourceAsStream(PROMPT_HEADER_PATH);

    assert s != null;
    promptHeader = s.readAllBytes();
    s.close();
  }

  public String createImage(int width, int height) throws HttpException, IOException {
    boolean wCheck = width>0 && width <=20000;
    boolean hCheck = height>0 && height <=50000;

    if( !(wCheck && hCheck) ) {
      throw new HttpException(400);
    }

    String imagePath = getImagePath(seq.getCurrentId()).toString();
    RandomAccessFile r = new RandomAccessFile(imagePath, "rw");
    r.setLength(0);
    r.write(createHeader(width, height));

    int byteWidth = getActualByteWidth(width);
    int cluster = 1024*1024;
    long size = ((long) byteWidth * height);
    byte[] filler = new byte[cluster];
    byte[] ost = new byte[(int) (size % cluster)];

    for (long i = 0; i < size / cluster; i++) {
      r.write(filler);
    }
    r.write(ost);
    r.close();

    return seq.increment();

  }

  public void writeFragment(String id, byte[] fragment, int xPos, int yPos, int fw, int fh)
    throws IOException, HttpException {

    Path imagePath = getImagePath(id);

    if(Files.notExists(imagePath)) {
      throw new HttpException(404);
    }

    setCurrentFileWH(id);
    boolean wCheck = (fw <= 5000) && (fw > 0);
    boolean hCheck = (fh <= 5000) && (fh > 0);
    boolean xPosCheck = (xPos + fw > 0) && (xPos < w);
    boolean yPosCheck = (yPos > -h) && (yPos < fh);

    if( !(wCheck && hCheck && xPosCheck && yPosCheck) ) {
      throw new HttpException(400);
    }

    RandomAccessFile r = new RandomAccessFile(imagePath.toString(), "rw");

    int yOffset = (fh - yPos > h) ? 0 : (h - (fh - yPos) );
    int fyOffset = (yOffset == 0) ? (fh - yPos - h) : 0;

    int xOffset = Math.max(xPos, 0);
    int fxOffset = (xOffset > 0) ? 0 : -xPos;

    int xDist = (xOffset == 0) ? Math.min(xPos + fw, w) : Math.min(xOffset + fw, w - xOffset);
    int yDist = (yOffset == 0) ? Math.min(h+yPos, h) : Math.min(h-yOffset, fh);

    int bytesFw = getActualByteWidth(fw);
    int bytesOffset = getDataOffset(fragment) + (fxOffset * PIXEL_DEPTH) + (fyOffset * bytesFw);

    r.skipBytes(DEFAULT_HEADER_SIZE + (yOffset * w * PIXEL_DEPTH) );

    for(int i = 0; i < yDist; i++){
      r.skipBytes(xOffset * PIXEL_DEPTH);
      r.write(fragment, bytesOffset, xDist * PIXEL_DEPTH);
      bytesOffset+=bytesFw;
      r.skipBytes(getActualByteWidth(w) - (xDist+xOffset) * 3);
    }

    r.close();

  }

  public byte[] getFragment(String id, int xPos, int yPos, int fw, int fh)
    throws HttpException, IOException {

    Path imagePath = getImagePath(id);

    if(Files.notExists(imagePath)) {
      throw new HttpException(404);
    }

    setCurrentFileWH(id);
    boolean wCheck = (fw <= 5000) && (fw > 0);
    boolean hCheck = (fh <= 5000) && (fh > 0);
    boolean xPosCheck = (xPos + fw > 0) && (xPos < w);
    boolean yPosCheck = (yPos > -h) && (yPos < fh);

    if( !(wCheck && hCheck && xPosCheck && yPosCheck) ) {
      throw new HttpException(400);
    }

    RandomAccessFile r = new RandomAccessFile(imagePath.toString(), "rw");

    int yOffset = (fh - yPos > h) ? 0 : (h - (fh - yPos) );
    int fyOffset = (yOffset == 0) ? (fh - yPos - h) : 0;

    int xOffset = Math.max(xPos, 0);
    int fxOffset = (xOffset > 0) ? 0 : -xPos;

    int xDist = (xOffset == 0) ? Math.min(xPos + fw, w) : Math.min(xOffset + fw, w - xOffset);
    int yDist = (yOffset == 0) ? Math.min(h + yPos, h) : Math.min(h - yOffset, fh);

    int bytesFw = getActualByteWidth(fw);
    int bytesOffset = DEFAULT_HEADER_SIZE + (fxOffset * PIXEL_DEPTH) + (fyOffset * bytesFw);

    byte[] response = new byte[DEFAULT_HEADER_SIZE + fh * bytesFw];
    System.arraycopy(createHeader(fw, fh), 0, response, 0, DEFAULT_HEADER_SIZE);

    r.skipBytes(DEFAULT_HEADER_SIZE + (yOffset * getActualByteWidth(w)) );

    for(int i = 0; i < yDist; i++){
      r.skipBytes(xOffset * PIXEL_DEPTH);
      r.read(response, bytesOffset, xDist*3);
      bytesOffset+=bytesFw;
      r.skipBytes(getActualByteWidth(w) - (xDist+xOffset) * 3);
    }

    r.close();

    return response;

  }

  public void delete (String id) throws HttpException, IOException {

    Path imagePath = getImagePath(id);

    if(Files.notExists(imagePath)) {
      throw new HttpException(404);
    }

    Files.delete(imagePath);

  }

  private byte[] createHeader(int width, int height) {

    ByteBuffer bb = ByteBuffer.wrap(promptHeader);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.putInt(HEADER_FILE_SIZE_OFFSET,(height * getActualByteWidth(width)) + DEFAULT_HEADER_SIZE);
    bb.putInt(HEADER_WIDTH_OFFSET, width);
    bb.putInt(HEADER_HEIGHT_OFFSET, height);
    bb.putInt(HEADER_IMAGE_SIZE_OFFSET,bb.getInt(HEADER_FILE_SIZE_OFFSET) - DEFAULT_HEADER_SIZE);

    return bb.array();

  }

  private int getDataOffset(byte[] fragment){

    ByteBuffer bb = ByteBuffer.wrap(fragment,10,4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.getInt();

  }

  private Path getImagePath(String id){
    return Path.of(workingDirectoryPath + id + SUFF);
  }

  private void setCurrentFileWH(String id) throws IOException {

    Path imagePath = getImagePath(id);
    byte[] metadata = new byte[DEFAULT_HEADER_SIZE];
    InputStream is = Files.newInputStream(imagePath);
    is.read(metadata);
    is.close();

    ByteBuffer bb = ByteBuffer.wrap(metadata);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    w = bb.getInt(HEADER_WIDTH_OFFSET);
    h = bb.getInt(HEADER_HEIGHT_OFFSET);

  }

  private int getActualByteWidth(int width) {

    if((width * PIXEL_DEPTH) % 4 == 0){
      return (width * PIXEL_DEPTH);
    } else {
      return (width * PIXEL_DEPTH) + (4 - ( (width * PIXEL_DEPTH) % 4) );
    }

  }

  private class Seq{

    private final Path seqFilePath = Path.of(workingDirectoryPath + ".seq");
    private int currentId;

    private Seq() throws IOException {

      if (Files.exists(seqFilePath)) {
        String currentIdString = Files.readString(seqFilePath);
        currentId = Integer.parseInt(currentIdString);
      } else {
        Files.writeString(seqFilePath, "0");
      }

    }

    public String getCurrentId() {
      return String.valueOf(currentId);
    }

    public String increment() throws IOException {
      Files.writeString(seqFilePath, String.valueOf(currentId));
      return String.valueOf(currentId++);
    }

  }

}
