package ru.axel.catty.launcher.controllers;

import ru.axel.catty.engine.response.ResponseCode;
import ru.axel.catty.engine.routing.ICattyRoute;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.launcher.annotations.GET;
import ru.axel.catty.launcher.annotations.POST;
import ru.axel.catty.launcher.factory.RouteFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BaseController {
    public BaseController(IRouting routing) {
        var methods = this.getClass().getDeclaredMethods();

        for(Method method: methods) {
            ICattyRoute route = null;
            
            if (method.isAnnotationPresent(GET.class)) {
                route = RouteFactory.get(method.getAnnotation(GET.class).path(), (request, response) -> {
                    try {
                        method.invoke(this, request, response);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                        e.printStackTrace();
                        request.addException(e);
                    }
                });
            } else if (method.isAnnotationPresent(POST.class)) {
                route = RouteFactory.post(method.getAnnotation(POST.class).path(), (request, response) -> {
                    try {
                        method.invoke(this, request, response);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                        e.printStackTrace();
                        request.addException(e);
                    }
                });
            }
            
            if (route != null) routing.addRoute(route);
        }
    }
}
