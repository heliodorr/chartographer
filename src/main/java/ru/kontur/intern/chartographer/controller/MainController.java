package ru.kontur.intern.chartographer.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kontur.intern.chartographer.image.HttpException;
import ru.kontur.intern.chartographer.image.ImageService;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class MainController {

  @Autowired
  private ImageService service;

  @PostMapping("/chartas/")
  public String create(@RequestParam int width, @RequestParam int height,
      HttpServletResponse response){

    try {
      response.setStatus(201);
      return service.createImage(width, height);
    } catch (HttpException e) {
      response.setStatus(e.getErrorCode());
    } catch (IOException e) {
      response.setStatus(500);
    }
    return null;

  }

  @PostMapping("/chartas/{id}/")
  public void writeFragment(@PathVariable String id, @RequestBody byte[] fragment,
      @RequestParam int x, @RequestParam int y, @RequestParam int width, @RequestParam int height,
      HttpServletResponse response){

    try {
      service.writeFragment(id, fragment, x, y, width, height);
      response.setStatus(200);
    } catch (HttpException e) {
      response.setStatus(e.getErrorCode());
    } catch (IOException e) {
      response.setStatus(500);
    }

  }

  @GetMapping("/chartas/{id}/")
  public byte[] getFragment(@PathVariable String id, @RequestParam int x, @RequestParam int y,
      @RequestParam int width, @RequestParam int height, HttpServletResponse response) {

    try {
      byte[] responseBody = service.getFragment(id, x, y, width, height);
      response.setStatus(200);
      return responseBody;
    } catch (HttpException e) {
      response.setStatus(e.getErrorCode());
    } catch (IOException e) {
      response.setStatus(500);
    }
    return null;

  }

  @DeleteMapping("/chartas/{id}/")
  public void delete(@PathVariable String id, HttpServletResponse response){

    try {
      service.delete(id);
      response.setStatus(200);
    } catch (HttpException e) {
      response.setStatus(e.getErrorCode());
    } catch (IOException e) {
      response.setStatus(500);
    }

  }

}










