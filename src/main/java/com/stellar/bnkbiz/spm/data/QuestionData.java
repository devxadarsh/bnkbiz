/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.stellar.bnkbiz.spm.data;

import java.util.List;

public class QuestionData {

    private Long id;
    private List<ResponseData> responseDatas;
    private String componentKey;
    private String key;
    private String text;
    private String description;
    private Integer sequenceNo;

    public QuestionData() {
        super();
    }

    public QuestionData(final Long id, final List<ResponseData> responseDatas, final String componentKey, final String key,
                        final String text, final String description, final Integer sequenceNo) {
        super();
        this.id = id;
        this.responseDatas = responseDatas;
        this.componentKey = componentKey;
        this.key = key;
        this.text = text;
        this.description = description;
        this.sequenceNo = sequenceNo;
    }

    public Long getId() {
        return id;
    }

    public List<ResponseData> getResponseDatas() {
        return responseDatas;
    }

    public void setResponseDatas(List<ResponseData> responseDatas) {
        this.responseDatas = responseDatas;
    }

    public String getComponentKey() {
        return componentKey;
    }

    public void setComponentKey(String componentKey) {
        this.componentKey = componentKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
    }
}
