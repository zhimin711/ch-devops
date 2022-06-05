/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch.cloud.support;

import com.ch.cloud.rocketmq.admin.annotation.OriginalControllerReturnValue;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * @author zhimin
 */
@ControllerAdvice(basePackages = {"com.ch.cloud.rocketmq", "com.ch.cloud.kafka"})
public class GlobalRestfulResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    @SuppressWarnings({"unckecked"})
    public Object beforeBodyWrite(
            Object obj, MethodParameter methodParameter, MediaType mediaType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        Annotation originalControllerReturnValue = methodParameter.getMethodAnnotation(OriginalControllerReturnValue.class);
        if (originalControllerReturnValue != null) {
            return obj;
        }
        Result<?> value;
        if (obj instanceof Result) {
            value = (Result<?>) obj;
        } else if (obj instanceof Collection) {
            if ((CommonUtils.isEmpty(obj))) {
                value = Result.success();
            } else {
                value = Result.success((Collection<?>) obj);
            }
        } else {
            if ((CommonUtils.isEmpty(obj))) {
                value = Result.success();
            } else {
                value = Result.success(obj);
            }
        }
        return value;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        return true;
    }

}
