package ru.kontur.intern.chartographer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@SpringBootApplication
public class ChartographerApplication {

  public static void main(String[] args) {
   argsFilter(args);
   SpringApplication.run(ChartographerApplication.class, args);
  }

   private static void argsFilter(String args[]) {

    if (args.length!=0) {
      if (args[0].endsWith("/")) {
        args[0] = "--work.directory=" + args[0];
      } else {
        args[0] = "--work.directory=" + args[0] + '/';
      }
    }

  }

}
