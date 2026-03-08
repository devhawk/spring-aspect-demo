package com.example.demo.aspect;

import java.lang.reflect.Method;

public record RegisteredDurableMethod(Object bean, Method method) {}
