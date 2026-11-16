package com.example.petmatch_lab.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(annotations = Controller.class)
public class PageErrorHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException exception) {

        ModelAndView modelAndView = new ModelAndView("errors/status");

        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("statusCode", 404);
        modelAndView.addObject("errorTitle", "Recurso no encontrado");
        modelAndView.addObject("errorMessage", exception.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusinessRule(BusinessRuleException exception) {

        ModelAndView modelAndView = new ModelAndView("errors/status");

        modelAndView.setStatus(HttpStatus.CONFLICT);
        modelAndView.addObject("statusCode", 409);
        modelAndView.addObject("errorTitle", "No se puede realizar la operacion");
        modelAndView.addObject("errorMessage", exception.getMessage());

        return modelAndView;
    }
}