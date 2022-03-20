package ru.kontur.intern.chartographer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ru.kontur.intern.chartographer.image.HttpException;
import ru.kontur.intern.chartographer.image.ImageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class ChartographerApplicationTests {

  ClassLoader cl = getClass().getClassLoader();

  @Test
  void create_400_test() throws IOException{

    String wdp = cl.getResource("").getPath();
    ImageService service = new ImageService(wdp);

    int[][] dataset = new int[][]{
        {0, 1},
        {1, 0},
        {20001, 1},
        {1, 50001},
    };

    for (int i = 1; i <= dataset.length; i++) {

      int width = dataset[i-1][0];
      int height = dataset[i-1][1];

      HttpException httpException = assertThrows(HttpException.class, () -> {
        service.createImage(width,height);
      } );

      Assertions.assertTrue(httpException.getErrorCode() == 400);

    }

  }

  @Test
  void write_correctness_test() throws IOException, HttpException {

    String wdp = cl.getResource("write/").getPath();
    ImageService service = new ImageService(wdp);

    int[][] coordinates = new int[][]{{0, 0}, {-100, 100}, {-100, -100}, {100, 100}, {100, -100}};

    String tempName = "temp";

    Path temp = Path.of(cl.getResource("write/").getPath() + tempName + ".bmp");
    Path testImage = Path.of(cl.getResource("write/testImage.bmp").getPath());
    Path testFragment = Path.of(cl.getResource("write/pos/testFragment.bmp").getPath());

    for (int i = 1; i <= 5; i++) {

      int xPos = coordinates[i - 1][0];
      int yPos = coordinates[i - 1][1];
      int width = 200;
      int height = 200;

      Files.write(temp, Files.readAllBytes(testImage));

      service.writeFragment(tempName, Files.readAllBytes(testFragment), xPos, yPos, width, height);

      Path expected = Path.of(cl.getResource("write/pos/").getPath() + "exp" + i + ".bmp");

      assertTrue(Arrays.equals(Files.readAllBytes(temp), Files.readAllBytes(expected)));

    }

  }

  @Test
  void write_400_test() throws IOException {

    String wdp = cl.getResource("write/").getPath();
    ImageService service = new ImageService(wdp);
    String tempName = "temp";

    int[][] coordinates = new int[][] {
        {-200, 0},
        {200, 0},
        {0, -200},
        {0, 200}
    };

    for (int i = 1; i <= coordinates.length; i++) {

      int x = coordinates[i-1][0];
      int y = coordinates[i-1][1];
      byte[] bytes = null;

      HttpException httpException = assertThrows(HttpException.class, () -> {

        service.writeFragment(tempName, bytes, x, y, 200, 200);

      });

      assertTrue(httpException.getErrorCode() == 400);

    }

  }

  @Test
  void write_404_test() throws IOException {

    String wdp = cl.getResource("write/").getPath();
    ImageService service = new ImageService(wdp);
    String incorrectName = "####";

    HttpException httpException = assertThrows(HttpException.class, () -> {

      byte[] bytes = null;
      service.writeFragment(incorrectName, bytes, 0, 0, 200, 200);

    });

    assertTrue(httpException.getErrorCode() == 404);


  }

  @Test
  void get_correctness_test() throws IOException, HttpException {

    String wdp = cl.getResource("get/").getPath();
    ImageService service = new ImageService(wdp);

    int[][] coordinates = new int[][]{
        {0, 0},
        {-100, 100},
        {-100, -100},
        {100, 100},
        {100, -100}
    };

    String name = "testImage";

    for (int i = 1; i <= 5; i++) {

      int xPos = coordinates[i - 1][0];
      int yPos = coordinates[i - 1][1];
      int width = 200;
      int height = 200;

      byte[] result = service.getFragment(name, xPos, yPos, width, height);

      Path expected = Path.of(cl.getResource("get/pos/").getPath() + "exp" + i + ".bmp");

      assertTrue(Arrays.equals(result, Files.readAllBytes(expected)));

    }

  }

  @Test
  void get_400_test() throws IOException {

    String wdp = cl.getResource("get/").getPath();
    ImageService service = new ImageService(wdp);
    String tempName = "testImage";

    int[][] coordinates = new int[][] {
        {-200, 0},
        {200, 0},
        {0, -200},
        {0, 200}
    };

    for (int i = 1; i <= coordinates.length; i++) {

      int x = coordinates[i-1][0];
      int y = coordinates[i-1][1];

      HttpException httpException = assertThrows(HttpException.class, () -> {

        service.getFragment(tempName, x, y, 200, 200);

      });

      assertTrue(httpException.getErrorCode() == 400);

    }

  }

  @Test
  void get_404_test() throws IOException {

    String wdp = cl.getResource("get/").getPath();
    ImageService service = new ImageService(wdp);
    String incorrectName = "####";

    HttpException httpException = assertThrows(HttpException.class, () -> {

      byte[] bytes = null;
      service.getFragment(incorrectName, 0, 0, 200, 200);

    });

    assertTrue(httpException.getErrorCode() == 404);


  }


}













