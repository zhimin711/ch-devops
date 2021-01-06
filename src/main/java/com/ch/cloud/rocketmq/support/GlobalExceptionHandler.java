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

package com.ch.cloud.rocketmq.support;

import com.ch.cloud.rocketmq.exception.ServiceException;
import com.ch.e.PubError;
import com.ch.e.PubException;
import com.ch.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = "com.ch.cloud.rocketmq")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result<Object> jsonErrorHandler(HttpServletRequest req, Exception ex) throws Exception {
        Result<Object> value = null;
        if (ex != null) {
            log.error("op=global_exception_handler_print_error", ex);
            if (ex instanceof PubException) {
                value = Result.error(((PubException) ex).getError(), ex.getMessage());
            } else if (ex instanceof MQBrokerException) {
                value = Result.error(PubError.DEFAULT, ((MQBrokerException) ex).getErrorMessage());
            } else if (ex instanceof MQClientException) {
                value = Result.error(PubError.DEFAULT, ((MQClientException) ex).getErrorMessage());
            } else {
                value = Result.error(PubError.UNKNOWN, ex.getMessage());
            }
        }
        return value;
    }
}